package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.service.HabitService;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

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
    private HabitService habitService;
    private Notifier notifier;

    @FXML
    private void initialize() {
        notifier = new NotificationHelper(notificationLabel);
        habitService = new HabitService(notifier);
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
            notifier.showMessage("Please select a habit to edit.", "red");
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
            notifier.showMessage("No habit selected for deletion.", "red");
        }
    }

    @FXML
    private void onMarkAsCompleted() {
        if (selectedHabit != null) {
            habitService.markHabitAsCompleted(selectedHabit);
            loadHabitList(); // Reload list to reflect streak update
        } else {
            notifier.showMessage("Please select a habit to mark as completed.", "red");
        }
    }

    @FXML
    public void onViewProgress() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            notifier.showMessage("Please select a habit to view progress.", "red");
            return;
        }

        String habitName = selectedItem.split(" - ")[0];
        Habit selectedHabit = habitService.findHabitByName(habitName);
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
