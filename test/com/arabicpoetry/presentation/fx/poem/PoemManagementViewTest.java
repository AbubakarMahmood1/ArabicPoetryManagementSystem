package com.arabicpoetry.presentation.fx.poem;

import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Poet;
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

public class PoemManagementViewTest extends ApplicationTest {
    private PoemManagementController controller;

    @Override
    public void start(Stage stage) throws Exception {
        Poet poet = new Poet();
        poet.setPoetId(1);
        poet.setName("Poet");

        Book book = new Book();
        book.setBookId(1);
        book.setTitle("Book");

        Poem poem = new Poem();
        poem.setPoemId(1);
        poem.setTitle("Poem Title");
        poem.setPoetId(poet.getPoetId());
        poem.setPoetName(poet.getName());
        poem.setBookId(book.getBookId());
        poem.setBookTitle(book.getTitle());

        var poemService = Mockito.mock(com.arabicpoetry.bll.service.PoemService.class);
        var poetService = Mockito.mock(com.arabicpoetry.bll.service.PoetService.class);
        var bookService = Mockito.mock(com.arabicpoetry.bll.service.BookService.class);

        when(poemService.getAllPoems()).thenReturn(Arrays.asList(poem));
        when(poemService.searchPoems(Mockito.anyString())).thenReturn(Arrays.asList(poem));
        when(poetService.getAllPoets()).thenReturn(Arrays.asList(poet));
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/poem/PoemManagementView.fxml"));
        loader.setControllerFactory(cls -> new PoemManagementController(poemService, poetService, bookService));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldLoadPoemsIntoTable() {
        assertThat(controller.poemTable.getItems()).hasSize(1);
        assertThat(controller.poemTable.getItems().get(0).getTitle()).isEqualTo("Poem Title");
    }
}
