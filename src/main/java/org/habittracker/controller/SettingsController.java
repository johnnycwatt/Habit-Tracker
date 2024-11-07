package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.habittracker.Main;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

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
}
