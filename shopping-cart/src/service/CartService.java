package service;

import config.DBConnection;
import model.CartItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    public void addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }
        
        // First get the cart id for the user
        int cartId = getCartIdForUser(userId);
        if (cartId == -1) return;

        // Check if product exists, is active, not expired, and has enough stock
        if (!isProductAvailable(productId, quantity)) {
            System.out.println("Product is not available in the requested quantity or is expired.");
            return;
        }

        // Add or update cart item
        String query = "INSERT INTO cart_item (cart_id, product_id, quantity) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, cartId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Item added to cart.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding to cart: " + e.getMessage());
        }
    }

    public List<CartItem> viewCart(int userId) {
        List<CartItem> items = new ArrayList<>();
        int cartId = getCartIdForUser(userId);
        if (cartId == -1) return items;

        String query = "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, p.name, p.price " +
                       "FROM cart_item ci " +
                       "JOIN product p ON ci.product_id = p.id " +
                       "WHERE ci.cart_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, cartId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CartItem item = new CartItem(
                    rs.getInt("id"),
                    rs.getInt("cart_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity")
                );
                item.setProductName(rs.getString("name"));
                item.setPrice(rs.getDouble("price"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing cart: " + e.getMessage());
        }
        return items;
    }

    public void modifyCartItem(int userId, int productId, int newQuantity) {
        int cartId = getCartIdForUser(userId);
        if (cartId == -1) return;

        if (newQuantity <= 0) {
            deleteCartItem(userId, productId);
            return;
        }

        if (!isProductAvailable(productId, newQuantity)) {
            System.out.println("Product does not have enough stock.");
            return;
        }

        String query = "UPDATE cart_item SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, cartId);
            stmt.setInt(3, productId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cart item modified.");
            } else {
                System.out.println("Item not found in cart.");
            }
        } catch (SQLException e) {
            System.out.println("Error modifying cart item: " + e.getMessage());
        }
    }

    public void deleteCartItem(int userId, int productId) {
        int cartId = getCartIdForUser(userId);
        if (cartId == -1) return;

        String query = "DELETE FROM cart_item WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, cartId);
            stmt.setInt(2, productId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Item removed from cart.");
            } else {
                System.out.println("Item not found in cart.");
            }
        } catch (SQLException e) {
            System.out.println("Error removing cart item: " + e.getMessage());
        }
    }

    public int getCartIdForUser(int userId) {
        String query = "SELECT id FROM cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching cart: " + e.getMessage());
        }
        return -1;
    }

    private boolean isProductAvailable(int productId, int quantity) {
        String query = "SELECT stock FROM product WHERE id = ? AND is_active = true AND (expiry_date IS NULL OR expiry_date >= CURRENT_DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int stock = rs.getInt("stock");
                return stock >= quantity;
            }
        } catch (SQLException e) {
            System.out.println("Error checking product availability: " + e.getMessage());
        }
        return false;
    }
}
