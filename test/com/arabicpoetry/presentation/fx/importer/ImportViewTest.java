package com.arabicpoetry.presentation.fx.importer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.assertions.api.Assertions.assertThat;

public class ImportViewTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 100, 100));
        stage.show();
    }

    @Test
    void importViewLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/importer/ImportView.fxml"));
        assertThat(root).isNotNull();
    }
}
