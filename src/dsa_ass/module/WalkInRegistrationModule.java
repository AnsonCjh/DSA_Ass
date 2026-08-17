package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.ConsoleUtils;
import dsa_ass.util.DataStore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Walk-In Registration Module
 *
 * Demonstrates the Queue ADT (FIFO) for managing walk-in guests.
 *
 * Queue ADT Operations used:
 *   enqueue()  - Register Walk-In Guest (adds guest to waiting queue)
 *   dequeue()  - Process Next Guest (removes front guest after reservation)
 *   peek()     - View queue front / Process Next (reads front without removing)
 *   isEmpty()  - Guards for view/process operations
 *   traversal  - View Walk-In Queue (iterates all waiting positions)
 *
 * Data stores:
 *   guestList       - shared Queue<Guest>, permanent registry, persisted to guests.csv
 *   walkInQueue     - local Queue<Guest>, FIFO waiting queue (session-only)
 *   reservationList - shared BST<Reservation>, persisted to reservations.csv
 *   roomList        - shared Queue<Room>, seeded from code
 *   walkInResIds    - local Queue<String>, tracks reservation IDs made via this module
 */
public class WalkInRegistrationModule {

    // ── Shared data stores (passed from DSA_Ass) ────────────────
    private final Queue<Guest>                 guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                  roomList;
    private final Scanner sc;

    // ── Queue ADT: Walk-In Waiting Queue (FIFO) ──────────────────
    // Separate from guestList — represents guests currently waiting to be served.
    // Guests are enqueue()d on registration and dequeue()d once processed.
    private final Queue<Guest>  walkInQueue;

    // ── Tracks reservation IDs created in this module ────────────
    private final Queue<String> walkInResIds;

    // ── Formatting & counters ────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static int guestCounter = 1;
    private static int resCounter   = 1;

    /** Called by DataStore after loading saved data to prevent ID collisions. */
    public static void setGuestCounter(int n) { guestCounter = n; }
    public static void setResCounter(int n)   { resCounter   = n; }

    public WalkInRegistrationModule(Queue<Guest>                 guestList,
                                    BinarySearchTree<Reservation> reservationList,
                                    Queue<Room>                  roomList,
                                    Queue<Guest>                 walkInQueue,
                                    Queue<String>                walkInResIds,
                                    Scanner sc) {
        this.guestList       = guestList;
        this.reservationList = reservationList;
        this.roomList        = roomList;
        this.walkInQueue     = walkInQueue;
        this.walkInResIds    = walkInResIds;
        this.sc              = sc;
    }

    public WalkInRegistrationModule(Queue<Guest>                 guestList,
                                    BinarySearchTree<Reservation> reservationList,
                                    Queue<Room>                  roomList,
                                    Scanner sc) {
        this(guestList, reservationList, roomList, new Queue<Guest>(), new Queue<String>(), sc);
    }

    // ══════════════════════════════════════════════════════════════
    // Sub-Menu
    // ══════════════════════════════════════════════════════════════
    public void showMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Walk-In Registration Module");
            System.out.println("  1. Register Walk-In Guest");
            System.out.println("  2. View Walk-In Queue");
            System.out.println("  3. Process Next Walk-In Guest");
            System.out.println("  4. Check Room Availability");
            System.out.println("  5. View Walk-In Reservations");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": registerWalkIn();       break;
                case "2": viewWalkInQueue();      break;
                case "3": processNextGuest();     break;
                case "4": checkRoomAvailability(); break;
                case "5": viewWalkInReservations(); break;
                case "0": back = true;            break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 5.");
                    pressEnterToContinue();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 1. Register Walk-In Guest
    //    Queue ADT: enqueue() — adds guest to back of walkInQueue (FIFO)
    // ══════════════════════════════════════════════════════════════
    private void registerWalkIn() {
        printHeader("Register Walk-In Guest");
        System.out.println("  (Enter 0 at any field to cancel)");
        System.out.println();

        String name = readNonEmptyInput("  Full Name         : ");
        if (name.equals("0")) { cancelled(); return; }

        String ic = readNonEmptyInput("  IC / Passport No  : ");
        if (ic.equals("0")) { cancelled(); return; }

        String phone = readNonEmptyInput("  Phone Number      : ");
        if (phone.equals("0")) { cancelled(); return; }

        String email = readNonEmptyInput("  Email Address     : ");
        if (email.equals("0")) { cancelled(); return; }

        String nationality = readNonEmptyInput("  Nationality       : ");
        if (nationality.equals("0")) { cancelled(); return; }

        // Generate unique guest ID: strictly increment beyond the highest existing guest ID
        int maxIdNum = 0;
        for (int i = 0; i < guestList.size(); i++) {
            int num = parseTrailingNum(guestList.get(i).getGuestId());
            if (num > maxIdNum) {
                maxIdNum = num;
            }
        }
        int nextIdNum = Math.max(maxIdNum + 1, guestCounter);
        guestCounter = nextIdNum + 1;
        String guestId = String.format("G%03d", nextIdNum);

        Guest g = new Guest(guestId, name, ic, phone, email, nationality);

        // enqueue() into permanent guest registry (persisted to CSV)
        guestList.enqueue(g);

        // enqueue() into walk-in FIFO waiting queue
        walkInQueue.enqueue(g);

        autoSave();

        // Queue position = current size (guest was just added to rear)
        int queuePosition = walkInQueue.size();

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("       Walk-In Registration Successful");
        System.out.println("  ============================================");
        System.out.printf ("  Guest ID       : %s%n", guestId);
        System.out.printf ("  Full Name      : %s%n", name);
        System.out.printf ("  IC / Passport  : %s%n", ic);
        System.out.printf ("  Phone Number   : %s%n", phone);
        System.out.printf ("  Email Address  : %s%n", email);
        System.out.printf ("  Nationality    : %s%n", nationality);
        System.out.println("  ============================================");
        System.out.println();
        System.out.printf ("  Current queue position : #%d%n", queuePosition);
        System.out.printf ("  Guests waiting         : %d%n", walkInQueue.size());
        if (queuePosition == 1) {
            System.out.println("  This guest is at the FRONT and will be served next.");
        } else {
            System.out.printf ("  %d guest(s) ahead in queue.%n", queuePosition - 1);
        }
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 2. View Walk-In Queue
    //    Queue ADT: peek() (front indicator) + traversal via get(i)
    // ══════════════════════════════════════════════════════════════
    private void viewWalkInQueue() {
        syncWalkInQueue();
        printHeader("View Walk-In Queue");
        System.out.println();

        // isEmpty() check
        if (walkInQueue.isEmpty()) {
            System.out.println("  No guests currently waiting.");
            System.out.println();
            System.out.println("  The walk-in queue is empty. Register a guest first (Option 1).");
            pressEnterToContinue();
            return;
        }

        // peek() — get front guest without removing
        Guest frontGuest = walkInQueue.peek();

        System.out.printf("  Total Guests Waiting : %d%n", walkInQueue.size());
        System.out.printf("  Next to be Served    : %s (%s)%n",
                frontGuest.getName(), frontGuest.getGuestId());
        System.out.println();
        System.out.printf("  %-5s %-10s %-22s %-18s%n",
                "Queue", "Guest ID", "Full Name", "Phone");
        printDivider();

        // Queue traversal — iterate all positions from front to rear
        for (int i = 0; i < walkInQueue.size(); i++) {
            Guest g = walkInQueue.get(i);  // traversal via get(i)
            String marker = (i == 0) ? "  <- NEXT" : "";
            System.out.printf("  #%-4d %-10s %-22s %-18s%s%n",
                    (i + 1), g.getGuestId(), g.getName(), g.getPhone(), marker);
        }
        printDivider();
        System.out.println();
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Process Next Walk-In Guest
    //    Queue ADT: peek() — read front, dequeue() — remove after processing
    // ══════════════════════════════════════════════════════════════
    private void processNextGuest() {
        syncWalkInQueue();
        printHeader("Process Next Walk-In Guest");
        System.out.println();

        // isEmpty() guard
        if (walkInQueue.isEmpty()) {
            System.out.println("  No guests in queue to process.");
            System.out.println();
            System.out.println("  Register a walk-in guest first (Option 1).");
            pressEnterToContinue();
            return;
        }

        // peek() — get front guest WITHOUT removing (dequeue happens only after processing)
        Guest guest = walkInQueue.peek();

        System.out.println("  ============================================");
        System.out.println("             Guest Information");
        System.out.println("  ============================================");
        System.out.printf ("  Guest ID       : %s%n", guest.getGuestId());
        System.out.printf ("  Full Name      : %s%n", guest.getName());
        System.out.printf ("  IC / Passport  : %s%n", guest.getIcNo());
        System.out.printf ("  Phone Number   : %s%n", guest.getPhone());
        System.out.printf ("  Email Address  : %s%n", guest.getEmail());
        System.out.printf ("  Nationality    : %s%n", guest.getNationality());
        System.out.println("  ============================================");
        System.out.println();

        // Begin room selection loop (handles unavailable fallback)
        boolean bookingComplete = processRoomSelection(guest);

        // dequeue() happens here — either after successful booking OR cancelled booking
        if (bookingComplete) {
            // Booking was either completed successfully or cancelled — remove from queue
            walkInQueue.dequeue();
            System.out.println();
            System.out.println("  Guest removed from walk-in queue.");
            System.out.printf ("  Guests remaining in queue: %d%n", walkInQueue.size());
        }
        // If bookingComplete is false, guest stays in queue (they chose to retry later - not used in this flow)
        pressEnterToContinue();
    }

    /**
     * Handles room selection for the guest being processed.
     * Returns true when the guest should be dequeue()d (booking done OR cancelled).
     */
    private boolean processRoomSelection(Guest guest) {
        while (true) {
            LocalDate checkIn  = null;
            LocalDate checkOut = null;
            Room.RoomType roomType = null;

            // Initial date & room type collection
            System.out.println("  Step 1: Select Room Type");
            roomType = selectRoomType();
            if (roomType == null) {
                // Staff backed out — keep guest in queue
                System.out.println("  Room selection cancelled. Guest remains in queue.");
                return false;
            }

            System.out.println();
            checkIn = readDateOrBack("  Check-In Date  (DD/MM/YYYY, or 0 to reselect room type): ");
            if (checkIn == null) {
                System.out.println("  [↺] Returning to room type selection...\n");
                continue;
            }

            checkOut = readDateAfterOrBack("  Check-Out Date (DD/MM/YYYY, or 0 to reselect room type): ", checkIn);
            if (checkOut == null) {
                System.out.println("  [↺] Returning to room type selection...\n");
                continue;
            }

            // Availability check + retry loop
            boolean reselectRoomType = false;
            while (!reselectRoomType) {
                // Find rooms of the selected type available for the given dates
                Queue<Room> available = findAvailableRooms(roomType, checkIn, checkOut);

                if (!available.isEmpty()) {
                    // ── Success path ──────────────────────────────────────────
                    System.out.println();
                    System.out.printf("  Available %s Rooms for %s to %s:%n",
                            roomType, checkIn.format(DATE_FMT), checkOut.format(DATE_FMT));
                    System.out.printf("  %-8s %-12s %-14s %-9s%n",
                            "Room No", "Type", "Price/Night", "Capacity");
                    printDivider();
                    for (int i = 0; i < available.size(); i++) {
                        Room r = available.get(i);
                        System.out.printf("  %-8s %-12s RM%-12.2f %-9d%n",
                                r.getRoomNo(), r.getRoomType(), r.getPricePerNight(), r.getCapacity());
                    }
                    printDivider();

                    System.out.print("  Enter Room Number to book (or 0 to reselect room type): ");
                    String roomNo = sc.nextLine().trim().toUpperCase();
                    if (roomNo.equals("0")) {
                        System.out.println("  [↺] Returning to room type selection...\n");
                        reselectRoomType = true;
                        break;
                    }

                    Room selectedRoom = null;
                    for (int i = 0; i < available.size(); i++) {
                        if (available.get(i).getRoomNo().equalsIgnoreCase(roomNo)) {
                            selectedRoom = available.get(i);
                            break;
                        }
                    }

                    if (selectedRoom == null) {
                        System.out.println("  [!] Room not found in available list. Please try again.");
                        pressEnterToContinue();
                        continue;
                    }

                    // Number of guests
                    System.out.printf("  Number of Guests (max %d): ", selectedRoom.getCapacity());
                    int numGuests;
                    try { numGuests = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { numGuests = 1; }
                    if (numGuests < 1) numGuests = 1;
                    if (numGuests > selectedRoom.getCapacity()) {
                        System.out.printf("  [!] Room %s holds max %d guest(s). Adjusting to %d.%n",
                                roomNo, selectedRoom.getCapacity(), selectedRoom.getCapacity());
                        numGuests = selectedRoom.getCapacity();
                    }

                    // Calculate totals
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                    double total = nights * selectedRoom.getPricePerNight();

                    // Generate reservation ID and 8-digit confirmation number (increments beyond highest existing ID)
                    int maxIdNum = 0;
                    for (int i = 0; i < reservationList.size(); i++) {
                        int num = parseTrailingNum(reservationList.get(i).getReservationId());
                        if (num > maxIdNum) {
                            maxIdNum = num;
                        }
                    }
                    int nextIdNum = Math.max(maxIdNum + 1, resCounter);
                    resCounter = nextIdNum + 1;
                    String resId  = String.format("R%03d", nextIdNum);
                    String confNo = String.format("%08d", nextIdNum);  // 8-digit confirmation number

                    // Reservation confirmation page
                    System.out.println();
                    System.out.println("  ============================================");
                    System.out.println("         Reservation Confirmation");
                    System.out.println("  ============================================");
                    System.out.printf ("  Confirmation No: %s  *** KEEP THIS ***%n", confNo);
                    System.out.println("  --------------------------------------------");
                    System.out.printf ("  Reservation ID : %s%n", resId);
                    System.out.printf ("  Guest ID       : %s%n", guest.getGuestId());
                    System.out.printf ("  Guest Name     : %s%n", guest.getName());
                    System.out.printf ("  Room           : %s (%s)%n", selectedRoom.getRoomNo(), selectedRoom.getRoomType());
                    System.out.printf ("  Check-In       : %s%n", checkIn.format(DATE_FMT));
                    System.out.printf ("  Check-Out      : %s%n", checkOut.format(DATE_FMT));
                    System.out.printf ("  Nights         : %d%n", nights);
                    System.out.printf ("  No. of Guests  : %d%n", numGuests);
                    System.out.printf ("  Price/Night    : RM %.2f%n", selectedRoom.getPricePerNight());
                    System.out.printf ("  Total Amount   : RM %.2f%n", total);
                    System.out.println("  ============================================");
                    System.out.println("  Present the Confirmation No. at the Front Desk");
                    System.out.println("  for Check-In, Check-Out, or Enquiries.");
                    System.out.println("  ============================================");
                    System.out.println();
                    System.out.print("  Confirm reservation? (Y/N): ");
                    String confirm = sc.nextLine().trim();
                    if (!confirm.equalsIgnoreCase("Y")) {
                        System.out.println("  Reservation not confirmed. Guest remains in queue.");
                        return false;
                    }

                    // Create and save reservation
                    Reservation res = new Reservation(resId, guest.getGuestId(),
                            selectedRoom.getRoomNo(), checkIn, checkOut, numGuests, total);
                    res.setStatus(Reservation.ReservationStatus.CONFIRMED);
                    res.setConfirmationNo(confNo);   // attach 8-digit confirmation number
                    reservationList.add(res);
                    selectedRoom.setStatus(Room.RoomStatus.OCCUPIED);

                    // Track this reservation as a walk-in reservation
                    walkInResIds.enqueue(resId);

                    autoSave();

                    System.out.println();
                    System.out.println("  [✓] Reservation created successfully!");
                    System.out.printf ("  Reservation ID %s saved to reservations.csv%n", resId);
                    return true;   // trigger dequeue()

                } else {
                    // ── Unavailable path ──────────────────────────────────────
                    System.out.println();
                    System.out.println("  ============================================");
                    System.out.printf ("  [!] No %s rooms available for%n", roomType);
                    System.out.printf ("      %s to %s%n",
                            checkIn.format(DATE_FMT), checkOut.format(DATE_FMT));
                    System.out.println("  ============================================");
                    System.out.println();
                    System.out.println("  Options:");
                    System.out.println("  1. Choose Another Room Type");
                    System.out.println("  2. Change Booking Dates");
                    System.out.println("  3. Cancel Booking (remove guest from queue)");
                    printDivider();
                    System.out.print("  Enter your choice: ");
                    String fallback = sc.nextLine().trim();
                    System.out.println();

                    switch (fallback) {
                        case "1":
                            reselectRoomType = true;
                            break;
                        case "2":
                            LocalDate newIn = readDateOrBack("  New Check-In Date  (DD/MM/YYYY, or 0 to reselect room type): ");
                            if (newIn == null) {
                                reselectRoomType = true;
                                break;
                            }
                            LocalDate newOut = readDateAfterOrBack("  New Check-Out Date (DD/MM/YYYY, or 0 to reselect room type): ", newIn);
                            if (newOut == null) {
                                reselectRoomType = true;
                                break;
                            }
                            checkIn  = newIn;
                            checkOut = newOut;
                            continue;
                        case "3":
                            System.out.println("  Booking cancelled. Guest removed from walk-in queue.");
                            return true;  // trigger dequeue() — guest cancelled
                        default:
                            System.out.println("  [!] Invalid option. Please try again.");
                    }
                }
            }
        }
    }

    /**
     * Prompts staff to select a room type.
     * Returns the chosen RoomType, or null if cancelled.
     */
    private Room.RoomType selectRoomType() {
        System.out.println();
        System.out.println("  Room Types:");
        System.out.println("  1. Single   (max 1 guest)  — RM 99/night");
        System.out.println("  2. Standard (max 2 guests) — RM 180/night");
        System.out.println("  3. Deluxe   (max 4 guests) — RM 280/night");
        System.out.println("  4. Suite    (max 4 guests) — RM 450/night");
        System.out.println("  5. Villa    (max 8 guests) — RM 950/night");
        System.out.println("  0. Cancel");
        printDivider();
        System.out.print("  Select room type: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1": return Room.RoomType.SINGLE;
            case "2": return Room.RoomType.STANDARD;
            case "3": return Room.RoomType.DELUXE;
            case "4": return Room.RoomType.SUITE;
            case "5": return Room.RoomType.VILLA;
            default:  return null;
        }
    }

    /**
     * Returns a Queue<Room> of rooms matching the type and available for the date range.
     * A room is available if:
     *   (a) its current status is AVAILABLE (not UNDER_MAINTENANCE), AND
     *   (b) no CONFIRMED/CHECKED_IN reservation overlaps the requested dates.
     */
    private Queue<Room> findAvailableRooms(Room.RoomType type, LocalDate checkIn, LocalDate checkOut) {
        Queue<Room> result = new Queue<>();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getRoomType() != type) continue;
            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) continue;

            // Check for date overlaps in existing reservations
            boolean hasOverlap = false;
            for (int j = 0; j < reservationList.size(); j++) {
                Reservation res = reservationList.get(j);
                if (!res.getRoomNo().equalsIgnoreCase(r.getRoomNo())) continue;
                if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED
                        && res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) continue;
                // Overlap check: requested check-in < existing check-out AND requested check-out > existing check-in
                if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                    hasOverlap = true;
                    break;
                }
            }
            if (!hasOverlap) {
                result.enqueue(r);
            }
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Check Room Availability
    //    Read-only: no queue operations — allows staff to check dates before registering
    // ══════════════════════════════════════════════════════════════
    private void checkRoomAvailability() {
        printHeader("Check Room Availability");
        System.out.println("  (Read-only: enter dates and room type to check availability)");
        System.out.println();

        LocalDate checkIn  = readDate("  Check-In Date  (DD/MM/YYYY): ");
        LocalDate checkOut = readDateAfter("  Check-Out Date (DD/MM/YYYY): ", checkIn);

        System.out.println();
        System.out.println("  Room Type:");
        System.out.println("  1. Single");
        System.out.println("  2. Standard");
        System.out.println("  3. Deluxe");
        System.out.println("  4. Suite");
        System.out.println("  5. Villa");
        System.out.println("  6. All Types");
        printDivider();
        System.out.print("  Select room type: ");
        String typeChoice = sc.nextLine().trim();
        System.out.println();

        Room.RoomType filterType = null;
        switch (typeChoice) {
            case "1": filterType = Room.RoomType.SINGLE;   break;
            case "2": filterType = Room.RoomType.STANDARD; break;
            case "3": filterType = Room.RoomType.DELUXE;   break;
            case "4": filterType = Room.RoomType.SUITE;    break;
            case "5": filterType = Room.RoomType.VILLA;    break;
            case "6": filterType = null; break;  // all types
            default:
                System.out.println("  [!] Invalid selection.");
                pressEnterToContinue();
                return;
        }

        System.out.printf("  Room Availability — %s to %s%n",
                checkIn.format(DATE_FMT), checkOut.format(DATE_FMT));
        if (filterType != null) {
            System.out.printf("  Filtering by type: %s%n", filterType);
        }
        System.out.println();
        System.out.printf("  %-8s %-12s %-14s %-9s %-12s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Availability");
        printDivider();

        int countAvailable = 0;
        int countTotal     = 0;

        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (filterType != null && r.getRoomType() != filterType) continue;
            countTotal++;

            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) {
                System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-12s%n",
                        r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                        r.getCapacity(), "MAINTENANCE");
                continue;
            }

            // Date-aware availability check
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

            String avail = hasOverlap ? "UNAVAILABLE" : "AVAILABLE";
            if (!hasOverlap) countAvailable++;
            System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-12s%n",
                    r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), avail);
        }

        printDivider();
        System.out.printf("  Rooms checked: %d  |  Available: %d  |  Unavailable: %d%n",
                countTotal, countAvailable, countTotal - countAvailable);
        System.out.println();
        System.out.println("  Note: This is a read-only check. Use Option 3 to process a guest.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 5. View Walk-In Reservations
    //    Displays reservations created through this module only
    // ══════════════════════════════════════════════════════════════
    private void viewWalkInReservations() {
        printHeader("Walk-In Reservations");
        System.out.println("  Reservations created through the Walk-In Registration Module:");
        System.out.println();

        if (walkInResIds.isEmpty()) {
            System.out.println("  No walk-in reservations have been made in this session.");
            System.out.println("  Use Option 3 (Process Next Walk-In Guest) to create reservations.");
            pressEnterToContinue();
            return;
        }

        System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-14s%n",
                "Res ID", "Guest Name", "Room", "Check-In", "Check-Out", "Status");
        printDivider();

        int count = 0;
        for (int i = 0; i < walkInResIds.size(); i++) {
            String resId = walkInResIds.get(i);

            // Find the reservation in the shared BST
            Reservation res = null;
            for (int j = 0; j < reservationList.size(); j++) {
                if (reservationList.get(j).getReservationId().equalsIgnoreCase(resId)) {
                    res = reservationList.get(j);
                    break;
                }
            }
            if (res == null) continue;

            // Look up guest name from guestList
            String guestName = res.getGuestId();
            for (int j = 0; j < guestList.size(); j++) {
                if (guestList.get(j).getGuestId().equalsIgnoreCase(res.getGuestId())) {
                    guestName = guestList.get(j).getName();
                    break;
                }
            }

            System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-14s%n",
                    res.getReservationId(),
                    guestName,
                    res.getRoomNo(),
                    res.getCheckInDate().format(DATE_FMT),
                    res.getCheckOutDate().format(DATE_FMT),
                    res.getStatus());
            count++;
        }

        if (count == 0) {
            System.out.println("  No walk-in reservations found.");
        }

        printDivider();
        System.out.printf("  Total walk-in reservations this session: %d%n", count);
        System.out.println();
        System.out.println("  Note: For check-in, check-out, billing, and advanced search,");
        System.out.println("        use the Front Desk Module from the Main Menu.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // Auto-save after every state-changing action
    // ══════════════════════════════════════════════════════════════
    private void autoSave() {
        DataStore.saveGuests(guestList);
        DataStore.saveReservations(reservationList);
        DataStore.saveRooms(roomList);
    }

    // ══════════════════════════════════════════════════════════════
    // Helper Methods
    // ══════════════════════════════════════════════════════════════

    /** Reads date, retries on parse error. */
    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(sc.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Use DD/MM/YYYY.");
            }
        }
    }

    /** Reads check-out date that must be strictly after checkIn. */
    private LocalDate readDateAfter(String prompt, LocalDate checkIn) {
        while (true) {
            LocalDate d = readDate(prompt);
            if (d.isAfter(checkIn)) return d;
            System.out.println("  [!] Check-out date must be after check-in date ("
                    + checkIn.format(DATE_FMT) + "). Please try again.");
        }
    }

    /** Reads date, returns null if user enters "0" to cancel/reselect. */
    private LocalDate readDateOrBack(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Use DD/MM/YYYY (or 0 to reselect room type).");
            }
        }
    }

    /** Reads check-out date strictly after checkIn, returns null if user enters "0". */
    private LocalDate readDateAfterOrBack(String prompt, LocalDate checkIn) {
        while (true) {
            LocalDate d = readDateOrBack(prompt);
            if (d == null) return null;
            if (d.isAfter(checkIn)) return d;
            System.out.println("  [!] Check-out date must be after check-in date ("
                    + checkIn.format(DATE_FMT) + "). Please try again (or 0 to reselect room type).");
        }
    }

    /** Reads a non-empty string; returns "0" if user types 0 to cancel. */
    private String readNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return "0";
            if (!input.isEmpty()) return input;
            System.out.println("  [!] Field cannot be empty. Please enter a value (or 0 to cancel).");
        }
    }

    /** Finds a Room by room number in the shared roomList. */
    private Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    /** Prints header and clears screen. */
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

    /** Prints "Registration cancelled" and waits for Enter. */
    private void cancelled() {
        System.out.println("  Registration cancelled.");
        pressEnterToContinue();
    }

    /**
     * Parses the trailing numeric part of an ID string.
     * e.g. "G003" -> 3,  "R002" -> 2
     */
    private int parseTrailingNum(String id) {
        if (id == null || id.isEmpty()) return 0;
        int i = 0;
        while (i < id.length() && !Character.isDigit(id.charAt(i))) i++;
        try {
            return Integer.parseInt(id.substring(i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Synchronizes walkInQueue with guestList by removing any waiting guests
     * that no longer exist in guestList (e.g. deleted via Front Desk guest management).
     */
    private void syncWalkInQueue() {
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
}
