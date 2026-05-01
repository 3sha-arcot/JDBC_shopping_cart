package model;

public class CartItem {
    private int id;
    private int cartId;
    private int productId;
    private int quantity;
    // Extra transient fields for display purposes
    private String productName;
    private double price;

    public CartItem(int id, int cartId, int productId, int quantity) {
        this.id = id;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public int getCartId() { return cartId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public void setId(int id) { this.id = id; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
