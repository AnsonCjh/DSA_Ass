package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.boundary.WalkInRegistrationBoundary;
import dsa_ass.control.WalkInRegistrationControl;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import java.util.Scanner;

/**
 * @deprecated Refactored to ECB architecture:
 * Use {@link dsa_ass.control.WalkInRegistrationControl} and {@link dsa_ass.boundary.WalkInRegistrationBoundary}.
 */
@Deprecated
public class WalkInRegistrationModule {

    private final WalkInRegistrationBoundary boundary;

    public static void setGuestCounter(int n) { WalkInRegistrationControl.setGuestCounter(n); }
    public static void setResCounter(int n)   { WalkInRegistrationControl.setResCounter(n); }

    public WalkInRegistrationModule(Queue<Guest> guestList,
                                   BinarySearchTree<Reservation> reservationList,
                                   Queue<Room> roomList,
                                   Queue<Guest> walkInQueue,
                                   Queue<String> walkInResIds,
                                   Scanner sc) {
        WalkInRegistrationControl control = new WalkInRegistrationControl(
                guestList, reservationList, roomList, walkInQueue, walkInResIds);
        this.boundary = new WalkInRegistrationBoundary(control, sc);
    }

    public void showMenu() {
        boundary.showMenu();
    }
}
