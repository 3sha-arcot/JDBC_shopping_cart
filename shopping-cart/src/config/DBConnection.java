package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Set JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_cart_db?useSSL=false";

    // Database credentials
    static final String USER = "root";
    static final String PASSWORD = "root123";

    public static Connection getConnection() throws SQLException {
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage(), e);
        }
        // Open a connection
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
