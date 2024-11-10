package org.habittracker.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainController {

    private Main mainApp;
    public HabitRepository habitRepository = HabitRepository.getInstance();
    private boolean remindersEnabled = true; // Default reminders to enabled
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @FXML
    private ListView<Habit> habitsDueTodayList;


    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox mainView;

    @FXML
    private VBox dynamicViewContainer;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label notificationLabel;

    @FXML
    private Label calendarMonthLabel;

    public Notifier notifier;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    private void initialize() {
        showMainView();
        startReminderScheduler(); // Schedule reminders when the app starts
    }

    public void setRemindersEnabled(boolean remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
        String message = remindersEnabled ? "Reminders Enabled" : "Reminders Disabled";
        String color = remindersEnabled ? "green" : "red";
        notifier.showMessage(message, color);
    }

    private void startReminderScheduler() {
        scheduler.scheduleAtFixedRate(this::checkUpcomingReminders, 0, 24, TimeUnit.HOURS);
    }

    public void shutdownScheduler() {
        scheduler.shutdown();
    }

    private void checkUpcomingReminders() {
        if (!remindersEnabled) {
            return; // Exit if reminders are disabled
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Habit> habits = habitRepository.getAllHabits();

        for (Habit habit : habits) {
            if (isReminderDue(habit, tomorrow)) {
                Platform.runLater(() -> {
                    String message = "Reminder: '" + habit.getName() + "' is due tomorrow!";
                    notifier.showMessage(message, "blue");
                });
            }
        }
    }

    boolean isReminderDue(Habit habit, LocalDate date) {
        if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            return habit.getCreationDate().plusWeeks(1).equals(date) ||
                    (habit.getLastCompletedDate() != null && habit.getLastCompletedDate().plusWeeks(1).equals(date));
        } else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
            return habit.getCreationDate().plusMonths(1).equals(date) ||
                    (habit.getLastCompletedDate() != null && habit.getLastCompletedDate().plusMonths(1).equals(date));
        }
        return false;
    }

    public void showMainView() {
        mainView.setVisible(true);
        dynamicViewContainer.setVisible(false);
        dynamicViewContainer.getChildren().clear();
        updateHabitsDueToday();
        populateCalendar(LocalDate.now());
    }

    @FXML
    private void openSettings() {
        showSettingsView();
    }

    public void showSettingsView() {
        loadView("/view/SettingsView.fxml", controller -> {
            if (controller instanceof SettingsController) {
                SettingsController settingsController = (SettingsController) controller;
                settingsController.setMainApp(mainApp);
                settingsController.setMainController(this);
            }
        });
    }

    @FXML
    public void showAddHabitView() {
        loadView("/view/AddHabitView.fxml", controller -> {
            if (controller instanceof AddHabitController) {
                AddHabitController addHabitController = (AddHabitController) controller;
                addHabitController.setMainApp(mainApp);
            }
        });
    }

    public void showEditHabitView(Habit habit) {
        loadView("/view/EditHabitView.fxml", controller -> {
            if (controller instanceof EditHabitController) {
                EditHabitController editHabitController = (EditHabitController) controller;
                editHabitController.setHabit(habit);
                editHabitController.setHabitRepository(habitRepository);
                editHabitController.setMainApp(mainApp);
            }
        });
    }

    public void showProgressView(Habit habit) {
        loadView("/view/ProgressView.fxml", controller -> {
            if (controller instanceof ProgressController) {
                ProgressController progressController = (ProgressController) controller;
                progressController.setHabit(habit);
                progressController.setMainApp(mainApp);
            }
        });
    }

    public void showHabitListView() {
        loadView("/view/HabitListView.fxml", controller -> {
            if (controller instanceof HabitListController) {
                HabitListController habitListController = (HabitListController) controller;
                habitListController.setMainApp(mainApp);
            }
        });
    }

    private void loadView(String fxmlPath, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            controllerSetup.accept(loader.getController());

            dynamicViewContainer.getChildren().setAll(view);
            mainView.setVisible(false);
            dynamicViewContainer.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML file: " + fxmlPath);
        }
    }

    public void updateHabitsDueToday() {
        habitsDueTodayList.getItems().clear();
        List<Habit> habits = habitRepository.getAllHabits();
        LocalDate today = LocalDate.now();

        for (Habit habit : habits) {
            if (isHabitDueToday(habit, today)) {
                habitsDueTodayList.getItems().add(habit);
            }
        }

        habitsDueTodayList.setCellFactory(listView -> new ListCell<Habit>() {
            @Override
            protected void updateItem(Habit habit, boolean empty) {
                super.updateItem(habit, empty);
                if (habit != null && !empty) {
                    setText(habit.getName());

                    boolean isCompletedToday = habit.getLastCompletedDate() != null && habit.getLastCompletedDate().equals(LocalDate.now());

                    // Set the background color based on completion
                    if (isCompletedToday) {
                        setStyle("-fx-background-color: #d4edda; -fx-font-weight: bold; -fx-text-fill: #155724;");
                        setGraphic(new Label("✔"));
                    } else {
                        setStyle("-fx-background-color: #ffffff; -fx-font-weight: normal; -fx-text-fill: #333333;");
                    }

                    listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                        if (newSelection == habit) {
                            // When selected, override the selection background color and keep text color intact
                            setStyle((isCompletedToday ? "-fx-background-color: #d4edda;" : "-fx-background-color: #cce5ff;") +
                                    " -fx-font-weight: bold; -fx-text-fill: " + (isCompletedToday ? "#155724;" : "#333333;"));
                        }
                    });

                    focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                        if (!isFocused) {
                            // Reset style when focus is lost
                            setStyle((isCompletedToday ? "-fx-background-color: #d4edda;" : "-fx-background-color: #ffffff;") +
                                    " -fx-font-weight: " + (isCompletedToday ? "bold;" : "normal;") +
                                    " -fx-text-fill: " + (isCompletedToday ? "#155724;" : "#333333;"));
                        }
                    });
                } else {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                }
            }
        });



    }

    public boolean isHabitDueToday(Habit habit, LocalDate today) {
        if (habit.getCreationDate().isAfter(today)) {
            return false;
        }
        LocalDate startDate = habit.getCreationDate();
        switch (habit.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                return !startDate.isAfter(today) && (daysBetween(startDate, today) % 7 == 0);
            case MONTHLY:
                int startDayOfMonth = startDate.getDayOfMonth();
                int daysInCurrentMonth = today.lengthOfMonth();
                int dueDayOfMonth = Math.min(startDayOfMonth, daysInCurrentMonth);
                return today.getDayOfMonth() == dueDayOfMonth && monthsBetween(startDate, today) % 1 == 0;
            case CUSTOM:
                return habit.getCustomDays() != null && habit.getCustomDays().contains(today.getDayOfWeek());
            default:
                return false;
        }
    }

    private long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    private long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(YearMonth.from(startDate), YearMonth.from(endDate));
    }

    private void populateCalendar(LocalDate referenceDate) {
        // Set the month label
        calendarMonthLabel.setText(referenceDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + referenceDate.getYear());

        calendarGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.from(referenceDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int startDay = firstOfMonth.getDayOfWeek().getValue(); // Monday = 1, Sunday = 7

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < dayNames.length; i++) {
            Label dayNameLabel = new Label(dayNames[i]);
            dayNameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            calendarGrid.add(dayNameLabel, i, 0);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));

            // Style the current day
            if (date.equals(LocalDate.now())) {
                dayLabel.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-border-color: blue; -fx-border-width: 1px; -fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;");
            } else {
                dayLabel.setStyle("-fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;");
            }

            // Add tooltip with habit due details
            List<Habit> dueHabits = habitRepository.getAllHabits().stream()
                    .filter(habit -> isHabitDueToday(habit, date))
                    .collect(Collectors.toList());

            if (!dueHabits.isEmpty()) {
                Tooltip tooltip = new Tooltip(
                        dueHabits.stream()
                                .map(Habit::getName)
                                .collect(Collectors.joining("\n"))
                );
                Tooltip.install(dayLabel, tooltip);
            }

            int row = (day + startDay - 2) / 7 + 1;
            int col = (day + startDay - 2) % 7;

            calendarGrid.add(dayLabel, col, row);
        }
    }

    public void enableDarkMode() {
        rootStackPane.getScene().getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
    }

    public void disableDarkMode() {
        rootStackPane.getScene().getStylesheets().remove(getClass().getResource("/css/dark-theme.css").toExternalForm());
    }
    public Notifier getNotifier() {
        return notifier;
    }

    public HabitRepository getHabitRepository() {
        return habitRepository;
    }

    // Expose checkUpcomingReminders() for testing
    public void triggerCheckUpcomingReminders() {
        checkUpcomingReminders();
    }

    public List<Habit> getAllHabits() {
        return HabitRepository.getInstance().getAllHabits();
    }


}
