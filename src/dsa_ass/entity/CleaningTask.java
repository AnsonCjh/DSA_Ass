package dsa_ass.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * CleaningTask entity - used by Housekeeping module
 */
public class CleaningTask {

    public enum TaskStatus   { PENDING, IN_PROGRESS, COMPLETED }
    public enum TaskPriority { LOW, MEDIUM, HIGH }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private String        taskId;
    private String        roomNo;
    private String        assignedStaff;
    private TaskPriority  priority;
    private TaskStatus    status;
    private LocalDate     assignedDate;
    private String        remarks;
    private String        updatedTime;

    public CleaningTask(String taskId, String roomNo, String assignedStaff,
                        TaskPriority priority, LocalDate assignedDate, String remarks, String updatedTime) {
        this.taskId        = taskId;
        this.roomNo        = roomNo;
        this.assignedStaff = assignedStaff;
        this.priority      = priority;
        this.status        = TaskStatus.PENDING;
        this.assignedDate  = assignedDate;
        this.remarks       = remarks;
        this.updatedTime   = (updatedTime != null && !updatedTime.trim().isEmpty())
                           ? updatedTime
                           : LocalTime.now().format(TIME_FMT);
    }

    public CleaningTask(String taskId, String roomNo, String assignedStaff,
                        TaskPriority priority, LocalDate assignedDate, String remarks) {
        this(taskId, roomNo, assignedStaff, priority, assignedDate, remarks,
             LocalTime.now().format(TIME_FMT));
    }

    // ── Getters ────────────────────────────────────────────────
    public String       getTaskId()       { return taskId; }
    public String       getRoomNo()       { return roomNo; }
    public String       getAssignedStaff(){ return assignedStaff; }
    public TaskPriority getPriority()     { return priority; }
    public TaskStatus   getStatus()       { return status; }
    public LocalDate    getAssignedDate() { return assignedDate; }
    public String       getRemarks()      { return remarks; }
    public String       getUpdatedTime()  { return updatedTime != null ? updatedTime : "09:00"; }

    // ── Setters ────────────────────────────────────────────────
    public void setStatus(TaskStatus status)          {
        this.status = status;
        this.updatedTime = LocalTime.now().format(TIME_FMT);
    }
    public void setAssignedStaff(String staff)        { this.assignedStaff = staff; }
    public void setPriority(TaskPriority priority)    { this.priority = priority; }
    public void setRemarks(String remarks)            { this.remarks = remarks; }
    public void setUpdatedTime(String updatedTime)    { this.updatedTime = updatedTime; }

    @Override
    public String toString() {
        return String.format("%-10s %-8s %-15s %-8s %-14s %-12s %-8s %s",
                taskId, roomNo, assignedStaff, priority, status, assignedDate, getUpdatedTime(), remarks);
    }
}
