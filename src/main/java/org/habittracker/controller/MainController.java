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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import org.habittracker.service.ReportGenerator;

public class MainController {

    private Main mainApp;
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);
    public HabitRepository habitRepository = HabitRepository.getInstance();
    boolean remindersEnabled = true; // Default reminders to enabled
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ReportGenerator reportGenerator;

    @FXML
    ListView<Habit> habitsDueTodayList;


    @FXML
    StackPane rootStackPane;

    @FXML
    VBox mainView;

    @FXML
    VBox dynamicViewContainer;

    @FXML
    GridPane calendarGrid;

    @FXML
    private Label notificationLabel;

    @FXML
    Label calendarMonthLabel;

    public Notifier notifier;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        this.notifier = new NotificationHelper(notificationLabel);
    }

    @FXML
    void initialize() {
            notifier = new NotificationHelper(notificationLabel);
            habitRepository = HabitRepository.getInstance();
            reportGenerator = new ReportGenerator(habitRepository, notifier);

            showMainView();
            startReminderScheduler();
            reportGenerator.startMonthlyReportScheduler();
            reportGenerator.checkForMissedReports();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            reportGenerator.stopScheduler();
        }));
    }

    public void setRemindersEnabled(boolean remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
        String message = remindersEnabled ? "Reminders Enabled" : "Reminders Disabled";
        String color = remindersEnabled ? "green" : "red";
        notifier.showMessage(message, color);
    }

    void startReminderScheduler() {
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
        LocalDate dueDate = habit.getLastCompletedDate() != null ? habit.getLastCompletedDate() : habit.getCreationDate();

        if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            return ChronoUnit.DAYS.between(dueDate, date) % 7 == 0 && !dueDate.isAfter(date);
        } else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
            LocalDate nextDueDate = dueDate.plusMonths(1).withDayOfMonth(
                    Math.min(dueDate.getDayOfMonth(), date.lengthOfMonth()));
            return nextDueDate.equals(date);
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
    void openSettings() {
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

    public void openHelp() {
        loadView("/view/HelpView.fxml", controller -> {
            if (controller instanceof HelpController) {
                HelpController helpController = (HelpController) controller;
                helpController.setMainController(this); // Pass MainController to HelpController
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
                progressController.setMainApp(mainApp);
                progressController.setMainController(this);
                progressController.setHabit(habit);
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


    public void showReportView() {
        loadView("/view/ReportView.fxml", controller -> {
            if (controller instanceof ReportViewController) {
                ReportViewController reportViewController = (ReportViewController) controller;
                reportViewController.setMainController(this);
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
            LOGGER.info("View {} loaded successfully", fxmlPath);
        } catch (IOException e) {
            LOGGER.error("Error loading FXML file: {}", fxmlPath, e);
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

    void populateCalendar(LocalDate referenceDate) {
        // Set the month label color based on Dark Mode
        calendarMonthLabel.setText(referenceDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + referenceDate.getYear());
        calendarMonthLabel.getStyleClass().add("custom-label");

        calendarGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.from(referenceDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int startDay = firstOfMonth.getDayOfWeek().getValue(); // Monday = 1, Sunday = 7

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < dayNames.length; i++) {
            Label dayNameLabel = new Label(dayNames[i]);
            dayNameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            dayNameLabel.getStyleClass().add("custom-label"); // Apply custom-label style for dark mode compatibility
            calendarGrid.add(dayNameLabel, i, 0);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("custom-label"); // Apply custom-label style for day labels

            // Style the current day with distinct colors for Dark and Light Modes
            if (date.equals(LocalDate.now())) {
                dayLabel.setStyle(isDarkModeEnabled
                        ? "-fx-background-color: #4d4dff; -fx-text-fill: white; -fx-border-color: blue; -fx-border-width: 1px; -fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;"
                        : "-fx-background-color: lightblue; -fx-text-fill: black; -fx-border-color: blue; -fx-border-width: 1px; -fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;");
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



    private boolean isDarkModeEnabled = false;

    public void enableDarkMode() {
        isDarkModeEnabled = true;
        rootStackPane.getScene().getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
    }

    public void disableDarkMode() {
        isDarkModeEnabled = false;
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

    public boolean isDarkModeEnabled() {
        return isDarkModeEnabled;
    }


}
