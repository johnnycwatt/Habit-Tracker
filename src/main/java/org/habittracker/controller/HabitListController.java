package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.service.HabitService;
import org.habittracker.util.NotificationColors;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class HabitListController {

    private static final String ITEM_SEPARATOR = " - ";
    private static final String DETAILS_TEXT_STYLE = "-fx-text-fill: #666666;";

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
    MainController mainController;

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
                    clearCell();
                } else {
                    configureCell(item);
                }
            }

            private void clearCell() {
                setText(null);
                setGraphic(null);
                setStyle("");
            }

            private void configureCell(String item) {
                VBox cellLayout = createCellLayout(item);
                setGraphic(cellLayout);
                updateCellStyle();
            }

            private VBox createCellLayout(String item) {
                String[] parts = item.split(ITEM_SEPARATOR);
                String habitName = parts[0];
                String details = String.join(ITEM_SEPARATOR, Arrays.copyOfRange(parts, 1, parts.length));

                VBox cellLayout = new VBox(5);
                Label nameLabel = createLabel(habitName, "habit-name");
                Label detailsLabel = createLabel(details, "habit-details");
                cellLayout.getChildren().addAll(nameLabel, detailsLabel);
                return cellLayout;
            }

            private Label createLabel(String text, String styleClass) {
                Label label = new Label(text);
                label.getStyleClass().add(styleClass);
                return label;
            }

            private void updateCellStyle() {
                selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        setSelectedStyle();
                    } else {
                        setDefaultStyle();
                    }
                });

                if (isSelected()) {
                    setSelectedStyle();
                } else {
                    setDefaultStyle();
                }
            }

            private void setSelectedStyle() {
                setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #333333; -fx-font-weight: bold;");
            }

            private void setDefaultStyle() {
                setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
            }
        });
    }

    private void loadHabitList() {
        habitListView.getItems().clear();
        List<Habit> habits = habitService.getAllHabits();
        for (Habit habit : habits) {
            String streakInfo = " (Streak: " + habit.getStreakCounter() + ")";
            habitListView.getItems().add(habit.getName() + ITEM_SEPARATOR + habit.getFrequency() + ITEM_SEPARATOR + habit.getCreationDate() + ITEM_SEPARATOR + streakInfo);
        }
    }

    @FXML
    public void onEditHabit() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            notifier.showMessage("Please select a habit to edit.", NotificationColors.RED);
            return;
        }

        String habitName = selectedItem.split(ITEM_SEPARATOR)[0];
        Habit selectedHabit = habitService.findHabitByName(habitName);
        mainApp.getMainController().showEditHabitView(selectedHabit);
    }

    @FXML
    private void onHabitSelected() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String habitName = selectedItem.split(ITEM_SEPARATOR)[0];
            selectedHabit = habitService.findHabitByName(habitName);
        }
    }

    @FXML
    private void onDeleteHabit() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String habitName = selectedItem.split(ITEM_SEPARATOR)[0];
            Habit habit = habitService.findHabitByName(habitName);
            if (habit != null) {
                habitService.deleteHabit(habit);
                loadHabitList();
                resetSelectedHabit();
            } else {
                notifier.showMessage("Habit not found for deletion.", NotificationColors.RED);
            }
        } else {
            notifier.showMessage("No habit selected for deletion.", NotificationColors.RED);
        }
    }


    private void resetSelectedHabit() {
        habitListView.getSelectionModel().clearSelection();
    }


    @FXML
    private void onMarkAsCompleted() {
        if (selectedHabit != null) {
            if (selectedHabit.getFrequency() == Habit.Frequency.CUSTOM &&
                    !selectedHabit.getCustomDays().contains(LocalDate.now().getDayOfWeek())) {
                notifier.showMessage("Today is not part of your specified habit days. Marking completion may affect statistics.", "orange");
            }
            habitService.markHabitAsCompleted(selectedHabit);
            loadHabitList();
        } else {
            notifier.showMessage("Please select a habit to mark as completed.", NotificationColors.RED);
        }
    }

    @FXML
    public void onViewProgress() {
        String selectedItem = habitListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            notifier.showMessage("Please select a habit to view progress.", NotificationColors.RED);
            return;
        }

        String habitName = selectedItem.split(ITEM_SEPARATOR)[0];
        Habit selectedHabit = habitService.findHabitByName(habitName);
        mainApp.getMainController().showProgressView(selectedHabit);
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void showReportView() {
        mainApp.getMainController().showReportView();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showMainView();
    }
}
