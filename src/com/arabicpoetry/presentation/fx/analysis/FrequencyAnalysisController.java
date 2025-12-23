package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.FrequencyService;
import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.linguistics.FrequencyEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
 * JavaFX controller for frequency analysis (poem/book scope).
 */
public class FrequencyAnalysisController {
    private static final Logger LOGGER = LogManager.getLogger(FrequencyAnalysisController.class);

    @FXML
    private ComboBox<String> scopeCombo;
    @FXML
    private ComboBox<Object> itemCombo;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private Button analyzeButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<FrequencyRow> resultTable;
    @FXML
    private TableColumn<FrequencyRow, Number> rankColumn;
    @FXML
    private TableColumn<FrequencyRow, String> termColumn;
    @FXML
    private TableColumn<FrequencyRow, Number> freqColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private final FrequencyService frequencyService;
    private final PoemService poemService;
    private final BookService bookService;

    public FrequencyAnalysisController() {
        this(createFrequencyService(), createPoemService(), createBookService());
    }

    // Visible for tests
    public FrequencyAnalysisController(FrequencyService frequencyService,
                                       PoemService poemService,
                                       BookService bookService) {
        this.frequencyService = frequencyService;
        this.poemService = poemService;
        this.bookService = bookService;
    }

    private static FrequencyService createFrequencyService() {
        try {
            return FrequencyService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize services", e);
        }
    }

    private static PoemService createPoemService() {
        try {
            return PoemService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize PoemService", e);
        }
    }

    private static BookService createBookService() {
        try {
            return BookService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize BookService", e);
        }
    }

    @FXML
    private void initialize() {
        scopeCombo.setItems(FXCollections.observableArrayList("Poem", "Book"));
        scopeCombo.getSelectionModel().selectFirst();
        typeCombo.setItems(FXCollections.observableArrayList("Tokens", "Lemmas", "Roots"));
        typeCombo.getSelectionModel().selectFirst();

        rankColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().rank()));
        termColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().term()));
        freqColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().frequency()));

        scopeCombo.setOnAction(e -> updateItems());
        clearButton.setOnAction(e -> clearResults());

        progressIndicator.setVisible(false);
        updateItems();
    }

    private void updateItems() {
        itemCombo.getItems().clear();
        String scope = scopeCombo.getSelectionModel().getSelectedItem();
        try {
            if ("Poem".equals(scope)) {
                var poems = poemService.getAllPoems();
                itemCombo.getItems().addAll(poems);
                statusLabel.setText("Loaded " + poems.size() + " poems");
            } else {
                var books = bookService.getAllBooks();
                itemCombo.getItems().addAll(books);
                statusLabel.setText("Loaded " + books.size() + " books");
            }
            itemCombo.getSelectionModel().selectFirst();
        } catch (Exception ex) {
            LOGGER.error("Error loading items for scope {}", scope, ex);
            showError("Error loading items: " + ex.getMessage());
            statusLabel.setText("Error loading " + scope.toLowerCase() + "s");
        }
    }

    @FXML
    private void handleAnalyze(ActionEvent event) {
        Object selected = itemCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a poem or book.");
            statusLabel.setText("Select a " + scopeCombo.getSelectionModel().getSelectedItem().toLowerCase());
            return;
        }

        String scope = scopeCombo.getSelectionModel().getSelectedItem();
        String type = typeCombo.getSelectionModel().getSelectedItem();
        runAnalysis(selected, scope, type);
    }

    private void runAnalysis(Object selected, String scope, String type) {
        Task<ObservableList<FrequencyRow>> task = new Task<>() {
            @Override
            protected ObservableList<FrequencyRow> call() throws Exception {
                updateMessage("Analyzing " + scope.toLowerCase() + "...");
                List<FrequencyEntry> results;
                if ("Poem".equals(scope)) {
                    int poemId = ((Poem) selected).getPoemId();
                    results = switch (type) {
                        case "Tokens" -> frequencyService.getTokenFrequenciesByPoem(poemId);
                        case "Lemmas" -> frequencyService.getLemmaFrequenciesByPoem(poemId);
                        default -> frequencyService.getRootFrequenciesByPoem(poemId);
                    };
                } else {
                    int bookId = ((Book) selected).getBookId();
                    results = switch (type) {
                        case "Tokens" -> frequencyService.getTokenFrequenciesByBook(bookId);
                        case "Lemmas" -> frequencyService.getLemmaFrequenciesByBook(bookId);
                        default -> frequencyService.getRootFrequenciesByBook(bookId);
                    };
                }

                ObservableList<FrequencyRow> rows = FXCollections.observableArrayList();
                int rank = 1;
                for (FrequencyEntry entry : results) {
                    rows.add(new FrequencyRow(rank++, entry.getTerm(), entry.getCount()));
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            resultTable.setItems(task.getValue());
            statusLabel.setText("Found " + task.getValue().size() + " unique " + type.toLowerCase());
            setBusy(false);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred");
            LOGGER.error("Frequency analysis failed", task.getException());
            showError("Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            setBusy(false);
        });

        statusLabel.textProperty().bind(task.messageProperty());
        setBusy(true);
        resultTable.getItems().clear();

        Thread worker = new Thread(task, "frequency-analysis");
        worker.setDaemon(true);
        worker.start();
    }

    private void clearResults() {
        statusLabel.textProperty().unbind();
        resultTable.getItems().clear();
        statusLabel.setText("Ready");
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
        analyzeButton.setDisable(busy);
        clearButton.setDisable(busy);
        scopeCombo.setDisable(busy);
        itemCombo.setDisable(busy);
        typeCombo.setDisable(busy);
        progressIndicator.setVisible(busy);
    }

    public record FrequencyRow(int rank, String term, int frequency) {}
}
