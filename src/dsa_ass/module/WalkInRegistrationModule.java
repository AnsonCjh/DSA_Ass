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
 * Handles: Register Guest, Reservation List, Room Availability,
 *          Choose Room Type, Search Reservation, Modify Reservation, Cancel Reservation
 */
public class WalkInRegistrationModule {

    private final Queue<Guest>                 guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                  roomList;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static int guestCounter = 1;
    private static int resCounter   = 1;

    /** Called by DataStore after loading saved data to prevent ID collisions. */
    public static void setGuestCounter(int n) { guestCounter = n; }
    public static void setResCounter(int n)   { resCounter   = n; }

    public WalkInRegistrationModule(Queue<Guest> guestList,
                                    BinarySearchTree<Reservation> reservationList,
                                    Queue<Room> roomList,
                                    Scanner sc) {
        this.guestList       = guestList;
        this.reservationList = reservationList;
        this.roomList        = roomList;
        this.sc              = sc;
    }

    // ── Sub-Menu ────────────────────────────────────────────────
    public void showMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("Walk-In Registration Module");
            System.out.println("  1. Register Guest");
            System.out.println("  2. Manage Reservations");
            System.out.println("  3. Room Availability");
            System.out.println("  4. Guest Management");
            System.out.println("  5. Generate Reports");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": registerGuest();           autoSave(); break;
                case "2": manageReservations();                  break;
                case "3": displayRoomAvailability();             break; // read-only
                case "4": guestManagement();                     break;
                case "5": generateReports();                     break; // read-only
                case "0": back = true;                           break;
                default:  System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }

    // ── Manage Reservations Sub-Menu ──────────────────────────────
    private void manageReservations() {
        boolean back = false;
        while (!back) {
            printHeader("Manage Reservations");
            System.out.println("  1. View Reservation List");
            System.out.println("  2. Search Reservation");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": displayReservationList(); break; // read-only
                case "2": searchReservation();      break;
                case "0": back = true;              break;
                default:  System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }

    // ── Auto-save after every state-changing action ──────────────
    private void autoSave() {
        DataStore.saveGuests(guestList);
        DataStore.saveReservations(reservationList);
        DataStore.saveRooms(roomList);
    }

    // ── 1. Register Guest ────────────────────────────────────────
    private void registerGuest() {
        printHeader("Register Guest");
        String guestId = String.format("G%03d", guestList.size() + 1);
        System.out.println("  Guest ID assigned : " + guestId);
        System.out.println("  (Enter 0 at any field to cancel and go back)");
        System.out.println();

        String name = readNonEmptyInput("  Full Name         : ");
        if (name.equals("0")) { System.out.println("  Registration cancelled."); pressEnterToContinue(); return; }

        String ic = readNonEmptyInput("  IC / Passport No  : ");
        if (ic.equals("0")) { System.out.println("  Registration cancelled."); pressEnterToContinue(); return; }

        String phone = readNonEmptyInput("  Phone Number      : ");
        if (phone.equals("0")) { System.out.println("  Registration cancelled."); pressEnterToContinue(); return; }

        String email = readNonEmptyInput("  Email Address     : ");
        if (email.equals("0")) { System.out.println("  Registration cancelled."); pressEnterToContinue(); return; }

        String nationality = readNonEmptyInput("  Nationality       : ");
        if (nationality.equals("0")) { System.out.println("  Registration cancelled."); pressEnterToContinue(); return; }

        Guest g = new Guest(guestId, name, ic, phone, email, nationality);
        guestList.enqueue(g);

        System.out.println();
        System.out.println("  Guest registered successfully!");
        System.out.println();
        makeReservation(guestId);
        pressEnterToContinue();
    }

    private String readNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return "0";
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] Field cannot be empty. Please enter a valid value (or 0 to cancel).");
        }
    }

    // ── Make Reservation ─────────────────────────────────────────
    private void makeReservation(String guestId) {
        printHeader("Make Reservation");
        displayRoomAvailability(false);  // show rooms without blocking for Enter

        System.out.print("  Enter Room Number  : ");
        String roomNo = sc.nextLine().trim().toUpperCase();

        Room selectedRoom = findRoom(roomNo);
        if (selectedRoom == null) {
            System.out.println("  [!] Room not found.");
            return;
        }
        if (!selectedRoom.isAvailable()) {
            System.out.println("  [!] Room is not available.");
            return;
        }

        LocalDate checkIn  = readDate("  Check-In Date  (DD/MM/YYYY): ");
        LocalDate checkOut = readDate("  Check-Out Date (DD/MM/YYYY): ");
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            System.out.println("  [!] Check-out must be after check-in.");
            return;
        }

        System.out.printf("  Number of Guests   (max %d): ", selectedRoom.getCapacity());
        int numGuests;
        try { numGuests = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { numGuests = 1; }

        if (numGuests < 1) {
            System.out.println("  [!] Number of guests must be at least 1.");
            return;
        }
        if (numGuests > selectedRoom.getCapacity()) {
            System.out.printf("  [!] Room %s can only accommodate %d guest(s). " +
                    "Please choose a larger room.%n", roomNo, selectedRoom.getCapacity());
            return;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = nights * selectedRoom.getPricePerNight();

        int maxId = 0;
        for (int i = 0; i < reservationList.size(); i++) {
            int idNum = parseTrailingNum(reservationList.get(i).getReservationId());
            if (idNum > maxId) maxId = idNum;
        }
        int nextIdNum = Math.max(maxId + 1, resCounter);
        resCounter = nextIdNum + 1;
        String resId = String.format("R%03d", nextIdNum);
        Reservation res = new Reservation(resId, guestId, roomNo, checkIn, checkOut, numGuests, total);
        res.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservationList.add(res);
        selectedRoom.setStatus(Room.RoomStatus.OCCUPIED);

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("        Reservation Confirmation");
        System.out.println("  ============================================");
        System.out.printf ("  Reservation ID : %s%n", resId);
        System.out.printf ("  Guest ID       : %s%n", guestId);
        System.out.printf ("  Room           : %s (%s)%n", roomNo, selectedRoom.getRoomType());
        System.out.printf ("  Check-In       : %s%n", checkIn.format(DATE_FMT));
        System.out.printf ("  Check-Out      : %s%n", checkOut.format(DATE_FMT));
        System.out.printf ("  Nights         : %d%n", nights);
        System.out.printf ("  Total Amount   : RM %.2f%n", total);
        System.out.println("  ============================================");
    }

    // ── 2. Reservation List ───────────────────────────────────────
    private void displayReservationList() {
        printHeader("Reservation List");
        System.out.printf("  %-12s %-10s %-8s %-12s %-12s %-6s %-12s %-12s%n",
                "Res ID", "Guest ID", "Room", "Check-In",
                "Check-Out", "Guests", "Total (RM)", "Status");
        printDivider();

        int count = 0;
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            // Only show active reservations — exclude CANCELLED and CHECKED_OUT
            if (r.getStatus() != Reservation.ReservationStatus.CANCELLED
                    && r.getStatus() != Reservation.ReservationStatus.CHECKED_OUT) {
                System.out.println("  " + r);
                count++;
            }
        }

        if (count == 0) {
            System.out.println("  No active reservations found.");
        }
        pressEnterToContinue();
    }


    // ── 3. Room Availability ──────────────────────────────────────
    public void displayRoomAvailability() {
        displayRoomAvailability(true);
    }

    /**
     * @param withPause  true when invoked from the menu (waits for Enter);
     *                   false when called internally (e.g. from makeReservation).
     */
    private void displayRoomAvailability(boolean withPause) {
        printHeader("Room Availability");
        System.out.printf("  %-8s %-12s %-14s %-9s %-20s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Status");
        printDivider();

        int available = 0, occupied = 0, maintenance = 0;
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            System.out.println("  " + r);
            switch (r.getStatus()) {
                case AVAILABLE:         available++;   break;
                case OCCUPIED:          occupied++;    break;
                case UNDER_MAINTENANCE: maintenance++; break;
            }
        }

        if (roomList.size() == 0) {
            System.out.println("  No rooms configured.");
        }

        printDivider();
        System.out.printf("  Total: %d room(s)   |   Available: %d   |   Occupied: %d",
                roomList.size(), available, occupied);
        if (maintenance > 0) {
            System.out.printf("   |   Under Maintenance: %d", maintenance);
        }
        System.out.println();
        System.out.println();

        if (withPause) pressEnterToContinue();
    }

    // ── 4. Choose Room Type ───────────────────────────────────────
    private void chooseRoomType() {
        printHeader("Choose Room Type");
        System.out.println("  1. Single   (max 1 guest)");
        System.out.println("  2. Standard (max 2 guests)");
        System.out.println("  3. Deluxe   (max 2-4 guests)");
        System.out.println("  4. Suite    (max 4 guests)");
        System.out.println("  5. Villa    (max 8 guests)");
        System.out.println("  0. Back");
        printDivider();
        System.out.print("  Select room type: ");
        String choice = sc.nextLine().trim();

        Room.RoomType type;
        switch (choice) {
            case "1": type = Room.RoomType.SINGLE;   break;
            case "2": type = Room.RoomType.STANDARD; break;
            case "3": type = Room.RoomType.DELUXE;   break;
            case "4": type = Room.RoomType.SUITE;     break;
            case "5": type = Room.RoomType.VILLA;     break;
            default:  return;
        }

        System.out.printf("%n  Available %-10s Rooms:%n", type);
        System.out.printf("  %-8s %-12s %-14s %-9s %-20s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Status");
        printDivider();
        boolean found = false;
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getRoomType() == type && r.isAvailable()) {
                System.out.println("  " + r);
                found = true;
            }
        }
        if (!found) System.out.println("  No available rooms of this type.");
        System.out.println();

        System.out.print("  Would you like to make a reservation? (Y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.print("  Enter Guest ID: ");
            String gid = sc.nextLine().trim();
            makeReservation(gid);
        }
        pressEnterToContinue();
    }

    // ── Search Reservation & Integrated Actions ───────────────────
    private void searchReservation() {
        printHeader("Search Reservation");
        System.out.print("  Enter Reservation ID or Guest ID: ");
        String keyword = sc.nextLine().trim().toUpperCase();
        if (keyword.isEmpty()) return;

        Reservation foundRes = null;
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            if (r.getReservationId().equalsIgnoreCase(keyword)
                    || r.getGuestId().equalsIgnoreCase(keyword)) {
                foundRes = r;
                break;
            }
        }

        if (foundRes == null) {
            System.out.println("  [!] No reservation found for: " + keyword);
            pressEnterToContinue();
            return;
        }

        boolean back = false;
        while (!back) {
            // Find Guest Name
            String guestName = foundRes.getGuestId();
            for (int i = 0; i < guestList.size(); i++) {
                if (guestList.get(i).getGuestId().equalsIgnoreCase(foundRes.getGuestId())) {
                    guestName = guestList.get(i).getName();
                    break;
                }
            }
            Room room = findRoom(foundRes.getRoomNo());
            String roomType = (room != null) ? room.getRoomType().name() : "N/A";

            ConsoleUtils.clearScreen();
            System.out.println();
            System.out.println("  Reservation Found");
            printDivider();
            System.out.printf ("  Reservation ID : %s%n", foundRes.getReservationId());
            System.out.printf ("  Guest Name     : %s (%s)%n", guestName, foundRes.getGuestId());
            System.out.printf ("  Room Type      : %s%n", roomType);
            System.out.printf ("  Room No.       : %s%n", foundRes.getRoomNo());
            System.out.printf ("  Check-In       : %s%n", foundRes.getCheckInDate().format(DATE_FMT));
            System.out.printf ("  Check-Out      : %s%n", foundRes.getCheckOutDate().format(DATE_FMT));
            System.out.printf ("  Total Amount   : RM %.2f%n", foundRes.getTotalAmount());
            System.out.printf ("  Status         : %s%n", foundRes.getStatus());
            printDivider();
            System.out.println();
            System.out.println("  1. Modify Reservation");
            System.out.println("  2. Cancel Reservation");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1":
                    modifyReservationDirect(foundRes);
                    autoSave();
                    break;
                case "2":
                    cancelReservationDirect(foundRes);
                    autoSave();
                    back = true;
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }

    private void modifyReservationDirect(Reservation res) {
        if (res.getStatus() == Reservation.ReservationStatus.CANCELLED
                || res.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) {
            System.out.println("  [!] Cannot modify a cancelled or checked-out reservation.");
            pressEnterToContinue();
            return;
        }

        System.out.println("  Current Check-In  : " + res.getCheckInDate().format(DATE_FMT));
        System.out.println("  Current Check-Out : " + res.getCheckOutDate().format(DATE_FMT));
        System.out.println("  Current Guests    : " + res.getNumGuests());
        System.out.println();
        System.out.print("  New Check-In  Date (DD/MM/YYYY) [Enter to keep]: ");
        String inStr = sc.nextLine().trim();
        System.out.print("  New Check-Out Date (DD/MM/YYYY) [Enter to keep]: ");
        String outStr = sc.nextLine().trim();
        System.out.print("  New Number of Guests [Enter to keep]: ");
        String guestStr = sc.nextLine().trim();

        if (!inStr.isEmpty()) {
            try {
                res.setCheckInDate(LocalDate.parse(inStr, DATE_FMT));
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Check-in date not changed.");
            }
        }
        if (!outStr.isEmpty()) {
            try {
                res.setCheckOutDate(LocalDate.parse(outStr, DATE_FMT));
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Check-out date not changed.");
            }
        }
        if (!guestStr.isEmpty()) {
            try {
                int gCount = Integer.parseInt(guestStr);
                Room room = findRoom(res.getRoomNo());
                if (room != null && gCount > room.getCapacity()) {
                    System.out.printf("  [!] Room capacity is %d. Guest count not changed.%n", room.getCapacity());
                } else if (gCount >= 1) {
                    res.setNumGuests(gCount);
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Guest count not changed.");
            }
        }

        Room room = findRoom(res.getRoomNo());
        if (room != null) {
            long nights = res.getNumNights();
            res.setTotalAmount(nights * room.getPricePerNight());
        }
        System.out.println();
        System.out.println("  Reservation updated successfully!");
        System.out.printf ("  New Total Amount: RM %.2f%n", res.getTotalAmount());
        pressEnterToContinue();
    }

    private void cancelReservationDirect(Reservation res) {
        if (res.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            System.out.println("  [!] Reservation is already cancelled.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("  Reservation %s for Guest %s will be cancelled.%n",
                res.getReservationId(), res.getGuestId());
        System.out.print("  Confirm cancellation? (Y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
            res.setStatus(Reservation.ReservationStatus.CANCELLED);
            Room room = findRoom(res.getRoomNo());
            if (room != null) room.setStatus(Room.RoomStatus.AVAILABLE);
            reservationList.remove(res);
            System.out.println("  Reservation cancelled and removed successfully.");
        } else {
            System.out.println("  Cancellation aborted.");
        }
        pressEnterToContinue();
    }

    // ── Generate Reports ──────────────────────────────────────────
    private void generateReports() {
        printHeader("Walk-In Registration Report");
        int activeRes = 0;
        int cancelledRes = 0;
        double totalRevenue = 0.0;
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            if (r.getStatus() == Reservation.ReservationStatus.CANCELLED) {
                cancelledRes++;
            } else {
                activeRes++;
                totalRevenue += r.getTotalAmount();
            }
        }
        int availableRooms = 0;
        int occupiedRooms = 0;
        int maintenanceRooms = 0;
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getStatus() == Room.RoomStatus.AVAILABLE) availableRooms++;
            else if (r.getStatus() == Room.RoomStatus.OCCUPIED) occupiedRooms++;
            else maintenanceRooms++;
        }

        System.out.println("  Total Guests Registered : " + guestList.size());
        System.out.println("  Active Reservations     : " + activeRes);
        System.out.println("  Cancelled Reservations  : " + cancelledRes);
        System.out.printf ("  Room Breakdown          : %d Available, %d Occupied, %d Maintenance%n",
                availableRooms, occupiedRooms, maintenanceRooms);
        System.out.printf ("  Total Expected Revenue  : RM %.2f%n", totalRevenue);
        printDivider();
        pressEnterToContinue();
    }

    // ── Guest Management Sub-Menu ──────────────────────────────────
    private void guestManagement() {
        boolean back = false;
        while (!back) {
            printHeader("Guest Management");
            System.out.println("  1. View All Guests (Summary)");
            System.out.println("  2. Search Guest");
            System.out.println("  3. Remove Guest");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": viewAllGuestsSummary(); break;
                case "2": searchGuest();          break;
                case "3": removeGuest();          break;
                case "0": back = true;            break;
                default:  System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }

    // ── 1. View All Guests (Summary Only) ──────────────────────────
    private void viewAllGuestsSummary() {
        printHeader("Guest List (Summary)");
        System.out.printf("  %-10s %-20s %-15s %-12s%n",
                "Guest ID", "Full Name", "Phone Number", "Nationality");
        printDivider();

        int count = 0;
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            System.out.printf("  %-10s %-20s %-15s %-12s%n",
                    g.getGuestId(), g.getName(), g.getPhone(), g.getNationality());
            count++;
        }

        if (count == 0) {
            System.out.println("  No guests registered yet.");
        }
        pressEnterToContinue();
    }

    // ── 2. Search Guest ───────────────────────────────────────────
    private void searchGuest() {
        printHeader("Search Guest");
        System.out.print("  Enter Guest ID, Full Name, or IC/Passport: ");
        String keyword = sc.nextLine().trim().toLowerCase();
        if (keyword.isEmpty()) return;

        boolean found = false;
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            if (g.getGuestId().toLowerCase().contains(keyword)
                    || g.getName().toLowerCase().contains(keyword)
                    || g.getIcNo().toLowerCase().contains(keyword)) {
                printFullGuestDetails(g);
                found = true;
            }
        }

        if (!found) {
            System.out.println("  [!] No guest found matching: " + keyword);
        }
        pressEnterToContinue();
    }

    private void printFullGuestDetails(Guest g) {
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("            Full Guest Details");
        System.out.println("  ============================================");
        System.out.printf ("  Guest ID       : %s%n", g.getGuestId());
        System.out.printf ("  Full Name      : %s%n", g.getName());
        System.out.printf ("  IC / Passport  : %s%n", g.getIcNo());
        System.out.printf ("  Phone Number   : %s%n", g.getPhone());
        System.out.printf ("  Email Address  : %s%n", g.getEmail());
        System.out.printf ("  Nationality    : %s%n", g.getNationality());
        System.out.println("  ============================================");
    }

    // ── 4. Remove Guest ───────────────────────────────────────────
    private void removeGuest() {
        printHeader("Remove Guest");
        System.out.print("  Enter Guest ID to remove: ");
        String guestId = sc.nextLine().trim();
        if (guestId.isEmpty()) return;

        int targetIndex = -1;
        Guest foundGuest = null;
        for (int i = 0; i < guestList.size(); i++) {
            if (guestList.get(i).getGuestId().equalsIgnoreCase(guestId)) {
                targetIndex = i;
                foundGuest = guestList.get(i);
                break;
            }
        }

        if (foundGuest == null) {
            System.out.println("  [!] Guest not found with ID: " + guestId);
            pressEnterToContinue();
            return;
        }

        // Display guest details for confirmation
        printFullGuestDetails(foundGuest);
        System.out.println();
        System.out.print("  Are you sure you want to remove this guest from database? (Y/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
            guestList.remove(targetIndex);
            autoSave();
            System.out.println();
            System.out.printf("  [✓] Guest %s (%s) has been successfully removed from database.%n",
                    foundGuest.getGuestId(), foundGuest.getName());
        } else {
            System.out.println("  [!] Removal cancelled.");
        }
        pressEnterToContinue();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    private Reservation findReservation(String resId) {
        for (int i = 0; i < reservationList.size(); i++) {
            if (reservationList.get(i).getReservationId().equalsIgnoreCase(resId))
                return reservationList.get(i);
        }
        return null;
    }

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
}

