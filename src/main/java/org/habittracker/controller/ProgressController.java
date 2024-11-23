package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.HabitStatisticsCalculator;
import org.habittracker.util.NotificationColors;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;

public class ProgressController {
    @FXML
    private Label habitNameLabel;
    @FXML
    private Label currentStreakLabel;
    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label totalCompletionsLabel;
    @FXML
    private Label weeklyPerformanceLabel;
    @FXML
    private Label monthlyPerformanceLabel;
    @FXML
    private Label overallPerformanceLabel;
    @FXML
    private Label bestStreakLabel;
    @FXML
    private Label historyLabel;

    @FXML
    private BarChart<String, Number> historyChart;

    @FXML
    private Label weeklyConsistencyLabel;

    @FXML
    private Label monthlyConsistencyLabel;

    private YearMonth currentYearMonth;



    @FXML
    private Label notificationLabel;

    @FXML
    private Label calendarMonthLabel;
    private int currentHistoryYear;


    private Notifier notifier;

    private Habit habit;
    private Main mainApp;
    private final HabitRepository habitRepository;
    private boolean isDarkModeEnabled;
    private String blackColor = "#000000";

    public ProgressController() {
        this.habitRepository = HabitRepository.getInstance();

    }
    private MainController mainController;

    @FXML
    private void initialize() {
        notifier = new NotificationHelper(notificationLabel);
    }

    public void setHabit(Habit habit) {
        this.habit = habit;

        this.isDarkModeEnabled = mainController.isDarkModeEnabled();
        String color = habit.getColor();
        applyColorTheme(color);
        habitNameLabel.setText(habit.getName());
        currentStreakLabel.setText(String.valueOf(habit.getStreakCounter()));
        currentYearMonth = YearMonth.now();
        currentHistoryYear = LocalDate.now().getYear();

        populateCalendar(LocalDate.now());
        displayStatistics();
        updateHistoryChart();
    }

    private void applyColorTheme(String color) {
        String adjustedColor = adjustColorForMode(color);

        // Define common styles
        String boldTextStyle = "-fx-text-fill: " + adjustedColor + "; -fx-font-weight: bold;";
        String defaultTextStyle = "-fx-text-fill: " + adjustedColor + ";";
        String headerSize = " -fx-font-size: 18px;";

        // Apply styles to labels
        habitNameLabel.setStyle(boldTextStyle + headerSize);
        currentStreakLabel.setStyle(boldTextStyle);
        bestStreakLabel.setStyle(boldTextStyle);
        totalCompletionsLabel.setStyle(defaultTextStyle);
        weeklyPerformanceLabel.setStyle(defaultTextStyle);
        monthlyPerformanceLabel.setStyle(defaultTextStyle);
        overallPerformanceLabel.setStyle(defaultTextStyle);
        weeklyConsistencyLabel.setStyle(defaultTextStyle);
        monthlyConsistencyLabel.setStyle(defaultTextStyle);

        calendarMonthLabel.setStyle(boldTextStyle + headerSize);
        historyLabel.setStyle(defaultTextStyle);

        // Adjust day labels color
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        Label[] dayNameLabels = new Label[dayNames.length];

        // Instantiate labels
        for (int i = 0; i < dayNames.length; i++) {
            dayNameLabels[i] = new Label(dayNames[i]);
            dayNameLabels[i].setStyle("-fx-text-fill: " + adjustedColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        // Add labels to the grid
        for (int i = 0; i < dayNameLabels.length; i++) {
            calendarGrid.add(dayNameLabels[i], i, 0);
        }
    }


    private String adjustColorForMode(String color) {
        if (isDarkModeEnabled) {
            // Dark Mode adjustments
            switch (color) {
                case "#000000": return "#FFFFFF"; // Black to White
                case "#FF0000": return "#FF6666"; // Red to Lighter Red
                case "#0000FF": return "#6699FF"; // Blue to Lighter Blue
                // Add any other adjustments for Dark Mode here
                default: return color; // No change for colors that look fine
            }
        } else {
            // Light Mode adjustments (e.g., for Yellow and Cyan that look better in Dark Mode)
               return color; // Keep the original color
        }
    }

    //Buttons to Navigate for Calendar

    @FXML
    private void showPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendarView();
    }

    @FXML
    private void showNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendarView();
    }


    private void updateCalendarView() {
        // Update the calendar label to show the new month and year
        calendarMonthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYearMonth.getYear());

        // Populate the calendar grid for the selected month
        populateCalendar(currentYearMonth.atDay(1));
    }


    //Buttons to Navigate for Bar Graph

    @FXML
    private void showPreviousYear() {
        int earliestYear = habitRepository.getEarliestCompletionYear(habit);


        if (currentHistoryYear > earliestYear && (LocalDate.now().getYear() - currentHistoryYear) < 1) {
            currentHistoryYear--;
            updateHistoryChart();
        } else {
            notifier.showMessage("No data available for earlier years.", NotificationColors.RED);
        }
    }

    @FXML
    private void showNextYear() {
        int currentYear = LocalDate.now().getYear();
        if (currentHistoryYear < currentYear) {
            currentHistoryYear++;
            updateHistoryChart();
        }
    }




    private void populateCalendar(LocalDate date) {
        setCalendarMonthLabel(date);

        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDayOfMonth = LocalDate.of(date.getYear(), date.getMonthValue(), 1);

        int startDay = calculateStartDay(firstDayOfMonth);

        prepareCalendarGrid();

        addDayNamesToCalendar();

        populateCalendarDays(firstDayOfMonth, daysInMonth, startDay);
    }

    private void setCalendarMonthLabel(LocalDate date) {
        String monthYear = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + date.getYear();
        calendarMonthLabel.setText(monthYear);
    }


    private int calculateStartDay(LocalDate firstDayOfMonth) {
        int startDay = firstDayOfMonth.getDayOfWeek().getValue(); // Monday = 1, Sunday = 7
        return startDay % 7; // Convert Sunday (7) to 6, and Monday (1) to 0
    }


    private void prepareCalendarGrid() {
        calendarGrid.getChildren().clear();
    }

    private void addDayNamesToCalendar() {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        Label[] dayNameLabels = new Label[dayNames.length];

        for (int i = 0; i < dayNames.length; i++) {
            dayNameLabels[i] = new Label(dayNames[i]);
            dayNameLabels[i].setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            dayNameLabels[i].getStyleClass().add("custom-label");
        }

        // Add labels to the grid
        for (int i = 0; i < dayNameLabels.length; i++) {
            calendarGrid.add(dayNameLabels[i], i, 0);
        }
    }


    private void populateCalendarDays(LocalDate firstDayOfMonth, int daysInMonth, int startDay) {
        Set<LocalDate> completedDates = habit.getCompletedDates();
        LocalDate today = LocalDate.now();

        String defaultStyle = "-fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;";

        // Loop through each day of the current month
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = firstDayOfMonth.plusDays(day - 1);
            Label dayLabel = createDayLabel(day, currentDate, completedDates, today, defaultStyle);

            // Add context menu for incomplete days
            addContextMenuToDayLabel(dayLabel, currentDate, completedDates, today);

            // Calculate the row and column for this day
            int row = (day + startDay - 1) / 7 + 1; // +1 for the header row
            int col = (day + startDay - 1) % 7;

            // Add the day label to the grid
            calendarGrid.add(dayLabel, col, row);
        }
    }


    private Label createDayLabel(int day, LocalDate currentDate, Set<LocalDate> completedDates, LocalDate today, String defaultStyle) {
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setStyle(defaultStyle);

        if (completedDates.contains(currentDate)) {
            String completedColor = habit.getColor();
            if ("#000000".equals(completedColor) && isDarkModeEnabled) {
                // Special handling for black color in dark mode
                dayLabel.setStyle(defaultStyle + "-fx-background-color: white; -fx-text-fill: black;");
            } else {
                // Use adjusted color for other cases
                completedColor = adjustColorForMode(completedColor);
                dayLabel.setStyle(defaultStyle + "-fx-background-color: " + completedColor + "; -fx-text-fill: white;");
            }
        } else if (currentDate.equals(today)) {
            String todayColor = isDarkModeEnabled ? "#add8e6" : "#b0c4de";
            dayLabel.setStyle(defaultStyle + "-fx-background-color: " + todayColor + "; -fx-text-fill: black; -fx-border-color: #6699FF; -fx-border-width: 1px;");
        } else {
            dayLabel.getStyleClass().add("custom-label");
        }

        return dayLabel;
    }


    private void addContextMenuToDayLabel(Label dayLabel, LocalDate currentDate, Set<LocalDate> completedDates, LocalDate today) {
        if (!completedDates.contains(currentDate) && !currentDate.isAfter(today)) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem markCompleteItem = new MenuItem("Mark Completed");
            markCompleteItem.setOnAction(e -> markHabitAsCompletedOnDate(currentDate));
            contextMenu.getItems().add(markCompleteItem);
            dayLabel.setOnContextMenuRequested(event -> {
                if (!currentDate.isBefore(habit.getCreationDate()) &&
                        !currentDate.isAfter(today) &&
                        !completedDates.contains(currentDate)) {
                    contextMenu.show(dayLabel, event.getScreenX(), event.getScreenY());
                }
            });
        }
    }


    private void markHabitAsCompletedOnDate(LocalDate date) {


        if (habit.getFrequency() == Habit.Frequency.CUSTOM && !habit.getCustomDays().contains(date.getDayOfWeek())) {
            notifier.showMessage("This habit can only be completed on specified days. Please select a valid day.", NotificationColors.RED);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Completion");
        confirmAlert.setHeaderText("Mark habit as completed on " + date);
        confirmAlert.setContentText("Are you sure you want to mark this habit as completed on " + date + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                habit.markAsCompletedOnDate(date);

                habitRepository.updateHabit(habit);
                currentStreakLabel.setText(String.valueOf(habit.getStreakCounter()));

                populateCalendar(LocalDate.now());
                displayStatistics();
                updateHistoryChart();
            }
        });
    }

    private void displayStatistics() {
        int totalCompletions = habit.getCompletedDates().size();
        totalCompletionsLabel.setText(String.valueOf(totalCompletions));

        int weeklyPerformance = HabitStatisticsCalculator.calculateWeeklyPerformance(habit);
        int monthlyPerformance = HabitStatisticsCalculator.calculateMonthlyPerformance(habit);
        int overallPerformance = HabitStatisticsCalculator.calculateOverallPerformance(habit);

        weeklyPerformanceLabel.setText(weeklyPerformance + "%");
        monthlyPerformanceLabel.setText(monthlyPerformance + "%");
        overallPerformanceLabel.setText(overallPerformance + "%");

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(habit);
        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(habit);

        weeklyConsistencyLabel.setText(weeklyConsistency + " weeks");
        monthlyConsistencyLabel.setText(monthlyConsistency + " months");


        bestStreakLabel.setText(String.valueOf(habit.getBestStreak()));
    }


    private void updateHistoryChart() {
        historyChart.getData().clear();

        // Fetch habit completions for the current year
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completions in " + currentHistoryYear);

        boolean hasData = false; // Track if there is data for the year
        for (int month = 1; month <= 12; month++) {
            int completions = habit.getCompletionsInMonth(currentHistoryYear, month);
            if (completions > 0) {
                hasData = true;
            }
            String monthName = YearMonth.of(currentHistoryYear, month)
                    .getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault());
            series.getData().add(new XYChart.Data<>(monthName, completions));
        }

        historyChart.getData().add(series);

        // Assign color to the chart bars
        String habitColor = habit.getColor();

        if (isDarkModeEnabled && blackColor.equals(habitColor)) {
            habitColor = blackColor; // Keep bars black even in dark mode
        } else {
            habitColor = adjustColorForMode(habitColor); // Adjust other colors for dark mode
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + habitColor + ";");
        }

        historyLabel.setText(String.valueOf(currentHistoryYear));

        if (!hasData) {
            notifier.showMessage("No data available for the selected year.", NotificationColors.RED);
        }
    }






    private void updateMonthView(XYChart.Series<String, Number> series) {
        int currentYear = LocalDate.now().getYear();
        for (int month = 1; month <= 12; month++) {
            int completions = habit.getCompletionsInMonth(currentYear, month);
            String monthLabel = YearMonth.of(currentYear, month)
                    .getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault());
            series.getData().add(new XYChart.Data<>(monthLabel, completions));
        }
    }

    @FXML
    private void goBack() {
        mainApp.getMainController().showHabitListView();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.isDarkModeEnabled = mainController.isDarkModeEnabled();
    }
}
