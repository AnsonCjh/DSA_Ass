package dsa_ass.module;

import dsa_ass.adt.LinkedList;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Room;
import dsa_ass.util.ConsoleUtils;
import dsa_ass.util.DataStore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Housekeeping Module
 * Handles: Cleaning Task Management, Room Inspection, Task Records
 */
public class HousekeepingModule {

    private final LinkedList<CleaningTask> taskList;
    private final LinkedList<Room>         roomList;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static int taskCounter = 1;

    /** Called by DataStore after loading saved data to prevent ID collisions. */
    public static void setTaskCounter(int n) { taskCounter = n; }

    public HousekeepingModule(LinkedList<CleaningTask> taskList,
                              LinkedList<Room> roomList,
                              Scanner sc) {
        this.taskList = taskList;
        this.roomList = roomList;
        this.sc       = sc;
    }

    // ── Sub-Menu ────────────────────────────────────────────────
    public void showMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Housekeeping Module");
            System.out.println("  1. Cleaning Task Management");
            System.out.println("  2. Room Inspection");
            System.out.println("  3. Task Records");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": cleaningTaskManagement(); autoSave(); break;
                case "2": roomInspection();         autoSave(); break;
                case "3": taskRecords();                        break; // read-only
                case "0": back = true;                          break;
                default:  System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }

    // ── Auto-save after every state-changing action ──────────────
    private void autoSave() {
        DataStore.saveTasks(taskList);
        DataStore.saveRooms(roomList);
    }

    // ── 1. Cleaning Task Management ───────────────────────────────
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
                case "1": addCleaningTask();   break;
                case "2": viewAllTasks();      break;
                case "3": updateTaskStatus();  break;
                case "4": deleteTask();        break;
                case "0": back = true;         break;
                default:  System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ── Add Cleaning Task ─────────────────────────────────────────
    private void addCleaningTask() {
        printHeader("Add Cleaning Task");
        String taskId = String.format("T%04d", taskCounter);
        System.out.println("  Task ID assigned : " + taskId);

        System.out.printf("  %-8s %-12s %-20s%n", "Room No", "Type", "Status");
        printDivider();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            System.out.printf("  %-8s %-12s %-20s%n", r.getRoomNo(), r.getRoomType(), r.getStatus());
        }
        System.out.println();

        System.out.print("  Room Number       : ");
        String roomNo = sc.nextLine().trim().toUpperCase();
        System.out.print("  Assigned Staff    : ");
        String staff = sc.nextLine().trim();

        System.out.println("  Priority (1=LOW, 2=MEDIUM, 3=HIGH):");
        System.out.print("  Select: ");
        String priChoice = sc.nextLine().trim();
        CleaningTask.TaskPriority priority;
        switch (priChoice) {
            case "3": priority = CleaningTask.TaskPriority.HIGH;   break;
            case "2": priority = CleaningTask.TaskPriority.MEDIUM; break;
            default:  priority = CleaningTask.TaskPriority.LOW;
        }

        System.out.print("  Remarks           : ");
        String remarks = sc.nextLine().trim();

        CleaningTask task = new CleaningTask(taskId, roomNo, staff, priority, LocalDate.now(), remarks);
        taskList.add(task);
        taskCounter++;

        System.out.println();
        System.out.println("  Cleaning task [" + taskId + "] added successfully!");
        pressEnterToContinue();
    }

    // ── View All Tasks ────────────────────────────────────────────
    private void viewAllTasks() {
        printHeader("All Cleaning Tasks");
        if (taskList.isEmpty()) {
            System.out.println("  No tasks found.");
        } else {
            System.out.printf("  %-10s %-8s %-15s %-8s %-12s %-12s %s%n",
                    "Task ID", "Room", "Staff", "Priority", "Status", "Date", "Remarks");
            printDivider();
            for (int i = 0; i < taskList.size(); i++) {
                System.out.println("  " + taskList.get(i));
            }
        }
        pressEnterToContinue();
    }

    // ── Update Task Status ────────────────────────────────────────
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

        System.out.println("  Current Status: " + task.getStatus());
        System.out.println("  1. PENDING");
        System.out.println("  2. IN_PROGRESS");
        System.out.println("  3. COMPLETED");
        System.out.print("  Select new status: ");
        String statusChoice = sc.nextLine().trim();
        switch (statusChoice) {
            case "1":
                task.setStatus(CleaningTask.TaskStatus.PENDING);
                break;
            case "2":
                task.setStatus(CleaningTask.TaskStatus.IN_PROGRESS);
                break;
            case "3":
                task.setStatus(CleaningTask.TaskStatus.COMPLETED);
                Room room = findRoom(task.getRoomNo());
                if (room != null && room.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) {
                    room.setStatus(Room.RoomStatus.AVAILABLE);
                }
                break;
            default:
                System.out.println("  [!] Invalid.");
                pressEnterToContinue();
                return;
        }
        System.out.println("  Task status updated to: " + task.getStatus());
        pressEnterToContinue();
    }

    // ── Delete Task ───────────────────────────────────────────────
    private void deleteTask() {
        printHeader("Delete Task");
        System.out.print("  Enter Task ID to delete: ");
        String taskId = sc.nextLine().trim().toUpperCase();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId)) {
                taskList.remove(i);
                System.out.println("  Task [" + taskId + "] deleted successfully.");
                pressEnterToContinue();
                return;
            }
        }
        System.out.println("  [!] Task not found.");
        pressEnterToContinue();
    }

    // ── 2. Room Inspection ────────────────────────────────────────
    public void roomInspection() {
        printHeader("Room Inspection");
        System.out.print("  Enter Room Number to inspect: ");
        String roomNo = sc.nextLine().trim().toUpperCase();
        Room room = findRoom(roomNo);
        if (room == null) {
            System.out.println("  [!] Room not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("  ============================================");
        System.out.printf ("  Room No       : %s%n", room.getRoomNo());
        System.out.printf ("  Type          : %s%n", room.getRoomType());
        System.out.printf ("  Capacity      : %d%n", room.getCapacity());
        System.out.printf ("  Price/Night   : RM %.2f%n", room.getPricePerNight());
        System.out.printf ("  Current Status: %s%n", room.getStatus());
        System.out.println("  --- Cleaning History ---");

        boolean found = false;
        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (t.getRoomNo().equalsIgnoreCase(roomNo)) {
                System.out.printf("  [%s] %-10s %-12s %-12s %s%n",
                        t.getAssignedDate().format(DATE_FMT),
                        t.getTaskId(), t.getAssignedStaff(), t.getStatus(), t.getRemarks());
                found = true;
            }
        }
        if (!found) System.out.println("  No cleaning records.");

        System.out.println();
        System.out.print("  Set room under maintenance? (Y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
            room.setStatus(Room.RoomStatus.UNDER_MAINTENANCE);
            System.out.println("  Room [" + roomNo + "] set to UNDER_MAINTENANCE.");
        }
        System.out.println("  ============================================");
        pressEnterToContinue();
    }

    // ── 3. Task Records ───────────────────────────────────────────
    public void taskRecords() {
        printHeader("Task Records");
        System.out.println("  Filter by:");
        System.out.println("  1. All Tasks");
        System.out.println("  2. Pending Tasks");
        System.out.println("  3. In-Progress Tasks");
        System.out.println("  4. Completed Tasks");
        printDivider();
        System.out.print("  Enter choice: ");
        String choice = sc.nextLine().trim();

        CleaningTask.TaskStatus filter = null;
        String label = "All";
        switch (choice) {
            case "2": filter = CleaningTask.TaskStatus.PENDING;     label = "Pending";     break;
            case "3": filter = CleaningTask.TaskStatus.IN_PROGRESS; label = "In-Progress"; break;
            case "4": filter = CleaningTask.TaskStatus.COMPLETED;   label = "Completed";   break;
            default:  label = "All";
        }

        System.out.printf("%n  === %s Tasks ===%n", label);
        System.out.printf("  %-10s %-8s %-15s %-8s %-12s %-12s %s%n",
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
        if (count == 0) System.out.println("  No tasks found.");
        System.out.printf("%n  Total: %d task(s)%n", count);
        pressEnterToContinue();
    }

    // ── Helpers ──────────────────────────────────────────────────
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
        System.out.printf ("        %s%n", title);
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
