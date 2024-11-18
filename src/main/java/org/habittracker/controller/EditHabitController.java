package org.habittracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import org.habittracker.util.NotificationColors;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EditHabitController {

    private static final String FREQUENCY_CUSTOM = "Custom";
    private static final String DEFAULT_COLOR = "#000000";

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

    private Notifier notifier;

    @FXML
    private ToggleButton mondayToggle;
    @FXML
    private ToggleButton tuesdayToggle;
    @FXML
    private ToggleButton wednesdayToggle;
    @FXML
    private ToggleButton thursdayToggle;
    @FXML
    private ToggleButton fridayToggle;
    @FXML
    private ToggleButton saturdayToggle;
    @FXML
    private ToggleButton sundayToggle;


    private HabitRepository habitRepository;
    private Habit habit;
    private Main mainApp;

    public void setHabit(Habit habit) {
        this.habit = habit;
        editHabitNameField.setText(habit.getName());
        frequencyChoiceBox.setValue(habit.getFrequency().toString());
        startDatePicker.setValue(habit.getCreationDate());
        colorChoiceBox.setValue(getColorNameFromHex(habit.getColor()));

        if (FREQUENCY_CUSTOM.equalsIgnoreCase(habit.getFrequency().toString())) {
            customDaysContainer.setVisible(true);
            updateCustomDayToggles(habit.getCustomDays());
        } else {
            customDaysContainer.setVisible(false);
            clearCustomDayToggles();
        }
    }

    @FXML
    void initialize() {
        notifier = new NotificationHelper(notificationLabel);
    }

    private void updateCustomDayToggles(List<DayOfWeek> customDays) {
        clearCustomDayToggles();
        if (customDays != null) {
            customDays.forEach(day -> {
                switch (day) {
                    case MONDAY -> mondayToggle.setSelected(true);
                    case TUESDAY -> tuesdayToggle.setSelected(true);
                    case WEDNESDAY -> wednesdayToggle.setSelected(true);
                    case THURSDAY -> thursdayToggle.setSelected(true);
                    case FRIDAY -> fridayToggle.setSelected(true);
                    case SATURDAY -> saturdayToggle.setSelected(true);
                    case SUNDAY -> sundayToggle.setSelected(true);
                }
            });
        }
    }

    private void clearCustomDayToggles() {
        mondayToggle.setSelected(false);
        tuesdayToggle.setSelected(false);
        wednesdayToggle.setSelected(false);
        thursdayToggle.setSelected(false);
        fridayToggle.setSelected(false);
        saturdayToggle.setSelected(false);
        sundayToggle.setSelected(false);
    }

    private String getColorHexCode(String colorName) {
        return switch (colorName) {
            case "Black" -> "#000000";
            case "Red" -> "#FF0000";
            case "Green" -> "#008000";
            case "Blue" -> "#0000FF";
            case "Magenta" -> "#FF00FF";
            case "Yellow" -> "#CCCC00";
            case "Orange" -> "#FFA500";
            case "Cyan" -> "#009999";
            default -> DEFAULT_COLOR;
        };
    }

    private String getColorNameFromHex(String colorHex) {
        return switch (colorHex) {
            case "#000000" -> "Black";
            case "#FF0000" -> "Red";
            case "#008000" -> "Green";
            case "#0000FF" -> "Blue";
            case "#FF00FF" -> "Magenta";
            case "#CCCC00" -> "Yellow";
            case "#FFA500" -> "Orange";
            case "#009999" -> "Cyan";
            default -> "Black";
        };
    }

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    @FXML
    private void onSaveChanges(ActionEvent event) {
        if (habit == null || habitRepository == null) {return;}

        String newName = editHabitNameField.getText();
        if (!isHabitNameValid(newName))  {return;}

        habit.setName(newName);
        habit.setCreationDate(startDatePicker.getValue());
        habit.setColor(getColorHexCode(colorChoiceBox.getValue()));

        updateHabitFrequencyAndDays();

        habitRepository.updateHabit(habit);
        notifier.showMessage("Habit updated successfully!", NotificationColors.GREEN);
        goBack();
    }

    private boolean isHabitNameValid(String habitName) {
        if (habitName == null || habitName.isBlank()) {
            notifier.showMessage("Habit name is required!", NotificationColors.RED);
            return false;
        }
        return true;
    }

    private void updateHabitFrequencyAndDays() {
        String selectedFrequency = frequencyChoiceBox.getValue().toUpperCase(Locale.ROOT);
        habit.setFrequency(Habit.Frequency.valueOf(selectedFrequency));

        if (FREQUENCY_CUSTOM.equals(frequencyChoiceBox.getValue())) {
            habit.setCustomDays(getSelectedCustomDays());
        } else {
            habit.setCustomDays(null);
        }
    }

    private List<DayOfWeek> getSelectedCustomDays() {
        List<DayOfWeek> selectedDays = new ArrayList<>();
        if (mondayToggle.isSelected()) {selectedDays.add(DayOfWeek.MONDAY);}
        if (tuesdayToggle.isSelected()) {selectedDays.add(DayOfWeek.TUESDAY);}
        if (wednesdayToggle.isSelected()) {selectedDays.add(DayOfWeek.WEDNESDAY);}
        if (thursdayToggle.isSelected()) {selectedDays.add(DayOfWeek.THURSDAY);}
        if (fridayToggle.isSelected()) {selectedDays.add(DayOfWeek.FRIDAY);}
        if (saturdayToggle.isSelected()) {selectedDays.add(DayOfWeek.SATURDAY);}
        if (sundayToggle.isSelected()) {selectedDays.add(DayOfWeek.SUNDAY);}
        return selectedDays;
    }

    @FXML
    private void onFrequencyChanged() {
        customDaysContainer.setVisible(FREQUENCY_CUSTOM.equals(frequencyChoiceBox.getValue()));
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Alert alert = createConfirmationAlert("Are you sure you want to discard changes?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            goBack();
        }
    }

    protected Alert createConfirmationAlert(String message) {
        return new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showHabitListView();
    }
}
