package org.habittracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private ChoiceBox<String> colorChoiceBox;

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
        String habitColorName = getColorNameFromHex(habit.getColor());
        colorChoiceBox.setValue(habitColorName);

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

    private String getColorHexCode(String colorName) {
        switch (colorName) {
            case "Black": return "#000000";
            case "Red": return "#FF0000";
            case "Green": return "#008000";
            case "Blue": return "#0000FF";
            case "Magenta": return "#FF00FF";
            case "Yellow": return "#FFFF00";
            case "Orange": return "#FFA500";
            case "Cyan": return "#00FFFF";
            default: return "#000000";
        }
    }

    private String getColorNameFromHex(String colorHex) {
        switch (colorHex) {
            case "#000000": return "Black";
            case "#FF0000": return "Red";
            case "#008000": return "Green";
            case "#0000FF": return "Blue";
            case "#FF00FF": return "Magenta";
            case "#FFFF00": return "Yellow";
            case "#FFA500": return "Orange";
            case "#00FFFF": return "Cyan";
            default: return "Black";
        }
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

            String selectedColorHex = getColorHexCode(colorChoiceBox.getValue());
            habit.setColor(selectedColorHex);



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
