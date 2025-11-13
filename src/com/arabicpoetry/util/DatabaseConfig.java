package com.arabicpoetry.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Singleton class for managing database configuration
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    // Private constructor for Singleton pattern
    private DatabaseConfig() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Load properties from file
     */
    private void loadProperties() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            // If file doesn't exist, create default properties
            createDefaultProperties();
        }
    }

    /**
     * Create default configuration file
     */
    private void createDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/arabic_poetry_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Arabic Poetry Management System - Database Configuration");
        } catch (IOException io) {
            System.err.println("Error creating default configuration file: " + io.getMessage());
        }
    }

    /**
     * Get property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Set property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Save properties to file
     */
    public void saveProperties() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Arabic Poetry Management System - Database Configuration");
        } catch (IOException io) {
            System.err.println("Error saving configuration file: " + io.getMessage());
        }
    }

    // Convenience methods
    public String getDbUrl() {
        return getProperty("db.url");
    }

    public String getDbUsername() {
        return getProperty("db.username");
    }

    public String getDbPassword() {
        return getProperty("db.password");
    }

    public String getDbDriver() {
        return getProperty("db.driver");
    }
}
