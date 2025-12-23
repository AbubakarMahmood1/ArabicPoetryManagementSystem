package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.LinguisticAnalysisService;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.LinguisticMatch;
import com.arabicpoetry.model.linguistics.LinguisticSearchMode;
import com.arabicpoetry.model.linguistics.TokenAnalysis;
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
 * JavaFX controller for the linguistic workbench (browse/search).
 */
public class LinguisticWorkbenchController {
    private static final Logger LOGGER = LogManager.getLogger(LinguisticWorkbenchController.class);

    @FXML
    private TabPane browseTabs;
    @FXML
    private ListView<String> tokenList;
    @FXML
    private ListView<String> lemmaList;
    @FXML
    private ListView<String> rootList;
    @FXML
    private ListView<String> segmentList;
    @FXML
    private ComboBox<LinguisticSearchMode> modeCombo;
    @FXML
    private TextField queryField;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<ResultRow> resultTable;
    @FXML
    private TableColumn<ResultRow, String> poemColumn;
    @FXML
    private TableColumn<ResultRow, String> verseNumberColumn;
    @FXML
    private TableColumn<ResultRow, String> tokenColumn;
    @FXML
    private TableColumn<ResultRow, String> lemmaColumn;
    @FXML
    private TableColumn<ResultRow, String> rootColumn;
    @FXML
    private TableColumn<ResultRow, String> segmentColumn;
    @FXML
    private TableColumn<ResultRow, String> posColumn;
    @FXML
    private TableColumn<ResultRow, String> verseTextColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private final LinguisticAnalysisService analysisService;

    public LinguisticWorkbenchController() {
        this(createAnalysisService());
    }

    // Visible for tests
    public LinguisticWorkbenchController(LinguisticAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    private static LinguisticAnalysisService createAnalysisService() {
        try {
            return LinguisticAnalysisService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize LinguisticAnalysisService", e);
        }
    }

    @FXML
    private void initialize() {
        modeCombo.setItems(FXCollections.observableArrayList(LinguisticSearchMode.values()));
        modeCombo.getSelectionModel().select(LinguisticSearchMode.TOKEN);

        poemColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().poem()));
        verseNumberColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().verseNumber()));
        tokenColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().tokenOrMatch()));
        lemmaColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().lemmas()));
        rootColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().roots()));
        segmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().segments()));
        posColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().posOrMode()));
        verseTextColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().verseText()));

        tokenList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> browseSelect(LinguisticSearchMode.TOKEN, newVal));
        lemmaList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> browseSelect(LinguisticSearchMode.LEMMA, newVal));
        rootList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> browseSelect(LinguisticSearchMode.ROOT, newVal));
        segmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> browseSelect(LinguisticSearchMode.SEGMENT, newVal));

        clearButton.setOnAction(e -> clearResults());
        refreshButton.setOnAction(e -> refreshAnalysis());

        progressIndicator.setVisible(false);
        statusLabel.setText("Ready");
        loadBrowseLists();
    }

    private void browseSelect(LinguisticSearchMode mode, String value) {
        if (value == null) {
            return;
        }
        modeCombo.getSelectionModel().select(mode);
        queryField.setText(value);
        runSearch(mode, value);
    }

    @FXML
    private void handleAnalyze(ActionEvent event) {
        LinguisticSearchMode mode = modeCombo.getSelectionModel().getSelectedItem();
        String query = queryField.getText() != null ? queryField.getText().trim() : "";
        runSearch(mode, query);
    }

    private void runSearch(LinguisticSearchMode mode, String query) {
        if (mode == null) {
            statusLabel.setText("Choose a search mode");
            return;
        }
        if (requiresQuery(mode) && (query == null || query.isBlank())) {
            statusLabel.setText("Enter a query for " + mode.getDisplayLabel());
            return;
        }

        Task<List<LinguisticMatch>> task = new Task<>() {
            @Override
            protected List<LinguisticMatch> call() throws Exception {
                updateMessage("Searching " + mode.getDisplayLabel() + "...");
                return analysisService.search(query, mode);
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            populateTable(task.getValue());
            statusLabel.setText("Found " + task.getValue().size() + " matches");
            setBusy(false);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred");
            Throwable ex = task.getException();
            if (ex instanceof IllegalArgumentException) {
                showError(ex.getMessage());
            } else {
                LOGGER.error("Search failed", ex);
                showError("Search failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
            }
            setBusy(false);
        });

        statusLabel.textProperty().bind(task.messageProperty());
        setBusy(true);
        resultTable.getItems().clear();

        Thread worker = new Thread(task, "linguistic-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void populateTable(List<LinguisticMatch> matches) {
        ObservableList<ResultRow> rows = FXCollections.observableArrayList();
        for (LinguisticMatch match : matches) {
            Verse verse = match.getVerse();
            TokenAnalysis analysis = match.getTokenAnalysis();
            rows.add(new ResultRow(
                    verse != null ? verse.getPoemTitle() : "",
                    verse != null ? String.valueOf(verse.getVerseNumber()) : "",
                    analysis != null ? analysis.getToken() : match.getMatchDetail(),
                    analysis != null ? analysis.getLemmaSummary() : "",
                    analysis != null ? analysis.getRootSummary() : "",
                    analysis != null ? analysis.getSegmentSummary() : "",
                    analysis != null ? safe(analysis.getPartOfSpeech()) : match.getMode().getDisplayLabel(),
                    verse != null ? safe(verse.getText()) : ""
            ));
        }
        resultTable.setItems(rows);
    }

    private void clearResults() {
        queryField.clear();
        resultTable.getItems().clear();
        statusLabel.textProperty().unbind();
        statusLabel.setText("Ready");
        setBusy(false);
    }

    private void refreshAnalysis() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Refreshing analyses...");
                analysisService.refresh();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            loadBrowseLists();
            resultTable.getItems().clear();
            statusLabel.setText("Linguistic index refreshed");
            setBusy(false);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred");
            LOGGER.error("Refresh failed", task.getException());
            showError("Unable to refresh linguistic index: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            setBusy(false);
        });

        statusLabel.textProperty().bind(task.messageProperty());
        setBusy(true);

        Thread worker = new Thread(task, "linguistic-refresh");
        worker.setDaemon(true);
        worker.start();
    }

    private boolean requiresQuery(LinguisticSearchMode mode) {
        return mode != LinguisticSearchMode.STRING && mode != LinguisticSearchMode.REGEX;
    }

    private void loadBrowseLists() {
        try {
            statusLabel.setText("Loading linguistic index...");
            setBusy(true);
            tokenList.setItems(FXCollections.observableArrayList(analysisService.getAllTokens()));
            lemmaList.setItems(FXCollections.observableArrayList(analysisService.getAllLemmas()));
            rootList.setItems(FXCollections.observableArrayList(analysisService.getAllRoots()));
            segmentList.setItems(FXCollections.observableArrayList(analysisService.getAllSegments()));
            statusLabel.setText("Ready");
        } catch (Exception ex) {
            LOGGER.error("Failed to load browse lists", ex);
            showError("Failed to build linguistic index: " + ex.getMessage());
            statusLabel.setText("Error loading linguistic index");
        } finally {
            setBusy(false);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void setBusy(boolean busy) {
        searchButton.setDisable(busy);
        clearButton.setDisable(busy);
        refreshButton.setDisable(busy);
        modeCombo.setDisable(busy);
        queryField.setDisable(busy);
        tokenList.setDisable(busy);
        lemmaList.setDisable(busy);
        rootList.setDisable(busy);
        segmentList.setDisable(busy);
        progressIndicator.setVisible(busy);
    }

    public record ResultRow(String poem, String verseNumber, String tokenOrMatch, String lemmas, String roots, String segments, String posOrMode, String verseText) {}
}
