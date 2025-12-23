package com.arabicpoetry.presentation.fx.analysis;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.assertions.api.Assertions.assertThat;

/**
 * Simple smoke tests to ensure analysis/import views load.
 */
public class AnalysisViewsTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {
        // Start with an empty scene; each test loads its own view.
        stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 100, 100));
        stage.show();
    }

    @Test
    void similarityViewLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/VerseSimilarityView.fxml"));
        assertThat(root).isNotNull();
    }

    @Test
    void frequencyViewLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/FrequencyAnalysisView.fxml"));
        assertThat(root).isNotNull();
    }

    @Test
    void bookIndexViewLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/BookIndexView.fxml"));
        assertThat(root).isNotNull();
    }

    @Test
    void linguisticWorkbenchViewLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/analysis/LinguisticWorkbenchView.fxml"));
        assertThat(root).isNotNull();
    }
}
