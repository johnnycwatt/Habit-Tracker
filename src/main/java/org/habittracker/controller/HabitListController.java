package org.habittracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

public class HabitListController {

    @FXML
    private TextField editHabitNameField;

    @FXML
    private ChoiceBox<String> editFrequencyChoiceBox;

    @FXML
    private DatePicker editStartDatePicker;

    @FXML
    private ListView<String> habitListView;

    private Habit selectedHabit;

    private final HabitRepository habitRepository = new HabitRepository();

    @FXML
    private void initialize() {
        loadHabitList();
    }

    private void loadHabitList() {
        habitListView.getItems().clear();
        List<Habit> habits = habitRepository.getAllHabits();
        for (Habit habit : habits) {
            habitListView.getItems().add(habit.getName() + " - " + habit.getFrequency() + " - " + habit.getCreationDate());
        }
    }

    @FXML
    public void onEditHabit(ActionEvent event) {
        // Get the selected item from the ListView
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            // If no habit is selected, show an alert
            Alert alert = new Alert(AlertType.WARNING, "Please select a habit to edit.");
            alert.showAndWait();
            return;
        }

        // Find the selected Habit based on its name
        String habitName = selectedItem.split(" - ")[0];
        selectedHabit = habitRepository.findHabitByName(habitName);

        if (selectedHabit != null) {
            // Load the Edit Habit view and populate it with the selected habit’s data
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddHabitView.fxml"));
                Parent editHabitRoot = loader.load();

                // Pass the selected habit to the AddHabitController for editing
                AddHabitController addHabitController = loader.getController();
                addHabitController.setHabitData(selectedHabit);  // Method to pre-fill data

                // Create a new window for editing the habit
                Stage editStage = new Stage();
                editStage.setScene(new Scene(editHabitRoot));
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.setTitle("Edit Habit");
                editStage.showAndWait();

                // Refresh the list view after editing
                loadHabitList();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error loading Habit edit view: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onHabitSelected() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Find the habit based on its name in the selected item
            String habitName = selectedItem.split(" - ")[0];
            selectedHabit = habitRepository.findHabitByName(habitName);

            if (selectedHabit != null) {
                // Populate the edit fields with the selected habit’s data
                editHabitNameField.setText(selectedHabit.getName());
                editFrequencyChoiceBox.setValue(selectedHabit.getFrequency().toString());
                editStartDatePicker.setValue(selectedHabit.getCreationDate());
            }
        }
    }

    @FXML
    private void onDeleteHabit() {
        if (selectedHabit != null) {
            habitRepository.deleteHabit(selectedHabit);
            habitListView.getItems().remove(habitListView.getSelectionModel().getSelectedItem());
            selectedHabit = null; // Clear selection
            clearEditForm();
            System.out.println("Habit deleted successfully!");
        } else {
            System.out.println("No habit selected for deletion.");
        }
    }

    @FXML
    private void onSaveChanges() {
        if (selectedHabit != null) {
            // Update habit details with data from the form
            selectedHabit.setName(editHabitNameField.getText());
            selectedHabit.setFrequency(Habit.Frequency.valueOf(editFrequencyChoiceBox.getValue().toUpperCase()));
            selectedHabit.setCreationDate(editStartDatePicker.getValue());

            // Persist changes to the database
            habitRepository.updateHabit(selectedHabit);

            // Refresh the list view and clear the form
            loadHabitList();
            selectedHabit = null;
            clearEditForm();
            System.out.println("Habit updated successfully!");
        } else {
            System.out.println("No habit selected for editing.");
        }
    }

    private void clearEditForm() {
        editHabitNameField.clear();
        editFrequencyChoiceBox.setValue("Daily");
        editStartDatePicker.setValue(null);
    }

    @FXML
    private void closeHabitList() {
        // Close the current window
        Stage stage = (Stage) habitListView.getScene().getWindow();
        stage.close();
    }
}
