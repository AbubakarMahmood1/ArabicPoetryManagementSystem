package com.arabicpoetry.presentation.fx.book;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.model.Book;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.geometry.NodeOrientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for managing books (port of the Swing BookManagementFrame).
 */
public class BookManagementController {
    private static final Logger LOGGER = LogManager.getLogger(BookManagementController.class);

    @FXML
    private TextField titleField;
    @FXML
    private TextField compilerField;
    @FXML
    private TextField eraField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField searchField;
    @FXML
    TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, Number> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> compilerColumn;
    @FXML
    private TableColumn<Book, String> eraColumn;

    private BookService bookService;
    private Book selectedBook;

    public BookManagementController() {
        try {
            this.bookService = BookService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize BookService", e);
        }
    }

    // For tests
    BookManagementController(BookService service) {
        this.bookService = service;
    }

    @FXML
    private void initialize() {
        applyRtl(titleField, compilerField, eraField, descriptionArea, searchField);

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getBookId()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        compilerColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCompiler()));
        eraColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEra()));

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> populateForm(newSel));

        loadBooks();
    }

    private void applyRtl(Node... nodes) {
        for (Node node : nodes) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    private void populateForm(Book book) {
        selectedBook = book;
        if (book == null) {
            clearForm();
            return;
        }
        titleField.setText(book.getTitle());
        compilerField.setText(book.getCompiler());
        eraField.setText(book.getEra());
        descriptionArea.setText(book.getDescription());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            Book book = new Book();
            book.setTitle(titleField.getText().trim());
            book.setCompiler(compilerField.getText().trim());
            book.setEra(eraField.getText().trim());
            book.setDescription(descriptionArea.getText().trim());

            bookService.createBook(book);
            showInfo("Book added successfully!");
            clearForm();
            loadBooks();
        } catch (Exception ex) {
            showError("Error adding book: " + ex.getMessage());
            LOGGER.error("Error adding book", ex);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedBook == null) {
            showInfo("Please select a book to update");
            return;
        }

        try {
            selectedBook.setTitle(titleField.getText().trim());
            selectedBook.setCompiler(compilerField.getText().trim());
            selectedBook.setEra(eraField.getText().trim());
            selectedBook.setDescription(descriptionArea.getText().trim());

            bookService.updateBook(selectedBook);
            showInfo("Book updated successfully!");
            clearForm();
            loadBooks();
        } catch (Exception ex) {
            showError("Error updating book: " + ex.getMessage());
            LOGGER.error("Error updating book", ex);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedBook == null) {
            showInfo("Please select a book to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this book?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                try {
                    bookService.deleteBook(selectedBook.getBookId());
                    showInfo("Book deleted successfully!");
                    clearForm();
                    loadBooks();
                } catch (Exception ex) {
                    showError("Error deleting book: " + ex.getMessage());
                    LOGGER.error("Error deleting book", ex);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks();
            return;
        }

        try {
            List<Book> books = bookService.searchBooks(keyword);
            updateTable(books);
        } catch (Exception ex) {
            showError("Error searching books: " + ex.getMessage());
            LOGGER.error("Error searching books", ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBooks();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            updateTable(books);
        } catch (Exception ex) {
            showError("Error loading books: " + ex.getMessage());
            LOGGER.error("Error loading books", ex);
        }
    }

    private void updateTable(List<Book> books) {
        ObservableList<Book> data = FXCollections.observableArrayList(books);
        bookTable.setItems(data);
    }

    private void clearForm() {
        selectedBook = null;
        titleField.clear();
        compilerField.clear();
        eraField.clear();
        descriptionArea.clear();
        bookTable.getSelectionModel().clearSelection();
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
