/*
 * MIT License
 *
 * Copyright (c) 2024 Johnny Chadwick-Watt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.BackupScheduler;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.NotificationColors;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

import java.io.File;
import java.util.List;

public class SettingsController {

    private MainController mainController;
    private Main mainApp;
    Notifier notifier;

    @FXML
    Label notificationLabel;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void backupDataToJson() {
        List<Habit> habits = HabitRepository.getInstance().getAllHabits();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            String filePath = file.getAbsolutePath();
            JsonBackupHelper.backupHabitsToJson(habits, filePath);
            notifier.showMessage("Backup created successfully", NotificationColors.GREEN);
        } else {
            notifier.showMessage("Backup cancelled", NotificationColors.RED);
        }
    }

    @FXML
    private void enableAutoBackup() {
        List<Habit> habits = HabitRepository.getInstance().getAllHabits();
        BackupScheduler.enableAutoBackup(habits);
        notifier.showMessage("Automatic Backup Enabled", NotificationColors.GREEN);
    }

    @FXML
    private void disableAutoBackup() {
        BackupScheduler.disableAutoBackup();
        notifier.showMessage("Automatic Backup Disabled", NotificationColors.RED);
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
            notifier.showMessage("Data restored successfully from " + filePath, NotificationColors.GREEN);
        } else {
            notifier.showMessage("Data restore cancelled", NotificationColors.RED);
        }
    }

    @FXML
    void enableDarkMode() {
        mainController.enableDarkMode();
        notifier.showMessage("Dark Mode Enabled", NotificationColors.GREEN);
    }

    @FXML
    void disableDarkMode() {
        mainController.disableDarkMode();
        notifier.showMessage("Dark Mode Disabled", NotificationColors.RED);
    }

    @FXML
    public void enableReminders() {
        mainController.getReminderScheduler().setRemindersEnabled(true);
        notifier.showMessage("Habit Reminders Enabled", NotificationColors.GREEN);
    }

    @FXML
    public void disableReminders() {
        mainController.getReminderScheduler().setRemindersEnabled(false);
        notifier.showMessage("Habit Reminders Disabled", NotificationColors.RED);
    }

    @FXML
    void goBack() {
        mainController.showMainView();
    }

    @FXML
    void openHelp() {
        mainController.openHelp();
    }
}
