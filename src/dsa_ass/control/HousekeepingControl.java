package dsa_ass.control;

import dsa_ass.adt.BinarySearchTree;
import dsa_ass.adt.Queue;
import dsa_ass.adt.Stack;
import dsa_ass.entity.CleaningTask;
import dsa_ass.entity.Guest;
import dsa_ass.entity.Reservation;
import dsa_ass.entity.Room;
import dsa_ass.entity.RoomStatusLog;
import dsa_ass.util.DataStore;
import java.time.LocalDate;

/**
 * Control: HousekeepingControl
 *
 * Implements business logic for cleaning task management (Stack ADT - LIFO),
 * room status lifecycle, undo/history stack management, and task filtering.
 */
public class HousekeepingControl {

    private final Stack<CleaningTask>           taskList;
    private final Queue<Room>                   roomList;
    private final BinarySearchTree<Reservation> reservationList;
    private final Queue<Guest>                  guestList;
    private final Stack<RoomStatusLog>          statusHistoryStack;

    private static int taskCounter = 1;

    public static void setTaskCounter(int n) { taskCounter = n; }
    public static int  getTaskCounter()      { return taskCounter; }

    public HousekeepingControl(Stack<CleaningTask>           taskList,
                               Queue<Room>                   roomList,
                               BinarySearchTree<Reservation> reservationList,
                               Queue<Guest>                  guestList,
                               Stack<RoomStatusLog>          statusHistoryStack) {
        this.taskList           = taskList;
        this.roomList           = roomList;
        this.reservationList    = reservationList;
        this.guestList          = guestList;
        this.statusHistoryStack = (statusHistoryStack != null) ? statusHistoryStack : new Stack<RoomStatusLog>();
    }

    public HousekeepingControl(Stack<CleaningTask>           taskList,
                               Queue<Room>                   roomList,
                               BinarySearchTree<Reservation> reservationList,
                               Queue<Guest>                  guestList) {
        this(taskList, roomList, reservationList, guestList, null);
    }

    public HousekeepingControl(Stack<CleaningTask> taskList,
                               Queue<Room>         roomList) {
        this(taskList, roomList, null, null, null);
    }

    // ── Task ID Generation ───────────────────────────────────────

    public static String generateTaskId(Stack<CleaningTask> taskList) {
        int maxIdNum = 0;
        if (taskList != null) {
            for (int i = 0; i < taskList.size(); i++) {
                CleaningTask t = taskList.get(i);
                if (t != null && t.getTaskId() != null) {
                    int num = parseTrailingNum(t.getTaskId());
                    if (num > maxIdNum) maxIdNum = num;
                }
            }
        }
        int nextIdNum = Math.max(maxIdNum + 1, taskCounter);
        taskCounter = nextIdNum + 1;
        return String.format("T%04d", nextIdNum);
    }

    public String generateTaskId() {
        return generateTaskId(this.taskList);
    }

    // ── Cleaning Task Management (Stack ADT - LIFO) ──────────────

    public CleaningTask addCleaningTask(String roomNo, String staff, CleaningTask.TaskPriority priority, String remarks) {
        String taskId = generateTaskId();
        if (staff == null || staff.trim().isEmpty()) staff = "Unassigned";
        if (remarks == null || remarks.trim().isEmpty()) remarks = "Checkout Cleaning";
        CleaningTask task = new CleaningTask(taskId, roomNo, staff, priority, LocalDate.now(), remarks);
        taskList.push(task);
        autoSave();
        return task;
    }

    public Stack<CleaningTask> getTaskList() {
        return taskList;
    }

    public CleaningTask findTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) return null;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId))
                return taskList.get(i);
        }
        return null;
    }

    public boolean deleteTask(String taskId) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskId().equalsIgnoreCase(taskId)) {
                taskList.remove(i);
                autoSave();
                return true;
            }
        }
        return false;
    }

    // ── Room & Status Management ─────────────────────────────────

    public Queue<Room> getRoomList() {
        return roomList;
    }

    public Room findRoom(String roomNo) {
        if (roomNo == null || roomNo.isEmpty()) return null;
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomNo().equalsIgnoreCase(roomNo))
                return roomList.get(i);
        }
        return null;
    }

    public void logRoomStatus(String roomNo, Room.RoomStatus prev, Room.RoomStatus next, String remarks) {
        statusHistoryStack.push(new RoomStatusLog(roomNo, prev, next, remarks));
    }

    public void updateRoomStatus(Room room, Room.RoomStatus newStatus, String remarks) {
        Room.RoomStatus oldStatus = room.getStatus();
        room.setStatus(newStatus);
        logRoomStatus(room.getRoomNo(), oldStatus, newStatus, remarks);
        autoSave();
    }

    public Stack<RoomStatusLog> getStatusHistoryStack() {
        return statusHistoryStack;
    }

    public boolean undoLatestStatusUpdate() {
        if (statusHistoryStack.isEmpty()) return false;
        RoomStatusLog lastLog = statusHistoryStack.pop();
        Room room = findRoom(lastLog.getRoomNo());
        if (room != null) {
            room.setStatus(lastLog.getPreviousStatus());
            autoSave();
            return true;
        }
        return false;
    }

    public Stack<CleaningTask> filterTasks(CleaningTask.TaskStatus filter) {
        Stack<CleaningTask> filtered = new Stack<CleaningTask>();
        for (int i = 0; i < taskList.size(); i++) {
            CleaningTask t = taskList.get(i);
            if (filter == null || t.getStatus() == filter) {
                filtered.push(t);
            }
        }
        return filtered;
    }

    public void autoSave() {
        DataStore.saveTasks(taskList);
        DataStore.saveRooms(roomList);
    }

    private static int parseTrailingNum(String id) {
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
