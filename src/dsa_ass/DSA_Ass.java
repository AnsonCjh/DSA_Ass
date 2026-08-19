/*
 * TARUMT Resort System
 * Data Structures & Algorithms Assignment
 * Architecture: ECB (Entity-Control-Boundary)
 */
package dsa_ass;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.boundary.FrontDeskBoundary;
import dsa_ass.boundary.HousekeepingBoundary;
import dsa_ass.boundary.WalkInRegistrationBoundary;
import dsa_ass.control.FrontDeskControl;
import dsa_ass.control.HousekeepingControl;
import dsa_ass.control.WalkInRegistrationControl;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import dsa_ass.util.ConsoleUtils;
import dsa_ass.util.DataStore;
import java.io.File;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Main entry point - coordinates ECB architecture boundaries and controllers.
 * ADTs Used: Queue<Guest>, Stack<CleaningTask>, BinarySearchTree<Reservation>, Queue<Room>
 */
public class DSA_Ass {

    // ── Shared data stores (Queue / Stack / BST ADTs) ───────────
    private static final Queue<Guest>                  guestList          = new Queue<Guest>();
    private static final BinarySearchTree<Reservation> reservationList    = new BinarySearchTree<Reservation>();
    private static final Queue<Room>                   roomList           = new Queue<Room>();
    private static final Stack<CleaningTask>           taskList           = new Stack<CleaningTask>();
    private static final Queue<Guest>                  walkInQueue        = new Queue<Guest>();
    private static final Queue<String>                 walkInResIds       = new Queue<String>();
    private static final Stack<RoomStatusLog>          statusHistoryStack = new Stack<RoomStatusLog>();

    private static final Scanner sc = new Scanner(System.in);

    // ── Entry Point ──────────────────────────────────────────────
    public static void main(String[] args) {
        // ── Step 1: Always seed the fixed room list from code ─────
        seedRooms();

        // ── Step 2: Load persisted guest / reservation / task data ─
        boolean firstRun = !new File("data/guests.csv").exists()
                        || !new File("data/rooms.csv").exists();
        if (firstRun) {
            // First-ever launch: also seed sample guests & tasks, then persist everything
            seedGuestsAndTasks();
            WalkInRegistrationControl.setGuestCounter(4);
            WalkInRegistrationControl.setResCounter  (1);
            HousekeepingControl.setTaskCounter       (3);
            DataStore.saveAll(guestList, roomList, reservationList, taskList);
            System.out.println("  [DB] Data directory created. Sample data seeded.");
        } else {
            // Normal launch: load guests, room statuses, reservations, tasks from CSV
            int[] counters = DataStore.loadAll(guestList, roomList, reservationList, taskList);
            WalkInRegistrationControl.setGuestCounter(counters[0] + 1);
            WalkInRegistrationControl.setResCounter  (counters[1] + 1);
            HousekeepingControl.setTaskCounter       (counters[2] + 1);
            System.out.println("  [DB] Data loaded from file.");
        }

        // ── Step 3: Re-sync room status from active reservations ──
        // Reset all rooms to AVAILABLE, then mark OCCUPIED for every
        // CONFIRMED / CHECKED_IN reservation that still references a room.
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getStatus() != Room.RoomStatus.UNDER_MAINTENANCE) {
                r.setStatus(Room.RoomStatus.AVAILABLE);
            }
        }
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);
            if (res.getStatus() == Reservation.ReservationStatus.CONFIRMED
                    || res.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
                for (int j = 0; j < roomList.size(); j++) {
                    if (roomList.get(j).getRoomNo().equalsIgnoreCase(res.getRoomNo())) {
                        roomList.get(j).setStatus(Room.RoomStatus.OCCUPIED);
                        break;
                    }
                }
            }
        }

        showMainMenu();

        // ── Final save on clean exit ──────────────────────────────────
        DataStore.saveAll(guestList, roomList, reservationList, taskList);
        System.out.println("\n  Thank you for using TARUMT Resort System. Goodbye!");
        sc.close();
    }

    // ── Main Menu ────────────────────────────────────────────────
    private static void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtils.clearScreen();
            printWelcome();
            System.out.print("  Enter a number to select: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1":
                    WalkInRegistrationControl walkInControl = new WalkInRegistrationControl(
                            guestList, reservationList, roomList, walkInQueue, walkInResIds);
                    new WalkInRegistrationBoundary(walkInControl, sc).showMenu();
                    DataStore.saveAll(guestList, roomList, reservationList, taskList);
                    break;
                case "2":
                    FrontDeskControl frontDeskControl = new FrontDeskControl(
                            guestList, reservationList, roomList, taskList, walkInQueue);
                    new FrontDeskBoundary(frontDeskControl, sc).showMenu();
                    DataStore.saveAll(guestList, roomList, reservationList, taskList);
                    break;
                case "3":
                    HousekeepingControl housekeepingControl = new HousekeepingControl(
                            taskList, roomList, reservationList, guestList, statusHistoryStack);
                    new HousekeepingBoundary(housekeepingControl, sc).showMenu();
                    DataStore.saveAll(guestList, roomList, reservationList, taskList);
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 1 - 4.");
                    pause();
            }
        }
    }

    // ── Welcome Banner ───────────────────────────────────────────
    private static void printWelcome() {
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("       Welcome to TARUMT Resort System       ");
        System.out.println("  ============================================");
        System.out.println("  1. Walk-In Registration Module");
        System.out.println("  2. Front Desk Module");
        System.out.println("  3. Housekeeping Module");
        System.out.println("  4. Exit");
        System.out.println("  ============================================");
    }

    // ── Seed Fixed Rooms (always called on every startup) ───────────
    private static void seedRooms() {
        roomList.enqueue(new Room("S001", Room.RoomType.SINGLE,   99.00,  1));
        roomList.enqueue(new Room("S002", Room.RoomType.SINGLE,   99.00,  1));
        roomList.enqueue(new Room("R101", Room.RoomType.STANDARD, 180.00, 2));
        roomList.enqueue(new Room("R102", Room.RoomType.STANDARD, 180.00, 2));
        roomList.enqueue(new Room("R201", Room.RoomType.DELUXE,   280.00, 4));
        roomList.enqueue(new Room("R202", Room.RoomType.DELUXE,   280.00, 4));
        roomList.enqueue(new Room("R301", Room.RoomType.SUITE,    450.00, 4));
        roomList.enqueue(new Room("R302", Room.RoomType.SUITE,    450.00, 4));
        roomList.enqueue(new Room("V001", Room.RoomType.VILLA,    950.00, 8));
        roomList.enqueue(new Room("V002", Room.RoomType.VILLA,    950.00, 8));
    }

    // ── Seed Sample Guests & Tasks (first run only) ──────────────
    private static void seedGuestsAndTasks() {
        // Sample Guests  (G001 – G003)
        guestList.enqueue(new Guest("G001", "Ahmad Faris",  "990101-14-5678", "012-3456789", "ahmad@mail.com",  "Malaysian"));
        guestList.enqueue(new Guest("G002", "Priya Nair",   "P12345678",      "016-9876543", "priya@mail.com",  "Indian"));
        guestList.enqueue(new Guest("G003", "Lim Wei Xian", "010203-10-1234", "011-2233445", "limwx@mail.com",  "Malaysian"));

        seedSampleTasks();
    }

    private static void seedSampleTasks() {
        CleaningTask t1 = new CleaningTask("T0001", "101", "Ali",
                CleaningTask.TaskPriority.HIGH, LocalDate.now(), "Routine clean", "09:10");
        t1.setStatus(CleaningTask.TaskStatus.PENDING);
        t1.setUpdatedTime("09:10");

        CleaningTask t2 = new CleaningTask("T0002", "205", "Mei Ling",
                CleaningTask.TaskPriority.MEDIUM, LocalDate.now(), "Checkout clean", "10:25");
        t2.setStatus(CleaningTask.TaskStatus.IN_PROGRESS);
        t2.setUpdatedTime("10:25");

        CleaningTask t3 = new CleaningTask("T0003", "301", "Kumar",
                CleaningTask.TaskPriority.HIGH, LocalDate.now(), "Deep clean", "11:40");
        t3.setStatus(CleaningTask.TaskStatus.COMPLETED);
        t3.setUpdatedTime("11:40");

        CleaningTask t4 = new CleaningTask("T0004", "108", "Ali",
                CleaningTask.TaskPriority.LOW, LocalDate.now(), "Towel restock", "12:15");
        t4.setStatus(CleaningTask.TaskStatus.COMPLETED);
        t4.setUpdatedTime("12:15");

        CleaningTask t5 = new CleaningTask("T0005", "210", "Mei Ling",
                CleaningTask.TaskPriority.MEDIUM, LocalDate.now(), "Inspection prep", "13:05");
        t5.setStatus(CleaningTask.TaskStatus.PENDING);
        t5.setUpdatedTime("13:05");

        taskList.push(t1);
        taskList.push(t2);
        taskList.push(t3);
        taskList.push(t4);
        taskList.push(t5);
        HousekeepingControl.setTaskCounter(6);
    }

    private static void pause() {
        System.out.print("  Press Enter to continue...");
        sc.nextLine();
    }
}
