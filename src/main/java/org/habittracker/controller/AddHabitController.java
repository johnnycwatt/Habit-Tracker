package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.time.LocalDate;

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
    private void initialize() {
        frequencyChoiceBox.setValue("Daily");
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

        // Create new Habit instance and save it to the repository
        Habit.Frequency habitFrequency = Habit.Frequency.valueOf(frequency.toUpperCase());
        Habit newHabit = new Habit(habitName, habitFrequency);
        newHabit.setCreationDate(startDate);

        habitRepository.addHabit(newHabit);

        System.out.println("Habit added: " + newHabit);
        showConfirmationMessage();
        clearForm();
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
