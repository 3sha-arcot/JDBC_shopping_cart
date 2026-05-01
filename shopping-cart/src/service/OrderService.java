package service;

import config.DBConnection;
import model.CartItem;
import model.Transaction;
import model.TransactionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private InvoiceService invoiceService = new InvoiceService();
    private CartService cartService = new CartService();

    public void checkout(int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // CRITICAL: Start transaction

            // Step 1: Lock wallet row
            String walletQuery = "SELECT id, balance FROM wallet WHERE user_id = ? FOR UPDATE";
            PreparedStatement walletStmt = conn.prepareStatement(walletQuery);
            walletStmt.setInt(1, userId);
            ResultSet walletRs = walletStmt.executeQuery();

            if (!walletRs.next()) {
                throw new SQLException("Wallet not found.");
            }
            int walletId = walletRs.getInt("id");
            double currentBalance = walletRs.getDouble("balance");

            // Step 2: Fetch and lock cart items, calculate total
            int cartId = cartService.getCartIdForUser(userId);
            if (cartId == -1) {
                throw new SQLException("Cart not found.");
            }

            String cartQuery = "SELECT ci.product_id, ci.quantity, p.price, p.name, p.stock " +
                               "FROM cart_item ci " +
                               "JOIN product p ON ci.product_id = p.id " +
                               "WHERE ci.cart_id = ? FOR UPDATE"; // Locking products in cart
            PreparedStatement cartStmt = conn.prepareStatement(cartQuery);
            cartStmt.setInt(1, cartId);
            ResultSet cartRs = cartStmt.executeQuery();

            double totalAmount = 0.0;
            List<TransactionItem> itemsToOrder = new ArrayList<>();
            boolean hasItems = false;

            while (cartRs.next()) {
                hasItems = true;
                int productId = cartRs.getInt("product_id");
                int quantity = cartRs.getInt("quantity");
                double price = cartRs.getDouble("price");
                String name = cartRs.getString("name");
                int stock = cartRs.getInt("stock");

                if (stock < quantity) {
                    throw new SQLException("Insufficient stock for product: " + name);
                }

                totalAmount += (price * quantity);
                TransactionItem item = new TransactionItem(0, 0, productId, quantity, price);
                item.setProductName(name);
                itemsToOrder.add(item);
            }

            if (!hasItems) {
                throw new SQLException("Cart is empty. Checkout prevented.");
            }

            // Step 3: Check balance
            if (currentBalance < totalAmount || currentBalance == 0) {
                throw new SQLException("Insufficient wallet balance.");
            }

            // Step 4: Deduct wallet balance
            String updateWallet = "UPDATE wallet SET balance = balance - ? WHERE id = ?";
            PreparedStatement updateWalletStmt = conn.prepareStatement(updateWallet);
            updateWalletStmt.setDouble(1, totalAmount);
            updateWalletStmt.setInt(2, walletId);
            updateWalletStmt.executeUpdate();

            // Log wallet transaction
            String logWalletTx = "INSERT INTO wallet_transaction (wallet_id, type, amount) VALUES (?, 'DEBIT', ?)";
            PreparedStatement logWalletTxStmt = conn.prepareStatement(logWalletTx);
            logWalletTxStmt.setInt(1, walletId);
            logWalletTxStmt.setDouble(2, totalAmount);
            logWalletTxStmt.executeUpdate();

            // Step 5: Insert Transaction
            String insertTx = "INSERT INTO transaction (user_id, total_amount) VALUES (?, ?)";
            PreparedStatement insertTxStmt = conn.prepareStatement(insertTx, Statement.RETURN_GENERATED_KEYS);
            insertTxStmt.setInt(1, userId);
            insertTxStmt.setDouble(2, totalAmount);
            insertTxStmt.executeUpdate();

            ResultSet generatedKeys = insertTxStmt.getGeneratedKeys();
            int transactionId = -1;
            if (generatedKeys.next()) {
                transactionId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating transaction failed, no ID obtained.");
            }

            // Step 6 & 7: Insert TransactionItems and Reduce product stock
            String insertItem = "INSERT INTO transaction_item (transaction_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement insertItemStmt = conn.prepareStatement(insertItem);

            String updateStock = "UPDATE product SET stock = stock - ? WHERE id = ?";
            PreparedStatement updateStockStmt = conn.prepareStatement(updateStock);

            for (TransactionItem item : itemsToOrder) {
                insertItemStmt.setInt(1, transactionId);
                insertItemStmt.setInt(2, item.getProductId());
                insertItemStmt.setInt(3, item.getQuantity());
                insertItemStmt.setDouble(4, item.getPrice());
                insertItemStmt.executeUpdate();

                updateStockStmt.setInt(1, item.getQuantity());
                updateStockStmt.setInt(2, item.getProductId());
                updateStockStmt.executeUpdate();
            }

            // Step 8: Clear cart
            String clearCart = "DELETE FROM cart_item WHERE cart_id = ?";
            PreparedStatement clearCartStmt = conn.prepareStatement(clearCart);
            clearCartStmt.setInt(1, cartId);
            clearCartStmt.executeUpdate();

            // Step 9: Commit
            conn.commit();
            System.out.println("Checkout successful!");

            // Print invoice
            Transaction tx = new Transaction(transactionId, userId, totalAmount, null);
            invoiceService.printInvoice(tx, itemsToOrder);

        } catch (SQLException e) {
            System.out.println("Checkout failed: " + e.getMessage());
            try {
                if (conn != null) {
                    System.out.println("Rolling back transaction...");
                    conn.rollback();
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

    public void viewAllTransactionsAdmin() {
        String query = "SELECT t.id, u.username, t.total_amount, t.transaction_date " +
                       "FROM transaction t JOIN user u ON t.user_id = u.id ORDER BY t.transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("\n--- All Transactions ---");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                System.out.printf("Order ID: %d | User: %s | Total: $%.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("username"),
                        rs.getDouble("total_amount"), rs.getTimestamp("transaction_date"));
            }
            if (!hasTransactions) {
                System.out.println("No transactions found in the system.");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing transactions: " + e.getMessage());
        }
    }

    public void viewUserTransactions(int userId) {
        String query = "SELECT * FROM transaction WHERE user_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n--- Your Transactions ---");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                int txId = rs.getInt("id");
                double total = rs.getDouble("total_amount");
                System.out.printf("Order ID: %d | Total: $%.2f | Date: %s\n", txId, total, rs.getTimestamp("transaction_date"));
                
                // Fetch items for this transaction
                String itemQuery = "SELECT ti.quantity, ti.price, p.name FROM transaction_item ti " +
                                   "JOIN product p ON ti.product_id = p.id WHERE ti.transaction_id = ?";
                try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                    itemStmt.setInt(1, txId);
                    ResultSet itemRs = itemStmt.executeQuery();
                    while (itemRs.next()) {
                        System.out.printf("  - %s (Qty: %d, Price: $%.2f)\n", 
                                itemRs.getString("name"), itemRs.getInt("quantity"), itemRs.getDouble("price"));
                    }
                }
            }
            if (!hasTransactions) {
                System.out.println("You haven't made any transactions yet.");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing transactions: " + e.getMessage());
        }
    }
}
