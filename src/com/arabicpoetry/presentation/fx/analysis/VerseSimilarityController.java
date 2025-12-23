package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.VerseSimilarityService;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.VerseSimilarity;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for verse similarity search.
 */
public class VerseSimilarityController {
    private static final Logger LOGGER = LogManager.getLogger(VerseSimilarityController.class);

    @FXML
    private TextArea queryArea;
    @FXML
    private Slider thresholdSlider;
    @FXML
    private Label thresholdValueLabel;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<SimilarityRow> resultTable;
    @FXML
    private TableColumn<SimilarityRow, Number> scoreColumn;
    @FXML
    private TableColumn<SimilarityRow, Number> verseIdColumn;
    @FXML
    private TableColumn<SimilarityRow, Number> poemIdColumn;
    @FXML
    private TableColumn<SimilarityRow, String> textColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private final VerseSimilarityService similarityService;

    public VerseSimilarityController() {
        this(createSimilarityService());
    }

    // Visible for tests
    public VerseSimilarityController(VerseSimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    private static VerseSimilarityService createSimilarityService() {
        try {
            return VerseSimilarityService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize VerseSimilarityService", e);
        }
    }

    @FXML
    private void initialize() {
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                thresholdValueLabel.setText(String.format("%.2f", newVal.doubleValue())));
        thresholdSlider.setValue(0.3);

        scoreColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().score()));
        verseIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().verseId()));
        poemIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().poemId()));
        textColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().text()));

        clearButton.setOnAction(e -> clearResults());
        progressIndicator.setVisible(false);
        statusLabel.setText(readyText());
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String queryText = queryArea.getText() != null ? queryArea.getText().trim() : "";
        if (queryText.isEmpty()) {
            showInfo("Please enter a verse text to search.");
            statusLabel.setText("Enter a verse to search");
            return;
        }

        double threshold = thresholdSlider.getValue();
        runSearch(queryText, threshold);
    }

    private void runSearch(String queryText, double threshold) {
        Task<ObservableList<SimilarityRow>> task = new Task<>() {
            @Override
            protected ObservableList<SimilarityRow> call() {
                updateMessage("Searching...");
                List<VerseSimilarity> results = similarityService.findSimilar(queryText, threshold);
                ObservableList<SimilarityRow> rows = FXCollections.observableArrayList();
                for (VerseSimilarity sim : results) {
                    Verse verse = sim.getVerse();
                    rows.add(new SimilarityRow(
                            sim.getSimilarityScore(),
                            verse.getVerseId(),
                            verse.getPoemId(),
                            verse.getText()
                    ));
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            resultTable.setItems(task.getValue());
            statusLabel.setText("Found " + task.getValue().size() + " similar verses (threshold " + String.format("%.2f", threshold) + ")");
            setBusy(false);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred");
            LOGGER.error("Similarity search failed", task.getException());
            showError("Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            setBusy(false);
        });

        statusLabel.textProperty().bind(task.messageProperty());
        setBusy(true);
        resultTable.getItems().clear();

        Thread worker = new Thread(task, "verse-similarity");
        worker.setDaemon(true);
        worker.start();
    }

    private void clearResults() {
        queryArea.clear();
        resultTable.getItems().clear();
        statusLabel.textProperty().unbind();
        statusLabel.setText(readyText());
        setBusy(false);
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

    private void setBusy(boolean busy) {
        searchButton.setDisable(busy);
        clearButton.setDisable(busy);
        queryArea.setDisable(busy);
        thresholdSlider.setDisable(busy);
        progressIndicator.setVisible(busy);
    }

    private String readyText() {
        return "Ready. N-gram size: " + similarityService.getNGramSize();
    }

    public record SimilarityRow(double score, int verseId, int poemId, String text) {}
}
