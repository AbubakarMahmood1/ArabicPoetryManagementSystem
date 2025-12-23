package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.FrequencyService;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.linguistics.IndexEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

class BookIndexControllerTest extends ApplicationTest {
    private FrequencyService frequencyService;
    private BookService bookService;

    @Override
    public void start(Stage stage) throws Exception {
        frequencyService = Mockito.mock(FrequencyService.class);
        bookService = Mockito.mock(BookService.class);

        Book book = new Book();
        book.setBookId(1);
        book.setTitle("Mock Book");

        when(bookService.getAllBooks()).thenReturn(List.of(book));
        when(frequencyService.generateTokenIndexByBook(1))
                .thenReturn(Map.of("token", List.of(new IndexEntry("token", 10, 20, 1))));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/BookIndexView.fxml"));
        loader.setControllerFactory(cls -> new BookIndexController(frequencyService, bookService));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Test
    void generatesIndexAndShowsOccurrences() {
        clickOn("#generateButton");
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);

        ListView<String> termList = lookup("#termList").queryListView();
        TableView<?> occurrenceTable = lookup("#occurrenceTable").queryTableView();
        Label status = lookup("#statusLabel").query();

        assertThat(termList.getItems()).contains("token");
        assertThat(occurrenceTable.getItems()).hasSize(1);
        assertThat(status.getText()).contains("Index generated");
    }
}
