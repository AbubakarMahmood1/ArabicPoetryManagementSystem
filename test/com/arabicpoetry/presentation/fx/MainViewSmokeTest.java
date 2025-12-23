package com.arabicpoetry.presentation.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.assertions.api.Assertions.assertThat;

/**
 * Simple smoke test to ensure the JavaFX main view loads.
 */
public class MainViewSmokeTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/arabicpoetry/presentation/fx/MainView.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldRenderMenusAndButtons() {
        MenuBar menuBar = lookup(".menu-bar").queryAs(MenuBar.class);
        assertThat(menuBar.getMenus()).isNotEmpty();
        assertThat(menuBar.getMenus().get(0).getText()).isEqualTo("File");
        assertThat(lookup(".button").nth(0).queryButton().getText()).isEqualTo("Manage Books");
    }

    @Override
    public void stop() throws Exception {
        FxToolkit.hideStage();
    }
}
