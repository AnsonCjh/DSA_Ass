package dsa_ass.control;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.DataStore;
import java.time.LocalDate;

/**
 * Control: FrontDeskControl
 *
 * Implements business logic, search routines (BST traversal),
 * guest management, check-in, check-out, and deposit handling.
 */
public class FrontDeskControl {

    private final Queue<Guest>                  guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                   roomList;
    private final Stack<CleaningTask>           taskList;
    private final Queue<Guest>                  walkInQueue;

    public FrontDeskControl(Queue<Guest>                  guestList,
                            BinarySearchTree<Reservation> reservationList,
                            Queue<Room>                   roomList,
                            Stack<CleaningTask>           taskList,
                            Queue<Guest>                  walkInQueue) {
        this.guestList       = guestList;
        this.reservationList = reservationList;
        this.roomList        = roomList;
        this.taskList        = taskList;
        this.walkInQueue     = walkInQueue;
    }

    public FrontDeskControl(Queue<Guest>                  guestList,
                            BinarySearchTree<Reservation> reservationList,
                            Queue<Room>                   roomList,
                            Stack<CleaningTask>           taskList) {
        this(guestList, reservationList, roomList, taskList, null);
    }

    public FrontDeskControl(Queue<Guest>                  guestList,
                            BinarySearchTree<Reservation> reservationList,
                            Queue<Room>                   roomList) {
        this(guestList, reservationList, roomList, null, null);
    }

    // ── Search by Confirmation Number (BST ADT) ──────────────────

    public Reservation searchByConfirmation(String confNo) {
        if (confNo == null || confNo.isEmpty()) return null;
        return reservationList.searchByConfirmation(confNo);
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

    // ── Guest Management ─────────────────────────────────────────

    public Queue<Guest> getAllGuests() {
        return guestList;
    }

    public Guest findGuest(String keyword) {
        if (keyword == null || keyword.isEmpty()) return null;
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            if (g.getGuestId().equalsIgnoreCase(keyword)
                    || g.getName().equalsIgnoreCase(keyword)
                    || g.getIcNo().equalsIgnoreCase(keyword)
                    || g.getPhone().equalsIgnoreCase(keyword)) {
                return g;
            }
        }
        return null;
    }

    public Queue<Guest> searchGuests(String keyword) {
        Queue<Guest> results = new Queue<Guest>();
        if (keyword == null || keyword.isEmpty()) return results;
        String lower = keyword.toLowerCase();
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            if (g.getGuestId().toLowerCase().contains(lower)
                    || g.getName().toLowerCase().contains(lower)
                    || g.getIcNo().toLowerCase().contains(lower)
                    || g.getPhone().toLowerCase().contains(lower)
                    || g.getEmail().toLowerCase().contains(lower)
                    || g.getNationality().toLowerCase().contains(lower)) {
                results.enqueue(g);
            }
        }
        return results;
    }

    public Reservation findReservationForGuest(String guestId) {
        if (guestId == null || guestId.isEmpty()) return null;
        Reservation latestActive = null;
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            if (r.getGuestId().equalsIgnoreCase(guestId)) {
                if (r.getStatus() == Reservation.ReservationStatus.CHECKED_IN
                        || r.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
                    return r;
                }
                latestActive = r;
            }
        }
        return latestActive;
    }

    public boolean hasActiveReservation(String guestId) {
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            if (r.getGuestId().equalsIgnoreCase(guestId)) {
                if (r.getStatus() == Reservation.ReservationStatus.CONFIRMED
                        || r.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteGuest(Guest targetGuest) {
        if (targetGuest == null) return false;
        if (hasActiveReservation(targetGuest.getGuestId())) return false;

        int targetIdx = -1;
        for (int i = 0; i < guestList.size(); i++) {
            if (guestList.get(i).getGuestId().equalsIgnoreCase(targetGuest.getGuestId())) {
                targetIdx = i;
                break;
            }
        }

        if (targetIdx != -1) {
            guestList.remove(targetIdx);

            if (walkInQueue != null) {
                for (int i = walkInQueue.size() - 1; i >= 0; i--) {
                    Guest qg = walkInQueue.get(i);
                    if (qg != null && qg.getGuestId().equalsIgnoreCase(targetGuest.getGuestId())) {
                        walkInQueue.remove(i);
                    }
                }
            }
            autoSave();
            return true;
        }
        return false;
    }

    // ── Check-In Business Logic ──────────────────────────────────

    public boolean isRoomReadyForCheckIn(Room room) {
        if (room == null) return true;
        return room.getStatus() != Room.RoomStatus.DIRTY
                && room.getStatus() != Room.RoomStatus.CLEANING_IN_PROGRESS
                && room.getStatus() != Room.RoomStatus.UNDER_MAINTENANCE;
    }

    public void processCheckIn(Reservation res, Room room, double depositAmount) {
        res.setStatus(Reservation.ReservationStatus.CHECKED_IN);
        res.setDeposit(depositAmount);
        if (room != null) {
            room.setStatus(Room.RoomStatus.OCCUPIED);
        }
        autoSave();
    }

    // ── Check-Out Business Logic ─────────────────────────────────

    public String processCheckOut(Reservation res, Room room, Guest guest) {
        res.setStatus(Reservation.ReservationStatus.CHECKED_OUT);
        if (room != null) {
            room.setStatus(Room.RoomStatus.DIRTY);
        }

        String generatedTaskId = null;
        if (taskList != null) {
            generatedTaskId = HousekeepingControl.generateTaskId(taskList);
            String guestName = (guest != null) ? guest.getName() : res.getGuestId();
            CleaningTask newTask = new CleaningTask(
                    generatedTaskId,
                    res.getRoomNo(),
                    "Unassigned",
                    CleaningTask.TaskPriority.HIGH,
                    LocalDate.now(),
                    "Checkout Cleaning for " + guestName
            );
            taskList.push(newTask);
            DataStore.saveTasks(taskList);
        }
        autoSave();
        return generatedTaskId;
    }

    // ── Room & Guest Lookups ─────────────────────────────────────

    public Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    public Queue<Room> getRoomList() {
        return roomList;
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

    public Queue<Reservation> getCurrentCheckedInReservations() {
        Queue<Reservation> list = new Queue<Reservation>();
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);
            if (res.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
                list.enqueue(res);
            }
        }
        return list;
    }

    public void autoSave() {
        DataStore.saveGuests(guestList);
        DataStore.saveReservations(reservationList);
        DataStore.saveRooms(roomList);
        if (taskList != null) {
            DataStore.saveTasks(taskList);
        }
    }
}
