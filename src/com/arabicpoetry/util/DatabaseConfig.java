package com.arabicpoetry.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton class for managing database configuration
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Properties properties;
    private static String configFilePath = null; // lazily resolved, allows overriding for tests
    private static final Logger LOGGER = LogManager.getLogger(DatabaseConfig.class);

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
     * Override the configuration file path before the singleton is created.
     * Useful for pointing tests to a dedicated test properties file.
     */
    public static synchronized void useConfigFile(String path) {
        if (instance != null) {
            throw new IllegalStateException("DatabaseConfig already initialized; call useConfigFile before first access.");
        }
        configFilePath = path;
    }

    /**
     * Reset the singleton so the next call reads fresh properties (primarily for test scenarios).
     */
    public static synchronized void reset() {
        instance = null;
    }

    /**
     * Load properties from file
     */
    private void loadProperties() {
        String resolvedPath = resolveConfigPath();
        LOGGER.info("Loading database configuration from: {}", resolvedPath);
        try {
            LOGGER.info("Runtime properties: user.dir='{}', db.config.file='{}'",
                System.getProperty("user.dir"),
                System.getProperty("db.config.file")
            );
        } catch (Exception ignored) {
            // best effort logging
        }
        try {
            File file = new File(resolvedPath);
            LOGGER.info("Config file exists={}, sizeBytes={}", file.exists(), file.exists() ? file.length() : -1);
        } catch (Exception ignored) {
            // best effort logging
        }
        try (InputStream input = new FileInputStream(resolvedPath)) {
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.warn("Failed to read config.properties at '{}'; creating defaults.", resolvedPath, ex);
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

        String resolvedPath = resolveConfigPath();
        try (OutputStream output = new FileOutputStream(resolvedPath)) {
            properties.store(output, "Arabic Poetry Management System - Database Configuration");
            LOGGER.warn("Wrote default database configuration to: {}", resolvedPath);
        } catch (IOException io) {
            LOGGER.error("Error creating default configuration file at {}", resolvedPath, io);
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
        String resolvedPath = resolveConfigPath();
        try (OutputStream output = new FileOutputStream(resolvedPath)) {
            properties.store(output, "Arabic Poetry Management System - Database Configuration");
        } catch (IOException io) {
            LOGGER.error("Error saving configuration file at {}", resolvedPath, io);
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

    private String resolveConfigPath() {
        if (configFilePath != null && !configFilePath.trim().isEmpty()) {
            return configFilePath;
        }
        // Allow override via system property or env variable for flexible test/prod selection
        String fromSystemProperty = System.getProperty("db.config.file");
        if (fromSystemProperty != null && !fromSystemProperty.trim().isEmpty()) {
            configFilePath = fromSystemProperty;
            return configFilePath;
        }
        String fromEnv = System.getenv("DB_CONFIG_FILE");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            configFilePath = fromEnv;
            return configFilePath;
        }
        configFilePath = "config.properties";
        return configFilePath;
    }
}
