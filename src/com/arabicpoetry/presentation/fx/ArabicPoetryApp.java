package com.arabicpoetry.presentation.fx;

import com.arabicpoetry.util.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX entry point for the application.
 */
public class ArabicPoetryApp extends Application {
    private static final Logger LOGGER = LogManager.getLogger(ArabicPoetryApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!DatabaseConnection.getInstance().testConnection()) {
                showErrorAndExit("Database connection failed! Please check your MySQL server and configuration in config.properties.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/LoginView.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Arabic Poetry Management System - Login");
            primaryStage.setScene(new Scene(root, 400, 250));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            LOGGER.error("Failed to start JavaFX application", ex);
            showErrorAndExit("Unable to start the application: " + ex.getMessage());
        }
    }

    private void showErrorAndExit(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }
}
