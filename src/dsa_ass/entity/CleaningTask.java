package dsa_ass.entity;

import java.time.LocalDate;

/**
 * CleaningTask entity - used by Housekeeping module
 */
public class CleaningTask {

    public enum TaskStatus   { PENDING, IN_PROGRESS, COMPLETED }
    public enum TaskPriority { LOW, MEDIUM, HIGH }

    private String        taskId;
    private String        roomNo;
    private String        assignedStaff;
    private TaskPriority  priority;
    private TaskStatus    status;
    private LocalDate     assignedDate;
    private String        remarks;

    public CleaningTask(String taskId, String roomNo, String assignedStaff,
                        TaskPriority priority, LocalDate assignedDate, String remarks) {
        this.taskId       = taskId;
        this.roomNo       = roomNo;
        this.assignedStaff = assignedStaff;
        this.priority     = priority;
        this.status       = TaskStatus.PENDING;
        this.assignedDate = assignedDate;
        this.remarks      = remarks;
    }

    // ── Getters ────────────────────────────────────────────────
    public String       getTaskId()       { return taskId; }
    public String       getRoomNo()       { return roomNo; }
    public String       getAssignedStaff(){ return assignedStaff; }
    public TaskPriority getPriority()     { return priority; }
    public TaskStatus   getStatus()       { return status; }
    public LocalDate    getAssignedDate() { return assignedDate; }
    public String       getRemarks()      { return remarks; }

    // ── Setters ────────────────────────────────────────────────
    public void setStatus(TaskStatus status)          { this.status = status; }
    public void setAssignedStaff(String staff)        { this.assignedStaff = staff; }
    public void setPriority(TaskPriority priority)    { this.priority = priority; }
    public void setRemarks(String remarks)            { this.remarks = remarks; }

    @Override
    public String toString() {
        return String.format("%-10s %-8s %-15s %-8s %-12s %-12s %s",
                taskId, roomNo, assignedStaff, priority, status, assignedDate, remarks);
    }
}
