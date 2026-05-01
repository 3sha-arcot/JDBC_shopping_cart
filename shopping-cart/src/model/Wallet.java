package model;

public class Wallet {
    private int id;
    private int userId;
    private double balance;

    public Wallet(int id, int userId, double balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getBalance() { return balance; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setBalance(double balance) { this.balance = balance; }
}
