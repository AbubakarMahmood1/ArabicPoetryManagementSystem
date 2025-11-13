package com.arabicpoetry.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connections
 * Implements the Singleton design pattern
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private DatabaseConfig config;

    // Private constructor for Singleton pattern
    private DatabaseConnection() {
        config = DatabaseConfig.getInstance();
        try {
            // Load MySQL JDBC driver
            Class.forName(config.getDbDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get database connection
     * Creates new connection if it doesn't exist or is closed
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword()
            );
            // Set character encoding for Arabic support
            connection.createStatement().execute("SET NAMES 'utf8mb4'");
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
