package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import dsa_ass.util.ConsoleUtils;
import dsa_ass.util.DataStore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Housekeeping & Task Log Module
 *
 * Implements:
 *   1. Cleaning Task Management (Stack ADT - LIFO)
 *   2. Room Inspection & Status Lifecycle (DIRTY -> CLEANING_IN_PROGRESS -> INSPECTED -> AVAILABLE)
 *      - Linear ADT: Stack<RoomStatusLog> for Undo and Status History
 *   3. Task Records (Filtered query views)
 */
public class HousekeepingModule {

    private final Stack<CleaningTask>           taskList;
    private final Queue<Room>                  roomList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Guest>                 guestList;
    private final Stack<RoomStatusLog>         statusHistoryStack;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static int taskCounter = 1;

    /** Called by DataStore after loading saved data to prevent ID collisions. */
    public static void setTaskCounter(int n) { taskCounter = n; }

    public HousekeepingModule(Stack<CleaningTask>           taskList,
                             Queue<Room>                  roomList,
                             BinarySearchTree<Reservation> reservationList,
                             Queue<Guest>                 guestList,
                             Stack<RoomStatusLog>         statusHistoryStack,
                             Scanner sc) {
        this.taskList           = taskList;
        this.roomList           = roomList;
        this.reservationList    = reservationList;
        this.guestList          = guestList;
        this.statusHistoryStack = (statusHistoryStack != null) ? statusHistoryStack : new Stack<RoomStatusLog>();
        this.sc                 = sc;
    }

    public HousekeepingModule(Stack<CleaningTask>           taskList,
                             Queue<Room>                  roomList,
                             BinarySearchTree<Reservation> reservationList,
                             Queue<Guest>                 guestList,
                             Scanner sc) {
        this(taskList, roomList, reservationList, guestList, null, sc);
    }

    public HousekeepingModule(Stack<CleaningTask> taskList,
                             Queue<Room>         roomList,
                             Scanner sc) {
        this(taskList, roomList, null, null, null, sc);
    }

    // ══════════════════════════════════════════════════════════════
    // Main Sub-Menu: HOUSEKEEPING & TASK LOG
    // ══════════════════════════════════════════════════════════════
    public void showMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("HOUSEKEEPING & TASK LOG");
            System.out.println("  1. Cleaning Task Management");
            System.out.println("  2. Room Inspection & Status");
            System.out.println("  3. Task Records");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": cleaningTaskManagement(); autoSave(); break;
                case "2": roomInspectionAndStatus(); autoSave(); break;
                case "3": taskRecords();                        break;
                case "0": back = true;                          break;
                default:
                    System.out.println("  [!] Invalid option. Please try again.");
                    pressEnterToContinue();
            }
        }
    }

    // ── Auto-save after every state-changing action ──────────────
    private void autoSave() {
        DataStore.saveTasks(taskList);
        DataStore.saveRooms(roomList);
    }

    // ══════════════════════════════════════════════════════════════
    // 1. Cleaning Task Management
    // ══════════════════════════════════════════════════════════════
    private void cleaningTaskManagement() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Cleaning Task Management");
            System.out.println("  1. Add Cleaning Task");
            System.out.println("  2. View All Tasks");
            System.out.println("  3. Update Task Status");
            System.out.println("  4. Delete Task");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": addCleaningTask();   autoSave(); break;
                case "2": viewAllTasks();                  break;
                case "3": updateTaskStatus();  autoSave(); break;
                case "4": deleteTask();        autoSave(); break;
                case "0": back = true;                     break;
                default:
                    System.out.println("  [!] Invalid option.");
                    pressEnterToContinue();
            }
        }
    }

    // ── 1.1 Add Cleaning Task ─────────────────────────────────────
    private void addCleaningTask() {
        printHeader("Add Cleaning Task");
        String taskId = generateTaskId(taskList);
        System.out.println("  Task ID assigned : " + taskId);
        System.out.println();

        System.out.printf("  %-8s %-12s %-24s%n", "Room No", "Type", "Status");
        printDivider();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            System.out.printf("  %-8s %-12s %-24s%n", r.getRoomNo(), r.getRoomType(), r.getStatus());
        }
        System.out.println();

        System.out.print("  Room Number       (or 0 to cancel): ");
        String roomNo = sc.nextLine().trim().toUpperCase();
        if (roomNo.equals("0")) {
            System.out.println("  Task creation cancelled.");
            pressEnterToContinue();
            return;
        }

        Room targetRoom = findRoom(roomNo);
        if (targetRoom == null) {
            System.out.println("  [!] Room [" + roomNo + "] not found in system.");
            pressEnterToContinue();
            return;
        }

        System.out.print("  Assigned Staff    : ");
        String staff = sc.nextLine().trim();
        if (staff.isEmpty()) staff = "Unassigned";

        System.out.println("  Priority (1=LOW, 2=MEDIUM, 3=HIGH):");
        System.out.print("  Select: ");
        String priChoice = sc.nextLine().trim();
        CleaningTask.TaskPriority priority;
        switch (priChoice) {
            case "3": priority = CleaningTask.TaskPriority.HIGH;   break;
            case "2": priority = CleaningTask.TaskPriority.MEDIUM; break;
            default:  priority = CleaningTask.TaskPriority.LOW;
        }

        System.out.print("  Task Description / Remarks : ");
        String remarks = sc.nextLine().trim();
        if (remarks.isEmpty()) remarks = "Checkout Cleaning";

        CleaningTask task = new CleaningTask(taskId, roomNo, staff, priority, LocalDate.now(), remarks);
        taskList.push(task);

        System.out.println();
        System.out.println("  [✓] Cleaning task [" + taskId + "] added successfully to Stack ADT!");
        pressEnterToContinue();
    }

    // ── 1.2 View All Tasks ────────────────────────────────────────
    private void viewAllTasks() {
        printHeader("All Cleaning Tasks (Stack ADT - LIFO)");
        if (taskList.isEmpty()) {
            System.out.println("  No tasks found in stack.");
        } else {
            System.out.printf("  %-10s %-8s %-15s %-8s %-14s %-12s %s%n",
                    "Task ID", "Room", "Staff", "Priority", "Status", "Date", "Remarks");
            printDivider();
            for (int i = 0; i < taskList.size(); i++) {
                System.out.println("  " + taskList.get(i));
            }
            printDivider();
            System.out.printf("  Total Tasks: %d%n", taskList.size());
        }
        pressEnterToContinue();
    }

    // ── 1.3 Update Task Status ────────────────────────────────────
    private void updateTaskStatus() {
        printHeader("Update Task Status");
        System.out.print("  Enter Task ID: ");
        String taskId = sc.nextLine().trim().toUpperCase();
        CleaningTask task = findTask(taskId);
        if (task == null) {
            System.out.println("  [!] Task not found.");
            pressEnterToContinue();
            return;
        }

        System.out.printf("  Task ID     : %s%n", task.getTaskId());
        System.out.printf("  Room Number : %s%n", task.getRoomNo());
        System.out.printf("  Staff       : %s%n", task.getAssignedStaff());
        System.out.printf("  Current Task Status : %s%n", task.getStatus());
        System.out.println();
        System.out.println("  Select new task status:");
        System.out.println("  1. PENDING");
        System.out.println("  2. IN_PROGRESS  (updates Room to CLEANING_IN_PROGRESS)");
        System.out.println("  3. COMPLETED    (updates Room to INSPECTED)");
        System.out.println("  0. Cancel");
        printDivider();
        System.out.print("  Select new status: ");
        String statusChoice = sc.nextLine().trim();

        Room room = findRoom(task.getRoomNo());
        Room.RoomStatus oldRoomStatus = room != null ? room.getStatus() : null;

        switch (statusChoice) {
            case "1":
                task.setStatus(CleaningTask.TaskStatus.PENDING);
                System.out.println("  Task status set to: PENDING");
                break;
            case "2":
                task.setStatus(CleaningTask.TaskStatus.IN_PROGRESS);
                System.out.println("  Task status set to: IN_PROGRESS");
                if (room != null && (room.getStatus() == Room.RoomStatus.DIRTY
                        || room.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE)) {
                    room.setStatus(Room.RoomStatus.CLEANING_IN_PROGRESS);
                    logRoomStatus(room.getRoomNo(), oldRoomStatus, Room.RoomStatus.CLEANING_IN_PROGRESS,
                            "Cleaning began (Task " + task.getTaskId() + ")");
                    System.out.printf("  [Room Sync] Room %s status: %s -> CLEANING_IN_PROGRESS%n",
                            room.getRoomNo(), oldRoomStatus);
                }
                break;
            case "3":
                task.setStatus(CleaningTask.TaskStatus.COMPLETED);
                System.out.println("  Task status set to: COMPLETED");
                System.out.printf ("  [Notice] Task %s is COMPLETED. Room %s (Status: %s) is now ready for supervisor inspection under 'Inspect Room'.%n",
                        task.getTaskId(), task.getRoomNo(), (room != null ? room.getStatus() : "CLEANING_IN_PROGRESS"));
                break;
            case "0":
                System.out.println("  Status update cancelled.");
                pressEnterToContinue();
                return;
            default:
                System.out.println("  [!] Invalid choice.");
                pressEnterToContinue();
                return;
        }

        System.out.println("  [✓] Task updated successfully!");
        pressEnterToContinue();
    }

    // ── 1.4 Delete Task ───────────────────────────────────────────
    private void deleteTask() {
        printHeader("Delete Task");
        System.out.print("  Enter Task ID to delete: ");
        String taskId = sc.nextLine().trim().toUpperCase();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId)) {
                taskList.remove(i);
                System.out.println("  [✓] Task [" + taskId + "] deleted successfully from stack.");
                pressEnterToContinue();
                return;
            }
        }
        System.out.println("  [!] Task not found.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 2. Room Inspection & Status
    // ══════════════════════════════════════════════════════════════
    public void roomInspectionAndStatus() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Room Inspection & Status");
            System.out.println("  1. Inspect Room");
            System.out.println("  2. Update Room Status");
            System.out.println("  3. View Current Status");
            System.out.println("  4. Undo Latest Status Update  [Linear ADT]");
            System.out.println("  5. View Status History        [Linear ADT]");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": inspectRoom();                autoSave(); break;
                case "2": updateRoomStatus();           autoSave(); break;
                case "3": viewCurrentStatus();                      break;
                case "4": undoLatestStatusUpdate();     autoSave(); break;
                case "5": viewStatusHistory();                      break;
                case "0": back = true;                              break;
                default:
                    System.out.println("  [!] Invalid option.");
                    pressEnterToContinue();
            }
        }
    }

    // ── 2.1 Inspect Room ──────────────────────────────────────────
    public void inspectRoom() {
        printHeader("Inspect Room");

        // 1. Find all completed tasks ready for room inspection
        int completedCount = 0;
        System.out.println("  --- Tasks Available for Room Inspection (Status: COMPLETED) ---");
        System.out.printf("  %-10s %-8s %-15s %-14s %-22s%n",
                "Task ID", "Room No", "Staff", "Task Status", "Room Status");
        printDivider();

        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (t.getStatus() == CleaningTask.TaskStatus.COMPLETED) {
                Room r = findRoom(t.getRoomNo());
                String rStatus = (r != null) ? r.getStatus().name() : "N/A";
                System.out.printf("  %-10s %-8s %-15s %-14s %-22s%n",
                        t.getTaskId(), t.getRoomNo(), t.getAssignedStaff(), t.getStatus(), rStatus);
                completedCount++;
            }
        }

        if (completedCount == 0) {
            System.out.println("  No tasks with status COMPLETED currently waiting for inspection.");
        }
        printDivider();
        System.out.println();

        System.out.print("  Enter Room Number or Task ID to inspect (or 0 to cancel): ");
        String input = sc.nextLine().trim().toUpperCase();
        if (input.equals("0")) return;

        // Locate task and room
        CleaningTask matchedTask = null;
        Room matchedRoom = null;

        // Try match task ID first
        matchedTask = findTask(input);
        if (matchedTask != null) {
            matchedRoom = findRoom(matchedTask.getRoomNo());
        } else {
            // Try match room No
            matchedRoom = findRoom(input);
            if (matchedRoom != null) {
                // Find most recent completed task for this room if any
                for (int i = 0; i < taskList.size(); i++) {
                    CleaningTask t = taskList.get(i);
                    if (t.getRoomNo().equalsIgnoreCase(input) && t.getStatus() == CleaningTask.TaskStatus.COMPLETED) {
                        matchedTask = t;
                        break;
                    }
                }
                if (matchedTask == null) {
                    // Find any task for this room
                    for (int i = 0; i < taskList.size(); i++) {
                        if (taskList.get(i).getRoomNo().equalsIgnoreCase(input)) {
                            matchedTask = taskList.get(i);
                            break;
                        }
                    }
                }
            }
        }

        if (matchedRoom == null) {
            System.out.println("  [!] Room or Task not found.");
            pressEnterToContinue();
            return;
        }

        String taskIdDisplay = (matchedTask != null) ? matchedTask.getTaskId() : "N/A";
        String taskStatusDisplay = (matchedTask != null) ? matchedTask.getStatus().name() : "NO_TASK";
        Room.RoomStatus currentRoomStatus = matchedRoom.getStatus();

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("           Supervisor Room Inspection");
        System.out.println("  ============================================");
        System.out.printf ("  Task %-6s = %s%n", taskIdDisplay, taskStatusDisplay);
        System.out.printf ("  Room %-6s = %s%n", matchedRoom.getRoomNo(), currentRoomStatus);
        if (matchedTask != null) {
            System.out.printf ("  Staff       : %s%n", matchedTask.getAssignedStaff());
            System.out.printf ("  Remarks     : %s%n", matchedTask.getRemarks());
        }
        System.out.println("  --------------------------------------------");
        System.out.printf ("  The supervisor is inspecting room %s...%n", matchedRoom.getRoomNo());
        System.out.println();
        System.out.print("  Does the room pass inspection? (Y/N/0 to cancel): ");
        String passChoice = sc.nextLine().trim().toUpperCase();

        if (passChoice.equals("0")) {
            System.out.println("  Inspection cancelled.");
            pressEnterToContinue();
            return;
        }

        if (passChoice.equalsIgnoreCase("Y")) {
            // Room passes inspection: CLEANING_IN_PROGRESS -> INSPECTED
            Room.RoomStatus prevStatus = matchedRoom.getStatus();
            matchedRoom.setStatus(Room.RoomStatus.INSPECTED);
            logRoomStatus(matchedRoom.getRoomNo(), prevStatus, Room.RoomStatus.INSPECTED,
                    "Supervisor inspection passed" + (matchedTask != null ? " (Task " + matchedTask.getTaskId() + ")" : ""));

            System.out.println();
            System.out.println("  ============================================");
            System.out.println("             Inspection Passed!");
            System.out.println("  ============================================");
            System.out.printf ("  Room %s%n", matchedRoom.getRoomNo());
            System.out.printf ("  %s → INSPECTED%n", prevStatus);
            System.out.println("  ============================================");
            System.out.println("  [✓] Room status updated to INSPECTED.");
            System.out.println("  [Linear ADT] Status logged to history stack.");

            // Offer next step: AVAILABLE
            System.out.println();
            System.out.print("  Mark room as AVAILABLE for Front Desk now? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                matchedRoom.setStatus(Room.RoomStatus.AVAILABLE);
                logRoomStatus(matchedRoom.getRoomNo(), Room.RoomStatus.INSPECTED, Room.RoomStatus.AVAILABLE,
                        "Supervisor confirmed room is available");

                System.out.println();
                System.out.printf ("  Room %s%n", matchedRoom.getRoomNo());
                System.out.println("  INSPECTED → AVAILABLE");
                System.out.println("  [✓] Front desk can now see: " + matchedRoom.getRoomNo() + " | AVAILABLE");
            }
        } else {
            // Inspection failed
            System.out.println();
            System.out.printf ("  [!] Room %s failed inspection.%n", matchedRoom.getRoomNo());
            System.out.println("  Select next action:");
            System.out.println("  1. Return room to DIRTY (schedule re-cleaning)");
            System.out.println("  2. Keep as CLEANING_IN_PROGRESS");
            System.out.print("  Select (1/2): ");
            String failAction = sc.nextLine().trim();

            if (failAction.equals("1")) {
                Room.RoomStatus prevStatus = matchedRoom.getStatus();
                matchedRoom.setStatus(Room.RoomStatus.DIRTY);
                logRoomStatus(matchedRoom.getRoomNo(), prevStatus, Room.RoomStatus.DIRTY,
                        "Failed supervisor inspection - marked DIRTY for re-cleaning");

                System.out.println();
                System.out.printf ("  Room %s%n", matchedRoom.getRoomNo());
                System.out.printf ("  %s → DIRTY%n", prevStatus);

                System.out.print("  Auto-create new re-cleaning task? (Y/N): ");
                if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                    String taskId = generateTaskId(taskList);
                    CleaningTask newTask = new CleaningTask(
                            taskId, matchedRoom.getRoomNo(),
                            matchedTask != null ? matchedTask.getAssignedStaff() : "Unassigned",
                            CleaningTask.TaskPriority.HIGH, LocalDate.now(),
                            "Re-clean after failed inspection"
                    );
                    taskList.push(newTask);
                    System.out.println("  [✓] Re-cleaning task [" + taskId + "] created.");
                }
            } else {
                System.out.printf("  Room %s remains CLEANING_IN_PROGRESS.%n", matchedRoom.getRoomNo());
            }
        }
        pressEnterToContinue();
    }

    // ── 2.2 Update Room Status ────────────────────────────────────
    public void updateRoomStatus() {
        printHeader("Update Room Status");
        System.out.printf("  %-8s %-12s %-24s%n", "Room No", "Type", "Current Status");
        printDivider();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            System.out.printf("  %-8s %-12s %-24s%n", r.getRoomNo(), r.getRoomType(), r.getStatus());
        }
        printDivider();
        System.out.println();

        System.out.print("  Enter Room Number to update (or 0 to cancel): ");
        String roomNo = sc.nextLine().trim().toUpperCase();
        if (roomNo.equals("0")) return;

        Room room = findRoom(roomNo);
        if (room == null) {
            System.out.println("  [!] Room not found.");
            pressEnterToContinue();
            return;
        }

        changeSingleRoomStatus(room);
    }

    private void changeSingleRoomStatus(Room room) {
        Room.RoomStatus oldStatus = room.getStatus();
        System.out.println();
        System.out.printf("  Current Status for Room %s: %s%n", room.getRoomNo(), oldStatus);
        System.out.println("  Select New Condition:");
        System.out.println("  1. DIRTY");
        System.out.println("  2. CLEANING_IN_PROGRESS");
        System.out.println("  3. INSPECTED");
        System.out.println("  4. AVAILABLE");
        System.out.println("  0. Cancel");
        printDivider();
        System.out.print("  Enter choice (1 - 4): ");
        String choice = sc.nextLine().trim();

        Room.RoomStatus newStatus;
        switch (choice) {
            case "1": newStatus = Room.RoomStatus.DIRTY;                break;
            case "2": newStatus = Room.RoomStatus.CLEANING_IN_PROGRESS; break;
            case "3": newStatus = Room.RoomStatus.INSPECTED;            break;
            case "4": newStatus = Room.RoomStatus.AVAILABLE;            break;
            case "0":
                System.out.println("  Update cancelled.");
                pressEnterToContinue();
                return;
            default:
                System.out.println("  [!] Invalid selection.");
                pressEnterToContinue();
                return;
        }

        System.out.print("  Enter update remarks (e.g. Supervisor inspection): ");
        String remarks = sc.nextLine().trim();
        if (remarks.isEmpty()) remarks = "Supervisor status update";

        // Update status and push to Linear ADT history stack
        room.setStatus(newStatus);
        logRoomStatus(room.getRoomNo(), oldStatus, newStatus, remarks);

        System.out.println();
        System.out.println("  ============================================");
        System.out.printf ("  [✓] Room %s updated:%n", room.getRoomNo());
        System.out.printf ("      %s  --->  %s%n", oldStatus, newStatus);
        System.out.println("  [Linear ADT] Status logged to history stack.");
        System.out.println("  ============================================");

        // If set to DIRTY, offer to auto-create a cleaning task
        if (newStatus == Room.RoomStatus.DIRTY) {
            System.out.print("\n  Auto-create cleaning task for this dirty room? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                String taskId = generateTaskId(taskList);
                CleaningTask newTask = new CleaningTask(
                        taskId, room.getRoomNo(), "Unassigned",
                        CleaningTask.TaskPriority.HIGH, LocalDate.now(),
                        "Checkout Cleaning for " + room.getRoomNo()
                );
                taskList.push(newTask);
                System.out.println("  [✓] Cleaning task [" + taskId + "] created.");
            }
        }
        pressEnterToContinue();
    }

    // ── 2.3 View Current Status ───────────────────────────────────
    public void viewCurrentStatus() {
        printHeader("Current Room Status Summary");
        System.out.printf("  %-8s %-12s %-14s %-9s %-24s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Status");
        printDivider();

        int dirty = 0, inProg = 0, inspected = 0, ready = 0, occupied = 0, other = 0;

        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-24s%n",
                    r.getRoomNo(), r.getRoomType(), r.getPricePerNight(), r.getCapacity(), r.getStatus());

            if (r.getStatus() == Room.RoomStatus.DIRTY) dirty++;
            else if (r.getStatus() == Room.RoomStatus.CLEANING_IN_PROGRESS) inProg++;
            else if (r.getStatus() == Room.RoomStatus.INSPECTED) inspected++;
            else if (r.getStatus() == Room.RoomStatus.AVAILABLE || r.getStatus() == Room.RoomStatus.READY_FOR_CHECK_IN) ready++;
            else if (r.getStatus() == Room.RoomStatus.OCCUPIED) occupied++;
            else other++;
        }

        printDivider();
        System.out.println("  --- Status Breakdown ---");
        System.out.printf ("  DIRTY                 : %d%n", dirty);
        System.out.printf ("  CLEANING_IN_PROGRESS  : %d%n", inProg);
        System.out.printf ("  INSPECTED             : %d%n", inspected);
        System.out.printf ("  AVAILABLE             : %d%n", ready);
        System.out.printf ("  OCCUPIED              : %d%n", occupied);
        if (other > 0) System.out.printf ("  OTHER / MAINTENANCE   : %d%n", other);
        printDivider();
        System.out.printf ("  Total Rooms           : %d%n", roomList.size());
        pressEnterToContinue();
    }

    // ── 2.4 Undo Latest Status Update (Linear ADT: Stack.pop()) ───
    public void undoLatestStatusUpdate() {
        printHeader("Undo Latest Status Update");
        System.out.println("  ADT: Linear ADT - Stack (LIFO)");
        System.out.println("  Operation: statusHistoryStack.pop()");
        System.out.println();

        if (statusHistoryStack.isEmpty()) {
            System.out.println("  [!] No status updates to undo. History stack is empty.");
            pressEnterToContinue();
            return;
        }

        RoomStatusLog lastLog = statusHistoryStack.pop();
        Room room = findRoom(lastLog.getRoomNo());

        if (room == null) {
            System.out.println("  [!] Associated room [" + lastLog.getRoomNo() + "] no longer found.");
            pressEnterToContinue();
            return;
        }

        Room.RoomStatus current = room.getStatus();
        Room.RoomStatus revertedTo = lastLog.getPreviousStatus();
        room.setStatus(revertedTo);

        System.out.println("  ============================================");
        System.out.println("        Undo Status Update Successful");
        System.out.println("  ============================================");
        System.out.printf ("  Room Number      : %s%n", room.getRoomNo());
        System.out.printf ("  Reverted From    : %s%n", current);
        System.out.printf ("  Reverted Back To : %s%n", revertedTo);
        System.out.printf ("  Original Remarks : %s%n", lastLog.getRemarks());
        System.out.printf ("  Remaining Logs   : %d update(s) in stack%n", statusHistoryStack.size());
        System.out.println("  ============================================");
        System.out.println("  [✓] Room status restored!");
        pressEnterToContinue();
    }

    // ── 2.5 View Status History (Linear ADT: Stack Traversal) ─────
    public void viewStatusHistory() {
        printHeader("Room Status History (Linear ADT Stack - LIFO)");
        System.out.println("  Displays all status updates from most recent to oldest:");
        System.out.println();

        if (statusHistoryStack.isEmpty()) {
            System.out.println("  No status transitions logged yet.");
            pressEnterToContinue();
            return;
        }

        printDivider();
        for (int i = 0; i < statusHistoryStack.size(); i++) {
            System.out.println("  " + statusHistoryStack.get(i));
        }
        printDivider();
        System.out.printf("  Total Log Entries: %d%n", statusHistoryStack.size());
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Task Records
    // ══════════════════════════════════════════════════════════════
    public void taskRecords() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Task Records");
            System.out.println("  Filter by:");
            System.out.println("  1. All Tasks");
            System.out.println("  2. Pending");
            System.out.println("  3. In Progress");
            System.out.println("  4. Completed");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter choice: ");
            String choice = sc.nextLine().trim();

            CleaningTask.TaskStatus filter = null;
            String label = "All";
            switch (choice) {
                case "1": filter = null;                                label = "All";         break;
                case "2": filter = CleaningTask.TaskStatus.PENDING;     label = "Pending";     break;
                case "3": filter = CleaningTask.TaskStatus.IN_PROGRESS; label = "In Progress"; break;
                case "4": filter = CleaningTask.TaskStatus.COMPLETED;   label = "Completed";   break;
                case "0": back = true; continue;
                default:
                    System.out.println("  [!] Invalid choice.");
                    pressEnterToContinue();
                    continue;
            }

            ConsoleUtils.clearScreen();
            printHeader(label + " Tasks");
            System.out.printf("  %-10s %-8s %-15s %-8s %-14s %-12s %s%n",
                    "Task ID", "Room", "Staff", "Priority", "Status", "Date", "Remarks");
            printDivider();

            int count = 0;
            for (int i = 0; i < taskList.size(); i++) {
                CleaningTask t = taskList.get(i);
                if (filter == null || t.getStatus() == filter) {
                    System.out.println("  " + t);
                    count++;
                }
            }
            if (count == 0) System.out.println("  No tasks found for category: " + label);
            printDivider();
            System.out.printf("  Total: %d task(s)%n", count);
            pressEnterToContinue();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    private void logRoomStatus(String roomNo, Room.RoomStatus prev, Room.RoomStatus next, String remarks) {
        statusHistoryStack.push(new RoomStatusLog(roomNo, prev, next, remarks));
    }

    private CleaningTask findTask(String taskId) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId))
                return taskList.get(i);
        }
        return null;
    }

    private Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    private void printHeader(String title) {
        ConsoleUtils.clearScreen();
        System.out.println();
        System.out.println("  ============================================");
        int totalWidth = 44;
        int pad = Math.max(0, (totalWidth - title.length()) / 2);
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(title);
        System.out.println(sb.toString());
        System.out.println("  ============================================");
    }

    private void printDivider() {
        System.out.println("  --------------------------------------------");
    }

    private void pressEnterToContinue() {
        System.out.print("\n  Press Enter to continue...");
        sc.nextLine();
    }

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
