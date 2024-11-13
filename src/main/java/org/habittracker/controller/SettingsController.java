package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.util.BackupScheduler;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class SettingsController {

    MainController mainController;
    private Main mainApp;

    @FXML
    Label notificationLabel;

    Notifier notifier;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    void enableDarkMode() {
        mainController.enableDarkMode();
        notifier.showMessage("Dark Mode Enabled", "green");
    }

    @FXML
    void disableDarkMode() {
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
    void goBack() {
        mainController.showMainView();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void backupDataToJson() {
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

    @FXML
    private void enableAutoBackup() {
        List<Habit> habits = mainController.getAllHabits();
        BackupScheduler.enableAutoBackup(habits);
        notifier.showMessage("Automatic Backup Enabled", "green");
    }

    @FXML
    private void disableAutoBackup() {
        BackupScheduler.disableAutoBackup();
        notifier.showMessage("Automatic Backup Disabled", "red");
    }


    @FXML
    void restoreDataFromJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File to Restore");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            String filePath = file.getAbsolutePath();
            JsonBackupHelper.restoreDataFromJson(filePath);
            notifier.showMessage("Data restored successfully from " + filePath, "green");
        } else {
            notifier.showMessage("Data restore cancelled", "red");
        }
    }

    @FXML
    void openHelp() {
        if (mainController != null) {
            mainController.openHelp();
        }
    }

}
