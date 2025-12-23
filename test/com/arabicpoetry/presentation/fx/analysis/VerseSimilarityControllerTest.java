package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.VerseSimilarityService;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.VerseSimilarity;
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

class VerseSimilarityControllerTest extends ApplicationTest {
    private VerseSimilarityService similarityService;

    @Override
    public void start(Stage stage) throws Exception {
        similarityService = Mockito.mock(VerseSimilarityService.class);

        Verse verse = new Verse();
        verse.setVerseId(5);
        verse.setPoemId(2);
        verse.setText("sample verse text");

        when(similarityService.getNGramSize()).thenReturn(3);
        when(similarityService.findSimilar(Mockito.anyString(), Mockito.anyDouble()))
                .thenReturn(List.of(new VerseSimilarity(verse, 0.74)));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/VerseSimilarityView.fxml"));
        loader.setControllerFactory(cls -> new VerseSimilarityController(similarityService));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Test
    void searchDisplaysResultsAndStatus() {
        clickOn("#queryArea").write("query text");
        clickOn("#searchButton");

        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);

        TableView<?> table = lookup("#resultTable").queryTableView();
        Label status = lookup("#statusLabel").query();

        assertThat(table.getItems()).hasSize(1);
        assertThat(status.getText()).contains("Found 1");
    }
}
