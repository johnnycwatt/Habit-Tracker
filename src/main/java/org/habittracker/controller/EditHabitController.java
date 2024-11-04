package org.habittracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.util.Optional;

public class EditHabitController {

    @FXML
    private TextField editHabitNameField;

    @FXML
    private ChoiceBox<String> frequencyChoiceBox;

    @FXML
    private DatePicker startDatePicker;

    private HabitRepository habitRepository;
    private Habit habit;

    public void setHabit(Habit habit) {
        this.habit = habit;
        editHabitNameField.setText(habit.getName());
        frequencyChoiceBox.setValue(habit.getFrequency().toString());
        startDatePicker.setValue(habit.getCreationDate());
    }

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    @FXML
    private void onSaveChanges(ActionEvent event) {
        if (habit != null && habitRepository != null) {
            String newName = editHabitNameField.getText();

            Habit existingHabit = habitRepository.findHabitByName(newName);
            if (existingHabit != null && !existingHabit.getId().equals(habit.getId())) {
                // A habit with the new name exists and it is not the current habit being edited
                Alert alert = new Alert(Alert.AlertType.WARNING, "A habit with this name already exists. Please choose a different name.");
                alert.showAndWait();
                return; // Exit without saving changes
            }


            // Update the habit with the new values
            habit.setName(editHabitNameField.getText());
            habit.setFrequency(Habit.Frequency.valueOf(frequencyChoiceBox.getValue().toUpperCase()));
            habit.setCreationDate(startDatePicker.getValue());
            System.out.println("Test in If Statement");

            habitRepository.updateHabit(habit);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Habit updated successfully!");
            alert.showAndWait();
            closeWindow();
        }
    }


    @FXML
    private void onCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to discard changes?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) editHabitNameField.getScene().getWindow();
        stage.close();
    }
}
