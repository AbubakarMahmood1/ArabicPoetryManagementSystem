package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.FrequencyService;
import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.linguistics.FrequencyEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

class FrequencyAnalysisControllerTest extends ApplicationTest {
    private FrequencyService frequencyService;
    private PoemService poemService;
    private BookService bookService;

    @Override
    public void start(Stage stage) throws Exception {
        frequencyService = Mockito.mock(FrequencyService.class);
        poemService = Mockito.mock(PoemService.class);
        bookService = Mockito.mock(BookService.class);

        Poem poem = new Poem();
        poem.setPoemId(1);
        poem.setTitle("Mock Poem");

        when(poemService.getAllPoems()).thenReturn(List.of(poem));
        when(frequencyService.getTokenFrequenciesByPoem(1))
                .thenReturn(List.of(new FrequencyEntry("word", 2)));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/FrequencyAnalysisView.fxml"));
        loader.setControllerFactory(cls -> new FrequencyAnalysisController(frequencyService, poemService, bookService));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Test
    void analyzePoemShowsStatusAndResults() {
        clickOn("#analyzeButton");
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);

        TableView<?> table = lookup("#resultTable").queryTableView();
        Label status = lookup("#statusLabel").query();

        assertThat(table.getItems()).hasSize(1);
        assertThat(status.getText()).contains("Found 1");
    }
}
