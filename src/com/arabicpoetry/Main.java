package com.arabicpoetry;

import com.arabicpoetry.presentation.ui.LoginFrame;
import com.arabicpoetry.util.DatabaseConnection;

import java.sql.SQLException;

import javax.swing.*;

/**
 * Main entry point for Arabic Poetry Management System
 */
public class Main {
    public static void main(String[] args) {
        // Test database connection
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        if (!dbConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Database connection failed!\n" +
                            "Please check your MySQL server and configuration in config.properties",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the application with login screen
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = null;
			try {
				loginFrame = new LoginFrame();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            loginFrame.setVisible(true);
        });
    }
}
