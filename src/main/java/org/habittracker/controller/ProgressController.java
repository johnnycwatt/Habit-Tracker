package org.habittracker.controller;


import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.util.HabitStatisticsCalculator;
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

    private Habit habit;
    private Main mainApp;

    public void setHabit(Habit habit) {
        this.habit = habit;

        String color = habit.getColor();
        applyColorTheme(color);
        habitNameLabel.setText(habit.getName());
        currentStreakLabel.setText(String.valueOf(habit.getStreakCounter()));

        populateCalendar(LocalDate.now());
        displayStatistics();
        updateHistoryChart();
    }

    private void applyColorTheme(String color) {
        habitNameLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        currentStreakLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        totalCompletionsLabel.setStyle("-fx-text-fill: " + color + ";");
        weeklyPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        monthlyPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        overallPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        bestStreakLabel.setStyle("-fx-text-fill: " + color + ";");
        historyLabel.setStyle("-fx-text-fill: " + color + ";");
        historyChart.setStyle("-fx-bar-fill: " + color + ";");
    }

    private void populateCalendar(LocalDate date) {
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDayOfMonth = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
        calendarGrid.getChildren().clear();

        Set<LocalDate> completedDates = habit.getCompletedDates();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = firstDayOfMonth.plusDays(day - 1);
            Label dayLabel = new Label(String.valueOf(day));

            if (completedDates.contains(currentDate)) {
                dayLabel.setStyle("-fx-background-color: " + habit.getColor() + "; -fx-text-fill: white; -fx-padding: 5;");
            } else if (currentDate.equals(today)) {
                dayLabel.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-padding: 5; -fx-border-color: blue; -fx-border-width: 1px;");
            }

            int row = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) / 7;
            int col = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;
            calendarGrid.add(dayLabel, col, row);
        }
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
        bestStreakLabel.setText(String.valueOf(calculateBestStreak()));
    }

    private int calculateBestStreak() {
        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;

        for (LocalDate date : habit.getCompletedDates().stream().sorted().toList()) {
            if (lastDate != null && date.equals(lastDate.plusDays(1))) {
                currentStreak++;
            } else {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 1;
            }
            lastDate = date;
        }
        return Math.max(longestStreak, currentStreak);
    }

    private void updateHistoryChart() {
        historyChart.getData().clear(); // Clear previous data

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completions");

        updateMonthView(series);

        historyChart.getData().add(series);

        // Apply color directly to each data point in the series
        String habitColor = habit.getColor();
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + habitColor + ";");
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
}
