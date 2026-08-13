package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.util.ConsoleUtils;
import dsa_ass.util.DataStore;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Front Desk Module
 * Handles: Check In, Check Out, Guest Inquiry, Room Information,
 *          Billing & Payment, Update Guest Information
 * ADT Used: BinarySearchTree<Reservation> (BST ADT)
 */
public class FrontDeskModule {

    private final Queue<Guest>                 guestList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Room>                  roomList;
    private final Scanner sc;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FrontDeskModule(Queue<Guest> guestList,
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
            printHeader("Front Desk Module");
            System.out.println("  1. Check In");
            System.out.println("  2. Check Out");
            System.out.println("  3. Guest Inquiry");
            System.out.println("  4. Room Information");
            System.out.println("  5. Billing & Payment");
            System.out.println("  6. Update Guest Information");
            System.out.println("  0. Back to Main Menu");
            printDivider();
            System.out.print("  Enter your choice: ");
            String choice = sc.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": checkIn();                  autoSave(); break;
                case "2": checkOut();                 autoSave(); break;
                case "3": guestInquiry();                         break; // read-only
                case "4": roomInformation();                       break; // read-only
                case "5": billingAndPayment();        autoSave(); break;
                case "6": updateGuestInformation();   autoSave(); break;
                case "0": back = true;                            break;
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

    // ── 1. Check In ───────────────────────────────────────────────
    private void checkIn() {
        printHeader("Check In");
        System.out.print("  Enter Reservation ID: ");
        String resId = sc.nextLine().trim().toUpperCase();
        Reservation res = findReservation(resId);
        if (res == null) {
            System.out.println("  [!] Reservation not found.");
            pressEnterToContinue();
            return;
        }
        if (res.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            System.out.println("  [!] Reservation is not in CONFIRMED status. Current: " + res.getStatus());
            pressEnterToContinue();
            return;
        }

        res.setStatus(Reservation.ReservationStatus.CHECKED_IN);
        Room room = findRoom(res.getRoomNo());
        if (room != null) room.setStatus(Room.RoomStatus.OCCUPIED);

        Guest guest = findGuest(res.getGuestId());
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("           Check-In Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Guest Name   : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room         : %s%n", res.getRoomNo());
        System.out.printf ("  Check-In     : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out    : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights       : %d%n", res.getNumNights());
        System.out.printf ("  Total Amount : RM %.2f%n", res.getTotalAmount());
        System.out.println("  ============================================");
        System.out.println("  Welcome to TARUMT Resort!");
        pressEnterToContinue();
    }

    // ── 2. Check Out ──────────────────────────────────────────────
    private void checkOut() {
        printHeader("Check Out");
        System.out.print("  Enter Reservation ID: ");
        String resId = sc.nextLine().trim().toUpperCase();
        Reservation res = findReservation(resId);
        if (res == null) {
            System.out.println("  [!] Reservation not found.");
            pressEnterToContinue();
            return;
        }
        if (res.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            System.out.println("  [!] Guest has not checked in. Status: " + res.getStatus());
            pressEnterToContinue();
            return;
        }

        res.setStatus(Reservation.ReservationStatus.CHECKED_OUT);
        Room room = findRoom(res.getRoomNo());
        if (room != null) room.setStatus(Room.RoomStatus.AVAILABLE);

        Guest guest = findGuest(res.getGuestId());
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("           Check-Out Successful!");
        System.out.println("  ============================================");
        System.out.printf ("  Guest Name   : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room         : %s%n", res.getRoomNo());
        System.out.printf ("  Total Billed : RM %.2f%n", res.getTotalAmount());
        System.out.println("  ============================================");
        System.out.println("  Thank you for staying at TARUMT Resort!");
        pressEnterToContinue();
    }

    // ── 3. Guest Inquiry ─────────────────────────────────────────
    private void guestInquiry() {
        printHeader("Guest Inquiry");
        System.out.print("  Enter Guest ID or Name: ");
        String keyword = sc.nextLine().trim();
        boolean found = false;
        for (int i = 0; i < guestList.size(); i++) {
            Guest g = guestList.get(i);
            if (g.getGuestId().equalsIgnoreCase(keyword)
                    || g.getName().toLowerCase().contains(keyword.toLowerCase())) {
                found = true;
                System.out.println();
                System.out.println("  ============================================");
                System.out.println("            Guest Information");
                System.out.println("  ============================================");
                System.out.printf ("  Guest ID    : %s%n", g.getGuestId());
                System.out.printf ("  Name        : %s%n", g.getName());
                System.out.printf ("  IC / Pass   : %s%n", g.getIcNo());
                System.out.printf ("  Phone       : %s%n", g.getPhone());
                System.out.printf ("  Email       : %s%n", g.getEmail());
                System.out.printf ("  Nationality : %s%n", g.getNationality());
                System.out.println("  --- Reservations ---");
                boolean hasRes = false;
                for (int j = 0; j < reservationList.size(); j++) {
                    Reservation r = reservationList.get(j);
                    if (r.getGuestId().equals(g.getGuestId())) {
                        System.out.printf("  %-10s  Room:%-6s  %s -> %s  RM%.2f  [%s]%n",
                                r.getReservationId(), r.getRoomNo(),
                                r.getCheckInDate().format(DATE_FMT),
                                r.getCheckOutDate().format(DATE_FMT),
                                r.getTotalAmount(), r.getStatus());
                        hasRes = true;
                    }
                }
                if (!hasRes) System.out.println("  No reservations.");
                System.out.println("  ============================================");
            }
        }
        if (!found) System.out.println("  [!] No guest found for: " + keyword);
        pressEnterToContinue();
    }

    // ── 4. Room Information ──────────────────────────────────────
    private void roomInformation() {
        printHeader("Room Information");
        System.out.printf("  %-8s %-12s %-14s %-9s %-20s%n",
                "Room No", "Type", "Price/Night", "Capacity", "Status");
        printDivider();
        for (int i = 0; i < roomList.size(); i++) {
            System.out.println("  " + roomList.get(i));
        }
        System.out.println();

        int available = 0, occupied = 0, maintenance = 0;
        for (int i = 0; i < roomList.size(); i++) {
            Room.RoomStatus s = roomList.get(i).getStatus();
            if (s == Room.RoomStatus.AVAILABLE)          available++;
            else if (s == Room.RoomStatus.OCCUPIED)      occupied++;
            else if (s == Room.RoomStatus.UNDER_MAINTENANCE) maintenance++;
        }
        System.out.println("  Total Rooms    : " + roomList.size());
        System.out.printf ("  Available      : %d%n", available);
        System.out.printf ("  Occupied       : %d%n", occupied);
        System.out.printf ("  Maintenance    : %d%n", maintenance);
        pressEnterToContinue();
    }

    // ── 5. Billing & Payment ─────────────────────────────────────
    private void billingAndPayment() {
        printHeader("Billing & Payment");
        System.out.print("  Enter Reservation ID: ");
        String resId = sc.nextLine().trim().toUpperCase();
        Reservation res = findReservation(resId);
        if (res == null) {
            System.out.println("  [!] Reservation not found.");
            pressEnterToContinue();
            return;
        }

        Guest guest = findGuest(res.getGuestId());
        Room  room  = findRoom(res.getRoomNo());

        System.out.println();
        System.out.println("  ============================================");
        System.out.println("            TARUMT Resort - Invoice");
        System.out.println("  ============================================");
        System.out.printf ("  Reservation ID : %s%n", res.getReservationId());
        System.out.printf ("  Guest Name     : %s%n", guest != null ? guest.getName() : res.getGuestId());
        System.out.printf ("  Room           : %s (%s)%n", res.getRoomNo(),
                room != null ? room.getRoomType() : "N/A");
        System.out.printf ("  Check-In       : %s%n", res.getCheckInDate().format(DATE_FMT));
        System.out.printf ("  Check-Out      : %s%n", res.getCheckOutDate().format(DATE_FMT));
        System.out.printf ("  Nights         : %d%n", res.getNumNights());
        System.out.printf ("  Rate/Night     : RM %.2f%n", room != null ? room.getPricePerNight() : 0.0);
        System.out.println("  --------------------------------------------");
        System.out.printf ("  TOTAL DUE      : RM %.2f%n", res.getTotalAmount());
        System.out.println("  ============================================");

        System.out.println();
        System.out.println("  Payment Method:");
        System.out.println("  1. Cash");
        System.out.println("  2. Credit / Debit Card");
        System.out.println("  3. Online Transfer");
        System.out.print("  Select payment method: ");
        String method = sc.nextLine().trim();
        String methodName;
        switch (method) {
            case "1": methodName = "Cash";                break;
            case "2": methodName = "Credit / Debit Card"; break;
            case "3": methodName = "Online Transfer";     break;
            default:  methodName = "Unknown";
        }
        System.out.println();
        System.out.println("  Payment of RM " + String.format("%.2f", res.getTotalAmount())
                + " via " + methodName + " recorded.");
        System.out.println("  Receipt issued. Thank you!");
        pressEnterToContinue();
    }

    // ── 6. Update Guest Information ──────────────────────────────
    private void updateGuestInformation() {
        printHeader("Update Guest Information");
        System.out.print("  Enter Guest ID to update: ");
        String guestId = sc.nextLine().trim().toUpperCase();
        Guest guest = findGuest(guestId);
        if (guest == null) {
            System.out.println("  [!] Guest not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println("  Current Name        : " + guest.getName());
        System.out.println("  Current Phone       : " + guest.getPhone());
        System.out.println("  Current Email       : " + guest.getEmail());
        System.out.println("  Current Nationality : " + guest.getNationality());
        System.out.println("  (Press Enter to keep current value)");
        System.out.println();

        System.out.print("  New Name        : ");
        String name = sc.nextLine().trim();
        System.out.print("  New Phone       : ");
        String phone = sc.nextLine().trim();
        System.out.print("  New Email       : ");
        String email = sc.nextLine().trim();
        System.out.print("  New Nationality : ");
        String nat = sc.nextLine().trim();

        if (!name.isEmpty())  guest.setName(name);
        if (!phone.isEmpty()) guest.setPhone(phone);
        if (!email.isEmpty()) guest.setEmail(email);
        if (!nat.isEmpty())   guest.setNationality(nat);

        System.out.println();
        System.out.println("  Guest information updated successfully!");
        pressEnterToContinue();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Reservation findReservation(String resId) {
        for (int i = 0; i < reservationList.size(); i++) {
            if (reservationList.get(i).getReservationId().equalsIgnoreCase(resId))
                return reservationList.get(i);
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

    private Guest findGuest(String guestId) {
        for (int i = 0; i < guestList.size(); i++) {
            if (guestList.get(i).getGuestId().equalsIgnoreCase(guestId))
                return guestList.get(i);
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
