package dsa_ass.entity;

/**
 * Room entity - stores room details and availability status
 */
public class Room {

    public enum RoomType  { SINGLE, STANDARD, DELUXE, SUITE, VILLA }
    public enum RoomStatus { AVAILABLE, OCCUPIED, UNDER_MAINTENANCE }

    private String roomNo;
    private RoomType roomType;
    private double pricePerNight;
    private RoomStatus status;
    private int capacity;

    public Room(String roomNo, RoomType roomType, double pricePerNight, int capacity) {
        this.roomNo        = roomNo;
        this.roomType      = roomType;
        this.pricePerNight = pricePerNight;
        this.status        = RoomStatus.AVAILABLE;
        this.capacity      = capacity;
    }

    // ── Getters ────────────────────────────────────────────────
    public String     getRoomNo()        { return roomNo; }
    public RoomType   getRoomType()      { return roomType; }
    public double     getPricePerNight() { return pricePerNight; }
    public RoomStatus getStatus()        { return status; }
    public int        getCapacity()      { return capacity; }

    // ── Setters ────────────────────────────────────────────────
    public void setStatus(RoomStatus status)            { this.status = status; }
    public void setPricePerNight(double pricePerNight)  { this.pricePerNight = pricePerNight; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("%-8s %-12s RM%-10.2f %-9d %-20s",
                roomNo, roomType, pricePerNight, capacity, status);
    }
}
