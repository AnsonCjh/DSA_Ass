package dsa_ass.entity;

import java.time.LocalDate;

/**
 * Reservation entity - links a guest to a room with dates
 */
public class Reservation implements Comparable<Reservation> {

    public enum ReservationStatus { PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private String reservationId;
    private String confirmationNo;   // 8-digit confirmation number (e.g. "00000001")
    private String guestId;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private ReservationStatus status;
    private double totalAmount;

    public Reservation(String reservationId, String guestId, String roomNo,
                       LocalDate checkInDate, LocalDate checkOutDate, int numGuests, double totalAmount) {
        this.reservationId = reservationId;
        this.guestId       = guestId;
        this.roomNo        = roomNo;
        this.checkInDate   = checkInDate;
        this.checkOutDate  = checkOutDate;
        this.numGuests     = numGuests;
        this.totalAmount   = totalAmount;
        this.status        = ReservationStatus.PENDING;
        this.confirmationNo = "";   // set via setConfirmationNo() after construction
    }

    // ── Getters ────────────────────────────────────────────────
    public String             getReservationId()  { return reservationId; }
    public String             getConfirmationNo() { return confirmationNo; }
    public String             getGuestId()        { return guestId; }
    public String             getRoomNo()         { return roomNo; }
    public LocalDate          getCheckInDate()    { return checkInDate; }
    public LocalDate          getCheckOutDate()   { return checkOutDate; }
    public int                getNumGuests()      { return numGuests; }
    public ReservationStatus  getStatus()         { return status; }
    public double             getTotalAmount()    { return totalAmount; }

    // ── Setters ────────────────────────────────────────────────
    public void setConfirmationNo(String confirmationNo)  { this.confirmationNo = confirmationNo; }
    public void setStatus(ReservationStatus status)       { this.status = status; }
    public void setRoomNo(String roomNo)                  { this.roomNo = roomNo; }
    public void setCheckInDate(LocalDate checkInDate)     { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate)   { this.checkOutDate = checkOutDate; }
    public void setNumGuests(int numGuests)               { this.numGuests = numGuests; }
    public void setTotalAmount(double totalAmount)        { this.totalAmount = totalAmount; }

    public long getNumNights() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    public int compareTo(Reservation other) {
        if (other == null || other.reservationId == null) return 1;
        if (this.reservationId == null) return -1;
        return this.reservationId.compareToIgnoreCase(other.reservationId);
    }

    @Override
    public String toString() {
        return String.format("%-12s %-10s %-8s %-12s %-12s %-6d RM%-10.2f %-12s",
                reservationId, guestId, roomNo,
                checkInDate, checkOutDate,
                numGuests, totalAmount, status);
    }
}

