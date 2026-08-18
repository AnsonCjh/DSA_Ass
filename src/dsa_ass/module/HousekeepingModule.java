package dsa_ass.module;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.boundary.HousekeepingBoundary;
import dsa_ass.control.HousekeepingControl;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import java.util.Scanner;

/**
 * @deprecated Refactored to ECB architecture:
 * Use {@link dsa_ass.control.HousekeepingControl} and {@link dsa_ass.boundary.HousekeepingBoundary}.
 */
@Deprecated
public class HousekeepingModule {

    private final HousekeepingBoundary boundary;

    public static void setTaskCounter(int n) { HousekeepingControl.setTaskCounter(n); }
    public static String generateTaskId(Stack<CleaningTask> taskList) {
        return HousekeepingControl.generateTaskId(taskList);
    }

    public HousekeepingModule(Stack<CleaningTask> taskList,
                              Queue<Room> roomList,
                              BinarySearchTree<Reservation> reservationList,
                              Queue<Guest> guestList,
                              Stack<RoomStatusLog> statusHistoryStack,
                              Scanner sc) {
        HousekeepingControl control = new HousekeepingControl(
                taskList, roomList, reservationList, guestList, statusHistoryStack);
        this.boundary = new HousekeepingBoundary(control, sc);
    }

    public void showMenu() {
        boundary.showMenu();
    }
}
