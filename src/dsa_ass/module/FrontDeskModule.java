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
 * Front Desk Service Module
 *
 * Non-Linear ADT Demonstrated: Binary Search Tree (BST)
 *
 * Handles existing guests and reservations after booking creation.
 * All reservation lookups use the BST's searchByConfirmation() method —
 * an in-order BST tree-walk.
 *
 * Main Menu:
 *   1. Search by Confirmation Number
 *   2. Guest Management
 *   3. Check-In Guest
 *   4. Check-Out Guest
 *   5. Check Room Availability
 *   6. View Current Guests
 *   0. Back to Main Menu
 */
public class FrontDeskModule {

    private final Queue<Guest>                 guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                  roomList;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FrontDeskModule(Queue<Guest>                 guestList,
                           BinarySearchTree<Reservation> reservationList,
                           Queue<Room>                  roomList,
                           Scanner sc) {
        this.guestList       = guestList;
        this.reservationList = reservationList;
        this.roomList        = roomList;
        this.sc              = sc;
    }

    // ══════════════════════════════════════════════════════════════
    // Main Menu
    // ══════════════════════════════════════════════════════════════
    public void showMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("FRONT DESK SERVICE");
            System.out.println("  1. Search by Confirmation Number");
            System.out.println("  2. Guest Management");
            System.out.println("  3. Check-In Guest");
            System.out.println("  4. Check-Out Guest");
            System.out.println("  5. Check Room Availability");
            System.out.println("  6. View Current Guests");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": searchByConfirmation();                 break;
                case "2": guestManagement();                      break;
                case "3": checkIn();               autoSave();   break;
                case "4": checkOut();              autoSave();   break;
                case "5": checkRoomAvailability();               break;
                case "6": viewCurrentGuests();                   break;
                case "0": back = true;                           break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 6.");
                    pressEnterToContinue();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 1. Search by Confirmation Number
    //    BST ADT: searchByConfirmation() — in-order BST traversal
    // ══════════════════════════════════════════════════════════════
    private void searchByConfirmation() {
        printHeader("Search by Confirmation Number");
        System.out.println("  ADT: Binary Search Tree (BST)");
        System.out.println("  Operation: In-order BST traversal search");
        System.out.println();

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        System.out.println("  [BST] Traversing tree nodes in-order...");
        Reservation res = reservationList.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [BST] Traversal complete. No match found.");
            System.out.println();
            System.out.println("  [!] Confirmation number '" + confNo + "' not found.");
            System.out.println("      Please verify the number and try again.");
            pressEnterToContinue();
            return;
        }

        System.out.println("  [BST] Match found at BST node.");
        System.out.println();
        printReservationDetails(res, true);
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 2. Guest Management Sub-Menu
    // ══════════════════════════════════════════════════════════════
    private void guestManagement() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("GUEST MANAGEMENT");
            System.out.println("  1. View All Guests");
            System.out.println("  2. Search Guest");
            System.out.println("  3. View Guest Details");
            System.out.println("  4. Modify Guest Details");
            System.out.println("  5. Delete Guest");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": viewAllGuests(); break;
                case "2": searchGuest(); break;
                case "3": viewGuestDetails(null); break;
                case "4": modifyGuestDetails(null); break;
                case "5": deleteGuest(null); break;
                case "0": back = true; break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 5.");
                    pressEnterToContinue();
            }
        }
    }

    // ── 2.1 View All Guests ───────────────────────────────────────
    private void viewAllGuests() {
        printHeader("View All Guests");
        System.out.printf("  %-10s %-20s %-15s %-15s %-25s %-12s%n",
                "Guest ID", "Full Name", "IC / Passport", "Phone", "Email", "Nationality");
        printDivider();

        int count = 0;
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            System.out.printf("  %-10s %-20s %-15s %-15s %-25s %-12s%n",
                    g.getGuestId(), g.getName(), g.getIcNo(),
                    g.getPhone(), g.getEmail(), g.getNationality());
            count++;
        }

        if (count == 0) {
            System.out.println("  No customer records found.");
        }
        printDivider();
        System.out.printf("  Total Guests Registered: %d%n", count);
        pressEnterToContinue();
    }

    // ── 2.2 Search Guest ──────────────────────────────────────────
    private void searchGuest() {
        printHeader("Search Guest");
        System.out.print("  Enter Guest ID, Name, Phone Number, or Confirmation Number: ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("  [!] Search keyword cannot be empty.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        boolean found = false;

        // First, check if keyword matches an 8-digit confirmation number in BST
        Reservation res = reservationList.searchByConfirmation(keyword);
        if (res != null) {
            Guest g = findGuest(res.getGuestId());
            if (g != null) {
                System.out.println("  [Found via Confirmation No. BST Search]");
                viewGuestDetails(g);
                return;
            }
        }

        // Search guestList directly by Guest ID, Name, Phone, or IC
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            if (g.getGuestId().equalsIgnoreCase(keyword)
                    || g.getName().toLowerCase().contains(keyword.toLowerCase())
                    || g.getPhone().contains(keyword)
                    || g.getIcNo().equalsIgnoreCase(keyword)) {
                viewGuestDetails(g);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("  [!] No guest record found matching: " + keyword);
            pressEnterToContinue();
        }
    }

    // ── 2.3 View Guest Details ────────────────────────────────────
    private void viewGuestDetails(Guest targetGuest) {
        if (targetGuest == null) {
            printHeader("View Guest Details");
            System.out.print("  Enter Guest ID, Name, or Confirmation Number: ");
            String keyword = sc.nextLine().trim();
            if (keyword.isEmpty()) {
                System.out.println("  [!] Search keyword cannot be empty.");
                pressEnterToContinue();
                return;
            }

            // Check confirmation number search first
            Reservation res = reservationList.searchByConfirmation(keyword);
            if (res != null) {
                targetGuest = findGuest(res.getGuestId());
            }

            // If not found by confirmation number, search guestList
            if (targetGuest == null) {
                for (int i = 0; i < guestList.size(); i++) {
                    Guest g = guestList.get(i);
                    if (g.getGuestId().equalsIgnoreCase(keyword)
                            || g.getName().toLowerCase().contains(keyword.toLowerCase())
                            || g.getPhone().contains(keyword)
                            || g.getIcNo().equalsIgnoreCase(keyword)) {
                        targetGuest = g;
                        break;
                    }
                }
            }

            if (targetGuest == null) {
                System.out.println("  [!] No guest found for: " + keyword);
                pressEnterToContinue();
                return;
            }
        }

        boolean back = false;
        while (!back) {
            ConsoleUtils.clearScreen();
            printHeader("GUEST DETAILS");

            // Look up latest active or past reservation for this guest
            Reservation guestRes = findReservationForGuest(targetGuest.getGuestId());

            System.out.printf("  Guest ID      : %s%n", targetGuest.getGuestId());
            System.out.printf("  Name          : %s%n", targetGuest.getName());
            System.out.printf("  IC / Passport : %s%n", targetGuest.getIcNo());
            System.out.printf("  Phone         : %s%n", targetGuest.getPhone());
            System.out.printf("  Email         : %s%n", targetGuest.getEmail());
            System.out.printf("  Nationality   : %s%n", targetGuest.getNationality());
            System.out.println();
            if (guestRes != null) {
                System.out.printf("  Reservation ID : %s%n", guestRes.getReservationId());
                System.out.printf("  Confirmation No: %s%n", guestRes.getConfirmationNo().isEmpty()
                        ? "(None)" : guestRes.getConfirmationNo());
                System.out.printf("  Room           : %s%n", guestRes.getRoomNo());
                System.out.printf("  Status         : %s%n", guestRes.getStatus());
            } else {
                System.out.println("  Reservation ID : (None / No Reservation)");
                System.out.println("  Confirmation No: (None)");
                System.out.println("  Room           : (None)");
                System.out.println("  Status         : (None)");
            }
            printDivider();
            System.out.println("  1. Modify Guest Details");
            System.out.println("  2. Delete Guest");
            System.out.println("  0. Back");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1":
                    modifyGuestDetails(targetGuest);
                    break;
                case "2":
                    boolean deleted = deleteGuest(targetGuest);
                    if (deleted) {
                        back = true;
                    }
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 2.");
                    pressEnterToContinue();
            }
        }
    }

    // ── 2.4 Modify Guest Details ──────────────────────────────────
    private void modifyGuestDetails(Guest targetGuest) {
        if (targetGuest == null) {
            printHeader("Modify Guest Details");
            System.out.print("  Enter Guest ID to modify: ");
            String keyword = sc.nextLine().trim();
            if (keyword.isEmpty()) return;

            targetGuest = findGuest(keyword);
            if (targetGuest == null) {
                System.out.println("  [!] Guest not found for ID: " + keyword);
                pressEnterToContinue();
                return;
            }
        }

        ConsoleUtils.clearScreen();
        printHeader("MODIFY GUEST DETAILS");
        System.out.printf("  Modifying record for Guest %s (%s)%n",
                targetGuest.getGuestId(), targetGuest.getName());
        System.out.println();
        System.out.println("  1. Name");
        System.out.println("  2. Phone Number");
        System.out.println("  3. Email");
        System.out.println("  4. IC / Passport");
        System.out.println("  5. Nationality");
        System.out.println("  0. Cancel");
        printDivider();
        System.out.print("  Select field to modify: ");
        String choice = sc.nextLine().trim();
        System.out.println();

        switch (choice) {
            case "1":
                System.out.println("  Current Name: " + targetGuest.getName());
                String name = readNonEmptyInput("  Enter New Name: ");
                if (!name.equals("0")) {
                    targetGuest.setName(name);
                    autoSave();
                    System.out.println("  [✓] Guest name updated successfully!");
                }
                break;
            case "2":
                System.out.println("  Current Phone: " + targetGuest.getPhone());
                String phone = readNonEmptyInput("  Enter New Phone Number: ");
                if (!phone.equals("0")) {
                    targetGuest.setPhone(phone);
                    autoSave();
                    System.out.println("  [✓] Phone number updated successfully!");
                }
                break;
            case "3":
                System.out.println("  Current Email: " + targetGuest.getEmail());
                String email = readNonEmptyInput("  Enter New Email Address: ");
                if (!email.equals("0")) {
                    targetGuest.setEmail(email);
                    autoSave();
                    System.out.println("  [✓] Email address updated successfully!");
                }
                break;
            case "4":
                System.out.println("  Current IC / Passport: " + targetGuest.getIcNo());
                String ic = readNonEmptyInput("  Enter New IC / Passport: ");
                if (!ic.equals("0")) {
                    targetGuest.setIcNo(ic);
                    autoSave();
                    System.out.println("  [✓] IC / Passport updated successfully!");
                }
                break;
            case "5":
                System.out.println("  Current Nationality: " + targetGuest.getNationality());
                String nat = readNonEmptyInput("  Enter New Nationality: ");
                if (!nat.equals("0")) {
                    targetGuest.setNationality(nat);
                    autoSave();
                    System.out.println("  [✓] Nationality updated successfully!");
                }
                break;
            case "0":
                System.out.println("  Modification cancelled.");
                break;
            default:
                System.out.println("  [!] Invalid selection.");
        }
        pressEnterToContinue();
    }

    // ── 2.5 Delete Guest ──────────────────────────────────────────
    private boolean deleteGuest(Guest targetGuest) {
        if (targetGuest == null) {
            printHeader("Delete Guest");
            System.out.print("  Enter Guest ID to delete: ");
            String keyword = sc.nextLine().trim();
            if (keyword.isEmpty()) return false;

            targetGuest = findGuest(keyword);
            if (targetGuest == null) {
                System.out.println("  [!] Guest not found for ID: " + keyword);
                pressEnterToContinue();
                return false;
            }
        }

        // Safety check: verify if guest has an active reservation (CONFIRMED or CHECKED_IN)
        boolean hasActiveReservation = false;
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation r = reservationList.get(i);
            if (r.getGuestId().equalsIgnoreCase(targetGuest.getGuestId())) {
                if (r.getStatus() == Reservation.ReservationStatus.CONFIRMED
                        || r.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
                    hasActiveReservation = true;
                    break;
                }
            }
        }

        if (hasActiveReservation) {
            System.out.println();
            System.out.printf("  Guest %s currently has an active reservation.%n",
                    targetGuest.getGuestId());
            System.out.println("  Guest cannot be deleted.");
            pressEnterToContinue();
            return false;
        }

        System.out.println();
        System.out.printf("  Are you sure you want to delete %s? (Y/N): ",
                targetGuest.getGuestId());
        String confirm = sc.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            // Find index in guestList
            int targetIdx = -1;
            for (int i = 0; i < guestList.size(); i++) {
                if (guestList.get(i).getGuestId().equalsIgnoreCase(targetGuest.getGuestId())) {
                    targetIdx = i;
                    break;
                }
            }
            if (targetIdx != -1) {
                guestList.remove(targetIdx);
                autoSave();
                System.out.println();
                System.out.printf("  [✓] Guest %s (%s) has been successfully deleted.%n",
                        targetGuest.getGuestId(), targetGuest.getName());
                pressEnterToContinue();
                return true;
            }
        } else {
            System.out.println("  Deletion cancelled.");
            pressEnterToContinue();
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Check-In Guest
    //    BST ADT: searchByConfirmation() → update status to CHECKED_IN
    // ══════════════════════════════════════════════════════════════
    private void checkIn() {
        printHeader("Check-In Guest");
        System.out.println("  ADT: BST in-order traversal search by confirmation number");
        System.out.println();

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        System.out.println("  [BST] Searching BST by confirmation number...");
        Reservation res = reservationList.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [!] No reservation found for confirmation number: " + confNo);
            pressEnterToContinue();
            return;
        }
        System.out.println("  [BST] Reservation found.");
        System.out.println();

        // Status guard
        if (res.getStatus() == Reservation.ReservationStatus.CHECKED_IN) {
            System.out.println("  [!] Guest is already checked in.");
            pressEnterToContinue();
            return;
        }
        if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            System.out.printf("  [!] Reservation cannot be checked in. Current status: %s%n",
                    res.getStatus());
            System.out.println("      Only CONFIRMED reservations can be checked in.");
            pressEnterToContinue();
            return;
        }

        // Display reservation for verification
        printReservationDetails(res, false);
        System.out.println();
        System.out.print("  Confirm check-in for this guest? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Check-in cancelled.");
            pressEnterToContinue();
            return;
        }

        // Update statuses
        res.setStatus(Reservation.ReservationStatus.CHECKED_IN);
        Room room = findRoom(res.getRoomNo());
        if (room != null) room.setStatus(Room.RoomStatus.OCCUPIED);

        Guest guest = findGuest(res.getGuestId());
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("            Check-In Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Confirmation No : %s%n", res.getConfirmationNo());
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room            : %s%n", res.getRoomNo());
        System.out.printf ("  Check-In        : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out       : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights          : %d%n", res.getNumNights());
        System.out.printf ("  Total Amount    : RM %.2f%n", res.getTotalAmount());
        System.out.printf ("  Status          : %s%n", res.getStatus());
        System.out.println("  ============================================");
        System.out.println("  Welcome to TARUMT Resort!");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Check-Out Guest
    //    BST ADT: searchByConfirmation() → update status to CHECKED_OUT
    //    Room status → UNDER_MAINTENANCE (dirty, ready for housekeeping)
    // ══════════════════════════════════════════════════════════════
    private void checkOut() {
        printHeader("Check-Out Guest");
        System.out.println("  ADT: BST in-order traversal search by confirmation number");
        System.out.println();

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        System.out.println("  [BST] Searching BST by confirmation number...");
        Reservation res = reservationList.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [!] No reservation found for confirmation number: " + confNo);
            pressEnterToContinue();
            return;
        }
        System.out.println("  [BST] Reservation found.");
        System.out.println();

        // Status guard
        if (res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            System.out.printf("  [!] Guest has not checked in. Current status: %s%n",
                    res.getStatus());
            System.out.println("      Only CHECKED_IN guests can be checked out.");
            pressEnterToContinue();
            return;
        }

        // Display for verification
        printReservationDetails(res, false);
        System.out.println();

        // Show invoice
        Room room = findRoom(res.getRoomNo());
        Guest guest = findGuest(res.getGuestId());
        System.out.println("  ============================================");
        System.out.println("        TARUMT Resort - Invoice");
        System.out.println("  ============================================");
        System.out.printf ("  Confirmation No : %s%n", res.getConfirmationNo());
        System.out.printf ("  Reservation ID  : %s%n", res.getReservationId());
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room            : %s (%s)%n", res.getRoomNo(),
                room != null ? room.getRoomType() : "N/A");
        System.out.printf ("  Check-In        : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out       : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights          : %d%n", res.getNumNights());
        System.out.printf ("  Rate/Night      : RM %.2f%n",
                room != null ? room.getPricePerNight() : 0.0);
        System.out.println("  --------------------------------------------");
        System.out.printf ("  TOTAL DUE       : RM %.2f%n", res.getTotalAmount());
        System.out.println("  ============================================");
        System.out.println();
        System.out.print("  Confirm check-out and payment received? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Check-out cancelled.");
            pressEnterToContinue();
            return;
        }

        // Update statuses
        res.setStatus(Reservation.ReservationStatus.CHECKED_OUT);
        // Room becomes UNDER_MAINTENANCE (dirty) — ready for Housekeeping Module
        if (room != null) room.setStatus(Room.RoomStatus.UNDER_MAINTENANCE);

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("           Check-Out Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room            : %s%n", res.getRoomNo());
        System.out.printf ("  Total Billed    : RM %.2f%n", res.getTotalAmount());
        System.out.printf ("  Room Status     : %s (needs cleaning)%n", Room.RoomStatus.UNDER_MAINTENANCE);
        System.out.println("  ============================================");
        System.out.println("  Thank you for staying at TARUMT Resort!");
        System.out.println("  Room has been flagged for Housekeeping.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 5. Check Room Availability
    //    Queue<Room> traversal — read-only enquiry for front desk
    // ══════════════════════════════════════════════════════════════
    private void checkRoomAvailability() {
        printHeader("Check Room Availability");
        System.out.println("  Enter dates to check availability, or press Enter to see all rooms.");
        System.out.println();

        System.out.print("  Check-In Date  (DD/MM/YYYY, or Enter to skip): ");
        String inStr = sc.nextLine().trim();
        LocalDate checkIn  = null;
        LocalDate checkOut = null;

        if (!inStr.isEmpty()) {
            try {
                checkIn = LocalDate.parse(inStr, DATE_FMT);
                System.out.print("  Check-Out Date (DD/MM/YYYY): ");
                String outStr = sc.nextLine().trim();
                checkOut = LocalDate.parse(outStr, DATE_FMT);
                if (!checkOut.isAfter(checkIn)) {
                    System.out.println("  [!] Check-out must be after check-in. Showing all rooms instead.");
                    checkIn = null; checkOut = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date. Showing all rooms.");
                checkIn = null; checkOut = null;
            }
        }

        System.out.println();
        if (checkIn != null) {
            System.out.printf("  Showing rooms available from %s to %s%n",
                    checkIn.format(DATE_FMT), checkOut.format(DATE_FMT));
        } else {
            System.out.println("  Showing all rooms (current status):");
        }
        System.out.println();
        System.out.printf("  %-8s %-12s %-14s %-9s %-15s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Availability");
        printDivider();

        int countAvail = 0, countTotal = 0;
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            countTotal++;

            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) {
                System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-15s%n",
                        r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                        r.getCapacity(), "MAINTENANCE");
                continue;
            }

            boolean available;
            if (checkIn != null) {
                // Date-aware: check for overlapping reservations
                boolean hasOverlap = false;
                for (int j = 0; j < reservationList.size(); j++) {
                    Reservation res = reservationList.get(j);
                    if (!res.getRoomNo().equalsIgnoreCase(r.getRoomNo())) continue;
                    if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED
                            && res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) continue;
                    if (checkIn.isBefore(res.getCheckOutDate())
                            && checkOut.isAfter(res.getCheckInDate())) {
                        hasOverlap = true;
                        break;
                    }
                }
                available = !hasOverlap;
            } else {
                available = r.isAvailable();
            }

            if (available) countAvail++;
            System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-15s%n",
                    r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), available ? "AVAILABLE" : "UNAVAILABLE");
        }
        printDivider();
        System.out.printf("  Total: %d  |  Available: %d  |  Unavailable: %d%n",
                countTotal, countAvail, countTotal - countAvail);
        System.out.println();
        System.out.println("  Note: This is a read-only enquiry. Walk-in bookings are handled");
        System.out.println("        in the Walk-In Registration Module.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 6. View Current Guests
    //    BST in-order traversal — filter CHECKED_IN reservations
    // ══════════════════════════════════════════════════════════════
    private void viewCurrentGuests() {
        printHeader("View Current Guests");
        System.out.println("  ADT: BST in-order traversal — displays all CHECKED_IN guests");
        System.out.println();
        System.out.println("  [BST] Performing in-order traversal of reservation BST...");
        System.out.println();
        System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-12s%n",
                "Conf No", "Guest Name", "Room", "Check-In", "Check-Out", "Status");
        printDivider();

        int count = 0;
        // BST in-order traversal via get(i) — visits all nodes in sorted order
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation res = reservationList.get(i);
            if (res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) continue;

            Guest guest = findGuest(res.getGuestId());
            String guestName = guest != null ? guest.getName() : res.getGuestId();
            String confNo    = res.getConfirmationNo().isEmpty() ? res.getReservationId()
                                                                 : res.getConfirmationNo();

            System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-12s%n",
                    confNo, guestName, res.getRoomNo(),
                    res.getCheckInDate().format(DATE_FMT),
                    res.getCheckOutDate().format(DATE_FMT),
                    res.getStatus());
            count++;
        }

        if (count == 0) {
            System.out.println("  No guests are currently checked in.");
        }
        printDivider();
        System.out.printf("  Guests currently checked in: %d%n", count);
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    private String readConfirmationNumber() {
        System.out.print("  Enter 8-digit Confirmation Number: ");
        String input = sc.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("  [!] No confirmation number entered.");
            return null;
        }
        return input;
    }

    private String readNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return "0";
            if (!input.isEmpty()) return input;
            System.out.println("  [!] Field cannot be empty. Please enter a value (or 0 to cancel).");
        }
    }

    private void printReservationDetails(Reservation res, boolean showConfirmation) {
        Guest guest = findGuest(res.getGuestId());
        Room  room  = findRoom(res.getRoomNo());

        System.out.println("  ============================================");
        System.out.println("           Reservation Details");
        System.out.println("  ============================================");
        if (showConfirmation) {
            System.out.printf("  Confirmation No : %s%n", res.getConfirmationNo().isEmpty()
                    ? "(not set)" : res.getConfirmationNo());
            System.out.println("  --------------------------------------------");
        }
        System.out.printf("  Reservation ID  : %s%n", res.getReservationId());
        System.out.printf("  Guest ID        : %s%n", res.getGuestId());
        System.out.printf("  Guest Name      : %s%n",
                guest != null ? guest.getName() : "N/A");
        System.out.printf("  Room Number     : %s%n", res.getRoomNo());
        System.out.printf("  Room Type       : %s%n",
                room != null ? room.getRoomType() : "N/A");
        System.out.printf("  Check-In Date   : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf("  Check-Out Date  : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf("  Nights          : %d%n", res.getNumNights());
        System.out.printf("  No. of Guests   : %d%n", res.getNumGuests());
        System.out.printf("  Total Amount    : RM %.2f%n", res.getTotalAmount());
        System.out.printf("  Status          : %s%n", res.getStatus());
        System.out.println("  ============================================");
    }

    private Room findRoom(String roomNo) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    private Guest findGuest(String guestId) {
        for (int i = 0; i < guestList.size(); i++) {
            if (guestList.get(i).getGuestId().equalsIgnoreCase(guestId))
                return guestList.get(i);
        }
        return null;
    }

    private Reservation findReservationForGuest(String guestId) {
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

    private void autoSave() {
        DataStore.saveGuests(guestList);
        DataStore.saveReservations(reservationList);
        DataStore.saveRooms(roomList);
    }

    private void printHeader(String title) {
        ConsoleUtils.clearScreen();
        System.out.println();
        System.out.println("  ============================================");
        System.out.printf ("              %s%n", title);
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
