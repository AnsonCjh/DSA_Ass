package dsa_ass.control;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import dsa_ass.util.DataStore;
import java.time.LocalDate;

/**
 * Control: HousekeepingControl
 *
 * Implements business logic for cleaning task management (Stack ADT - LIFO),
 * room status lifecycle, undo/history stack management, task filtering,
 * and management report generation for housekeeping operations.
 */
public class HousekeepingControl {

    private final Stack<CleaningTask>           taskList;
    private final Queue<Room>                   roomList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Guest>                  guestList;
    private final Stack<RoomStatusLog>          statusHistoryStack;

    private static int taskCounter = 1;

    public static void setTaskCounter(int n) { taskCounter = n; }
    public static int  getTaskCounter()      { return taskCounter; }

    public HousekeepingControl(Stack<CleaningTask>           taskList,
                               Queue<Room>                   roomList,
                               BinarySearchTree<Reservation> reservationList,
                               Queue<Guest>                  guestList,
                               Stack<RoomStatusLog>          statusHistoryStack) {
        this.taskList           = taskList;
        this.roomList           = roomList;
        this.reservationList    = reservationList;
        this.guestList          = guestList;
        this.statusHistoryStack = (statusHistoryStack != null) ? statusHistoryStack : new Stack<RoomStatusLog>();
    }

    public HousekeepingControl(Stack<CleaningTask>           taskList,
                               Queue<Room>                   roomList,
                               BinarySearchTree<Reservation> reservationList,
                               Queue<Guest>                  guestList) {
        this(taskList, roomList, reservationList, guestList, null);
    }

    public HousekeepingControl(Stack<CleaningTask> taskList,
                               Queue<Room>         roomList) {
        this(taskList, roomList, null, null, null);
    }

    // ── Task ID Generation ───────────────────────────────────────

    public static String generateTaskId(Stack<CleaningTask> taskList) {
        int maxIdNum = 0;
        if (taskList != null) {
            for (int i = 0; i < taskList.size(); i++) {
                CleaningTask t = taskList.get(i);
                if (t != null && t.getTaskId() != null) {
                    int num = parseTrailingNum(t.getTaskId());
                    if (num > maxIdNum) maxIdNum = num;
                }
            }
        }
        int nextIdNum = Math.max(maxIdNum + 1, taskCounter);
        taskCounter = nextIdNum + 1;
        return String.format("T%04d", nextIdNum);
    }

    public String generateTaskId() {
        return generateTaskId(this.taskList);
    }

    // ── Cleaning Task Management (Stack ADT - LIFO) ──────────────

    public CleaningTask addCleaningTask(String roomNo, String staff, CleaningTask.TaskPriority priority, String remarks) {
        String taskId = generateTaskId();
        if (staff == null || staff.trim().isEmpty()) staff = "Unassigned";
        if (remarks == null || remarks.trim().isEmpty()) remarks = "Checkout Cleaning";
        CleaningTask task = new CleaningTask(taskId, roomNo, staff, priority, LocalDate.now(), remarks);
        taskList.push(task);
        autoSave();
        return task;
    }

    public Stack<CleaningTask> getTaskList() {
        return taskList;
    }

    public CleaningTask findTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) return null;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId))
                return taskList.get(i);
        }
        return null;
    }

    public CleaningTask findActiveTaskByRoom(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return null;
        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (t.getRoomNo().equalsIgnoreCase(roomNo) && t.getStatus() != CleaningTask.TaskStatus.COMPLETED) {
                return t;
            }
        }
        return null;
    }

    public CleaningTask findLatestTaskForRoom(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return null;
        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (t.getRoomNo().equalsIgnoreCase(roomNo)) {
                return t;
            }
        }
        return null;
    }

    public boolean deleteTask(String taskId) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId)) {
                taskList.remove(i);
                autoSave();
                return true;
            }
        }
        return false;
    }

    // ── Room & Status Management ─────────────────────────────────

    public Queue<Room> getRoomList() {
        return roomList;
    }

    public Queue<Room> getDirtyRooms() {
        Queue<Room> dirtyRooms = new Queue<Room>();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getStatus() == Room.RoomStatus.DIRTY) {
                dirtyRooms.enqueue(r);
            }
        }
        return dirtyRooms;
    }

    public Queue<Room> getInspectedRooms() {
        Queue<Room> inspected = new Queue<Room>();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getStatus() == Room.RoomStatus.INSPECTED) {
                inspected.enqueue(r);
            }
        }
        return inspected;
    }

    public Room findRoom(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return null;
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    public void logRoomStatus(String roomNo, Room.RoomStatus prev, Room.RoomStatus next, String remarks) {
        statusHistoryStack.push(new RoomStatusLog(roomNo, prev, next, remarks));
    }

    public void updateRoomStatus(Room room, Room.RoomStatus newStatus, String remarks) {
        Room.RoomStatus oldStatus = room.getStatus();
        room.setStatus(newStatus);
        logRoomStatus(room.getRoomNo(), oldStatus, newStatus, remarks);
        autoSave();
    }

    public Stack<RoomStatusLog> getStatusHistoryStack() {
        return statusHistoryStack;
    }

    public boolean undoLatestStatusUpdate() {
        if (statusHistoryStack.isEmpty()) return false;
        RoomStatusLog lastLog = statusHistoryStack.pop();
        Room room = findRoom(lastLog.getRoomNo());
        if (room != null) {
            room.setStatus(lastLog.getPreviousStatus());
            autoSave();
            return true;
        }
        return false;
    }

    public Stack<CleaningTask> filterTasks(CleaningTask.TaskStatus filter) {
        Stack<CleaningTask> filtered = new Stack<CleaningTask>();
        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (filter == null || t.getStatus() == filter) {
                filtered.push(t);
            }
        }
        return filtered;
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 1: Room Cleaning Status Report
    // ══════════════════════════════════════════════════════════════

    public static class RoomCleaningStatusItem {
        private final String roomNo;
        private final String floor;
        private final Room.RoomType roomType;
        private final Room.RoomStatus cleaningStatus;
        private final String assignedStaff;
        private final String lastUpdated;
        private final int priorityScore; // 1=DIRTY, 2=IN_PROGRESS, 3=INSPECTED, 4=READY/AVAIL, 5=OCCUPIED, 6=MAINTENANCE

        public RoomCleaningStatusItem(String roomNo, String floor, Room.RoomType roomType,
                                      Room.RoomStatus cleaningStatus, String assignedStaff,
                                      String lastUpdated, int priorityScore) {
            this.roomNo         = roomNo;
            this.floor          = floor;
            this.roomType       = roomType;
            this.cleaningStatus = cleaningStatus;
            this.assignedStaff  = assignedStaff;
            this.lastUpdated    = lastUpdated;
            this.priorityScore  = priorityScore;
        }

        public String getRoomNo()                  { return roomNo; }
        public String getFloor()                   { return floor; }
        public Room.RoomType getRoomType()         { return roomType; }
        public Room.RoomStatus getCleaningStatus() { return cleaningStatus; }
        public String getAssignedStaff()           { return assignedStaff; }
        public String getLastUpdated()             { return lastUpdated; }
        public int getPriorityScore()              { return priorityScore; }
    }

    public static class RoomCleaningReportResult {
        private final RoomCleaningStatusItem[] items;
        private final int totalRooms;
        private final int dirtyCount;
        private final int inProgressCount;
        private final int inspectedCount;
        private final int availableCount;
        private final int occupiedCount;
        private final int maintenanceCount;
        private final double readinessRate;

        public RoomCleaningReportResult(RoomCleaningStatusItem[] items, int totalRooms,
                                        int dirtyCount, int inProgressCount,
                                        int inspectedCount, int availableCount,
                                        int occupiedCount, int maintenanceCount,
                                        double readinessRate) {
            this.items            = items;
            this.totalRooms       = totalRooms;
            this.dirtyCount       = dirtyCount;
            this.inProgressCount  = inProgressCount;
            this.inspectedCount   = inspectedCount;
            this.availableCount   = availableCount;
            this.occupiedCount    = occupiedCount;
            this.maintenanceCount = maintenanceCount;
            this.readinessRate    = readinessRate;
        }

        public RoomCleaningStatusItem[] getItems() { return items; }
        public int getTotalRooms()                 { return totalRooms; }
        public int getDirtyCount()                 { return dirtyCount; }
        public int getInProgressCount()            { return inProgressCount; }
        public int getInspectedCount()             { return inspectedCount; }
        public int getAvailableCount()             { return availableCount; }
        public int getOccupiedCount()              { return occupiedCount; }
        public int getMaintenanceCount()           { return maintenanceCount; }
        public double getReadinessRate()           { return readinessRate; }
    }

    public RoomCleaningReportResult generateRoomCleaningStatusReport(Room.RoomType roomTypeFilter,
                                                                     Room.RoomStatus statusFilter,
                                                                     String floorFilter) {
        // ── [SEARCHING TECHNIQUE] ──
        // Traverses roomList (Queue ADT), searches taskList (Stack ADT) for active staff,
        // and searches statusHistoryStack (Stack ADT) for last status update.
        Queue<RoomCleaningStatusItem> matchingQueue = new Queue<RoomCleaningStatusItem>();

        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);

            // ── [FILTERING CRITERIA: Room Type & Cleaning Status] ──
            if (roomTypeFilter != null && r.getRoomType() != roomTypeFilter) continue;
            if (statusFilter != null && r.getStatus() != statusFilter) continue;

            // Determine floor
            String floor = determineFloor(r.getRoomNo());
            if (floorFilter != null && !floorFilter.equalsIgnoreCase("ALL")) {
                if (!floor.equalsIgnoreCase(floorFilter.trim())) continue;
            }

            // Search assigned staff from latest task
            String staff = "Unassigned";
            String lastUpdated = "Today";

            for (int j = 0; j < taskList.size(); j++) {
                CleaningTask t = taskList.get(j);
                if (t.getRoomNo().equalsIgnoreCase(r.getRoomNo())) {
                    staff = t.getAssignedStaff();
                    lastUpdated = t.getUpdatedTime();
                    break; // Stack ADT is LIFO, first match is the latest task
                }
            }

            // Assign status priority score for custom sorting:
            // Dirty (1) -> Cleaning In Progress (2) -> Inspected (3) -> Ready/Available (4) -> Occupied (5) -> Maintenance (6)
            int priorityScore;
            if (r.getStatus() == Room.RoomStatus.DIRTY) priorityScore = 1;
            else if (r.getStatus() == Room.RoomStatus.CLEANING_IN_PROGRESS) priorityScore = 2;
            else if (r.getStatus() == Room.RoomStatus.INSPECTED) priorityScore = 3;
            else if (r.getStatus() == Room.RoomStatus.AVAILABLE || r.getStatus() == Room.RoomStatus.READY_FOR_CHECK_IN) priorityScore = 4;
            else if (r.getStatus() == Room.RoomStatus.OCCUPIED) priorityScore = 5;
            else priorityScore = 6;

            matchingQueue.enqueue(new RoomCleaningStatusItem(
                    r.getRoomNo(), floor, r.getRoomType(), r.getStatus(), staff, lastUpdated, priorityScore));
        }

        // Convert Queue to array for manual sorting
        int count = matchingQueue.size();
        RoomCleaningStatusItem[] arr = new RoomCleaningStatusItem[count];
        for (int i = 0; i < count; i++) {
            arr[i] = matchingQueue.get(i);
        }

        // ── [SORTING TECHNIQUE: Custom Status Priority Bubble Sort] ──
        // Order: Dirty -> Cleaning In Progress -> Inspected -> Ready for Check-In
        for (int i = 0; i < count - 1; i++) {
            for (int j = 0; j < count - i - 1; j++) {
                if (arr[j].getPriorityScore() > arr[j + 1].getPriorityScore()) {
                    RoomCleaningStatusItem temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                } else if (arr[j].getPriorityScore() == arr[j + 1].getPriorityScore()) {
                    if (arr[j].getRoomNo().compareToIgnoreCase(arr[j + 1].getRoomNo()) > 0) {
                        RoomCleaningStatusItem temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;
                    }
                }
            }
        }

        // ── [CALCULATION: Aggregate Totals & Readiness Rate] ──
        int dirty = 0, inProg = 0, inspected = 0, avail = 0, occupied = 0, maint = 0;
        for (int i = 0; i < count; i++) {
            Room.RoomStatus s = arr[i].getCleaningStatus();
            if (s == Room.RoomStatus.DIRTY) dirty++;
            else if (s == Room.RoomStatus.CLEANING_IN_PROGRESS) inProg++;
            else if (s == Room.RoomStatus.INSPECTED) inspected++;
            else if (s == Room.RoomStatus.AVAILABLE || s == Room.RoomStatus.READY_FOR_CHECK_IN) avail++;
            else if (s == Room.RoomStatus.OCCUPIED) occupied++;
            else maint++;
        }

        double readinessRate = (count > 0) ? (((double) (avail + inspected)) / count) * 100.0 : 0.0;
        return new RoomCleaningReportResult(arr, count, dirty, inProg, inspected, avail, occupied, maint, readinessRate);
    }

    private String determineFloor(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return "Other";
        if (roomNo.startsWith("V")) return "Villa Wing";
        if (roomNo.startsWith("S")) return "Single Wing";
        if (roomNo.startsWith("R1") || roomNo.startsWith("1")) return "Floor 1";
        if (roomNo.startsWith("R2") || roomNo.startsWith("2")) return "Floor 2";
        if (roomNo.startsWith("R3") || roomNo.startsWith("3")) return "Floor 3";
        return "Main Wing";
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 2: Housekeeping Staff Performance Report
    // ══════════════════════════════════════════════════════════════

    public static class StaffPerformanceItem {
        private int rank;
        private final String staffName;
        private final int completedTasks;
        private final int inProgressTasks;
        private final int pendingTasks;
        private final int totalHandled;
        private final double completionRate;

        public StaffPerformanceItem(String staffName, int completedTasks, int inProgressTasks,
                                    int pendingTasks, int totalHandled, double completionRate) {
            this.staffName       = staffName;
            this.completedTasks  = completedTasks;
            this.inProgressTasks = inProgressTasks;
            this.pendingTasks    = pendingTasks;
            this.totalHandled    = totalHandled;
            this.completionRate  = completionRate;
            this.rank            = 0;
        }

        public int getRank()                  { return rank; }
        public void setRank(int rank)         { this.rank = rank; }
        public String getStaffName()          { return staffName; }
        public int getCompletedTasks()        { return completedTasks; }
        public int getInProgressTasks()       { return inProgressTasks; }
        public int getPendingTasks()          { return pendingTasks; }
        public int getTotalHandled()          { return totalHandled; }
        public double getCompletionRate()     { return completionRate; }
    }

    public static class StaffPerformanceReportResult {
        private final StaffPerformanceItem[] items;
        private final int overallTotalTasks;
        private final int overallCompletedTasks;
        private final String topPerformer;
        private final double overallEfficiencyRate;

        public StaffPerformanceReportResult(StaffPerformanceItem[] items, int overallTotalTasks,
                                            int overallCompletedTasks, String topPerformer,
                                            double overallEfficiencyRate) {
            this.items                 = items;
            this.overallTotalTasks     = overallTotalTasks;
            this.overallCompletedTasks = overallCompletedTasks;
            this.topPerformer          = topPerformer;
            this.overallEfficiencyRate = overallEfficiencyRate;
        }

        public StaffPerformanceItem[] getItems()     { return items; }
        public int getOverallTotalTasks()             { return overallTotalTasks; }
        public int getOverallCompletedTasks()         { return overallCompletedTasks; }
        public String getTopPerformer()               { return topPerformer; }
        public double getOverallEfficiencyRate()      { return overallEfficiencyRate; }
    }

    public StaffPerformanceReportResult generateStaffPerformanceReport(LocalDate startDate,
                                                                       LocalDate endDate,
                                                                       String staffFilter,
                                                                       CleaningTask.TaskStatus taskStatusFilter) {
        // ── [SEARCHING TECHNIQUE & EXTRACTION OF UNIQUE STAFF] ──
        // Traverses taskList Stack ADT to extract all unique staff members without Java Collections.
        Queue<String> uniqueStaffQueue = new Queue<String>();

        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            String staff = t.getAssignedStaff();
            if (staff == null || staff.trim().isEmpty()) staff = "Unassigned";

            // Filter specific staff if requested
            if (staffFilter != null && !staffFilter.trim().isEmpty() && !staffFilter.equalsIgnoreCase("ALL")) {
                if (!staff.equalsIgnoreCase(staffFilter.trim())) continue;
            }

            // Check if already in unique staff queue
            boolean exists = false;
            for (int j = 0; j < uniqueStaffQueue.size(); j++) {
                if (uniqueStaffQueue.get(j).equalsIgnoreCase(staff)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueStaffQueue.enqueue(staff);
            }
        }

        int staffCount = uniqueStaffQueue.size();
        StaffPerformanceItem[] items = new StaffPerformanceItem[staffCount];

        int globalTotalHandled = 0;
        int globalTotalCompleted = 0;

        // ── [FILTERING & CALCULATION PER STAFF MEMBER] ──
        for (int i = 0; i < staffCount; i++) {
            String staff = uniqueStaffQueue.get(i);
            int completed = 0, inProg = 0, pending = 0, total = 0;

            for (int j = 0; j < taskList.size(); j++) {
                CleaningTask t = taskList.get(j);
                String tStaff = t.getAssignedStaff();
                if (tStaff == null || tStaff.trim().isEmpty()) tStaff = "Unassigned";

                if (!tStaff.equalsIgnoreCase(staff)) continue;

                // Date filter
                if (startDate != null && t.getAssignedDate().isBefore(startDate)) continue;
                if (endDate != null && t.getAssignedDate().isAfter(endDate)) continue;

                // Status filter
                if (taskStatusFilter != null && t.getStatus() != taskStatusFilter) continue;

                total++;
                if (t.getStatus() == CleaningTask.TaskStatus.COMPLETED) completed++;
                else if (t.getStatus() == CleaningTask.TaskStatus.IN_PROGRESS) inProg++;
                else if (t.getStatus() == CleaningTask.TaskStatus.PENDING) pending++;
            }

            double rate = (total > 0) ? (((double) completed) / total) * 100.0 : 0.0;
            items[i] = new StaffPerformanceItem(staff, completed, inProg, pending, total, rate);

            globalTotalHandled += total;
            globalTotalCompleted += completed;
        }

        // ── [SORTING TECHNIQUE: Custom Selection Sort from Highest to Lowest Completed Tasks] ──
        for (int i = 0; i < items.length - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < items.length; j++) {
                if (items[j].getCompletedTasks() > items[maxIdx].getCompletedTasks()) {
                    maxIdx = j;
                } else if (items[j].getCompletedTasks() == items[maxIdx].getCompletedTasks()) {
                    // Tie-breaker: higher total handled tasks
                    if (items[j].getTotalHandled() > items[maxIdx].getTotalHandled()) {
                        maxIdx = j;
                    }
                }
            }
            if (maxIdx != i) {
                StaffPerformanceItem temp = items[i];
                items[i] = items[maxIdx];
                items[maxIdx] = temp;
            }
        }

        // Assign rank numbers 1, 2, 3...
        for (int i = 0; i < items.length; i++) {
            items[i].setRank(i + 1);
        }

        String topPerformer = (items.length > 0 && items[0].getCompletedTasks() > 0)
                ? items[0].getStaffName() + " (" + items[0].getCompletedTasks() + " completed tasks)"
                : "None";

        double overallEfficiency = (globalTotalHandled > 0)
                ? (((double) globalTotalCompleted) / globalTotalHandled) * 100.0
                : 0.0;

        return new StaffPerformanceReportResult(items, globalTotalHandled, globalTotalCompleted, topPerformer, overallEfficiency);
    }

    public void autoSave() {
        DataStore.saveTasks(taskList);
        DataStore.saveRooms(roomList);
    }

    private static int parseTrailingNum(String id) {
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
