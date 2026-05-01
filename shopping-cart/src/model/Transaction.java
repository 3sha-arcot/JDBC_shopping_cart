package model;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int userId;
    private double totalAmount;
    private Timestamp transactionDate;

    public Transaction(int id, int userId, double totalAmount, Timestamp transactionDate) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public Timestamp getTransactionDate() { return transactionDate; }
}
