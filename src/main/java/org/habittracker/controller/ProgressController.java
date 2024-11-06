package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.util.HabitStatisticsCalculator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class ProgressController {
    @FXML
    private Label habitNameLabel;
    @FXML
    private Label currentStreakLabel;
    @FXML
    private GridPane calendarGrid;

    private Habit habit;
    private Main mainApp;
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

    public void setHabit(Habit habit) {
        this.habit = habit;

        String color = habit.getColor();
        applyColorTheme(color);
        habitNameLabel.setText(habit.getName());
        currentStreakLabel.setText(String.valueOf(habit.getStreakCounter()));

        populateCalendar(LocalDate.now());
        displayStatistics();

    }

    private void applyColorTheme(String color) {
        // Apply color to the title and all stat labels
        habitNameLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        currentStreakLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        totalCompletionsLabel.setStyle("-fx-text-fill: " + color + ";");
        weeklyPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        monthlyPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        overallPerformanceLabel.setStyle("-fx-text-fill: " + color + ";");
        bestStreakLabel.setStyle("-fx-text-fill: " + color + ";");
    }




    private void populateCalendar(LocalDate date) {
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDayOfMonth = LocalDate.of(date.getYear(), date.getMonthValue(), 1);

        // Clear previous content
        calendarGrid.getChildren().clear();

        Set<LocalDate> completedDates = habit.getCompletedDates();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = firstDayOfMonth.plusDays(day - 1);
            Label dayLabel = new Label(String.valueOf(day));
            String color = habit.getColor();
            applyColorTheme(color);

            // Apply lightGreen for completed dates
            if (completedDates.contains(currentDate)) {
                dayLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 5;");
            }

            // Apply lightBlue for today
            if (currentDate.equals(today)) {
                dayLabel.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-padding: 5; -fx-border-color: blue; -fx-border-width: 1px;");
            }

            // If the date is both completed and today, we can merge styles
            if (completedDates.contains(currentDate) && currentDate.equals(today)) {
                dayLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 5; -fx-border-color: blue; -fx-border-width: 1px;");
            }

            int row = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) / 7;
            int col = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;
            calendarGrid.add(dayLabel, col, row);
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    private void displayStatistics() {
        Set<LocalDate> completedDates = habit.getCompletedDates();
        int totalCompletions = completedDates.size();
        totalCompletionsLabel.setText(String.valueOf(totalCompletions));

        // Calculate performance statistics using HabitStatisticsCalculator
        int weeklyPerformance = HabitStatisticsCalculator.calculateWeeklyPerformance(habit);
        int monthlyPerformance = HabitStatisticsCalculator.calculateMonthlyPerformance(habit);
        int overallPerformance = HabitStatisticsCalculator.calculateOverallPerformance(habit);

        weeklyPerformanceLabel.setText(weeklyPerformance + "%");
        monthlyPerformanceLabel.setText(monthlyPerformance + "%");
        overallPerformanceLabel.setText(overallPerformance + "%");

        int bestStreak = calculateBestStreak();
        bestStreakLabel.setText(String.valueOf(bestStreak));
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
                currentStreak = 1; // Reset streak
            }
            lastDate = date;
        }
        return Math.max(longestStreak, currentStreak);
    }



    @FXML
    private void goBack() {
        System.out.println("goBack method in ProgressController called");
        mainApp.getMainController().showHabitListView();
    }


}
