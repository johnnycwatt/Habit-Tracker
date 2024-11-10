package org.habittracker.util;

import org.habittracker.model.Habit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class HabitStatisticsCalculator {

    public static int calculateWeeklyPerformance(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        int expectedCompletions = calculateExpectedCompletions(habit, startOfWeek, endOfWeek);
        int actualCompletions = (int) habit.getCompletedDates().stream()
                .filter(date -> !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek))
                .count();

        int performance = expectedCompletions > 0 ? (int) ((actualCompletions / (double) expectedCompletions) * 100) : 0;
        return Math.min(performance, 100);
    }

    public static int calculateMonthlyPerformance(Habit habit) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        int expectedCompletions = calculateExpectedCompletions(habit, startOfMonth, endOfMonth);
        int actualCompletions = (int) habit.getCompletedDates().stream()
                .filter(date -> !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth))
                .count();

        int performance = expectedCompletions > 0 ? (int) ((actualCompletions / (double) expectedCompletions) * 100) : 0;
        return Math.min(performance, 100);
    }

    public static int calculateOverallPerformance(Habit habit) {
        LocalDate startDate = habit.getCreationDate();
        LocalDate endDate = LocalDate.now();
        int expectedCompletions = calculateExpectedCompletions(habit, startDate, endDate);
        int actualCompletions = habit.getCompletedDates().size();

        int performance = expectedCompletions > 0 ? (int) ((actualCompletions / (double) expectedCompletions) * 100) : 0;
        return Math.min(performance, 100);
    }


    public static int calculateExpectedCompletions(Habit habit, LocalDate startDate, LocalDate endDate) {
        int expectedCount = 0;

        if (habit.getFrequency() == Habit.Frequency.DAILY) {
            expectedCount = (int) startDate.until(endDate, ChronoUnit.DAYS) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            expectedCount = (int) startDate.until(endDate, ChronoUnit.WEEKS) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
            expectedCount = (int) startDate.until(endDate, ChronoUnit.MONTHS) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.CUSTOM) {
            int customFrequencyCount = habit.getCustomDays().size();
            long totalWeeks = startDate.until(endDate, ChronoUnit.WEEKS) + 1;
            expectedCount = (int) totalWeeks * customFrequencyCount;
        }

        return expectedCount;
    }
}
