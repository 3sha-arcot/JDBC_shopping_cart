package model;

public class TransactionItem {
    private int id;
    private int transactionId;
    private int productId;
    private int quantity;
    private double price;
    
    // Extra transient field for product name
    private String productName;

    public TransactionItem(int id, int transactionId, int productId, int quantity, double price) {
        this.id = id;
        this.transactionId = transactionId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public int getTransactionId() { return transactionId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
