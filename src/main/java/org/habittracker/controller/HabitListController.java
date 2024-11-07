package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.service.HabitService;
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
    private Main mainApp;
    private HabitService habitService; // Use HabitService instead of HabitRepository directly

    @FXML
    private void initialize() {
        habitService = new HabitService(notificationLabel); // Initialize HabitService with notificationLabel
        loadHabitList();
    }

    private void loadHabitList() {
        habitListView.getItems().clear();
        List<Habit> habits = habitService.getAllHabits();
        for (Habit habit : habits) {
            String streakInfo = " (Streak: " + habit.getStreakCounter() + ")";
            habitListView.getItems().add(habit.getName() + " - " + habit.getFrequency() + " - " + habit.getCreationDate() + " - " + streakInfo);
        }
    }

    @FXML
    public void onEditHabit() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            habitService.getNotificationHelper().showTemporaryMessage("Please select a habit to edit.", "red");
            return;
        }

        String habitName = selectedItem.split(" - ")[0];
        Habit selectedHabit = habitService.findHabitByName(habitName);
        mainApp.getMainController().showEditHabitView(selectedHabit);
    }

    @FXML
    private void onHabitSelected() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String habitName = selectedItem.split(" - ")[0];
            selectedHabit = habitService.findHabitByName(habitName);
        }
    }

    @FXML
    private void onDeleteHabit() {
        if (selectedHabit != null) {
            habitService.deleteHabit(selectedHabit);
            loadHabitList();
            selectedHabit = null;
        } else {
            habitService.getNotificationHelper().showTemporaryMessage("No habit selected for deletion.", "red");
        }
    }

    @FXML
    private void onMarkAsCompleted() {
        if (selectedHabit != null) {
            habitService.markHabitAsCompleted(selectedHabit);
            loadHabitList(); // Reload list to reflect streak update
        } else {
            habitService.getNotificationHelper().showTemporaryMessage("Please select a habit to mark as completed.", "red");
        }
    }

    @FXML
    public void onViewProgress() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            habitService.getNotificationHelper().showTemporaryMessage("Please select a habit to view progress.", "red");
            return;
        }

        String habitName = selectedItem.split(" - ")[0];
        Habit selectedHabit = habitService.findHabitByName(habitName); // Updated to use HabitService
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
