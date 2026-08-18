package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.boundary.FrontDeskBoundary;
import dsa_ass.control.FrontDeskControl;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import java.util.Scanner;

/**
 * @deprecated Refactored to ECB architecture:
 * Use {@link dsa_ass.control.FrontDeskControl} and {@link dsa_ass.boundary.FrontDeskBoundary}.
 */
@Deprecated
public class FrontDeskModule {

    private final FrontDeskBoundary boundary;

    public FrontDeskModule(Queue<Guest> guestList,
                           BinarySearchTree<Reservation> reservationList,
                           Queue<Room> roomList,
                           Stack<CleaningTask> taskList,
                           Queue<Guest> walkInQueue,
                           Scanner sc) {
        FrontDeskControl control = new FrontDeskControl(
                guestList, reservationList, roomList, taskList, walkInQueue);
        this.boundary = new FrontDeskBoundary(control, sc);
    }

    public void showMenu() {
        boundary.showMenu();
    }
}
