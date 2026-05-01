package service;

import config.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public User login(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return false;
        }

        String checkQuery = "SELECT id FROM user WHERE username = ?";
        String insertQuery = "INSERT INTO user (username, password, role) VALUES (?, ?, 'CUSTOMER')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            
            // Check if username exists
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Username already exists.");
                return false;
            }
            
            // Register new customer
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            int rowsAffected = insertStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Also create a wallet and cart for the new user
                setupNewUserDependencies(username);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
        }
        return false;
    }

    private void setupNewUserDependencies(String username) {
        String findUserQuery = "SELECT id FROM user WHERE username = ?";
        String insertWalletQuery = "INSERT INTO wallet (user_id, balance) VALUES (?, 0.0)";
        String insertCartQuery = "INSERT INTO cart (user_id) VALUES (?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findUserQuery);
             PreparedStatement walletStmt = conn.prepareStatement(insertWalletQuery);
             PreparedStatement cartStmt = conn.prepareStatement(insertCartQuery)) {
            
            findStmt.setString(1, username);
            ResultSet rs = findStmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                
                walletStmt.setInt(1, userId);
                walletStmt.executeUpdate();
                
                cartStmt.setInt(1, userId);
                cartStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error setting up user dependencies: " + e.getMessage());
        }
    }
}
