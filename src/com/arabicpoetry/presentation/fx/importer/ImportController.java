package com.arabicpoetry.presentation.fx.importer;

import com.arabicpoetry.bll.service.ImportService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * JavaFX controller for importing poems from a file.
 */
public class ImportController {
    private static final Logger LOGGER = LogManager.getLogger(ImportController.class);

    @FXML
    private TextField fileField;
    @FXML
    private TextArea outputArea;
    @FXML
    private Button browseButton;
    @FXML
    private Button importButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;

    private final ImportService importService;

    public ImportController() {
        this(createImportService());
    }

    // Visible for tests
    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    private static ImportService createImportService() {
        try {
            return ImportService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize ImportService", e);
        }
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Poems File");
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            fileField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleImport(ActionEvent event) {
        String path = fileField.getText();
        if (path == null || path.isBlank()) {
            showInfo("Please choose a file to import.");
            return;
        }
        Path filePath = Path.of(path);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            showError("Selected path is not a readable file.");
            return;
        }

        runImport(filePath.toFile());
    }

    private void runImport(File file) {
        importButton.setDisable(true);
        browseButton.setDisable(true);
        outputArea.clear();
        statusLabel.textProperty().unbind();
        statusLabel.setText("Importing " + file.getName() + "...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Importing " + file.getName() + "...");
                updateMessage("Reading " + file.getName());
                return importService.importFromFile(file.getAbsolutePath());
            }
        };

        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            progressBar.setProgress(0);
            outputArea.setText(task.getValue());
            statusLabel.textProperty().unbind();
            statusLabel.setText("Import completed");
            importButton.setDisable(false);
            browseButton.setDisable(false);
            showInfo("Import completed.");
        });

        task.setOnFailed(e -> {
            progressBar.setProgress(0);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Import failed");
            importButton.setDisable(false);
            browseButton.setDisable(false);
            Throwable ex = task.getException();
            LOGGER.error("Import failed for file {}", file.getAbsolutePath(), ex);
            showError("Import failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
    }

    private Stage getStage() {
        return (Stage) importButton.getScene().getWindow();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
