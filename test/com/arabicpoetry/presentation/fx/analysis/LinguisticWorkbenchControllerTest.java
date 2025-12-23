package com.arabicpoetry.presentation.fx.analysis;

import com.arabicpoetry.bll.service.LinguisticAnalysisService;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.LinguisticMatch;
import com.arabicpoetry.model.linguistics.LinguisticSearchMode;
import com.arabicpoetry.model.linguistics.TokenAnalysis;
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

class LinguisticWorkbenchControllerTest extends ApplicationTest {
    private LinguisticAnalysisService analysisService;

    @Override
    public void start(Stage stage) throws Exception {
        analysisService = Mockito.mock(LinguisticAnalysisService.class);

        Verse verse = new Verse();
        verse.setVerseId(1);
        verse.setPoemId(1);
        verse.setVerseNumber(1);
        verse.setPoemTitle("Mock Poem");
        verse.setText("token in verse");

        TokenAnalysis analysis = new TokenAnalysis(
                verse,
                "token",
                "token",
                1,
                List.of("lemma"),
                List.of("root"),
                List.of("seg"),
                "POS",
                null,
                null,
                null
        );

        LinguisticMatch match = new LinguisticMatch(verse, analysis, LinguisticSearchMode.TOKEN, "token");

        when(analysisService.getAllTokens()).thenReturn(List.of("token"));
        when(analysisService.getAllLemmas()).thenReturn(List.of("lemma"));
        when(analysisService.getAllRoots()).thenReturn(List.of("root"));
        when(analysisService.getAllSegments()).thenReturn(List.of("seg"));
        when(analysisService.search("token", LinguisticSearchMode.TOKEN)).thenReturn(List.of(match));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/LinguisticWorkbenchView.fxml"));
        loader.setControllerFactory(cls -> new LinguisticWorkbenchController(analysisService));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Test
    void searchShowsResultsAndStatus() {
        clickOn("#queryField").write("token");
        clickOn("#searchButton");

        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);

        TableView<?> table = lookup("#resultTable").queryTableView();
        Label status = lookup("#statusLabel").query();

        assertThat(table.getItems()).hasSize(1);
        assertThat(status.getText()).contains("Found 1");
    }
}
