package dsa_ass.boundary;

import dsa_ass.adt.Queue;
import dsa_ass.control.WalkInRegistrationControl;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.ConsoleUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Boundary: WalkInRegistrationBoundary
 *
 * Handles presentation and user interactions for walk-in registrations,
 * displaying menus, prompts, forms, and formatted tables.
 */
public class WalkInRegistrationBoundary {

    private final WalkInRegistrationControl control;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public WalkInRegistrationBoundary(WalkInRegistrationControl control, Scanner sc) {
        this.control = control;
        this.sc      = sc;
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
                case "1": registerWalkIn();         break;
                case "2": viewWalkInQueue();        break;
                case "3": processNextGuest();       break;
                case "4": checkRoomAvailability();  break;
                case "5": viewWalkInReservations(); break;
                case "0": back = true;              break;
                default:
                    System.out.println("  [!] Invalid option. Please enter 0 - 5.");
                    pressEnterToContinue();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 1. Register Walk-In Guest
    // ══════════════════════════════════════════════════════════════
    private void registerWalkIn() {
        printHeader("Register Walk-In Guest");
        System.out.println("  (Enter 0 at any field to cancel)");
        System.out.println();

        String name = readNonEmptyInput("  Full Name         : ");
        if (name.equals("0")) { cancelled(); return; }

        String ic = readIcInput("  IC Number (XXXXXX-XX-XXXX) : ");
        if (ic.equals("0")) { cancelled(); return; }

        String phone = readPhoneInput("  Phone Number (01X-XXXXXXX)  : ");
        if (phone.equals("0")) { cancelled(); return; }

        String email = readGmailInput("  Gmail Address (@gmail.com)  : ");
        if (email.equals("0")) { cancelled(); return; }

        String nationality = readNonEmptyInput("  Nationality       : ");
        if (nationality.equals("0")) { cancelled(); return; }

        Guest g = control.registerWalkIn(name, ic, phone, email, nationality);
        int queuePosition = control.getQueueSize();

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("       Walk-In Registration Successful");
        System.out.println("  ============================================");
        System.out.printf ("  Guest ID       : %s%n", g.getGuestId());
        System.out.printf ("  Full Name      : %s%n", g.getName());
        System.out.printf ("  IC / Passport  : %s%n", g.getIcNo());
        System.out.printf ("  Phone Number   : %s%n", g.getPhone());
        System.out.printf ("  Email Address  : %s%n", g.getEmail());
        System.out.printf ("  Nationality    : %s%n", g.getNationality());
        System.out.println("  ============================================");
        System.out.println();
        System.out.printf ("  Current queue position : #%d%n", queuePosition);
        System.out.printf ("  Guests waiting         : %d%n", control.getQueueSize());
        if (queuePosition == 1) {
            System.out.println("  This guest is at the FRONT and will be served next.");
        } else {
            System.out.printf ("  %d guest(s) ahead in queue.%n", queuePosition - 1);
        }
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 2. View Walk-In Queue
    // ══════════════════════════════════════════════════════════════
    private void viewWalkInQueue() {
        printHeader("View Walk-In Queue");
        System.out.println();

        if (control.isQueueEmpty()) {
            System.out.println("  The walk-in queue is currently empty.");
            System.out.println("  No guests are waiting.");
            pressEnterToContinue();
            return;
        }

        Queue<Guest> queue = control.getWalkInQueue();
        System.out.printf("  %-5s %-10s %-22s %-16s %-14s%n",
                "Pos", "Guest ID", "Full Name", "Phone", "Status");
        printDivider();

        for (int i = 0; i < queue.size(); i++) {
            Guest g = queue.get(i);
            String positionTag = (i == 0) ? "#1 [NEXT]" : "#" + (i + 1);
            System.out.printf("  %-5s %-10s %-22s %-16s %-14s%n",
                    positionTag,
                    g.getGuestId(),
                    g.getName(),
                    g.getPhone(),
                    (i == 0) ? "Serving Next" : "Waiting");
        }

        printDivider();
        System.out.printf("  Total waiting in queue: %d%n", queue.size());
        Guest frontGuest = control.peekNextGuest();
        if (frontGuest != null) {
            System.out.printf("  Next to be processed : %s (%s)%n",
                    frontGuest.getName(), frontGuest.getGuestId());
        }
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Process Next Walk-In Guest
    // ══════════════════════════════════════════════════════════════
    private void processNextGuest() {
        printHeader("Process Next Walk-In Guest");

        if (control.isQueueEmpty()) {
            System.out.println("  No guests in the waiting queue.");
            System.out.println("  Please register a walk-in guest first (Option 1).");
            pressEnterToContinue();
            return;
        }

        Guest guest = control.peekNextGuest();

        System.out.println("  Now Serving (Queue Front):");
        System.out.println("  --------------------------------------------");
        System.out.printf ("  Guest ID      : %s%n", guest.getGuestId());
        System.out.printf ("  Full Name     : %s%n", guest.getName());
        System.out.printf ("  IC / Passport : %s%n", guest.getIcNo());
        System.out.printf ("  Phone         : %s%n", guest.getPhone());
        System.out.printf ("  Email         : %s%n", guest.getEmail());
        System.out.printf ("  Nationality   : %s%n", guest.getNationality());
        System.out.println("  --------------------------------------------");
        System.out.println();
        System.out.print("  Proceed to book a room for this guest? (Y/N, or 0 to return): ");
        String proceed = sc.nextLine().trim();
        if (proceed.equals("0") || proceed.equalsIgnoreCase("N")) {
            System.out.println("  Operation cancelled. Guest remains at the front of the queue.");
            pressEnterToContinue();
            return;
        }

        // Room booking loop
        boolean booked = bookRoomForGuest(guest);

        if (booked) {
            Guest removed = control.dequeueNextGuest();
            System.out.println();
            System.out.println("  ============================================");
            System.out.printf ("  [✓] %s (%s) has been served and removed from queue.%n",
                    removed.getName(), removed.getGuestId());
            System.out.printf ("  Remaining in queue: %d%n", control.getQueueSize());
            System.out.println("  ============================================");
            pressEnterToContinue();
        }
    }

    private boolean bookRoomForGuest(Guest guest) {
        boolean bookingFlowActive = true;

        while (bookingFlowActive) {
            System.out.println();
            printDivider();
            System.out.println("  Select Room Type:");
            System.out.println("  1. SINGLE     (RM  99.00/night, max 1 guest)");
            System.out.println("  2. STANDARD   (RM 180.00/night, max 2 guests)");
            System.out.println("  3. DELUXE     (RM 280.00/night, max 4 guests)");
            System.out.println("  4. SUITE      (RM 450.00/night, max 4 guests)");
            System.out.println("  5. VILLA      (RM 950.00/night, max 8 guests)");
            System.out.println("  0. Cancel (keep guest in queue)");
            printDivider();
            System.out.print("  Enter choice (1 - 5, or 0): ");
            String typeChoice = sc.nextLine().trim();

            if (typeChoice.equals("0")) {
                System.out.println("  Booking cancelled. Guest remains in queue.");
                return false;
            }

            Room.RoomType roomType;
            switch (typeChoice) {
                case "1": roomType = Room.RoomType.SINGLE;   break;
                case "2": roomType = Room.RoomType.STANDARD; break;
                case "3": roomType = Room.RoomType.DELUXE;   break;
                case "4": roomType = Room.RoomType.SUITE;    break;
                case "5": roomType = Room.RoomType.VILLA;    break;
                default:
                    System.out.println("  [!] Invalid room type. Please select 1 - 5.");
                    continue;
            }

            // Dates
            System.out.println();
            LocalDate checkIn = readDateOrBack("  Check-In Date  (DD/MM/YYYY, or 0 to reselect room type): ");
            if (checkIn == null) {
                System.out.println("  [↺] Returning to room type selection...\n");
                continue;
            }

            LocalDate checkOut = readDateAfterOrBack("  Check-Out Date (DD/MM/YYYY, or 0 to reselect room type): ", checkIn);
            if (checkOut == null) {
                System.out.println("  [↺] Returning to room type selection...\n");
                continue;
            }

            // Availability check
            boolean reselectRoomType = false;
            while (!reselectRoomType) {
                Queue<Room> available = control.findAvailableRooms(roomType, checkIn, checkOut);

                if (!available.isEmpty()) {
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

                    String resId  = control.generateReservationId();
                    String confNo = control.generateConfirmationNumber(resId);

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
                    System.out.println("  Full room payment is required to confirm booking.");
                    System.out.println("  ============================================");
                    System.out.println();
                    System.out.print("  Confirm reservation? (Y/N): ");
                    String confirm = sc.nextLine().trim();
                    if (!confirm.equalsIgnoreCase("Y")) {
                        System.out.println("  Reservation not confirmed. Guest remains in queue.");
                        return false;
                    }

                    // Proceed to Full Room Payment
                    System.out.println();
                    System.out.println("  --------------------------------------------");
                    System.out.println("             Full Room Payment");
                    System.out.println("  --------------------------------------------");
                    System.out.printf ("  Total Room Charges Due : RM %.2f%n", total);
                    System.out.println();
                    System.out.println("  Select Payment Method for Full Room Payment:");
                    System.out.println("  1. Cash");
                    System.out.println("  2. Credit / Debit Card");
                    System.out.println("  3. E-Wallet / QR Pay");
                    System.out.println("  0. Cancel Reservation");
                    printDivider();
                    System.out.print("  Enter choice (1-3, or 0 to cancel): ");
                    String payChoice = sc.nextLine().trim();

                    String paymentMethod;
                    switch (payChoice) {
                        case "1": paymentMethod = "Cash"; break;
                        case "2": paymentMethod = "Credit / Debit Card"; break;
                        case "3": paymentMethod = "E-Wallet / QR Pay"; break;
                        default:
                            System.out.println("  Payment cancelled. Reservation aborted. Guest remains in queue.");
                            pressEnterToContinue();
                            return false;
                    }

                    System.out.println();
                    System.out.printf ("  [✓] Full Room Payment of RM %.2f received via %s.%n", total, paymentMethod);

                    Reservation res = control.confirmAndCreateReservation(resId, confNo, guest,
                            selectedRoom, checkIn, checkOut, numGuests, total);

                    System.out.println();
                    System.out.println("  ============================================");
                    System.out.println("        Reservation & Payment Successful!");
                    System.out.println("  ============================================");
                    System.out.printf ("  Confirmation No : %s  *** KEEP THIS ***%n", confNo);
                    System.out.printf ("  Reservation ID  : %s%n", resId);
                    System.out.printf ("  Guest Name      : %s%n", guest.getName());
                    System.out.printf ("  Room            : %s (%s)%n", selectedRoom.getRoomNo(), selectedRoom.getRoomType());
                    System.out.printf ("  Check-In Date   : %s%n", checkIn.format(DATE_FMT));
                    System.out.printf ("  Check-Out Date  : %s%n", checkOut.format(DATE_FMT));
                    System.out.printf ("  Nights          : %d%n", nights);
                    System.out.printf ("  Total Paid      : RM %.2f (via %s)%n", total, paymentMethod);
                    System.out.printf ("  Status          : %s%n", res.getStatus());
                    System.out.println("  ============================================");
                    System.out.println("  Present the Confirmation No. at the Front Desk");
                    System.out.println("  for Check-In (RM100 refundable deposit required).");
                    System.out.println("  ============================================");
                    pressEnterToContinue();
                    return true;

                } else {
                    // Unavailable path
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
                            break;
                        case "3":
                            Guest removed = control.dequeueNextGuest();
                            System.out.printf("  Guest %s (%s) has been removed from the queue.%n",
                                    removed.getName(), removed.getGuestId());
                            pressEnterToContinue();
                            return false;
                        default:
                            System.out.println("  [!] Invalid choice. Returning to room type selection.");
                            reselectRoomType = true;
                    }
                }
            }
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Check Room Availability
    // ══════════════════════════════════════════════════════════════
    private void checkRoomAvailability() {
        printHeader("Check Room Availability");
        System.out.println("  Enter dates to check availability, or press Enter to see current status.");
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
                System.out.println("  [!] Invalid date format. Showing all rooms.");
                checkIn = null; checkOut = null;
            }
        }

        System.out.println();
        if (checkIn != null) {
            System.out.printf("  Availability from %s to %s:%n",
                    checkIn.format(DATE_FMT), checkOut.format(DATE_FMT));
        } else {
            System.out.println("  Current Room Status (All Rooms):");
        }
        System.out.println();
        System.out.printf("  %-8s %-12s %-14s %-9s %-15s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Availability");
        printDivider();

        int countAvail = 0, countTotal = 0;
        Queue<Room> rooms = control.getRoomList();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            countTotal++;

            if (r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE) {
                System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-15s%n",
                        r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                        r.getCapacity(), "MAINTENANCE");
                continue;
            }

            boolean avail = control.isRoomAvailable(r, checkIn, checkOut);
            if (avail) countAvail++;

            System.out.printf("  %-8s %-12s RM%-12.2f %-9d %-15s%n",
                    r.getRoomNo(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), avail ? "AVAILABLE" : "UNAVAILABLE");
        }

        printDivider();
        System.out.printf("  Total: %d  |  Available: %d  |  Unavailable: %d%n",
                countTotal, countAvail, countTotal - countAvail);
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // 5. View Walk-In Reservations
    // ══════════════════════════════════════════════════════════════
    private void viewWalkInReservations() {
        printHeader("Walk-In Reservations");
        System.out.println("  Reservations created through the Walk-In Registration Module:");
        System.out.println();

        Queue<String> resIds = control.getWalkInResIds();
        if (resIds.isEmpty()) {
            System.out.println("  No walk-in reservations have been created in this session.");
            pressEnterToContinue();
            return;
        }

        System.out.printf("  %-10s %-8s %-20s %-8s %-12s %-12s %-10s %-12s%n",
                "Conf No", "Res ID", "Guest Name", "Room", "Check-In", "Check-Out", "Amount", "Status");
        printDivider();

        int displayed = 0;
        for (int i = 0; i < resIds.size(); i++) {
            String resId = resIds.get(i);
            Reservation res = control.getReservationById(resId);
            if (res == null) continue;

            Guest g = control.findGuest(res.getGuestId());
            String guestName = (g != null) ? g.getName() : res.getGuestId();
            String confNo    = res.getConfirmationNo().isEmpty() ? res.getReservationId()
                                                                 : res.getConfirmationNo();

            System.out.printf("  %-10s %-8s %-20s %-8s %-12s %-12s RM%-8.2f %-12s%n",
                    confNo,
                    res.getReservationId(),
                    guestName,
                    res.getRoomNo(),
                    res.getCheckInDate().format(DATE_FMT),
                    res.getCheckOutDate().format(DATE_FMT),
                    res.getTotalAmount(),
                    res.getStatus());
            displayed++;
        }

        printDivider();
        System.out.printf("  Total Walk-In Reservations: %d%n", displayed);
        pressEnterToContinue();
    }

    // ══════════════════════════════════════════════════════════════
    // Helper Methods
    // ══════════════════════════════════════════════════════════════

    private LocalDate readDateOrBack(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.equals("0")) return null;
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Please use DD/MM/YYYY (e.g. 25/12/2026).");
            }
        }
    }

    private LocalDate readDateAfterOrBack(String prompt, LocalDate checkIn) {
        while (true) {
            LocalDate d = readDateOrBack(prompt);
            if (d == null) return null;
            if (d.isAfter(checkIn)) return d;
            System.out.println("  [!] Check-out date must be after check-in date ("
                    + checkIn.format(DATE_FMT) + "). Please try again (or 0 to reselect room type).");
        }
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

    private void cancelled() {
        System.out.println("  Registration cancelled.");
        pressEnterToContinue();
    }
}
