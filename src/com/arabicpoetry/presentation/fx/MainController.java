package com.arabicpoetry.presentation.fx;

import com.arabicpoetry.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the JavaFX main window.
 * Currently provides placeholder handlers until individual screens are fully ported.
 */
public class MainController {
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    @FXML
    private Label userLabel;

    public void setCurrentUser(User user) {
        if (user != null) {
            userLabel.setText(user.getFullName());
        } else {
            userLabel.setText("");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void handleImport(ActionEvent event) {
        openImport();
    }

    @FXML
    private void handleManageBooks(ActionEvent event) {
        openBooks();
    }

    @FXML
    private void handleManagePoets(ActionEvent event) {
        openPoets();
    }

    @FXML
    private void handleManagePoems(ActionEvent event) {
        openPoems();
    }

    @FXML
    private void handleManageVerses(ActionEvent event) {
        openVerses();
    }

    @FXML
    private void handleLinguistics(ActionEvent event) {
        openLinguistics();
    }

    @FXML
    private void handleSimilarity(ActionEvent event) {
        openSimilarity();
    }

    @FXML
    private void handleFrequency(ActionEvent event) {
        openFrequency();
    }

    @FXML
    private void handleIndex(ActionEvent event) {
        openIndex();
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Arabic Poetry Management System");
        alert.setContentText("JavaFX UI is the primary interface. All management, analysis, and import screens are available here.");
        alert.showAndWait();
    }

    private void openBooks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/book/BookManagementView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Manage Books");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Book Management", ex);
            showError("Unable to open Book Management: " + ex.getMessage());
        }
    }

    private void openPoets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/poet/PoetManagementView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Manage Poets");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Poet Management", ex);
            showError("Unable to open Poet Management: " + ex.getMessage());
        }
    }

    private void openPoems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/poem/PoemManagementView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Manage Poems");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Poem Management", ex);
            showError("Unable to open Poem Management: " + ex.getMessage());
        }
    }

    private void openVerses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/verse/VerseManagementView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Manage Verses");
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Verse Management", ex);
            showError("Unable to open Verse Management: " + ex.getMessage());
        }
    }

    private void openSimilarity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/VerseSimilarityView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Verse Similarity Search");
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Verse Similarity", ex);
            showError("Unable to open Verse Similarity: " + ex.getMessage());
        }
    }

    private void openFrequency() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/FrequencyAnalysisView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Frequency Analysis");
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Frequency Analysis", ex);
            showError("Unable to open Frequency Analysis: " + ex.getMessage());
        }
    }

    private void openIndex() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/BookIndexView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Book Index Generator");
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Book Index", ex);
            showError("Unable to open Book Index: " + ex.getMessage());
        }
    }

    private void openLinguistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/LinguisticWorkbenchView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Linguistic Workbench");
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Linguistic Workbench", ex);
            showError("Unable to open Linguistic Workbench: " + ex.getMessage());
        }
    }

    private void openImport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/importer/ImportView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Import Poems from File");
            stage.setScene(new Scene(root, 700, 400));
            stage.show();
        } catch (Exception ex) {
            LOGGER.error("Unable to open Import", ex);
            showError("Unable to open Import: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
