package com.arabicpoetry.presentation.fx.verse;

import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.bll.service.VerseService;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Verse;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.geometry.NodeOrientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for managing verses (port of VerseManagementFrame).
 */
public class VerseManagementController {
    private static final Logger LOGGER = LogManager.getLogger(VerseManagementController.class);

    @FXML
    private ComboBox<Poem> poemCombo;
    @FXML
    private TextField verseNumberField;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField searchField;
    @FXML
    TableView<Verse> verseTable;
    @FXML
    private TableColumn<Verse, Number> idColumn;
    @FXML
    private TableColumn<Verse, String> poemColumn;
    @FXML
    private TableColumn<Verse, Number> numberColumn;
    @FXML
    private TableColumn<Verse, String> textColumn;

    private final VerseService verseService;
    private final PoemService poemService;
    private Verse selectedVerse;

    public VerseManagementController() {
        this.verseService = getVerseService();
        this.poemService = getPoemService();
    }

    // Visible for tests
    public VerseManagementController(VerseService verseService, PoemService poemService) {
        this.verseService = verseService;
        this.poemService = poemService;
    }

    private static VerseService getVerseService() {
        try {
            return VerseService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize VerseService", e);
        }
    }

    private static PoemService getPoemService() {
        try {
            return PoemService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize PoemService", e);
        }
    }

    @FXML
    private void initialize() {
        applyRtl(textArea, searchField);

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getVerseId()));
        poemColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPoemTitle() != null ? cell.getValue().getPoemTitle() : "N/A"));
        numberColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getVerseNumber()));
        textColumn.setCellValueFactory(cell -> new SimpleStringProperty(truncate(cell.getValue().getText())));

        verseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> populateForm(newSel));
        poemCombo.setOnAction(e -> loadVersesByPoem());

        loadPoems();
        loadVerses();
    }

    private void applyRtl(Node... nodes) {
        for (Node node : nodes) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private void populateForm(Verse verse) {
        selectedVerse = verse;
        if (verse == null) {
            clearForm();
            return;
        }
        selectPoem(verse.getPoemId());
        verseNumberField.setText(String.valueOf(verse.getVerseNumber()));
        textArea.setText(verse.getText());
    }

    private void selectPoem(int poemId) {
        for (Poem poem : poemCombo.getItems()) {
            if (poem.getPoemId() == poemId) {
                poemCombo.getSelectionModel().select(poem);
                return;
            }
        }
        poemCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        Poem poem = poemCombo.getSelectionModel().getSelectedItem();
        if (poem == null) {
            showInfo("Please select a poem");
            return;
        }
        try {
            Verse verse = new Verse();
            verse.setPoemId(poem.getPoemId());
            verse.setVerseNumber(Integer.parseInt(verseNumberField.getText().trim()));
            verse.setText(textArea.getText().trim());

            verseService.createVerse(verse);
            showInfo("Verse added successfully!");
            clearForm();
            loadVersesByPoem();
        } catch (NumberFormatException ex) {
            showError("Verse number must be a valid integer");
        } catch (Exception ex) {
            showError("Error adding verse: " + ex.getMessage());
            LOGGER.error("Error adding verse", ex);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedVerse == null) {
            showInfo("Please select a verse to update");
            return;
        }
        Poem poem = poemCombo.getSelectionModel().getSelectedItem();
        if (poem == null) {
            showInfo("Please select a poem");
            return;
        }
        try {
            selectedVerse.setPoemId(poem.getPoemId());
            selectedVerse.setVerseNumber(Integer.parseInt(verseNumberField.getText().trim()));
            selectedVerse.setText(textArea.getText().trim());

            verseService.updateVerse(selectedVerse);
            showInfo("Verse updated successfully!");
            clearForm();
            loadVersesByPoem();
        } catch (NumberFormatException ex) {
            showError("Verse number must be a valid integer");
        } catch (Exception ex) {
            showError("Error updating verse: " + ex.getMessage());
            LOGGER.error("Error updating verse", ex);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedVerse == null) {
            showInfo("Please select a verse to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this verse?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                try {
                    verseService.deleteVerse(selectedVerse.getVerseId());
                    showInfo("Verse deleted successfully!");
                    clearForm();
                    loadVersesByPoem();
                } catch (Exception ex) {
                    showError("Error deleting verse: " + ex.getMessage());
                    LOGGER.error("Error deleting verse", ex);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadVerses();
            return;
        }
        try {
            List<Verse> verses = verseService.searchVerses(keyword);
            updateTable(verses);
        } catch (Exception ex) {
            showError("Error searching verses: " + ex.getMessage());
            LOGGER.error("Error searching verses", ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadVerses();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void loadPoems() {
        try {
            List<Poem> poems = poemService.getAllPoems();
            poemCombo.setItems(FXCollections.observableArrayList(poems));
        } catch (Exception ex) {
            showError("Error loading poems: " + ex.getMessage());
            LOGGER.error("Error loading poems", ex);
        }
    }

    private void loadVerses() {
        try {
            List<Verse> verses = verseService.getAllVerses();
            updateTable(verses);
        } catch (Exception ex) {
            showError("Error loading verses: " + ex.getMessage());
            LOGGER.error("Error loading verses", ex);
        }
    }

    private void loadVersesByPoem() {
        Poem poem = poemCombo.getSelectionModel().getSelectedItem();
        if (poem == null) {
            return;
        }
        try {
            List<Verse> verses = verseService.getVersesByPoem(poem.getPoemId());
            updateTable(verses);
        } catch (Exception ex) {
            showError("Error loading verses: " + ex.getMessage());
            LOGGER.error("Error loading verses by poem", ex);
        }
    }

    private void updateTable(List<Verse> verses) {
        ObservableList<Verse> data = FXCollections.observableArrayList(verses);
        verseTable.setItems(data);
    }

    private void clearForm() {
        selectedVerse = null;
        poemCombo.getSelectionModel().clearSelection();
        verseNumberField.clear();
        textArea.clear();
        verseTable.getSelectionModel().clearSelection();
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
