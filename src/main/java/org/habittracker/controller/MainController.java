package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MainController {
    @FXML
    private void handleAddHabit() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Habit Added");
        alert.setHeaderText(null);
        alert.setContentText("New habit has been added! Well done, you have taken the first step!");
        alert.showAndWait();
    }
}
