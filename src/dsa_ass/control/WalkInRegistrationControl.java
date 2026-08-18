package dsa_ass.control;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.DataStore;
import java.time.LocalDate;

/**
 * Control: WalkInRegistrationControl
 *
 * Implements business logic, validation rules, ID generation,
 * and Queue ADT management for walk-in guest registration and booking.
 */
public class WalkInRegistrationControl {

    private final Queue<Guest>                  guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                   roomList;
    private final Queue<Guest>                  walkInQueue;
    private final Queue<String>                 walkInResIds;

    private static int guestCounter = 1;
    private static int resCounter   = 1;

    public static void setGuestCounter(int n) { guestCounter = n; }
    public static void setResCounter(int n)   { resCounter   = n; }
    public static int  getGuestCounter()      { return guestCounter; }
    public static int  getResCounter()        { return resCounter; }

    public WalkInRegistrationControl(Queue<Guest>                  guestList,
                                     BinarySearchTree<Reservation> reservationList,
                                     Queue<Room>                   roomList,
                                     Queue<Guest>                  walkInQueue,
                                     Queue<String>                 walkInResIds) {
        this.guestList       = guestList;
        this.reservationList = reservationList;
        this.roomList        = roomList;
        this.walkInQueue     = walkInQueue;
        this.walkInResIds    = walkInResIds;
    }

    public WalkInRegistrationControl(Queue<Guest>                  guestList,
                                     BinarySearchTree<Reservation> reservationList,
                                     Queue<Room>                   roomList) {
        this(guestList, reservationList, roomList, new Queue<Guest>(), new Queue<String>());
    }

    // ── Validation Helpers ───────────────────────────────────────

    public boolean isValidIc(String ic) {
        return ic != null && ic.matches("^\\d{6}-\\d{2}-\\d{4}$");
    }

    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^01\\d-\\d{7,8}$");
    }

    public boolean isValidGmail(String email) {
        return email != null && email.matches("(?i)^[a-zA-Z0-9._%+-]+@gmail\\.com$");
    }

    // ── Guest Registration (Queue ADT enqueue) ───────────────────

    public String generateGuestId() {
        int maxIdNum = 0;
        for (int i = 0; i < guestList.size(); i++) {
            int num = parseTrailingNum(guestList.get(i).getGuestId());
            if (num > maxIdNum) {
                maxIdNum = num;
            }
        }
        int nextIdNum = Math.max(maxIdNum + 1, guestCounter);
        guestCounter = nextIdNum + 1;
        return String.format("G%03d", nextIdNum);
    }

    public Guest registerWalkIn(String name, String ic, String phone, String email, String nationality) {
        String guestId = generateGuestId();
        Guest g = new Guest(guestId, name, ic, phone, email, nationality);
        guestList.enqueue(g);
        walkInQueue.enqueue(g);
        autoSave();
        return g;
    }

    // ── Queue Management (Queue ADT peek / dequeue / sync) ──────

    public Queue<Guest> getWalkInQueue() {
        syncWalkInQueue();
        return walkInQueue;
    }

    public int getQueueSize() {
        syncWalkInQueue();
        return walkInQueue.size();
    }

    public boolean isQueueEmpty() {
        syncWalkInQueue();
        return walkInQueue.isEmpty();
    }

    public Guest peekNextGuest() {
        syncWalkInQueue();
        return walkInQueue.peek();
    }

    public Guest dequeueNextGuest() {
        syncWalkInQueue();
        return walkInQueue.dequeue();
    }

    public void syncWalkInQueue() {
        if (walkInQueue == null || guestList == null) return;
        for (int i = walkInQueue.size() - 1; i >= 0; i--) {
            Guest qg = walkInQueue.get(i);
            if (qg == null) {
                walkInQueue.remove(i);
                continue;
            }
            boolean exists = false;
            for (int j = 0; j < guestList.size(); j++) {
                Guest gl = guestList.get(j);
                if (gl != null && gl.getGuestId().equalsIgnoreCase(qg.getGuestId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                walkInQueue.remove(i);
            }
        }
    }

    // ── Room Availability Lookup ─────────────────────────────────

    public Queue<Room> getRoomList() {
        return roomList;
    }

    public Queue<Room> findAvailableRooms(Room.RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        Queue<Room> available = new Queue<Room>();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (roomType != null && r.getRoomType() != roomType) continue;
            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) continue;

            boolean hasOverlap = false;
            for (int j = 0; j < reservationList.size(); j++) {
                Reservation res = reservationList.get(j);
                if (!res.getRoomNo().equalsIgnoreCase(r.getRoomNo())) continue;
                if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED
                        && res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) continue;

                if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                    hasOverlap = true;
                    break;
                }
            }

            if (!hasOverlap) {
                available.enqueue(r);
            }
        }
        return available;
    }

    public boolean isRoomAvailable(Room r, LocalDate checkIn, LocalDate checkOut) {
        if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) return false;
        if (checkIn == null || checkOut == null) return r.isAvailable();

        for (int j = 0; j < reservationList.size(); j++) {
            Reservation res = reservationList.get(j);
            if (!res.getRoomNo().equalsIgnoreCase(r.getRoomNo())) continue;
            if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED
                    && res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) continue;

            if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                return false;
            }
        }
        return true;
    }

    public Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    public Guest findGuest(String guestId) {
        for (int i = 0; i < guestList.size(); i++) {
            if (guestList.get(i).getGuestId().equalsIgnoreCase(guestId))
                return guestList.get(i);
        }
        return null;
    }

    // ── Reservation Creation ─────────────────────────────────────

    public String generateReservationId() {
        int maxIdNum = 0;
        for (int i = 0; i < reservationList.size(); i++) {
            int num = parseTrailingNum(reservationList.get(i).getReservationId());
            if (num > maxIdNum) {
                maxIdNum = num;
            }
        }
        int nextIdNum = Math.max(maxIdNum + 1, resCounter);
        resCounter = nextIdNum + 1;
        return String.format("R%03d", nextIdNum);
    }

    public String generateConfirmationNumber(String resId) {
        int idNum = parseTrailingNum(resId);
        return String.format("%08d", idNum);
    }

    public Reservation confirmAndCreateReservation(String resId, String confNo, Guest guest,
                                                   Room selectedRoom, LocalDate checkIn,
                                                   LocalDate checkOut, int numGuests, double totalAmount) {
        Reservation res = new Reservation(resId, guest.getGuestId(), selectedRoom.getRoomNo(),
                checkIn, checkOut, numGuests, totalAmount);
        res.setStatus(Reservation.ReservationStatus.CONFIRMED);
        res.setConfirmationNo(confNo);
        reservationList.add(res);
        selectedRoom.setStatus(Room.RoomStatus.OCCUPIED);
        walkInResIds.enqueue(resId);
        autoSave();
        return res;
    }

    public Queue<String> getWalkInResIds() {
        return walkInResIds;
    }

    public Reservation getReservationById(String resId) {
        for (int i = 0; i < reservationList.size(); i++) {
            if (reservationList.get(i).getReservationId().equalsIgnoreCase(resId)) {
                return reservationList.get(i);
            }
        }
        return null;
    }

    // ── Persistence ──────────────────────────────────────────────

    public void autoSave() {
        DataStore.saveGuests(guestList);
        DataStore.saveReservations(reservationList);
        DataStore.saveRooms(roomList);
    }

    public int parseTrailingNum(String id) {
        if (id == null || id.isEmpty()) return 0;
        int i = 0;
        while (i < id.length() && !Character.isDigit(id.charAt(i))) i++;
        try {
            return Integer.parseInt(id.substring(i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
