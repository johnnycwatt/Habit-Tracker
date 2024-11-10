package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.service.HabitService;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

import java.time.LocalDate;
import java.util.Arrays;
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
        setupHabitListView();
        loadHabitList();
    }

    private void setupHabitListView() {
        habitListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Split item text to separate the habit name and details
                    String[] parts = item.split(" - ");
                    String habitName = parts[0];
                    String details = String.join(" - ", Arrays.copyOfRange(parts, 1, parts.length));

                    VBox cellLayout = new VBox(5);
                    Label nameLabel = new Label(habitName);
                    nameLabel.getStyleClass().add("habit-name");

                    Label detailsLabel = new Label(details);
                    detailsLabel.getStyleClass().add("habit-details");

                    cellLayout.getChildren().addAll(nameLabel, detailsLabel);
                    setGraphic(cellLayout);

                    selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                        if (isSelected) {
                            // Style for selected cell
                            setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #333333; -fx-font-weight: bold;");
                            nameLabel.setStyle("-fx-text-fill: #333333;");
                            detailsLabel.setStyle("-fx-text-fill: #666666;");
                        } else {
                            // Styled for non-selected cell
                            setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
                            nameLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");
                            detailsLabel.setStyle("-fx-text-fill: #666666;");
                        }
                    });

                    if (isSelected()) {
                        setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #333333; -fx-font-weight: bold;");
                        nameLabel.setStyle("-fx-text-fill: #333333;");
                        detailsLabel.setStyle("-fx-text-fill: #666666;");
                    } else {
                        setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
                        nameLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");
                        detailsLabel.setStyle("-fx-text-fill: #666666;");
                    }
                }
            }
        });
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
            // Check if the habit is custom and today is outside of specified days
            if (selectedHabit.getFrequency() == Habit.Frequency.CUSTOM &&
                    !selectedHabit.getCustomDays().contains(LocalDate.now().getDayOfWeek())) {
                notifier.showMessage("Today is not part of your specified habit days. Marking completion may affect statistics. Good work on getting the habit done! That is the much more important!", "orange");
            }
            habitService.markHabitAsCompleted(selectedHabit);
            loadHabitList();
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
