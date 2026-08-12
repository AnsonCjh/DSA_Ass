package dsa_ass.entity;

/**
 * Guest entity - stores guest personal details
 */
public class Guest {

    private String guestId;
    private String name;
    private String icNo;
    private String phone;
    private String email;
    private String nationality;

    public Guest(String guestId, String name, String icNo,
                 String phone, String email, String nationality) {
        this.guestId    = guestId;
        this.name       = name;
        this.icNo       = icNo;
        this.phone      = phone;
        this.email      = email;
        this.nationality = nationality;
    }

    // ── Getters ────────────────────────────────────────────────
    public String getGuestId()     { return guestId; }
    public String getName()        { return name; }
    public String getIcNo()        { return icNo; }
    public String getPhone()       { return phone; }
    public String getEmail()       { return email; }
    public String getNationality() { return nationality; }

    // ── Setters ────────────────────────────────────────────────
    public void setName(String name)               { this.name = name; }
    public void setIcNo(String icNo)               { this.icNo = icNo; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setEmail(String email)             { this.email = email; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-15s %-13s %-25s %-12s",
                guestId, name, icNo, phone, email, nationality);
    }
}
