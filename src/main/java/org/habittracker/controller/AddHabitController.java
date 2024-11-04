package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddHabitController {

    @FXML
    private TextField habitNameField;

    @FXML
    private ChoiceBox<String> frequencyChoiceBox;

    @FXML
    private DatePicker startDatePicker;

    private final HabitRepository habitRepository = new HabitRepository();

    @FXML
    private Label confirmationMessageLabel;

    @FXML
    private HBox customDaysContainer;
    @FXML
    private CheckBox mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, fridayCheckBox, saturdayCheckBox, sundayCheckBox;

    @FXML
    private void onFrequencyChanged() {
        String selectedFrequency = frequencyChoiceBox.getValue();
        customDaysContainer.setVisible("Custom".equals(selectedFrequency));
    }

    @FXML
    private void initialize() {
        frequencyChoiceBox.setValue("Daily");
    }

    private Main mainApp;


    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showMainView();
    }



    @FXML
    private void addHabit() {
        String habitName = habitNameField.getText();
        String frequency = frequencyChoiceBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        // validate input
        if (habitName == null || habitName.trim().isEmpty()) {
            System.out.println("Habit name is required!");
            return;
        }
        if (frequency == null) {
            System.out.println("Please select a frequency.");
            return;
        }
        if (startDate == null) {
            System.out.println("Please select a start date.");
            return;
        }

        // Check for duplicate habit name
        if (habitRepository.habitExistsByName(habitName)) {
            showAlert("A habit with this name already exists. Please choose a different name.");
            return;
        }

        Habit newHabit = new Habit(habitName, Habit.Frequency.valueOf(frequency.toUpperCase()));
        newHabit.setCreationDate(startDate);

        if ("Custom".equals(frequency)) {
            List<DayOfWeek> selectedDays = new ArrayList<>();
            if (mondayCheckBox.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
            if (tuesdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
            if (wednesdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
            if (thursdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
            if (fridayCheckBox.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
            if (saturdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
            if (sundayCheckBox.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);
            newHabit.setCustomDays(selectedDays);
        }



        habitRepository.addHabit(newHabit);
        showConfirmationMessage();
        clearForm();
        mainApp.getMainController().updateHabitsDueToday();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Duplicate Habit");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmationMessage() {
        confirmationMessageLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Hide the message after 5 seconds
                confirmationMessageLabel.setVisible(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clearForm() {
        habitNameField.clear();
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(null);
    }

    public void setHabitData(Habit habit) {
        if (habit != null) {
            habitNameField.setText(habit.getName());
            frequencyChoiceBox.setValue(habit.getFrequency().toString());
            startDatePicker.setValue(habit.getCreationDate());
        }
    }
}
