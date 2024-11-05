package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.habittracker.Main;
import org.habittracker.model.Habit;

import java.time.LocalDate;
import java.time.YearMonth;
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

    public void setHabit(Habit habit) {
        this.habit = habit;
        habitNameLabel.setText(habit.getName());
        currentStreakLabel.setText(String.valueOf(habit.getStreakCounter()));

        populateCalendar(LocalDate.now());
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

            // Apply lightGreen for completed dates
            if (completedDates.contains(currentDate)) {
                dayLabel.setStyle("-fx-background-color: lightgreen; -fx-text-fill: white; -fx-padding: 5;");
            }

            // Apply lightBlue for today
            if (currentDate.equals(today)) {
                dayLabel.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-padding: 5; -fx-border-color: blue; -fx-border-width: 1px;");
            }

            // If the date is both completed and today, we can merge styles
            if (completedDates.contains(currentDate) && currentDate.equals(today)) {
                dayLabel.setStyle("-fx-background-color: darkgreen; -fx-text-fill: white; -fx-padding: 5; -fx-border-color: blue; -fx-border-width: 1px;");
            }

            int row = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) / 7;
            int col = (day + firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;
            calendarGrid.add(dayLabel, col, row);
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }


    @FXML
    private void goBack() {
        System.out.println("goBack method in ProgressController called");
        mainApp.getMainController().showHabitListView();
    }

}
