package com.arabicpoetry.presentation.fx.book;

import com.arabicpoetry.model.Book;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

public class BookManagementViewTest extends ApplicationTest {
    private BookManagementController controller;

    @Override
    public void start(Stage stage) throws Exception {
        Book mockBook = new Book();
        mockBook.setBookId(1);
        mockBook.setTitle("Sample Book");
        mockBook.setCompiler("Compiler");
        mockBook.setEra("Era");

        var service = Mockito.mock(com.arabicpoetry.bll.service.BookService.class);
        when(service.getAllBooks()).thenReturn(Arrays.asList(mockBook));
        when(service.searchBooks(Mockito.anyString())).thenReturn(Arrays.asList(mockBook));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/book/BookManagementView.fxml"));
        loader.setControllerFactory(cls -> new BookManagementController(service));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldLoadBooksIntoTable() {
        // Table should contain the mocked book
        assertThat(controller.bookTable.getItems()).hasSize(1);
        assertThat(controller.bookTable.getItems().get(0).getTitle()).isEqualTo("Sample Book");
    }
}
