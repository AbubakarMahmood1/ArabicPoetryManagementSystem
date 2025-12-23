package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.FrequencyService;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.linguistics.IndexEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for book index generation.
 */
public class BookIndexController {
    private static final Logger LOGGER = LogManager.getLogger(BookIndexController.class);

    @FXML
    private ComboBox<Book> bookCombo;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private Button generateButton;
    @FXML
    private Button clearButton;
    @FXML
    private ListView<String> termList;
    @FXML
    private TableView<IndexRow> occurrenceTable;
    @FXML
    private TableColumn<IndexRow, Number> verseIdColumn;
    @FXML
    private TableColumn<IndexRow, Number> poemIdColumn;
    @FXML
    private TableColumn<IndexRow, Number> positionColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private final FrequencyService frequencyService;
    private final BookService bookService;

    private Map<String, List<IndexEntry>> currentIndex;

    public BookIndexController() {
        this(createFrequencyService(), createBookService());
    }

    // Visible for tests
    public BookIndexController(FrequencyService frequencyService, BookService bookService) {
        this.frequencyService = frequencyService;
        this.bookService = bookService;
    }

    private static FrequencyService createFrequencyService() {
        try {
            return FrequencyService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize services", e);
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
        typeCombo.setItems(FXCollections.observableArrayList("Tokens", "Lemmas", "Roots"));
        typeCombo.getSelectionModel().selectFirst();

        verseIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().verseId()));
        poemIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().poemId()));
        positionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().position()));

        termList.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> showOccurrences(newSel));
        clearButton.setOnAction(e -> clearResults());

        progressIndicator.setVisible(false);
        loadBooks();
    }

    private void loadBooks() {
        try {
            var books = FXCollections.observableArrayList(bookService.getAllBooks());
            bookCombo.setItems(books);
            bookCombo.getSelectionModel().selectFirst();
            statusLabel.setText("Loaded " + books.size() + " books");
        } catch (Exception ex) {
            LOGGER.error("Error loading books", ex);
            showError("Error loading books: " + ex.getMessage());
            statusLabel.setText("Error loading books");
        }
    }

    @FXML
    private void handleGenerate(ActionEvent event) {
        Book book = bookCombo.getSelectionModel().getSelectedItem();
        if (book == null) {
            showInfo("Please select a book.");
            return;
        }
        String type = typeCombo.getSelectionModel().getSelectedItem();

        generateIndex(book, type);
    }

    private void generateIndex(Book book, String type) {
        Task<Map<String, List<IndexEntry>>> task = new Task<>() {
            @Override
            protected Map<String, List<IndexEntry>> call() throws Exception {
                updateMessage("Generating " + type.toLowerCase() + " index...");
                if ("Tokens".equals(type)) {
                    return frequencyService.generateTokenIndexByBook(book.getBookId());
                } else if ("Lemmas".equals(type)) {
                    return frequencyService.generateLemmaIndexByBook(book.getBookId());
                } else {
                    return frequencyService.generateRootIndexByBook(book.getBookId());
                }
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            currentIndex = task.getValue();
            List<String> terms = new ArrayList<>(currentIndex.keySet());
            Collections.sort(terms);
            termList.setItems(FXCollections.observableArrayList(terms));
            if (!terms.isEmpty()) {
                termList.getSelectionModel().selectFirst();
                showOccurrences(termList.getSelectionModel().getSelectedItem());
            }
            statusLabel.setText("Index generated: " + terms.size() + " terms");
            setBusy(false);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred");
            LOGGER.error("Index generation failed", task.getException());
            showError("Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            setBusy(false);
        });

        statusLabel.textProperty().bind(task.messageProperty());
        setBusy(true);
        termList.getItems().clear();
        occurrenceTable.getItems().clear();

        Thread worker = new Thread(task, "book-index");
        worker.setDaemon(true);
        worker.start();
    }

    private void showOccurrences(String term) {
        if (term == null || currentIndex == null) {
            return;
        }
        List<IndexEntry> occurrences = currentIndex.get(term);
        ObservableList<IndexRow> rows = FXCollections.observableArrayList();
        if (occurrences != null) {
            for (IndexEntry entry : occurrences) {
                rows.add(new IndexRow(entry.getVerseId(), entry.getPoemId(), entry.getPosition()));
            }
        }
        occurrenceTable.setItems(rows);
        if (term != null && currentIndex != null) {
            statusLabel.setText("Showing " + rows.size() + " occurrences for \"" + term + "\"");
        }
    }

    private void clearResults() {
        statusLabel.textProperty().unbind();
        termList.getItems().clear();
        occurrenceTable.getItems().clear();
        currentIndex = null;
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
        generateButton.setDisable(busy);
        clearButton.setDisable(busy);
        bookCombo.setDisable(busy);
        typeCombo.setDisable(busy);
        progressIndicator.setVisible(busy);
    }

    public record IndexRow(int verseId, int poemId, int position) {}
}
