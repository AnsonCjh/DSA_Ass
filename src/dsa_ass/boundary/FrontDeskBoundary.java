package dsa_ass.boundary;

import dsa_ass.adt.Queue;
import dsa_ass.control.FrontDeskControl;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.ConsoleUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Boundary: FrontDeskBoundary
 *
 * Handles presentation and user interactions for Front Desk Service,
 * including confirmation lookups, guest management, check-in, check-out,
 * room availability enquiries, and current guest reports.
 */
public class FrontDeskBoundary {

    private final FrontDeskControl control;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FrontDeskBoundary(FrontDeskControl control, Scanner sc) {
        this.control = control;
        this.sc      = sc;
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
                case "3": checkIn();               control.autoSave(); break;
                case "4": checkOut();              control.autoSave(); break;
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
    // ══════════════════════════════════════════════════════════════
    private void searchByConfirmation() {
        printHeader("Search by Confirmation Number");

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        Reservation res = control.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [!] Confirmation number '" + confNo + "' not found.");
            System.out.println("      Please verify the number and try again.");
            pressEnterToContinue();
            return;
        }

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
                case "1": viewAllGuests();     break;
                case "2": searchGuest();       break;
                case "3": viewGuestDetails();  break;
                case "4": modifyGuest();       break;
                case "5": deleteGuest(null);   break;
                case "0": back = true;         break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 5.");
                    pressEnterToContinue();
            }
        }
    }

    private void viewAllGuests() {
        printHeader("All Registered Guests");
        Queue<Guest> list = control.getAllGuests();
        if (list.isEmpty()) {
            System.out.println("  No guests registered in the system.");
            pressEnterToContinue();
            return;
        }

        System.out.printf("  %-8s %-20s %-16s %-14s %-26s %-14s%n",
                "Guest ID", "Full Name", "IC/Passport", "Phone", "Email", "Nationality");
        printDivider();

        for (int i = 0; i < list.size(); i++) {
            Guest g = list.get(i);
            System.out.printf("  %-8s %-20s %-16s %-14s %-26s %-14s%n",
                    g.getGuestId(), g.getName(), g.getIcNo(),
                    g.getPhone(), g.getEmail(), g.getNationality());
        }

        printDivider();
        System.out.printf("  Total Guests: %d%n", list.size());
        pressEnterToContinue();
    }

    private void searchGuest() {
        printHeader("Search Guest");
        System.out.print("  Enter Search Keyword (Name / ID / IC / Phone): ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("  [!] Keyword cannot be empty.");
            pressEnterToContinue();
            return;
        }

        Queue<Guest> results = control.searchGuests(keyword);
        System.out.println();
        if (results.isEmpty()) {
            System.out.println("  No matching guests found for: " + keyword);
            pressEnterToContinue();
            return;
        }

        System.out.printf("  Found %d matching guest(s):%n", results.size());
        System.out.println();
        System.out.printf("  %-8s %-20s %-16s %-14s %-26s %-14s%n",
                "Guest ID", "Full Name", "IC/Passport", "Phone", "Email", "Nationality");
        printDivider();

        for (int i = 0; i < results.size(); i++) {
            Guest g = results.get(i);
            System.out.printf("  %-8s %-20s %-16s %-14s %-26s %-14s%n",
                    g.getGuestId(), g.getName(), g.getIcNo(),
                    g.getPhone(), g.getEmail(), g.getNationality());
        }

        printDivider();
        pressEnterToContinue();
    }

    private void viewGuestDetails() {
        printHeader("View Guest Details");
        System.out.print("  Enter Guest ID, Name, or Phone: ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) return;

        Guest g = control.findGuest(keyword);
        if (g == null) {
            System.out.println("  [!] Guest not found for: " + keyword);
            pressEnterToContinue();
            return;
        }

        printGuestProfile(g);
        pressEnterToContinue();
    }

    private void printGuestProfile(Guest g) {
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("                Guest Profile");
        System.out.println("  ============================================");
        System.out.printf ("  Guest ID      : %s%n", g.getGuestId());
        System.out.printf ("  Full Name     : %s%n", g.getName());
        System.out.printf ("  IC / Passport : %s%n", g.getIcNo());
        System.out.printf ("  Phone Number  : %s%n", g.getPhone());
        System.out.printf ("  Email Address : %s%n", g.getEmail());
        System.out.printf ("  Nationality   : %s%n", g.getNationality());
        System.out.println("  ============================================");

        Reservation res = control.findReservationForGuest(g.getGuestId());
        if (res != null) {
            System.out.println("  Active/Latest Booking:");
            System.out.printf ("    Confirmation No : %s%n",
                    res.getConfirmationNo().isEmpty() ? res.getReservationId() : res.getConfirmationNo());
            System.out.printf ("    Room            : %s%n", res.getRoomNo());
            System.out.printf ("    Stay Dates      : %s - %s (%d nights)%n",
                    res.getCheckInDate().format(DATE_FMT),
                    res.getCheckOutDate().format(DATE_FMT),
                    res.getNumNights());
            System.out.printf ("    Total Charges   : RM %.2f%n", res.getTotalAmount());
            System.out.printf ("    Status          : %s%n", res.getStatus());
            System.out.println("  ============================================");
        }
    }

    private void modifyGuest() {
        printHeader("Modify Guest Details");
        System.out.print("  Enter Guest ID, Name, or Phone: ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) return;

        Guest targetGuest = control.findGuest(keyword);
        if (targetGuest == null) {
            System.out.println("  [!] Guest not found for: " + keyword);
            pressEnterToContinue();
            return;
        }

        printGuestProfile(targetGuest);
        System.out.println();
        System.out.println("  1. Full Name");
        System.out.println("  2. Phone Number");
        System.out.println("  3. Email Address");
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
                    control.autoSave();
                    System.out.println("  [✓] Guest name updated successfully!");
                }
                break;
            case "2":
                System.out.println("  Current Phone: " + targetGuest.getPhone());
                String phone = readPhoneInput("  Enter New Phone Number (01X-XXXXXXX): ");
                if (!phone.equals("0")) {
                    targetGuest.setPhone(phone);
                    control.autoSave();
                    System.out.println("  [✓] Phone number updated successfully!");
                }
                break;
            case "3":
                System.out.println("  Current Email: " + targetGuest.getEmail());
                String email = readGmailInput("  Enter New Gmail Address (@gmail.com): ");
                if (!email.equals("0")) {
                    targetGuest.setEmail(email);
                    control.autoSave();
                    System.out.println("  [✓] Email address updated successfully!");
                }
                break;
            case "4":
                System.out.println("  Current IC / Passport: " + targetGuest.getIcNo());
                String ic = readIcInput("  Enter New IC Number (XXXXXX-XX-XXXX): ");
                if (!ic.equals("0")) {
                    targetGuest.setIcNo(ic);
                    control.autoSave();
                    System.out.println("  [✓] IC number updated successfully!");
                }
                break;
            case "5":
                System.out.println("  Current Nationality: " + targetGuest.getNationality());
                String nat = readNonEmptyInput("  Enter New Nationality: ");
                if (!nat.equals("0")) {
                    targetGuest.setNationality(nat);
                    control.autoSave();
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

    private boolean deleteGuest(Guest targetGuest) {
        if (targetGuest == null) {
            printHeader("Delete Guest");
            System.out.print("  Enter Guest ID to delete: ");
            String keyword = sc.nextLine().trim();
            if (keyword.isEmpty()) return false;

            targetGuest = control.findGuest(keyword);
            if (targetGuest == null) {
                System.out.println("  [!] Guest not found for ID: " + keyword);
                pressEnterToContinue();
                return false;
            }
        }

        if (control.hasActiveReservation(targetGuest.getGuestId())) {
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
            boolean deleted = control.deleteGuest(targetGuest);
            if (deleted) {
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
    // ══════════════════════════════════════════════════════════════
    private void checkIn() {
        printHeader("Check-In Guest");

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        Reservation res = control.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [!] No reservation found for confirmation number: " + confNo);
            pressEnterToContinue();
            return;
        }

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

        // Room readiness guard
        Room room = control.findRoom(res.getRoomNo());
        if (!control.isRoomReadyForCheckIn(room)) {
            System.out.println();
            System.out.printf("  [!] Notice: Room %s is currently '%s'.%n", room.getRoomNo(), room.getStatus());
            System.out.println("      Room is not ready for guest check-in.");
            System.out.print("      Force override and proceed with check-in? (Y/N): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
                System.out.println("  Check-in suspended. Please wait for Housekeeping.");
                pressEnterToContinue();
                return;
            }
        }

        // Show Invoice / Reservation Details
        Guest guest = control.findGuest(res.getGuestId());
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("        TARUMT Resort - Check-In Invoice");
        System.out.println("  ============================================");
        System.out.printf ("  Confirmation No : %s%n", res.getConfirmationNo().isEmpty() ? "(not set)" : res.getConfirmationNo());
        System.out.printf ("  Reservation ID  : %s%n", res.getReservationId());
        System.out.printf ("  Guest ID        : %s%n", res.getGuestId());
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room Number     : %s (%s)%n", res.getRoomNo(), room != null ? room.getRoomType() : "N/A");
        System.out.printf ("  Check-In Date   : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out Date  : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights          : %d%n", res.getNumNights());
        System.out.printf ("  Rate / Night    : RM %.2f%n", room != null ? room.getPricePerNight() : 0.0);
        System.out.printf ("  Room Charges    : RM %.2f (PAID)%n", res.getTotalAmount());
        System.out.println("  --------------------------------------------");
        System.out.println("  Security Deposit: RM 100.00 (Refundable upon check-out)");
        System.out.println("  ============================================");

        // Confirm Check-In? (Y/N)
        System.out.println();
        System.out.print("  Confirm Check-In? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Check-in cancelled.");
            pressEnterToContinue();
            return;
        }

        // Pay RM100 Deposit
        System.out.println();
        System.out.println("  --------------------------------------------");
        System.out.println("             Security Deposit Payment");
        System.out.println("  --------------------------------------------");
        System.out.println("  Deposit Amount Required : RM 100.00");
        System.out.println();
        System.out.println("  Select Payment Method for RM100 Deposit:");
        System.out.println("  1. Cash");
        System.out.println("  2. Credit / Debit Card");
        System.out.println("  3. E-Wallet / QR Pay");
        System.out.println("  0. Cancel Check-In");
        printDivider();
        System.out.print("  Enter choice (1-3, or 0 to cancel): ");
        String payChoice = sc.nextLine().trim();

        String depositPaymentMethod;
        switch (payChoice) {
            case "1": depositPaymentMethod = "Cash"; break;
            case "2": depositPaymentMethod = "Credit / Debit Card"; break;
            case "3": depositPaymentMethod = "E-Wallet / QR Pay"; break;
            default:
                System.out.println("  Deposit payment cancelled. Check-in aborted.");
                pressEnterToContinue();
                return;
        }

        System.out.println();
        System.out.printf ("  [✓] RM 100.00 Security Deposit received via %s.%n", depositPaymentMethod);

        control.processCheckIn(res, room, 100.00);

        // Check-In Successful
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("            Check-In Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Confirmation No : %s%n", res.getConfirmationNo());
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room            : %s (%s)%n", res.getRoomNo(), room != null ? room.getRoomType() : "");
        System.out.printf ("  Check-In        : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out       : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights          : %d%n", res.getNumNights());
        System.out.printf ("  Room Charges    : RM %.2f (PAID)%n", res.getTotalAmount());
        System.out.printf ("  Deposit Paid    : RM %.2f (%s - Refundable)%n", res.getDeposit(), depositPaymentMethod);
        System.out.printf ("  Status          : %s%n", res.getStatus());
        System.out.println("  ============================================");
        System.out.println("  Welcome to TARUMT Resort! Room key issued.");
        System.out.println("  Please present your Confirmation No. at Check-Out.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Check-Out Guest
    // ══════════════════════════════════════════════════════════════
    private void checkOut() {
        printHeader("Check-Out Guest");

        String confNo = readConfirmationNumber();
        if (confNo == null) { pressEnterToContinue(); return; }

        System.out.println();
        Reservation res = control.searchByConfirmation(confNo);

        if (res == null) {
            System.out.println("  [!] No reservation found for confirmation number: " + confNo);
            pressEnterToContinue();
            return;
        }

        // Status guard
        if (res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            System.out.printf("  [!] Guest has not checked in. Current status: %s%n",
                    res.getStatus());
            System.out.println("      Only CHECKED_IN guests can be checked out.");
            pressEnterToContinue();
            return;
        }

        // Show invoice with room charges & deposit refund
        Room room = control.findRoom(res.getRoomNo());
        Guest guest = control.findGuest(res.getGuestId());
        double depositToRefund = res.getDeposit() > 0 ? res.getDeposit() : 100.00;

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("        TARUMT Resort - Check-Out Invoice");
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
        System.out.printf ("  Total Room Bill : RM %.2f (PAID during reservation)%n", res.getTotalAmount());
        System.out.printf ("  Deposit Paid    : RM %.2f%n", depositToRefund);
        System.out.println("  --------------------------------------------");
        System.out.printf ("  Deposit Refund  : RM %.2f (To be returned to guest)%n", depositToRefund);
        System.out.printf ("  Outstanding Due : RM 0.00%n");
        System.out.println("  ============================================");
        System.out.println();
        System.out.print("  Confirm check-out and refund deposit? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Check-out cancelled.");
            pressEnterToContinue();
            return;
        }

        // Refund RM100 Deposit
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("        Security Deposit Refund Receipt");
        System.out.println("  ============================================");
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Deposit Amount  : RM %.2f%n", depositToRefund);
        System.out.println("  Refund Status   : REFUNDED (Returned to Guest)");
        System.out.println("  ============================================");
        System.out.printf ("  [✓] RM %.2f Security Deposit has been refunded to guest.%n", depositToRefund);

        String generatedTaskId = control.processCheckOut(res, room, guest);

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("           Check-Out Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Guest Name      : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room            : %s%n", res.getRoomNo());
        System.out.printf ("  Total Room Bill : RM %.2f (PAID)%n", res.getTotalAmount());
        System.out.printf ("  Deposit Refund  : RM %.2f (REFUNDED)%n", depositToRefund);
        System.out.printf ("  Room Status     : %s (needs housekeeping)%n", Room.RoomStatus.DIRTY);
        if (generatedTaskId != null) {
            System.out.printf ("  Housekeeping    : Task %s created & queued%n", generatedTaskId);
        }
        System.out.println("  ============================================");
        System.out.println("  Thank you for staying at TARUMT Resort!");
        System.out.println("  Room status updated to DIRTY.");
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 5. Check Room Availability
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
        Queue<Room> roomList = control.getRoomList();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            countTotal++;

            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) {
                System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-15s%n",
                        r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                        r.getCapacity(), "MAINTENANCE");
                continue;
            }

            boolean available = control.isRoomAvailable(r, checkIn, checkOut);
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
    // ══════════════════════════════════════════════════════════════
    private void viewCurrentGuests() {
        printHeader("View Current Guests");
        System.out.println();
        System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-12s%n",
                "Conf No", "Guest Name", "Room", "Check-In", "Check-Out", "Status");
        printDivider();

        Queue<Reservation> checkedInList = control.getCurrentCheckedInReservations();
        for (int i = 0; i < checkedInList.size(); i++) {
            Reservation res = checkedInList.get(i);
            Guest guest = control.findGuest(res.getGuestId());
            String guestName = guest != null ? guest.getName() : res.getGuestId();
            String confNo    = res.getConfirmationNo().isEmpty() ? res.getReservationId()
                                                                 : res.getConfirmationNo();

            System.out.printf("  %-10s %-22s %-8s %-12s %-12s %-12s%n",
                    confNo, guestName, res.getRoomNo(),
                    res.getCheckInDate().format(DATE_FMT),
                    res.getCheckOutDate().format(DATE_FMT),
                    res.getStatus());
        }

        if (checkedInList.isEmpty()) {
            System.out.println("  No guests are currently checked in.");
        }
        printDivider();
        System.out.printf("  Guests currently checked in: %d%n", checkedInList.size());
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

    private String readIcInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return "0";
            if (control.isValidIc(input)) {
                return input;
            }
            System.out.println("  [!] Invalid IC format. Must be in format XXXXXX-XX-XXXX (e.g. 990101-14-5678).");
            System.out.println("      Please try again (or enter 0 to cancel).");
        }
    }

    private String readPhoneInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return "0";
            if (control.isValidPhone(input)) {
                return input;
            }
            System.out.println("  [!] Invalid phone number format. Must start with 01X- followed by 7-8 digits (e.g. 012-3456789).");
            System.out.println("      Please try again (or enter 0 to cancel).");
        }
    }

    private String readGmailInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return "0";
            if (control.isValidGmail(input)) {
                return input;
            }
            System.out.println("  [!] Invalid Gmail address. Must be a valid address ending with @gmail.com (e.g. user@gmail.com).");
            System.out.println("      Please try again (or enter 0 to cancel).");
        }
    }

    private void printReservationDetails(Reservation res, boolean showConfirmation) {
        Guest guest = control.findGuest(res.getGuestId());
        Room  room  = control.findRoom(res.getRoomNo());

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
        if (res.getStatus() == Reservation.ReservationStatus.CHECKED_IN
                || res.getStatus() == Reservation.ReservationStatus.CHECKED_OUT
                || res.getDeposit() > 0) {
            System.out.printf("  Security Deposit: RM %.2f%n", res.getDeposit());
        }
        System.out.printf("  Status          : %s%n", res.getStatus());
        System.out.println("  ============================================");
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
}
