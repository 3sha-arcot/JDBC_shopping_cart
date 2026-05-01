package service;

import config.DBConnection;
import model.WalletRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WalletRequestService {

    public void requestMoney(int userId, double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        String query = "INSERT INTO wallet_request (user_id, amount, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Wallet request submitted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error requesting money: " + e.getMessage());
        }
    }

    public List<WalletRequest> getPendingRequests() {
        List<WalletRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM wallet_request WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                requests.add(new WalletRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending requests: " + e.getMessage());
        }
        return requests;
    }

    public void handleRequest(int requestId, boolean approve) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get request details
            String selectRequest = "SELECT * FROM wallet_request WHERE id = ? AND status = 'PENDING' FOR UPDATE";
            PreparedStatement selectStmt = conn.prepareStatement(selectRequest);
            selectStmt.setInt(1, requestId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Invalid WalletRequest ID or request is not pending.");
                conn.rollback();
                return;
            }

            int userId = rs.getInt("user_id");
            double amount = rs.getDouble("amount");
            String newStatus = approve ? "APPROVED" : "REJECTED";

            // 2. Update request status
            String updateRequest = "UPDATE wallet_request SET status = ? WHERE id = ?";
            PreparedStatement updateReqStmt = conn.prepareStatement(updateRequest);
            updateReqStmt.setString(1, newStatus);
            updateReqStmt.setInt(2, requestId);
            updateReqStmt.executeUpdate();

            // 3. If approved, add money to wallet and log transaction
            if (approve) {
                // Get wallet id and update balance
                String getWallet = "SELECT id FROM wallet WHERE user_id = ? FOR UPDATE";
                PreparedStatement getWalletStmt = conn.prepareStatement(getWallet);
                getWalletStmt.setInt(1, userId);
                ResultSet walletRs = getWalletStmt.executeQuery();
                
                if (walletRs.next()) {
                    int walletId = walletRs.getInt("id");
                    
                    String updateWallet = "UPDATE wallet SET balance = balance + ? WHERE id = ?";
                    PreparedStatement updateWalletStmt = conn.prepareStatement(updateWallet);
                    updateWalletStmt.setDouble(1, amount);
                    updateWalletStmt.setInt(2, walletId);
                    updateWalletStmt.executeUpdate();
                    
                    String insertWalletTx = "INSERT INTO wallet_transaction (wallet_id, type, amount) VALUES (?, 'CREDIT', ?)";
                    PreparedStatement insertWalletTxStmt = conn.prepareStatement(insertWalletTx);
                    insertWalletTxStmt.setInt(1, walletId);
                    insertWalletTxStmt.setDouble(2, amount);
                    insertWalletTxStmt.executeUpdate();
                } else {
                    throw new SQLException("Wallet not found for user.");
                }
            }

            conn.commit();
            System.out.println("Wallet request " + newStatus.toLowerCase() + " successfully.");

        } catch (SQLException e) {
            System.out.println("Error handling wallet request: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
}
