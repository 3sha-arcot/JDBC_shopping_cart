package model;

import java.sql.Timestamp;

public class WalletRequest {
    private int id;
    private int userId;
    private double amount;
    private String status; // PENDING, APPROVED, REJECTED
    private Timestamp requestDate;

    public WalletRequest(int id, int userId, double amount, String status, Timestamp requestDate) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.requestDate = requestDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public Timestamp getRequestDate() { return requestDate; }

    public void setStatus(String status) { this.status = status; }
}
