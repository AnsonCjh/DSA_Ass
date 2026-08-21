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
 * guest management, check-in, check-out, deposit handling,
 * and management report generation for front desk operations.
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

    // ══════════════════════════════════════════════════════════════
    // REPORT 1: Check-In and Check-Out Activity Report
    // ══════════════════════════════════════════════════════════════

    public static class ActivityReportItem {
        private final String confirmationNo;
        private final String guestName;
        private final String roomNo;
        private final String roomType;
        private final String activityType; // "CHECK_IN" or "CHECK_OUT"
        private final LocalDate activityDate;
        private final double depositAmount;

        public ActivityReportItem(String confirmationNo, String guestName, String roomNo, String roomType,
                                  String activityType, LocalDate activityDate, double depositAmount) {
            this.confirmationNo = confirmationNo;
            this.guestName      = guestName;
            this.roomNo         = roomNo;
            this.roomType       = roomType;
            this.activityType   = activityType;
            this.activityDate   = activityDate;
            this.depositAmount  = depositAmount;
        }

        public String getConfirmationNo() { return confirmationNo; }
        public String getGuestName()      { return guestName; }
        public String getRoomNo()         { return roomNo; }
        public String getRoomType()       { return roomType; }
        public String getActivityType()   { return activityType; }
        public LocalDate getActivityDate(){ return activityDate; }
        public double getDepositAmount()  { return depositAmount; }
    }

    public static class ActivityReportResult {
        private final ActivityReportItem[] items;
        private final int totalActivities;
        private final int totalCheckIns;
        private final int totalCheckOuts;

        public ActivityReportResult(ActivityReportItem[] items, int totalActivities, int totalCheckIns, int totalCheckOuts) {
            this.items           = items;
            this.totalActivities = totalActivities;
            this.totalCheckIns   = totalCheckIns;
            this.totalCheckOuts  = totalCheckOuts;
        }

        public ActivityReportItem[] getItems() { return items; }
        public int getTotalActivities()        { return totalActivities; }
        public int getTotalCheckIns()          { return totalCheckIns; }
        public int getTotalCheckOuts()         { return totalCheckOuts; }
    }

    public ActivityReportResult generateActivityReport(LocalDate startDate,
                                                       LocalDate endDate,
                                                       String activityTypeFilter,
                                                       Room.RoomType roomTypeFilter) {
        // ── [SEARCHING TECHNIQUE] ──
        // Traverses the Binary Search Tree and performs linear search on guests and rooms.
        Queue<ActivityReportItem> matchingQueue = new Queue<ActivityReportItem>();

        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);
            Guest g = findGuest(res.getGuestId());
            Room r  = findRoom(res.getRoomNo());

            // ── [FILTERING CRITERIA: Room Type] ──
            if (roomTypeFilter != null) {
                if (r == null || r.getRoomType() != roomTypeFilter) continue;
            }

            String guestName = (g != null) ? g.getName() : res.getGuestId();
            String confNo    = res.getConfirmationNo().isEmpty() ? res.getReservationId() : res.getConfirmationNo();
            String roomType  = (r != null) ? r.getRoomType().name() : "N/A";

            // If reservation has CHECKED_IN or CHECKED_OUT, record the check-in activity
            if (res.getStatus() == Reservation.ReservationStatus.CHECKED_IN
                    || res.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) {
                LocalDate checkInDate = res.getCheckInDate();
                boolean matchDate = (startDate == null || !checkInDate.isBefore(startDate))
                                 && (endDate == null || !checkInDate.isAfter(endDate));
                boolean matchActivity = activityTypeFilter == null
                                     || activityTypeFilter.equalsIgnoreCase("ALL")
                                     || activityTypeFilter.equalsIgnoreCase("CHECK_IN");

                if (matchDate && matchActivity) {
                    matchingQueue.enqueue(new ActivityReportItem(
                            confNo, guestName, res.getRoomNo(), roomType, "CHECK_IN", checkInDate, res.getDeposit()));
                }
            }

            // If reservation is CHECKED_OUT, record the check-out activity
            if (res.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) {
                LocalDate checkOutDate = res.getCheckOutDate();
                boolean matchDate = (startDate == null || !checkOutDate.isBefore(startDate))
                                 && (endDate == null || !checkOutDate.isAfter(endDate));
                boolean matchActivity = activityTypeFilter == null
                                     || activityTypeFilter.equalsIgnoreCase("ALL")
                                     || activityTypeFilter.equalsIgnoreCase("CHECK_OUT");

                if (matchDate && matchActivity) {
                    matchingQueue.enqueue(new ActivityReportItem(
                            confNo, guestName, res.getRoomNo(), roomType, "CHECK_OUT", checkOutDate, res.getDeposit()));
                }
            }
        }

        // Convert Queue to array for manual sorting without collections framework
        int count = matchingQueue.size();
        ActivityReportItem[] arr = new ActivityReportItem[count];
        for (int i = 0; i < count; i++) {
            arr[i] = matchingQueue.get(i);
        }

        // ── [SORTING TECHNIQUE: Custom Bubble Sort by Activity Date (Chronological)] ──
        for (int i = 0; i < count - 1; i++) {
            for (int j = 0; j < count - i - 1; j++) {
                if (arr[j].getActivityDate().isAfter(arr[j + 1].getActivityDate())) {
                    ActivityReportItem temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        // ── [CALCULATION: Check-In vs Check-Out Metrics] ──
        int checkIns = 0, checkOuts = 0;
        for (int i = 0; i < count; i++) {
            if (arr[i].getActivityType().equals("CHECK_IN")) checkIns++;
            else if (arr[i].getActivityType().equals("CHECK_OUT")) checkOuts++;
        }

        return new ActivityReportResult(arr, count, checkIns, checkOuts);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 2: Deposit and Payment Status Report
    // ══════════════════════════════════════════════════════════════

    public static class DepositPaymentReportItem {
        private final String confirmationNo;
        private final String guestName;
        private final String roomNo;
        private final double totalCharges;
        private final String paymentStatus; // "PAID", "PENDING"
        private final double depositAmount;
        private final String depositStatus; // "HELD", "REFUNDED", "PENDING_COLLECTION"
        private final int priorityScore;    // 1 = Attention Required (Pending payment or Held deposit), 2 = Normal Confirmed, 3 = Completed

        public DepositPaymentReportItem(String confirmationNo, String guestName, String roomNo,
                                        double totalCharges, String paymentStatus,
                                        double depositAmount, String depositStatus, int priorityScore) {
            this.confirmationNo = confirmationNo;
            this.guestName      = guestName;
            this.roomNo         = roomNo;
            this.totalCharges   = totalCharges;
            this.paymentStatus  = paymentStatus;
            this.depositAmount  = depositAmount;
            this.depositStatus  = depositStatus;
            this.priorityScore  = priorityScore;
        }

        public String getConfirmationNo() { return confirmationNo; }
        public String getGuestName()      { return guestName; }
        public String getRoomNo()         { return roomNo; }
        public double getTotalCharges()   { return totalCharges; }
        public String getPaymentStatus()  { return paymentStatus; }
        public double getDepositAmount()  { return depositAmount; }
        public String getDepositStatus()  { return depositStatus; }
        public int getPriorityScore()     { return priorityScore; }
    }

    public static class DepositPaymentReportResult {
        private final DepositPaymentReportItem[] items;
        private final double totalPaidCharges;
        private final double totalPendingCharges;
        private final double totalDepositsHeld;
        private final double totalDepositsRefunded;
        private final int totalRecords;
        private final int attentionCount;

        public DepositPaymentReportResult(DepositPaymentReportItem[] items,
                                          double totalPaidCharges, double totalPendingCharges,
                                          double totalDepositsHeld, double totalDepositsRefunded,
                                          int totalRecords, int attentionCount) {
            this.items                 = items;
            this.totalPaidCharges      = totalPaidCharges;
            this.totalPendingCharges   = totalPendingCharges;
            this.totalDepositsHeld     = totalDepositsHeld;
            this.totalDepositsRefunded = totalDepositsRefunded;
            this.totalRecords          = totalRecords;
            this.attentionCount        = attentionCount;
        }

        public DepositPaymentReportItem[] getItems() { return items; }
        public double getTotalPaidCharges()          { return totalPaidCharges; }
        public double getTotalPendingCharges()       { return totalPendingCharges; }
        public double getTotalDepositsHeld()         { return totalDepositsHeld; }
        public double getTotalDepositsRefunded()     { return totalDepositsRefunded; }
        public int getTotalRecords()                 { return totalRecords; }
        public int getAttentionCount()               { return attentionCount; }
    }

    public DepositPaymentReportResult generateDepositPaymentReport(LocalDate startDate,
                                                                   LocalDate endDate,
                                                                   String paymentStatusFilter,
                                                                   String depositStatusFilter) {
        // ── [SEARCHING TECHNIQUE] ──
        // In-order traversal across reservationList BST and linear lookups on guestList
        Queue<DepositPaymentReportItem> matchingQueue = new Queue<DepositPaymentReportItem>();

        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);

            // ── [FILTERING CRITERIA: Date Range] ──
            if (startDate != null && res.getCheckInDate().isBefore(startDate)) continue;
            if (endDate != null && res.getCheckInDate().isAfter(endDate)) continue;

            // Determine Payment Status & Deposit Status
            String paymentStatus;
            String depositStatus;
            int priorityScore;

            if (res.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
                paymentStatus = "PAID";
                depositStatus = (res.getDeposit() > 0) ? "HELD" : "PENDING_COLLECTION";
                priorityScore = 1; // Attention: In-house active deposit held
            } else if (res.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) {
                paymentStatus = "PAID";
                depositStatus = "REFUNDED";
                priorityScore = 3; // Completed
            } else if (res.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
                paymentStatus = "PAID";
                depositStatus = "PENDING_COLLECTION";
                priorityScore = 2; // Confirmed, awaiting check-in
            } else if (res.getStatus() == Reservation.ReservationStatus.PENDING) {
                paymentStatus = "PENDING";
                depositStatus = "NONE";
                priorityScore = 1; // Attention: Pending payment
            } else {
                paymentStatus = "CANCELLED";
                depositStatus = "REFUNDED";
                priorityScore = 3;
            }

            // ── [FILTERING CRITERIA: Payment Status & Deposit Status] ──
            if (paymentStatusFilter != null && !paymentStatusFilter.equalsIgnoreCase("ALL")) {
                if (!paymentStatus.equalsIgnoreCase(paymentStatusFilter)) continue;
            }

            if (depositStatusFilter != null && !depositStatusFilter.equalsIgnoreCase("ALL")) {
                if (!depositStatus.equalsIgnoreCase(depositStatusFilter)) continue;
            }

            Guest g = findGuest(res.getGuestId());
            String guestName = (g != null) ? g.getName() : res.getGuestId();
            String confNo    = res.getConfirmationNo().isEmpty() ? res.getReservationId() : res.getConfirmationNo();

            matchingQueue.enqueue(new DepositPaymentReportItem(
                    confNo, guestName, res.getRoomNo(), res.getTotalAmount(),
                    paymentStatus, res.getDeposit(), depositStatus, priorityScore));
        }

        // Convert Queue to array for manual sorting
        int count = matchingQueue.size();
        DepositPaymentReportItem[] arr = new DepositPaymentReportItem[count];
        for (int i = 0; i < count; i++) {
            arr[i] = matchingQueue.get(i);
        }

        // ── [SORTING TECHNIQUE: Custom Insertion Sort (Attention Priority First: 1 -> 2 -> 3)] ──
        for (int i = 1; i < count; i++) {
            DepositPaymentReportItem key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].getPriorityScore() > key.getPriorityScore()) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }

        // ── [CALCULATION: Financial & Audit Metrics] ──
        double paidCharges = 0.0, pendingCharges = 0.0, heldDeposits = 0.0, refundedDeposits = 0.0;
        int attentionTotal = 0;

        for (int i = 0; i < count; i++) {
            DepositPaymentReportItem item = arr[i];
            if (item.getPaymentStatus().equalsIgnoreCase("PAID")) {
                paidCharges += item.getTotalCharges();
            } else if (item.getPaymentStatus().equalsIgnoreCase("PENDING")) {
                pendingCharges += item.getTotalCharges();
            }

            if (item.getDepositStatus().equalsIgnoreCase("HELD")) {
                heldDeposits += (item.getDepositAmount() > 0 ? item.getDepositAmount() : 100.00);
            } else if (item.getDepositStatus().equalsIgnoreCase("REFUNDED")) {
                refundedDeposits += (item.getDepositAmount() > 0 ? item.getDepositAmount() : 100.00);
            }

            if (item.getPriorityScore() == 1) {
                attentionTotal++;
            }
        }

        return new DepositPaymentReportResult(arr, paidCharges, pendingCharges, heldDeposits, refundedDeposits, count, attentionTotal);
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
