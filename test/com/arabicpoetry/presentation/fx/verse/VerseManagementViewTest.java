package com.arabicpoetry.presentation.fx.verse;

import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Verse;
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

public class VerseManagementViewTest extends ApplicationTest {
    private VerseManagementController controller;

    @Override
    public void start(Stage stage) throws Exception {
        Poem poem = new Poem();
        poem.setPoemId(1);
        poem.setTitle("Poem Title");

        Verse verse = new Verse();
        verse.setVerseId(1);
        verse.setPoemId(poem.getPoemId());
        verse.setPoemTitle(poem.getTitle());
        verse.setVerseNumber(1);
        verse.setText("Sample verse text");

        var verseService = Mockito.mock(com.arabicpoetry.bll.service.VerseService.class);
        var poemService = Mockito.mock(com.arabicpoetry.bll.service.PoemService.class);

        when(verseService.getAllVerses()).thenReturn(Arrays.asList(verse));
        when(verseService.getVersesByPoem(Mockito.anyInt())).thenReturn(Arrays.asList(verse));
        when(poemService.getAllPoems()).thenReturn(Arrays.asList(poem));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/verse/VerseManagementView.fxml"));
        loader.setControllerFactory(cls -> new VerseManagementController(verseService, poemService));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldLoadVersesIntoTable() {
        assertThat(controller.verseTable.getItems()).hasSize(1);
        assertThat(controller.verseTable.getItems().get(0).getText()).contains("Sample verse");
    }
}
