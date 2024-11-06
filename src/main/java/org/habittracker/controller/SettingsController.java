package org.habittracker.controller;

import javafx.fxml.FXML;
import org.habittracker.Main;

public class SettingsController {

    private MainController mainController;
    private Main mainApp;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void enableDarkMode() {
        mainController.enableDarkMode(); // Call method in MainController to enable Dark Mode
    }

    @FXML
    private void disableDarkMode() {
        mainController.disableDarkMode(); // Call method in MainController to disable Dark Mode
    }

    @FXML
    private void goBack() {
        mainController.showMainView(); // Navigate back to the main view
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
}
