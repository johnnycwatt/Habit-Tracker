package org.habittracker.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditHabitController {

    @FXML
    private TextField editHabitNameField;

    @FXML
    private ChoiceBox<String> frequencyChoiceBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private HBox customDaysContainer;
    @FXML
    private Label notificationLabel;

    private NotificationHelper notificationHelper;

    @FXML
    private CheckBox mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, fridayCheckBox, saturdayCheckBox, sundayCheckBox;

    private HabitRepository habitRepository;
    private Habit habit;
    private Main mainApp;

    public void setHabit(Habit habit) {
        this.habit = habit;
        editHabitNameField.setText(habit.getName());
        frequencyChoiceBox.setValue(habit.getFrequency().toString());
        startDatePicker.setValue(habit.getCreationDate());

        if ("Custom".equals(habit.getFrequency().toString())) {
            customDaysContainer.setVisible(true);
            // Check the boxes for the custom days that were previously set
            if (habit.getCustomDays() != null) {
                habit.getCustomDays().forEach(day -> {
                    switch (day) {
                        case MONDAY -> mondayCheckBox.setSelected(true);
                        case TUESDAY -> tuesdayCheckBox.setSelected(true);
                        case WEDNESDAY -> wednesdayCheckBox.setSelected(true);
                        case THURSDAY -> thursdayCheckBox.setSelected(true);
                        case FRIDAY -> fridayCheckBox.setSelected(true);
                        case SATURDAY -> saturdayCheckBox.setSelected(true);
                        case SUNDAY -> sundayCheckBox.setSelected(true);
                    }
                });
            }
        }
    }

    @FXML
    private void initialize() {
        notificationHelper = new NotificationHelper(notificationLabel);
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
                notificationHelper.showTemporaryMessage("A habit with this name already exists. Please choose a different name.", "red");
                return;
            }

            if (newName == null || newName.trim().isEmpty()) {
                notificationHelper.showTemporaryMessage("Habit name is required!", "red");
                return;
            }

            habit.setName(newName);
            habit.setFrequency(Habit.Frequency.valueOf(frequencyChoiceBox.getValue().toUpperCase()));
            habit.setCreationDate(startDatePicker.getValue());



            if ("Custom".equals(frequencyChoiceBox.getValue())) {
                List<DayOfWeek> selectedDays = new ArrayList<>();
                if (mondayCheckBox.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
                if (tuesdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
                if (wednesdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
                if (thursdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
                if (fridayCheckBox.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
                if (saturdayCheckBox.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
                if (sundayCheckBox.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);
                habit.setCustomDays(selectedDays);
            } else {
                habit.setCustomDays(null); // Clear custom days if not custom frequency
            }

            habitRepository.updateHabit(habit);


            notificationHelper.showTemporaryMessage("Habit updated successfully!", "green");
            goBack();
        }
    }

    @FXML
    private void onFrequencyChanged() {
        String selectedFrequency = frequencyChoiceBox.getValue();
        customDaysContainer.setVisible("Custom".equals(selectedFrequency));
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to discard changes?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            goBack();
        }
    }
    
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showHabitListView();
    }
}
