package org.habittracker.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.util.BackupScheduler;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SettingsControllerTest {

    @Mock
    private MainController mainController;

    @Mock
    private Main mainApp;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private SettingsController settingsController;

    @BeforeEach
    void setUp() {
        JavaFxInitializer.initToolkit();
        MockitoAnnotations.openMocks(this);
        settingsController.setMainController(mainController);
        settingsController.setMainApp(mainApp);

        settingsController.notificationLabel = new Label();
        settingsController.notifier = notifier;
    }

    @Test
    @Tag("JavaFX")
    void testEnableDarkMode() {
        mainController.rootStackPane = new StackPane();
        Scene scene = new Scene(mainController.rootStackPane);
        settingsController.enableDarkMode();

        verify(mainController, times(1)).enableDarkMode();
        verify(notifier, times(1)).showMessage("Dark Mode Enabled", "green");
    }

    @Test
    @Tag("JavaFX")
    void testDisableDarkMode() {
        mainController.rootStackPane = new StackPane();
        Scene scene = new Scene(mainController.rootStackPane);
        settingsController.disableDarkMode();

        verify(mainController, times(1)).disableDarkMode();
        verify(notifier, times(1)).showMessage("Dark Mode Disabled", "red");
    }

    @Test
    @Tag("JavaFX")
    void testEnableReminders() {
        settingsController.enableReminders();

        verify(mainController, times(1)).setRemindersEnabled(true);
        verify(notifier, times(1)).showMessage("Habit Reminders Enabled", "green");
    }

    @Test
    @Tag("JavaFX")
    void testDisableReminders() {
        settingsController.disableReminders();

        verify(mainController, times(1)).setRemindersEnabled(false);
        verify(notifier, times(1)).showMessage("Habit Reminders Disabled", "red");
    }

    @Test
    @Tag("JavaFX")
    void testGoBack() {
        settingsController.goBack();
        verify(mainController, times(1)).showMainView();
    }
}
