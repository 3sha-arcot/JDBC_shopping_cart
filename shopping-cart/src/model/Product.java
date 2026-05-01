package model;

import java.sql.Date;

public class Product {
    private int id;
    private String name;
    private double price;
    private int stock;
    private Date expiryDate;
    private boolean isActive;

    public Product(int id, String name, double price, int stock, Date expiryDate, boolean isActive) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public Date getExpiryDate() { return expiryDate; }
    public boolean isActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}
