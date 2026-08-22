package dsa_ass.util;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import java.io.*;
import java.time.LocalDate;

/**
 * DataStore – CSV file-based persistence for TARUMT Resort System.
 *
 * Files are stored in the "data/" folder relative to the project working
 * directory (the NetBeans project root when run from the IDE).
 *
 * Delimiter: pipe '|' — avoids clashes with commas in names / remarks.
 *
 * Usage:
 *   DataStore.saveAll(guests, rooms, reservations, tasks);
 *   int[] counters = DataStore.loadAll(guests, rooms, reservations, tasks);
 *   // counters[0] = max guest num, [1] = max res num, [2] = max task num
 */
public class DataStore {

    private static final String DATA_DIR    = "data";
    private static final String GUESTS_FILE = DATA_DIR + "/guests.csv";
    private static final String ROOMS_FILE  = DATA_DIR + "/rooms.csv";
    private static final String RES_FILE    = DATA_DIR + "/reservations.csv";
    private static final String TASKS_FILE  = DATA_DIR + "/tasks.csv";

    /** Field separator — must not appear in user data after escaping. */
    private static final String SEP       = "|";
    private static final String SEP_REGEX = "\\|";

    // ── Save All ─────────────────────────────────────────────────────────────

    /**
     * Persists all four data lists to their respective CSV files.
     * Creates the data/ directory automatically if it does not exist.
     */
    public static void saveAll(Queue<Guest>                  guests,
                               Queue<Room>                   rooms,
                               BinarySearchTree<Reservation> reservations,
                               Stack<CleaningTask>           tasks) {
        ensureDataDir();
        saveGuests(guests);
        saveRooms(rooms);
        saveReservations(reservations);
        saveTasks(tasks);
    }

    // ── Load All ─────────────────────────────────────────────────────────────

    /**
     * Loads guests, reservations and tasks from CSV files.
     * Rooms are NOT loaded here — they are always seeded from code in DSA_Ass
     * and their status is re-synced from active reservations at startup.
     *
     * @return int[] { maxGuestNum, maxResNum, maxTaskNum }
     */
    public static int[] loadAll(Queue<Guest>                  guests,
                                Queue<Room>                   rooms,
                                BinarySearchTree<Reservation> reservations,
                                Stack<CleaningTask>           tasks) {
        int maxGuest = loadGuests(guests);
        loadRooms(rooms);
        int maxRes   = loadReservations(reservations);
        int maxTask  = loadTasks(tasks);
        return new int[]{ maxGuest, maxRes, maxTask };
    }

    // ── Guests ────────────────────────────────────────────────────────────────

    /** Writes all guests to guests.csv. */
    public static void saveGuests(Queue<Guest> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(GUESTS_FILE))) {
            for (int i = 0; i < list.size(); i++) {
                Guest g = list.get(i);
                pw.println(esc(g.getGuestId())    + SEP
                         + esc(g.getName())        + SEP
                         + esc(g.getIcNo())        + SEP
                         + esc(g.getPhone())       + SEP
                         + esc(g.getEmail())       + SEP
                         + esc(g.getNationality()));
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not save guests: " + e.getMessage());
        }
    }

    /**
     * Reads guests.csv and appends into list via enqueue.
     * @return highest numeric suffix found in guest IDs (e.g. 3 for "G0003").
     */
    public static int loadGuests(Queue<Guest> list) {
        int max = 0;
        File f = new File(GUESTS_FILE);
        if (!f.exists()) return max;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(SEP_REGEX, -1);
                if (p.length < 6) continue;
                list.enqueue(new Guest(p[0], p[1], p[2], p[3], p[4], p[5]));
                max = Math.max(max, parseTrailingNum(p[0]));
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not load guests: " + e.getMessage());
        }
        return max;
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────

    /** Writes all rooms (including current status) to rooms.csv. */
    public static void saveRooms(Queue<Room> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (int i = 0; i < list.size(); i++) {
                Room r = list.get(i);
                pw.println(esc(r.getRoomNo())         + SEP
                         + r.getRoomType().name()      + SEP
                         + r.getPricePerNight()        + SEP
                         + r.getCapacity()             + SEP
                         + r.getStatus().name());
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not save rooms: " + e.getMessage());
        }
    }

    /** Reads rooms.csv and syncs room status into existing list (or appends new rooms). */
    public static void loadRooms(Queue<Room> list) {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(SEP_REGEX, -1);
                if (p.length < 5) continue;
                String roomNo = p[0];
                Room.RoomStatus status;
                try {
                    status = Room.RoomStatus.valueOf(p[4]);
                } catch (IllegalArgumentException e) {
                    status = Room.RoomStatus.AVAILABLE;
                }
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    Room r = list.get(i);
                    if (r.getRoomNo().equalsIgnoreCase(roomNo)) {
                        r.setStatus(status);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Room r = new Room(
                            p[0],
                            Room.RoomType.valueOf(p[1]),
                            Double.parseDouble(p[2]),
                            Integer.parseInt(p[3]));
                    r.setStatus(status);
                    list.enqueue(r);
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not load rooms: " + e.getMessage());
        }
    }

    // ── Reservations ──────────────────────────────────────────────────────────

    /** Writes all active reservations to reservations.csv. */
    public static void saveReservations(BinarySearchTree<Reservation> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RES_FILE))) {
            for (int i = 0; i < list.size(); i++) {
                Reservation r = list.get(i);
                if (r.getStatus() != Reservation.ReservationStatus.CANCELLED) {
                    pw.println(esc(r.getReservationId())  + SEP
                             + esc(r.getGuestId())        + SEP
                             + esc(r.getRoomNo())         + SEP
                             + r.getCheckInDate()         + SEP
                             + r.getCheckOutDate()        + SEP
                             + r.getNumGuests()           + SEP
                             + r.getTotalAmount()         + SEP
                             + r.getStatus().name()       + SEP
                             + esc(r.getConfirmationNo()) + SEP
                             + r.getDeposit());
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not save reservations: " + e.getMessage());
        }
    }

    /**
     * Reads reservations.csv and appends active reservations into list.
     * Column layout: resId|guestId|roomNo|checkIn|checkOut|numGuests|total|status|confirmationNo|deposit
     * The confirmationNo (index 8) and deposit (index 9) columns are optional for backward compatibility.
     * @return highest numeric suffix found in reservation IDs.
     */
    public static int loadReservations(BinarySearchTree<Reservation> list) {
        int max = 0;
        File f = new File(RES_FILE);
        if (!f.exists()) return max;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(SEP_REGEX, -1);
                if (p.length < 8) continue;
                Reservation res = new Reservation(
                        p[0], p[1], p[2],
                        LocalDate.parse(p[3]),
                        LocalDate.parse(p[4]),
                        Integer.parseInt(p[5]),
                        Double.parseDouble(p[6]));
                Reservation.ReservationStatus status =
                        Reservation.ReservationStatus.valueOf(p[7]);
                res.setStatus(status);
                // Load confirmationNo if present (column 9, index 8)
                if (p.length >= 9 && !p[8].isEmpty()) {
                    res.setConfirmationNo(p[8]);
                }
                // Load deposit if present (column 10, index 9)
                if (p.length >= 10 && !p[9].isEmpty()) {
                    try {
                        res.setDeposit(Double.parseDouble(p[9]));
                    } catch (NumberFormatException e) {
                        res.setDeposit(0.0);
                    }
                }
                max = Math.max(max, parseTrailingNum(p[0]));
                if (status != Reservation.ReservationStatus.CANCELLED) {
                    list.add(res);
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not load reservations: " + e.getMessage());
        }
        return max;
    }

    // ── Cleaning Tasks ────────────────────────────────────────────────────────

    /** Writes all cleaning tasks to tasks.csv (in bottom-to-top order to maintain LIFO stack order on reload). */
    public static void saveTasks(Stack<CleaningTask> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TASKS_FILE))) {
            for (int i = list.size() - 1; i >= 0; i--) {
                CleaningTask t = list.get(i);
                pw.println(esc(t.getTaskId())        + SEP
                         + esc(t.getRoomNo())        + SEP
                         + esc(t.getAssignedStaff()) + SEP
                         + t.getStatus().name()      + SEP
                         + t.getAssignedDate()       + SEP
                         + esc(t.getRemarks())       + SEP
                         + esc(t.getUpdatedTime()));
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not save tasks: " + e.getMessage());
        }
    }

    /**
     * Reads tasks.csv and appends into list.
     * Supports both 7-field (TaskId|RoomNo|Staff|Status|Date|Remarks|Time)
     * and 8-field (TaskId|RoomNo|Staff|Priority|Status|Date|Remarks|Time) formats.
     * @return highest numeric suffix found in task IDs.
     */
    public static int loadTasks(Stack<CleaningTask> list) {
        int max = 0;
        File f = new File(TASKS_FILE);
        if (!f.exists()) return max;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(SEP_REGEX, -1);
                if (p.length < 6) continue;

                CleaningTask t;
                if (p.length >= 8) {
                    String updatedTime = (!p[7].isEmpty()) ? p[7] : "09:00";
                    CleaningTask.TaskPriority priority = CleaningTask.TaskPriority.LOW;
                    try { priority = CleaningTask.TaskPriority.valueOf(p[3]); } catch (Exception ignored) {}
                    CleaningTask.TaskStatus status = CleaningTask.TaskStatus.PENDING;
                    try { status = CleaningTask.TaskStatus.valueOf(p[4]); } catch (Exception ignored) {}
                    LocalDate date = LocalDate.now();
                    try { date = LocalDate.parse(p[5]); } catch (Exception ignored) {}

                    t = new CleaningTask(p[0], p[1], p[2], priority, date, p[6], updatedTime);
                    t.setStatus(status);
                    t.setUpdatedTime(updatedTime);
                } else {
                    CleaningTask.TaskStatus status = CleaningTask.TaskStatus.PENDING;
                    try { status = CleaningTask.TaskStatus.valueOf(p[3]); } catch (Exception ignored) {}
                    LocalDate date = LocalDate.now();
                    try { date = LocalDate.parse(p[4]); } catch (Exception ignored) {}
                    String remarks = (p.length > 5) ? p[5] : "Checkout Cleaning";
                    String updatedTime = (p.length > 6 && !p[6].isEmpty()) ? p[6] : "09:00";

                    t = new CleaningTask(p[0], p[1], p[2], CleaningTask.TaskPriority.LOW, date, remarks, updatedTime);
                    t.setStatus(status);
                    t.setUpdatedTime(updatedTime);
                }
                list.push(t);
                max = Math.max(max, parseTrailingNum(p[0]));
            }
        } catch (IOException e) {
            System.out.println("  [!] Could not load tasks: " + e.getMessage());
        }
        return max;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates the data/ directory if it does not already exist. */
    private static void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Parses the trailing numeric part of an ID string.
     * e.g. "G0003" -> 3,  "R00002" -> 2,  "T0004" -> 4
     */
    private static int parseTrailingNum(String id) {
        if (id == null || id.isEmpty()) return 0;
        // Strip leading non-digit characters
        int i = 0;
        while (i < id.length() && !Character.isDigit(id.charAt(i))) i++;
        try {
            return Integer.parseInt(id.substring(i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Escapes pipe characters in a field value so they cannot break parsing.
     * Pipes are replaced with a forward-slash (visually similar, safe).
     */
    private static String esc(String s) {
        return s == null ? "" : s.replace("|", "/");
    }
}
