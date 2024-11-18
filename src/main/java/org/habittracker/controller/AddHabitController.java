package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import org.habittracker.util.NotificationColors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddHabitController {

    private static final Logger LOGGER = LogManager.getLogger(AddHabitController.class);
    private static final String CUSTOM_FREQUENCY = "Custom";

    @FXML
    private TextField habitNameField;

    @FXML
    private ChoiceBox<String> frequencyChoiceBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Label notificationLabel;

    @FXML
    private HBox customDaysContainer;

    @FXML
    private ChoiceBox<String> colorChoiceBox;

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


    private final HabitRepository habitRepository;
    private Notifier notifier;

    private Main mainApp;

    public AddHabitController(HabitRepository habitRepository, Notifier notifier) {
        this.habitRepository = habitRepository;
        this.notifier = notifier;
    }

    public AddHabitController() {
        this.habitRepository = HabitRepository.getInstance();
        this.notifier = new NotificationHelper(notificationLabel);
    }


    @FXML
    private void onFrequencyChanged() {
        String selectedFrequency = frequencyChoiceBox.getValue();
        customDaysContainer.setVisible("Custom".equals(selectedFrequency));
    }


    @FXML
    private void initialize() {
        this.notifier = new NotificationHelper(notificationLabel);
        frequencyChoiceBox.setValue("Daily");
        colorChoiceBox.setValue("Black");
        startDatePicker.setValue(LocalDate.now());
        frequencyChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> toggleCustomDaysVisibility("Custom".equals(newValue)));
    }

    @FXML
    private void addHabit() {
        if (!validateInput()) {return;}

        String habitName = habitNameField.getText().trim();
        String frequency = frequencyChoiceBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        if (habitRepository.habitExistsByName(habitName)) {
            LOGGER.warn("Habit with this name already exists");
            notifier.showMessage("A habit with this name already exists. Please choose a different name.", NotificationColors.RED);
            return;
        }

        Habit newHabit = createHabit(habitName, frequency, startDate);

        habitRepository.addHabit(newHabit);
        LOGGER.info("Habit added to repository");
        notifier.showMessage("Habit added successfully!", NotificationColors.GREEN);

        clearForm();
        mainApp.getMainController().updateHabitsDueToday();
    }

    private boolean validateInput() {
        if (isEmptyField(habitNameField.getText())) {
            LOGGER.warn("Habit name is empty");
            notifier.showMessage("Habit name is required!", NotificationColors.RED);
            return false;
        }

        if (frequencyChoiceBox.getValue() == null) {
            LOGGER.warn("Frequency not selected");
            notifier.showMessage("Please select a frequency.", NotificationColors.RED);
            return false;
        }

        if (startDatePicker.getValue() == null) {
            LOGGER.warn("Start date not selected");
            notifier.showMessage("Please select a start date.", NotificationColors.RED);
            return false;
        }

        return true;
    }


    private Habit createHabit(String habitName, String frequency, LocalDate startDate) {
        Habit newHabit = new Habit(habitName, Habit.Frequency.valueOf(frequency.toUpperCase(Locale.ROOT)));
        newHabit.setCreationDate(startDate);

        String selectedColorHex = getColorHexCode(colorChoiceBox.getValue());
        newHabit.setColor(selectedColorHex);

        if (CUSTOM_FREQUENCY.equals(frequency)) {
            newHabit.setCustomDays(getSelectedCustomDays());
        }

        return newHabit;
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
            default -> "#000000";
        };
    }

    private void toggleCustomDaysVisibility(boolean visible) {
        customDaysContainer.setVisible(visible);
    }

    private boolean isEmptyField(String input) {
        return input == null || input.isBlank(); // More efficient than trim().isEmpty()
    }

    private void clearForm() {
        habitNameField.clear();
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showMainView();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
}
