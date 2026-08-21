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
 * Queue ADT management, and management report generation for walk-in operations.
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
        return ic != null && ic.matches("^(\\d{12}|\\d{6}-\\d{2}-\\d{4})$");
    }

    public boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digitsOnly = phone.replace("-", "");
        return digitsOnly.matches("^\\d{10,11}$");
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("(?i)^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public boolean isValidGmail(String email) {
        return isValidEmail(email);
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
        return String.format("R%03d", nextIdNum);
    }

    public String generateConfirmationNumber(String resId) {
        int idNum = parseTrailingNum(resId);
        return String.format("12345%03d", idNum);
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
        int idNum = parseTrailingNum(resId);
        resCounter = Math.max(resCounter, idNum + 1);
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

    // ══════════════════════════════════════════════════════════════
    // REPORT 1: Walk-In Reservation Summary Report
    // ══════════════════════════════════════════════════════════════

    public static class ReservationSummaryItem {
        private final String guestId;
        private final String guestName;
        private final String roomType;
        private final String roomNo;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;
        private final Reservation.ReservationStatus status;
        private final double totalAmount;

        public ReservationSummaryItem(String guestId, String guestName, String roomType, String roomNo,
                                      LocalDate checkInDate, LocalDate checkOutDate,
                                      Reservation.ReservationStatus status, double totalAmount) {
            this.guestId      = guestId;
            this.guestName    = guestName;
            this.roomType     = roomType;
            this.roomNo       = roomNo;
            this.checkInDate  = checkInDate;
            this.checkOutDate = checkOutDate;
            this.status       = status;
            this.totalAmount  = totalAmount;
        }

        public String getGuestId()                   { return guestId; }
        public String getGuestName()                 { return guestName; }
        public String getRoomType()                  { return roomType; }
        public String getRoomNo()                    { return roomNo; }
        public LocalDate getCheckInDate()            { return checkInDate; }
        public LocalDate getCheckOutDate()           { return checkOutDate; }
        public Reservation.ReservationStatus getStatus() { return status; }
        public double getTotalAmount()               { return totalAmount; }
    }

    public static class ReservationSummaryReportResult {
        private final ReservationSummaryItem[] items;
        private final int totalReservations;
        private final int confirmedCount;
        private final int cancelledCount;
        private final int checkedInCount;
        private final int checkedOutCount;
        private final int pendingCount;
        private final double totalRevenue;

        public ReservationSummaryReportResult(ReservationSummaryItem[] items, int totalReservations,
                                              int confirmedCount, int cancelledCount,
                                              int checkedInCount, int checkedOutCount,
                                              int pendingCount, double totalRevenue) {
            this.items             = items;
            this.totalReservations = totalReservations;
            this.confirmedCount    = confirmedCount;
            this.cancelledCount    = cancelledCount;
            this.checkedInCount    = checkedInCount;
            this.checkedOutCount   = checkedOutCount;
            this.pendingCount      = pendingCount;
            this.totalRevenue      = totalRevenue;
        }

        public ReservationSummaryItem[] getItems() { return items; }
        public int getTotalReservations()          { return totalReservations; }
        public int getConfirmedCount()             { return confirmedCount; }
        public int getCancelledCount()             { return cancelledCount; }
        public int getCheckedInCount()             { return checkedInCount; }
        public int getCheckedOutCount()            { return checkedOutCount; }
        public int getPendingCount()               { return pendingCount; }
        public double getTotalRevenue()            { return totalRevenue; }
    }

    public ReservationSummaryReportResult generateReservationSummaryReport(LocalDate startDate,
                                                                          LocalDate endDate,
                                                                          Room.RoomType roomTypeFilter,
                                                                          Reservation.ReservationStatus statusFilter) {
        // ── [SEARCHING TECHNIQUE] ──
        // Traverses the Binary Search Tree (in-order) to retrieve reservations
        // and performs Linear Search across guestList and roomList.
        Queue<ReservationSummaryItem> matchingQueue = new Queue<ReservationSummaryItem>();

        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);

            // ── [FILTERING CRITERIA] ──
            // 1. Date range filter on check-in date
            if (startDate != null && res.getCheckInDate().isBefore(startDate)) continue;
            if (endDate != null && res.getCheckInDate().isAfter(endDate)) continue;

            // 2. Reservation status filter
            if (statusFilter != null && res.getStatus() != statusFilter) continue;

            // ── [SEARCHING TECHNIQUE: Linear search for Guest & Room] ──
            Guest g = findGuest(res.getGuestId());
            Room r  = findRoom(res.getRoomNo());

            // 3. Room type filter
            if (roomTypeFilter != null) {
                if (r == null || r.getRoomType() != roomTypeFilter) continue;
            }

            String guestName = (g != null) ? g.getName() : res.getGuestId();
            String typeStr   = (r != null) ? r.getRoomType().name() : "N/A";

            matchingQueue.enqueue(new ReservationSummaryItem(
                    res.getGuestId(), guestName, typeStr, res.getRoomNo(),
                    res.getCheckInDate(), res.getCheckOutDate(), res.getStatus(), res.getTotalAmount()));
        }

        // Convert Queue to array for manual sorting without collections framework
        int count = matchingQueue.size();
        ReservationSummaryItem[] arr = new ReservationSummaryItem[count];
        for (int i = 0; i < count; i++) {
            arr[i] = matchingQueue.get(i);
        }

        // ── [SORTING TECHNIQUE: Custom Insertion Sort by Check-In Date] ──
        for (int i = 1; i < count; i++) {
            ReservationSummaryItem key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].getCheckInDate().isAfter(key.getCheckInDate())) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }

        // ── [CALCULATION: Aggregate Totals] ──
        int confirmed = 0, cancelled = 0, checkedIn = 0, checkedOut = 0, pending = 0;
        double revenue = 0.0;

        for (int i = 0; i < count; i++) {
            ReservationSummaryItem item = arr[i];
            if (item.getStatus() == Reservation.ReservationStatus.CONFIRMED)   confirmed++;
            else if (item.getStatus() == Reservation.ReservationStatus.CANCELLED) cancelled++;
            else if (item.getStatus() == Reservation.ReservationStatus.CHECKED_IN) checkedIn++;
            else if (item.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) checkedOut++;
            else if (item.getStatus() == Reservation.ReservationStatus.PENDING) pending++;

            if (item.getStatus() != Reservation.ReservationStatus.CANCELLED) {
                revenue += item.getTotalAmount();
            }
        }

        return new ReservationSummaryReportResult(arr, count, confirmed, cancelled, checkedIn, checkedOut, pending, revenue);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 2: Room Type Demand Report
    // ══════════════════════════════════════════════════════════════

    public static class RoomTypeDemandItem {
        private int rank;
        private final Room.RoomType roomType;
        private final int reservationCount;
        private double demandShare;
        private final double totalRevenue;

        public RoomTypeDemandItem(Room.RoomType roomType, int reservationCount, double totalRevenue) {
            this.roomType         = roomType;
            this.reservationCount = reservationCount;
            this.totalRevenue     = totalRevenue;
            this.rank             = 0;
            this.demandShare      = 0.0;
        }

        public int getRank()                  { return rank; }
        public void setRank(int rank)         { this.rank = rank; }
        public Room.RoomType getRoomType()    { return roomType; }
        public int getReservationCount()      { return reservationCount; }
        public double getDemandShare()        { return demandShare; }
        public void setDemandShare(double s)  { this.demandShare = s; }
        public double getTotalRevenue()       { return totalRevenue; }
    }

    public static class RoomTypeDemandReportResult {
        private final RoomTypeDemandItem[] items;
        private final int totalReservations;
        private final double totalRevenue;
        private final String mostRequestedRoomType;

        public RoomTypeDemandReportResult(RoomTypeDemandItem[] items, int totalReservations,
                                          double totalRevenue, String mostRequestedRoomType) {
            this.items                 = items;
            this.totalReservations     = totalReservations;
            this.totalRevenue          = totalRevenue;
            this.mostRequestedRoomType = mostRequestedRoomType;
        }

        public RoomTypeDemandItem[] getItems()    { return items; }
        public int getTotalReservations()         { return totalReservations; }
        public double getTotalRevenue()           { return totalRevenue; }
        public String getMostRequestedRoomType()  { return mostRequestedRoomType; }
    }

    public RoomTypeDemandReportResult generateRoomTypeDemandReport(LocalDate startDate,
                                                                   LocalDate endDate,
                                                                   boolean onlyConfirmed) {
        Room.RoomType[] types = Room.RoomType.values();
        int[] counts = new int[types.length];
        double[] revenues = new double[types.length];
        int overallTotalReservations = 0;
        double overallTotalRevenue = 0.0;

        // ── [SEARCHING TECHNIQUE & FILTERING CRITERIA] ──
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);

            // Filter date range
            if (startDate != null && res.getCheckInDate().isBefore(startDate)) continue;
            if (endDate != null && res.getCheckInDate().isAfter(endDate)) continue;

            // Filter confirmed/active reservations
            if (onlyConfirmed) {
                if (res.getStatus() == Reservation.ReservationStatus.CANCELLED) continue;
            }

            // Search matching room type
            Room r = findRoom(res.getRoomNo());
            if (r != null) {
                Room.RoomType rt = r.getRoomType();
                for (int t = 0; t < types.length; t++) {
                    if (types[t] == rt) {
                        counts[t]++;
                        revenues[t] += res.getTotalAmount();
                        overallTotalReservations++;
                        overallTotalRevenue += res.getTotalAmount();
                        break;
                    }
                }
            }
        }

        // Create item array for each room type
        RoomTypeDemandItem[] items = new RoomTypeDemandItem[types.length];
        for (int t = 0; t < types.length; t++) {
            items[t] = new RoomTypeDemandItem(types[t], counts[t], revenues[t]);
        }

        // ── [SORTING TECHNIQUE: Custom Selection Sort from Highest to Lowest Demand] ──
        for (int i = 0; i < items.length - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < items.length; j++) {
                if (items[j].getReservationCount() > items[maxIdx].getReservationCount()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                RoomTypeDemandItem temp = items[i];
                items[i] = items[maxIdx];
                items[maxIdx] = temp;
            }
        }

        // ── [CALCULATION: Demand Shares and Rankings] ──
        for (int i = 0; i < items.length; i++) {
            items[i].setRank(i + 1);
            double share = (overallTotalReservations > 0)
                    ? ((double) items[i].getReservationCount() / overallTotalReservations) * 100.0
                    : 0.0;
            items[i].setDemandShare(share);
        }

        String mostRequested = (items.length > 0 && items[0].getReservationCount() > 0)
                ? items[0].getRoomType().name() + " (" + items[0].getReservationCount() + " reservations)"
                : "None";

        return new RoomTypeDemandReportResult(items, overallTotalReservations, overallTotalRevenue, mostRequested);
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
