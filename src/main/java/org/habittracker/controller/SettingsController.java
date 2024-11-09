package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class SettingsController {

    private MainController mainController;
    private Main mainApp;

    @FXML
    private Label notificationLabel;

    private Notifier notifier;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    private void enableDarkMode() {
        mainController.enableDarkMode();
        notifier.showMessage("Dark Mode Enabled", "green");
    }

    @FXML
    private void disableDarkMode() {
        mainController.disableDarkMode();
        notifier.showMessage("Dark Mode Disabled", "red");
    }

    @FXML
    public void enableReminders() {
        mainController.setRemindersEnabled(true);
        notifier.showMessage("Habit Reminders Enabled", "green");
    }

    @FXML
    public void disableReminders() {
        mainController.setRemindersEnabled(false);
        notifier.showMessage("Habit Reminders Disabled", "red");
    }

    @FXML
    private void goBack() {
        mainController.showMainView();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void backupDataToJson() {
        List<Habit> habits = mainController.getAllHabits();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            String filePath = file.getAbsolutePath();
            JsonBackupHelper.backupHabitsToJson(habits, filePath);
            notifier.showMessage("Backup created successfully " , "green");
        } else {
            notifier.showMessage("Backup cancelled", "red");
        }
    }
}
