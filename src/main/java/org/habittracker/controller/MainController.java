package org.habittracker.controller;

import javafx.fxml.FXML;
import org.habittracker.Main;

public class MainController {

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void openAddHabitView() {
        mainApp.openAddHabitView();
    }

    @FXML
    private void openHabitListView() {
        mainApp.openHabitListView();
    }

}
