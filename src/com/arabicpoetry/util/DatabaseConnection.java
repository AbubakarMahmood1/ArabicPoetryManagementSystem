package com.arabicpoetry.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton class for managing database connections
 * Implements the Singleton design pattern
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private DatabaseConfig config;
    private static final Logger LOGGER = LogManager.getLogger(DatabaseConnection.class);
    private static DatabaseConfig overrideConfig;

    // Private constructor for Singleton pattern
    private DatabaseConnection() {
        config = overrideConfig != null ? overrideConfig : DatabaseConfig.getInstance();
        try {
            // Load MySQL JDBC driver
            Class.forName(config.getDbDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.error("MySQL JDBC Driver not found: {}", config.getDbDriver(), e);
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
     * Point the connection factory at a specific DatabaseConfig (e.g., for tests) and reset any cached connection.
     */
    public static synchronized void configure(DatabaseConfig databaseConfig) {
        overrideConfig = databaseConfig;
        reset();
    }

    /**
     * Force the connection factory to reload using a given properties file (prod/test switch).
     */
    public static synchronized void useConfigFile(String path) {
        overrideConfig = null;
        DatabaseConfig.reset();
        DatabaseConfig.useConfigFile(path);
        reset();
    }

    /**
     * Reset the singleton/connection so the next request rebuilds from the latest configuration.
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.closeConnection();
        }
        instance = null;
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
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.error("Error closing database connection", e);
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
            LOGGER.error("Database connection test failed", e);
            return false;
        }
    }
}
