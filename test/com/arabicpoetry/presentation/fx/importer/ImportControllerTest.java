package com.arabicpoetry.presentation.fx.importer;

import com.arabicpoetry.bll.service.ImportService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

class ImportControllerTest extends ApplicationTest {
    private ImportService importService;
    private File tempFile;

    @Override
    public void start(Stage stage) throws Exception {
        importService = Mockito.mock(ImportService.class);
        tempFile = File.createTempFile("poems", ".txt");
        tempFile.deleteOnExit();

        when(importService.importFromFile(tempFile.getAbsolutePath())).thenReturn("import summary");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/arabicpoetry/presentation/fx/importer/ImportView.fxml"));
        loader.setControllerFactory(cls -> new ImportController(importService));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Test
    void importCompletesAndShowsOutput() {
        interact(() -> ((TextField) lookup("#fileField").query()).setText(tempFile.getAbsolutePath()));
        clickOn("#importButton");

        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);

        Label status = lookup("#statusLabel").query();
        TextArea output = lookup("#outputArea").query();

        assertThat(status.getText()).contains("Import completed");
        assertThat(output.getText()).contains("import summary");
    }
}
