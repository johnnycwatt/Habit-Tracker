package org.habittracker.util;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HabitCalendarPopulator {
    private final GridPane calendarGrid;
    private final Label calendarMonthLabel;
    private final HabitRepository habitRepository;

    // Pre-instantiate reusable objects
    private final Label[] dayNameLabels = new Label[7];
    private final List<Label> dayLabels = new ArrayList<>();
    private final Tooltip reusableTooltip = new Tooltip();

    public HabitCalendarPopulator(GridPane calendarGrid, Label calendarMonthLabel, HabitRepository habitRepository) {
        this.calendarGrid = calendarGrid;
        this.calendarMonthLabel = calendarMonthLabel;
        this.habitRepository = habitRepository;

        // Initialize day name labels
        initializeDayNameLabels();
    }

    private void initializeDayNameLabels() {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < dayNames.length; i++) {
            dayNameLabels[i] = new Label(dayNames[i]);
            dayNameLabels[i].setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            dayNameLabels[i].getStyleClass().add("custom-label");
        }
    }

    public void populateCalendar(LocalDate referenceDate, boolean isDarkModeEnabled) {
        // Set the month label text and apply custom styles
        calendarMonthLabel.setText(referenceDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + referenceDate.getYear());
        calendarMonthLabel.getStyleClass().add("custom-label");

        calendarGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.from(referenceDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Determine the starting day of the week (Monday = 1, Sunday = 7)
        int startDay = firstOfMonth.getDayOfWeek().getValue();

        // Add day names to the first row
        addDayNamesToCalendar();

        // Clear and prepare day labels
        dayLabels.clear();
        for (int i = 0; i < daysInMonth; i++) {
            dayLabels.add(new Label());
        }

        // Populate calendar grid with day labels
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Label dayLabel = dayLabels.get(day - 1);
            dayLabel.setText(String.valueOf(day));
            dayLabel.getStyleClass().clear();
            dayLabel.getStyleClass().add("custom-label");

            // Style the current day with distinct colors and borders
            if (date.equals(LocalDate.now())) {
                dayLabel.setStyle(isDarkModeEnabled
                        ? "-fx-background-color: #4d4dff; -fx-text-fill: white; -fx-border-color: blue; -fx-border-width: 1px; -fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;"
                        : "-fx-background-color: lightblue; -fx-text-fill: black; -fx-border-color: blue; -fx-border-width: 1px; -fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;");
            } else {
                dayLabel.setStyle("-fx-alignment: center; -fx-pref-width: 35; -fx-pref-height: 35;");
            }

            // Add tooltip showing habits due on this day
            List<Habit> dueHabits = habitRepository.getAllHabits().stream()
                    .filter(habit -> isHabitDueToday(habit, date))
                    .collect(Collectors.toList());

            if (!dueHabits.isEmpty()) {
                reusableTooltip.setText(dueHabits.stream()
                        .map(Habit::getName)
                        .collect(Collectors.joining("\n")));
                Tooltip.install(dayLabel, reusableTooltip);
            } else {
                Tooltip.uninstall(dayLabel, reusableTooltip);
            }

            // Calculate the row and column for this day
            int row = (day + startDay - 2) / 7 + 1;
            int col = (day + startDay - 2) % 7;

            calendarGrid.add(dayLabel, col, row);
        }
    }

    private void addDayNamesToCalendar() {
        for (int i = 0; i < dayNameLabels.length; i++) {
            calendarGrid.add(dayNameLabels[i], i, 0);
        }
    }

    boolean isHabitDueToday(Habit habit, LocalDate today) {
        return habit.getFrequency() == Habit.Frequency.DAILY ||
                (habit.getCustomDays() != null && habit.getCustomDays().contains(today.getDayOfWeek()));
    }
}
