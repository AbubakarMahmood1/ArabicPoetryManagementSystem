package com.arabicpoetry.presentation.fx;

import com.arabicpoetry.bll.service.AuthenticationService;
import com.arabicpoetry.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * Controller for the JavaFX login view.
 */
public class LoginController {
    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private final AuthenticationService authService;

    public LoginController() {
        try {
            this.authService = AuthenticationService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize AuthenticationService", e);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Please enter username and password");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                showAlert(AlertType.INFORMATION, "Login Successful", "Welcome, " + user.getFullName() + "!");
                openMainWindow(user);
            } else {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception ex) {
            LOGGER.error("Login error", ex);
            showAlert(AlertType.ERROR, "Error", "Database error: " + ex.getMessage());
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage mainStage = new Stage();
            mainStage.setTitle("Arabic Poetry Management System - " + user.getFullName());
            mainStage.setScene(new Scene(root, 800, 600));
            mainStage.show();

            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception ex) {
            LOGGER.error("Failed to open main window", ex);
            showAlert(AlertType.ERROR, "Error", "Unable to open main window: " + ex.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
