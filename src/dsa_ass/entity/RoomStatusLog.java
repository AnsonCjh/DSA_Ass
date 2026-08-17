package dsa_ass.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RoomStatusLog entity - records room status state transitions
 * Used by Housekeeping Linear ADT (Stack) for Undo and Status History
 */
public class RoomStatusLog {

    private final String roomNo;
    private final Room.RoomStatus previousStatus;
    private final Room.RoomStatus newStatus;
    private final LocalDateTime timestamp;
    private final String remarks;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public RoomStatusLog(String roomNo, Room.RoomStatus previousStatus, Room.RoomStatus newStatus, String remarks) {
        this.roomNo         = roomNo;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
        this.timestamp      = LocalDateTime.now();
        this.remarks        = remarks != null && !remarks.isEmpty() ? remarks : "Status update";
    }

    public String          getRoomNo()         { return roomNo; }
    public Room.RoomStatus getPreviousStatus() { return previousStatus; }
    public Room.RoomStatus getNewStatus()      { return newStatus; }
    public LocalDateTime   getTimestamp()      { return timestamp; }
    public String          getRemarks()        { return remarks; }

    @Override
    public String toString() {
        return String.format("[%s] Room %-6s : %-20s -> %-20s (%s)",
                timestamp.format(FMT), roomNo, previousStatus, newStatus, remarks);
    }
}
