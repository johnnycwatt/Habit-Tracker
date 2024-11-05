package org.habittracker.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;

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
    private Label notificationLabel;

    @FXML
    private ListView<String> habitListView;

    private Habit selectedHabit;

    private final HabitRepository habitRepository = new HabitRepository();
    private Main mainApp;
    private NotificationHelper notificationHelper;



    @FXML
    private void initialize() {
        loadHabitList();
        notificationHelper = new NotificationHelper(notificationLabel);
    }

    private void loadHabitList() {
        habitListView.getItems().clear();
        List<Habit> habits = habitRepository.getAllHabits();
        for (Habit habit : habits) {
            String streakInfo = " (Streak: " + habit.getStreakCounter() + ")";
            habitListView.getItems().add(habit.getName() + " - " + habit.getFrequency() + " - " + habit.getCreationDate()+ " - " + streakInfo);
        }
    }

    @FXML
    public void onEditHabit() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            notificationHelper.showTemporaryMessage("Please select a habit to edit.", "red");
            return;
        }

        String habitName = selectedItem.split(" - ")[0]; // Extract the habit name
        Habit selectedHabit = habitRepository.findHabitByName(habitName);

        mainApp.getMainController().showEditHabitView(selectedHabit); // Use mainApp to navigate
    }



    @FXML
    private void onHabitSelected() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Find the habit based on its name in the selected item
            String habitName = selectedItem.split(" - ")[0];
            selectedHabit = habitRepository.findHabitByName(habitName);
        }
    }

    @FXML
    private void onDeleteHabit() {
        if (selectedHabit != null) {
            habitRepository.deleteHabit(selectedHabit);
            habitListView.getItems().remove(habitListView.getSelectionModel().getSelectedItem());
            habitListView.getSelectionModel().clearSelection(); // Clear the selection
            selectedHabit = null; // Clear the habit reference
            notificationHelper.showTemporaryMessage("Habit deleted successfully!", "green");
        } else {
            notificationHelper.showTemporaryMessage("No habit selected for deletion.", "red");
        }
    }


    @FXML
    private void onMarkAsCompleted() {
        if (selectedHabit != null) {
            if (selectedHabit.isCompletedToday()) {
                notificationHelper.showTemporaryMessage("Habit already marked as completed for today.", "red");
                return;
            }

            // Mark the habit as completed and update the repository
            selectedHabit.markAsCompleted();  // handles streak update logic
            habitRepository.updateHabit(selectedHabit);

            loadHabitList();
            notificationHelper.showTemporaryMessage("Habit marked as completed for today! Streak: " + selectedHabit.getStreakCounter(), "green");
        } else {
            notificationHelper.showTemporaryMessage("Please select a habit to mark as completed.", "red");
        }
    }

    @FXML
    public void onViewProgress() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            notificationHelper.showTemporaryMessage("Please select a habit to view progress.", "red");
            return;
        }

        String habitName = selectedItem.split(" - ")[0]; // Extract the habit name
        Habit selectedHabit = habitRepository.findHabitByName(habitName);

        mainApp.getMainController().showProgressView(selectedHabit);
    }


    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }


    @FXML
    private void goBack() {
        mainApp.getMainController().showMainView();
    }
}
