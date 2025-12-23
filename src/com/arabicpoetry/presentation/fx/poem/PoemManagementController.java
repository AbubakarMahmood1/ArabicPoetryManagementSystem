package com.arabicpoetry.presentation.fx.poem;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.bll.service.PoetService;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Poet;
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
 * JavaFX controller for managing poems (port of PoemManagementFrame).
 */
public class PoemManagementController {
    private static final Logger LOGGER = LogManager.getLogger(PoemManagementController.class);

    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<Poet> poetCombo;
    @FXML
    private ComboBox<Book> bookCombo;
    @FXML
    private TextField searchField;
    @FXML
    TableView<Poem> poemTable;
    @FXML
    private TableColumn<Poem, Number> idColumn;
    @FXML
    private TableColumn<Poem, String> titleColumn;
    @FXML
    private TableColumn<Poem, String> poetColumn;
    @FXML
    private TableColumn<Poem, String> bookColumn;

    private final PoemService poemService;
    private final PoetService poetService;
    private final BookService bookService;
    private Poem selectedPoem;

    public PoemManagementController() {
        this.poemService = getPoemService();
        this.poetService = getPoetService();
        this.bookService = getBookService();
    }

    // Visible for tests
    public PoemManagementController(PoemService poemService, PoetService poetService, BookService bookService) {
        this.poemService = poemService;
        this.poetService = poetService;
        this.bookService = bookService;
    }

    private static PoemService getPoemService() {
        try {
            return PoemService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize PoemService", e);
        }
    }

    private static PoetService getPoetService() {
        try {
            return PoetService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize PoetService", e);
        }
    }

    private static BookService getBookService() {
        try {
            return BookService.getInstance();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize BookService", e);
        }
    }

    @FXML
    private void initialize() {
        applyRtl(titleField, searchField);

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPoemId()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        poetColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPoetName() != null ? cell.getValue().getPoetName() : "N/A"));
        bookColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBookTitle() != null ? cell.getValue().getBookTitle() : "N/A"));

        poemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> populateForm(newSel));

        loadCombos();
        loadPoems();
    }

    private void applyRtl(Node... nodes) {
        for (Node node : nodes) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    private void populateForm(Poem poem) {
        selectedPoem = poem;
        if (poem == null) {
            clearForm();
            return;
        }
        titleField.setText(poem.getTitle());
        selectPoet(poem.getPoetId());
        selectBook(poem.getBookId());
    }

    private void selectPoet(Integer id) {
        if (id == null) {
            poetCombo.getSelectionModel().clearSelection();
            return;
        }
        for (Poet poet : poetCombo.getItems()) {
            if (poet.getPoetId() == id) {
                poetCombo.getSelectionModel().select(poet);
                return;
            }
        }
        poetCombo.getSelectionModel().clearSelection();
    }

    private void selectBook(Integer id) {
        if (id == null) {
            bookCombo.getSelectionModel().clearSelection();
            return;
        }
        for (Book book : bookCombo.getItems()) {
            if (book.getBookId() == id) {
                bookCombo.getSelectionModel().select(book);
                return;
            }
        }
        bookCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            Poem poem = new Poem();
            poem.setTitle(titleField.getText().trim());
            Poet poet = poetCombo.getSelectionModel().getSelectedItem();
            Book book = bookCombo.getSelectionModel().getSelectedItem();
            poem.setPoetId(poet != null ? poet.getPoetId() : null);
            poem.setBookId(book != null ? book.getBookId() : null);

            poemService.createPoem(poem);
            showInfo("Poem added successfully!");
            clearForm();
            loadPoems();
        } catch (Exception ex) {
            showError("Error adding poem: " + ex.getMessage());
            LOGGER.error("Error adding poem", ex);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedPoem == null) {
            showInfo("Please select a poem to update");
            return;
        }
        try {
            selectedPoem.setTitle(titleField.getText().trim());
            Poet poet = poetCombo.getSelectionModel().getSelectedItem();
            Book book = bookCombo.getSelectionModel().getSelectedItem();
            selectedPoem.setPoetId(poet != null ? poet.getPoetId() : null);
            selectedPoem.setBookId(book != null ? book.getBookId() : null);

            poemService.updatePoem(selectedPoem);
            showInfo("Poem updated successfully!");
            clearForm();
            loadPoems();
        } catch (Exception ex) {
            showError("Error updating poem: " + ex.getMessage());
            LOGGER.error("Error updating poem", ex);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedPoem == null) {
            showInfo("Please select a poem to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this poem? This will also delete all verses.", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                try {
                    poemService.deletePoem(selectedPoem.getPoemId());
                    showInfo("Poem deleted successfully!");
                    clearForm();
                    loadPoems();
                } catch (Exception ex) {
                    showError("Error deleting poem: " + ex.getMessage());
                    LOGGER.error("Error deleting poem", ex);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPoems();
            return;
        }
        try {
            List<Poem> poems = poemService.searchPoems(keyword);
            updateTable(poems);
        } catch (Exception ex) {
            showError("Error searching poems: " + ex.getMessage());
            LOGGER.error("Error searching poems", ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPoems();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void loadCombos() {
        try {
            List<Poet> poets = poetService.getAllPoets();
            poetCombo.setItems(FXCollections.observableArrayList(poets));
            List<Book> books = bookService.getAllBooks();
            bookCombo.setItems(FXCollections.observableArrayList(books));
        } catch (Exception ex) {
            showError("Error loading poets/books: " + ex.getMessage());
            LOGGER.error("Error loading poets/books", ex);
        }
    }

    private void loadPoems() {
        try {
            List<Poem> poems = poemService.getAllPoems();
            updateTable(poems);
        } catch (Exception ex) {
            showError("Error loading poems: " + ex.getMessage());
            LOGGER.error("Error loading poems", ex);
        }
    }

    private void updateTable(List<Poem> poems) {
        ObservableList<Poem> data = FXCollections.observableArrayList(poems);
        poemTable.setItems(data);
    }

    private void clearForm() {
        selectedPoem = null;
        titleField.clear();
        poetCombo.getSelectionModel().clearSelection();
        bookCombo.getSelectionModel().clearSelection();
        poemTable.getSelectionModel().clearSelection();
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
