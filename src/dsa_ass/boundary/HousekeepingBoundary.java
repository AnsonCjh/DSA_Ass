package dsa_ass.boundary;

import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.control.HousekeepingControl;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import dsa_ass.util.ConsoleUtils;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Boundary: HousekeepingBoundary
 *
 * Handles presentation and user interactions for Housekeeping & Task Log,
 * room inspections, status updates, history logs, and task records.
 */
public class HousekeepingBoundary {

    private final HousekeepingControl control;
    private final Scanner sc;

    public HousekeepingBoundary(HousekeepingControl control, Scanner sc) {
        this.control = control;
        this.sc      = sc;
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
                case "1": cleaningTaskManagement(); control.autoSave(); break;
                case "2": roomInspectionAndStatus(); control.autoSave(); break;
                case "3": taskRecords();                               break;
                case "0": back = true;                                 break;
                default:
                    System.out.println("  [!] Invalid option. Please try again.");
                    pressEnterToContinue();
            }
        }
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
                case "1": addCleaningTask();   control.autoSave(); break;
                case "2": viewAllTasks();                          break;
                case "3": updateTaskStatus();  control.autoSave(); break;
                case "4": deleteTask();        control.autoSave(); break;
                case "0": back = true;                             break;
                default:
                    System.out.println("  [!] Invalid option.");
                    pressEnterToContinue();
            }
        }
    }

    private void addCleaningTask() {
        printHeader("Add Cleaning Task");
        System.out.printf("  %-8s %-12s %-24s%n", "Room No", "Type", "Status");
        printDivider();
        Queue<Room> rooms = control.getRoomList();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
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

        Room targetRoom = control.findRoom(roomNo);
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

        CleaningTask task = control.addCleaningTask(roomNo, staff, priority, remarks);

        System.out.println();
        System.out.println("  [✓] Cleaning task [" + task.getTaskId() + "] added successfully to Stack ADT!");
        pressEnterToContinue();
    }

    private void viewAllTasks() {
        printHeader("All Cleaning Tasks (Stack ADT - LIFO)");
        Stack<CleaningTask> taskList = control.getTaskList();
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

    private void updateTaskStatus() {
        printHeader("Update Task Status");
        System.out.print("  Enter Task ID (or 0 to cancel): ");
        String taskId = sc.nextLine().trim().toUpperCase();
        if (taskId.equals("0")) {
            System.out.println("  Task status update cancelled.");
            pressEnterToContinue();
            return;
        }
        CleaningTask task = control.findTask(taskId);
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
        System.out.println("  3. COMPLETED    (marks Task COMPLETED; Room ready for inspection)");
        System.out.println("  0. Cancel");
        printDivider();
        System.out.print("  Select new status: ");
        String statusChoice = sc.nextLine().trim();

        Room room = control.findRoom(task.getRoomNo());
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
                    control.logRoomStatus(room.getRoomNo(), oldRoomStatus, Room.RoomStatus.CLEANING_IN_PROGRESS,
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

    private void deleteTask() {
        printHeader("Delete Task");
        System.out.print("  Enter Task ID to delete (or 0 to cancel): ");
        String taskId = sc.nextLine().trim().toUpperCase();
        if (taskId.equals("0")) {
            System.out.println("  Task deletion cancelled.");
            pressEnterToContinue();
            return;
        }
        boolean deleted = control.deleteTask(taskId);
        if (deleted) {
            System.out.println("  [✓] Task [" + taskId + "] deleted successfully from stack.");
        } else {
            System.out.println("  [!] Task not found.");
        }
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
            System.out.println("  4. Undo Latest Status Update");
            System.out.println("  5. View Status History");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": inspectRoom();                control.autoSave(); break;
                case "2": updateRoomStatus();           control.autoSave(); break;
                case "3": viewCurrentStatus();                             break;
                case "4": undoLatestStatusUpdate();     control.autoSave(); break;
                case "5": viewStatusHistory();                             break;
                case "0": back = true;                                     break;
                default:
                    System.out.println("  [!] Invalid option.");
                    pressEnterToContinue();
            }
        }
    }

    public void inspectRoom() {
        printHeader("Inspect Room");

        Stack<CleaningTask> taskList = control.getTaskList();
        int completedCount = 0;
        System.out.println("  --- Tasks Available for Room Inspection (Status: COMPLETED) ---");
        System.out.printf("  %-10s %-8s %-15s %-14s %-22s%n",
                "Task ID", "Room No", "Staff", "Task Status", "Room Status");
        printDivider();

        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (t.getStatus() == CleaningTask.TaskStatus.COMPLETED) {
                Room r = control.findRoom(t.getRoomNo());
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

        CleaningTask matchedTask = control.findTask(input);
        Room matchedRoom = null;

        if (matchedTask != null) {
            matchedRoom = control.findRoom(matchedTask.getRoomNo());
        } else {
            matchedRoom = control.findRoom(input);
            if (matchedRoom != null) {
                for (int i = 0; i < taskList.size(); i++) {
                    CleaningTask t = taskList.get(i);
                    if (t.getRoomNo().equalsIgnoreCase(input) && t.getStatus() == CleaningTask.TaskStatus.COMPLETED) {
                        matchedTask = t;
                        break;
                    }
                }
                if (matchedTask == null) {
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
            Room.RoomStatus prevStatus = matchedRoom.getStatus();
            matchedRoom.setStatus(Room.RoomStatus.INSPECTED);
            control.logRoomStatus(matchedRoom.getRoomNo(), prevStatus, Room.RoomStatus.INSPECTED,
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

            System.out.println();
            System.out.print("  Mark room as AVAILABLE for Front Desk now? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                matchedRoom.setStatus(Room.RoomStatus.AVAILABLE);
                control.logRoomStatus(matchedRoom.getRoomNo(), Room.RoomStatus.INSPECTED, Room.RoomStatus.AVAILABLE,
                        "Supervisor confirmed room is available");

                System.out.println();
                System.out.printf ("  Room %s%n", matchedRoom.getRoomNo());
                System.out.println("  INSPECTED → AVAILABLE");
                System.out.println("  [✓] Front desk can now see: " + matchedRoom.getRoomNo() + " | AVAILABLE");
            }
        } else {
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
                control.logRoomStatus(matchedRoom.getRoomNo(), prevStatus, Room.RoomStatus.DIRTY,
                        "Failed supervisor inspection - marked DIRTY for re-cleaning");

                System.out.println();
                System.out.printf ("  Room %s%n", matchedRoom.getRoomNo());
                System.out.printf ("  %s → DIRTY%n", prevStatus);

                System.out.print("  Auto-create new re-cleaning task? (Y/N): ");
                if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                    CleaningTask newTask = control.addCleaningTask(
                            matchedRoom.getRoomNo(),
                            matchedTask != null ? matchedTask.getAssignedStaff() : "Unassigned",
                            CleaningTask.TaskPriority.HIGH,
                            "Re-clean after failed inspection"
                    );
                    System.out.println("  [✓] Re-cleaning task [" + newTask.getTaskId() + "] created.");
                }
            } else {
                System.out.printf("  Room %s remains CLEANING_IN_PROGRESS.%n", matchedRoom.getRoomNo());
            }
        }
        pressEnterToContinue();
    }

    public void updateRoomStatus() {
        printHeader("Update Room Status");
        System.out.printf("  %-8s %-12s %-24s%n", "Room No", "Type", "Current Status");
        printDivider();
        Queue<Room> rooms = control.getRoomList();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            System.out.printf("  %-8s %-12s %-24s%n", r.getRoomNo(), r.getRoomType(), r.getStatus());
        }
        printDivider();
        System.out.println();

        System.out.print("  Enter Room Number to update (or 0 to cancel): ");
        String roomNo = sc.nextLine().trim().toUpperCase();
        if (roomNo.equals("0")) return;

        Room room = control.findRoom(roomNo);
        if (room == null) {
            System.out.println("  [!] Room not found.");
            pressEnterToContinue();
            return;
        }

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

        control.updateRoomStatus(room, newStatus, remarks);

        System.out.println();
        System.out.println("  ============================================");
        System.out.printf ("  [✓] Room %s updated:%n", room.getRoomNo());
        System.out.printf ("      %s  --->  %s%n", oldStatus, newStatus);
        System.out.println("  [Linear ADT] Status logged to history stack.");
        System.out.println("  ============================================");

        if (newStatus == Room.RoomStatus.DIRTY) {
            System.out.print("\n  Auto-create cleaning task for this dirty room? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                CleaningTask newTask = control.addCleaningTask(
                        room.getRoomNo(), "Unassigned",
                        CleaningTask.TaskPriority.HIGH,
                        "Checkout Cleaning for " + room.getRoomNo()
                );
                System.out.println("  [✓] Cleaning task [" + newTask.getTaskId() + "] created.");
            }
        }
        pressEnterToContinue();
    }

    public void viewCurrentStatus() {
        printHeader("Current Room Status Summary");
        System.out.printf("  %-8s %-12s %-14s %-9s %-24s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Status");
        printDivider();

        int dirty = 0, inProg = 0, inspected = 0, ready = 0, occupied = 0, other = 0;
        Queue<Room> rooms = control.getRoomList();

        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
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
        System.out.printf ("  Total Rooms           : %d%n", rooms.size());
        pressEnterToContinue();
    }

    public void undoLatestStatusUpdate() {
        printHeader("Undo Latest Status Update");
        System.out.println("  ADT: Linear ADT - Stack (LIFO)");
        System.out.println("  Operation: statusHistoryStack.pop()");
        System.out.println();

        Stack<RoomStatusLog> stack = control.getStatusHistoryStack();
        if (stack.isEmpty()) {
            System.out.println("  [!] No status updates to undo. History stack is empty.");
            pressEnterToContinue();
            return;
        }

        RoomStatusLog lastLog = stack.peek();
        Room room = control.findRoom(lastLog.getRoomNo());

        if (room == null) {
            stack.pop();
            System.out.println("  [!] Associated room [" + lastLog.getRoomNo() + "] no longer found.");
            pressEnterToContinue();
            return;
        }

        Room.RoomStatus current = room.getStatus();
        Room.RoomStatus revertedTo = lastLog.getPreviousStatus();

        System.out.println("  --- Latest Status Update Details ---");
        System.out.printf ("  Room Number      : %s%n", room.getRoomNo());
        System.out.printf ("  Current Status   : %s%n", current);
        System.out.printf ("  Will Revert To   : %s%n", revertedTo);
        System.out.printf ("  Remarks          : %s%n", lastLog.getRemarks());
        printDivider();
        System.out.print("  Confirm to undo status (Y/N): ");
        String confirm = sc.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("  [!] Undo operation cancelled.");
            pressEnterToContinue();
            return;
        }

        boolean success = control.undoLatestStatusUpdate();
        if (success) {
            System.out.println();
            System.out.println("  ============================================");
            System.out.println("        Undo Status Update Successful");
            System.out.println("  ============================================");
            System.out.printf ("  Room Number      : %s%n", room.getRoomNo());
            System.out.printf ("  Reverted From    : %s%n", current);
            System.out.printf ("  Reverted Back To : %s%n", revertedTo);
            System.out.printf ("  Original Remarks : %s%n", lastLog.getRemarks());
            System.out.printf ("  Remaining Logs   : %d update(s) in stack%n", stack.size());
            System.out.println("  ============================================");
            System.out.println("  [✓] Room status restored!");
        }
        pressEnterToContinue();
    }

    public void viewStatusHistory() {
        printHeader("Room Status History (Linear ADT Stack - LIFO)");
        System.out.println("  Displays all status updates from most recent to oldest:");
        System.out.println();

        Stack<RoomStatusLog> stack = control.getStatusHistoryStack();
        if (stack.isEmpty()) {
            System.out.println("  No status transitions logged yet.");
            pressEnterToContinue();
            return;
        }

        printDivider();
        for (int i = 0; i < stack.size(); i++) {
            System.out.println("  " + stack.get(i));
        }
        printDivider();
        System.out.printf("  Total Log Entries: %d%n", stack.size());
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

            CleaningTask.TaskStatus filter;
            String label;
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

            Stack<CleaningTask> filtered = control.filterTasks(filter);
            for (int i = 0; i < filtered.size(); i++) {
                System.out.println("  " + filtered.get(i));
            }
            if (filtered.isEmpty()) System.out.println("  No tasks found for category: " + label);
            printDivider();
            System.out.printf("  Total: %d task(s)%n", filtered.size());
            pressEnterToContinue();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

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
}
