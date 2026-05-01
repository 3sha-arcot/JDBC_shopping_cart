package model;

import java.sql.Timestamp;

public class WalletTransaction {
    private int id;
    private int walletId;
    private String type; // CREDIT or DEBIT
    private double amount;
    private Timestamp transactionDate;

    public WalletTransaction(int id, int walletId, String type, double amount, Timestamp transactionDate) {
        this.id = id;
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    public int getId() { return id; }
    public int getWalletId() { return walletId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public Timestamp getTransactionDate() { return transactionDate; }
}
