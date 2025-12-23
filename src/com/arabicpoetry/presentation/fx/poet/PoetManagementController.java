package com.arabicpoetry.presentation.fx.poet;

import com.arabicpoetry.bll.service.PoetService;
import com.arabicpoetry.model.Poet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.geometry.NodeOrientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for managing poets (port of the Swing PoetManagementFrame).
 */
public class PoetManagementController {
    private static final Logger LOGGER = LogManager.getLogger(PoetManagementController.class);

    @FXML
    private TextField nameField;
    @FXML
    private TextField birthYearField;
    @FXML
    private TextField deathYearField;
    @FXML
    private TextArea biographyArea;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Poet> poetTable;
    @FXML
    private TableColumn<Poet, Number> idColumn;
    @FXML
    private TableColumn<Poet, String> nameColumn;
    @FXML
    private TableColumn<Poet, String> birthYearColumn;
    @FXML
    private TableColumn<Poet, String> deathYearColumn;

    private PoetService poetService;
    private Poet selectedPoet;

    public PoetManagementController() {
        try {
            this.poetService = PoetService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize PoetService", e);
        }
    }

    // For tests
    PoetManagementController(PoetService service) {
        this.poetService = service;
    }

    @FXML
    private void initialize() {
        applyRtl(nameField, birthYearField, deathYearField, biographyArea, searchField);

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPoetId()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        birthYearColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBirthYear()));
        deathYearColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDeathYear()));

        poetTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> populateForm(newSel));

        loadPoets();
    }

    private void applyRtl(Node... nodes) {
        for (Node node : nodes) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    private void populateForm(Poet poet) {
        selectedPoet = poet;
        if (poet == null) {
            clearForm();
            return;
        }
        nameField.setText(poet.getName());
        birthYearField.setText(poet.getBirthYear());
        deathYearField.setText(poet.getDeathYear());
        biographyArea.setText(poet.getBiography());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            Poet poet = new Poet();
            poet.setName(nameField.getText().trim());
            poet.setBirthYear(birthYearField.getText().trim());
            poet.setDeathYear(deathYearField.getText().trim());
            poet.setBiography(biographyArea.getText().trim());

            poetService.createPoet(poet);
            showInfo("Poet added successfully!");
            clearForm();
            loadPoets();
        } catch (Exception ex) {
            showError("Error adding poet: " + ex.getMessage());
            LOGGER.error("Error adding poet", ex);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedPoet == null) {
            showInfo("Please select a poet to update");
            return;
        }

        try {
            selectedPoet.setName(nameField.getText().trim());
            selectedPoet.setBirthYear(birthYearField.getText().trim());
            selectedPoet.setDeathYear(deathYearField.getText().trim());
            selectedPoet.setBiography(biographyArea.getText().trim());

            poetService.updatePoet(selectedPoet);
            showInfo("Poet updated successfully!");
            clearForm();
            loadPoets();
        } catch (Exception ex) {
            showError("Error updating poet: " + ex.getMessage());
            LOGGER.error("Error updating poet", ex);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedPoet == null) {
            showInfo("Please select a poet to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this poet?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                try {
                    poetService.deletePoet(selectedPoet.getPoetId());
                    showInfo("Poet deleted successfully!");
                    clearForm();
                    loadPoets();
                } catch (Exception ex) {
                    showError("Error deleting poet: " + ex.getMessage());
                    LOGGER.error("Error deleting poet", ex);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPoets();
            return;
        }

        try {
            List<Poet> poets = poetService.searchPoets(keyword);
            updateTable(poets);
        } catch (Exception ex) {
            showError("Error searching poets: " + ex.getMessage());
            LOGGER.error("Error searching poets", ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPoets();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void loadPoets() {
        try {
            List<Poet> poets = poetService.getAllPoets();
            updateTable(poets);
        } catch (Exception ex) {
            showError("Error loading poets: " + ex.getMessage());
            LOGGER.error("Error loading poets", ex);
        }
    }

    private void updateTable(List<Poet> poets) {
        ObservableList<Poet> data = FXCollections.observableArrayList(poets);
        poetTable.setItems(data);
    }

    private void clearForm() {
        selectedPoet = null;
        nameField.clear();
        birthYearField.clear();
        deathYearField.clear();
        biographyArea.clear();
        poetTable.getSelectionModel().clearSelection();
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
