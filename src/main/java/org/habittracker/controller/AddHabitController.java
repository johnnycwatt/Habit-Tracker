package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.habittracker.util.NotificationHelper;

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

    private HabitRepository habitRepository = new HabitRepository();

    @FXML
    private Label notificationLabel;

    private Notifier notifier; // Use the Notifier interface for flexibility

    @FXML
    private HBox customDaysContainer;

    @FXML
    private ChoiceBox<String> colorChoiceBox;

    @FXML
    private ToggleButton mondayToggle, tuesdayToggle, wednesdayToggle, thursdayToggle, fridayToggle, saturdayToggle, sundayToggle;

    @FXML
    private void onFrequencyChanged() {
        String selectedFrequency = frequencyChoiceBox.getValue();
        customDaysContainer.setVisible("Custom".equals(selectedFrequency));
    }

    @FXML
    private void initialize() {
        frequencyChoiceBox.setValue("Daily");
        notifier = new NotificationHelper(notificationLabel); // Initialize notifier with NotificationHelper
        colorChoiceBox.setValue("Black");
        startDatePicker.setValue(LocalDate.now());
    }

    public AddHabitController() {
        // This constructor is required for FXML loading
    }

    public AddHabitController(HabitRepository habitRepository, Notifier notifier) {
        this.habitRepository = habitRepository;
        this.notifier = notifier;
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

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showMainView();
    }

    @FXML
    void addHabit() {
        String habitName = habitNameField.getText();
        String frequency = frequencyChoiceBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        // validate input
        if (habitName == null || habitName.trim().isEmpty()) {
            System.out.println("Habit name is empty");
            notifier.showMessage("Habit name is required!", "red");
            return;
        }
        if (frequency == null) {
            System.out.println("Frequency not selected");
            notifier.showMessage("Please select a frequency.", "red");
            return;
        }
        if (startDate == null) {
            System.out.println("Start date not selected");
            notifier.showMessage("Please select a start date.", "red");
            return;
        }

        // Check for duplicate habit name
        if (habitRepository.habitExistsByName(habitName)) {
            System.out.println("Habit with this name already exists");
            notifier.showMessage("A habit with this name already exists. Please choose a different name.", "red");
            return;
        }

        System.out.println("Creating new habit");
        Habit newHabit = new Habit(habitName, Habit.Frequency.valueOf(frequency.toUpperCase()));
        newHabit.setCreationDate(startDate);

        String selectedColorName = colorChoiceBox.getValue();
        String selectedColorHex = getColorHexCode(selectedColorName);
        newHabit.setColor(selectedColorHex);

        if ("Custom".equals(frequency)) {
            List<DayOfWeek> selectedDays = new ArrayList<>();
            if (mondayToggle.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
            if (tuesdayToggle.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
            if (wednesdayToggle.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
            if (thursdayToggle.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
            if (fridayToggle.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
            if (saturdayToggle.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
            if (sundayToggle.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);
            newHabit.setCustomDays(selectedDays);
        }

        habitRepository.addHabit(newHabit);
        System.out.println("Habit added to repository");
        notifier.showMessage("Habit added successfully!", "green");
        clearForm();
        mainApp.getMainController().updateHabitsDueToday();
    }

    private void clearForm() {
        habitNameField.clear();
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
    }
}
