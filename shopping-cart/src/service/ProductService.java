package service;

import config.DBConnection;
import model.Product;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    public void addProduct(String name, double price, int stock, Date expiryDate) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Product name cannot be empty.");
            return;
        }
        if (price < 0 || stock < 0) {
            System.out.println("Price and stock cannot be negative.");
            return;
        }

        String query = "INSERT INTO product (name, price, stock, expiry_date, is_active) VALUES (?, ?, ?, ?, true)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setDate(4, expiryDate);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product added successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    public void modifyProduct(int productId, int newStock, Date newExpiryDate) {
        if (newStock < 0) {
            System.out.println("Stock cannot be negative.");
            return;
        }

        String query = "UPDATE product SET stock = ?, expiry_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, newStock);
            stmt.setDate(2, newExpiryDate);
            stmt.setInt(3, productId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product modified successfully.");
            } else {
                System.out.println("Product not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error modifying product: " + e.getMessage());
        }
    }

    public void deleteProduct(int productId) {
        // Soft delete using is_active
        String query = "UPDATE product SET is_active = false WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Product deleted successfully.");
            } else {
                System.out.println("Product not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }

    public List<Product> getActiveAvailableProducts() {
        List<Product> products = new ArrayList<>();
        // ONLY show stock > 0 AND not expired AND active
        String query = "SELECT * FROM product WHERE is_active = true AND stock > 0 AND (expiry_date IS NULL OR expiry_date >= CURRENT_DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching available products: " + e.getMessage());
        }
        return products;
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("stock"),
            rs.getDate("expiry_date"),
            rs.getBoolean("is_active")
        );
    }
}
