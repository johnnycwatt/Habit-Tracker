package org.habittracker.util;

import org.habittracker.model.Habit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

public class HabitStatisticsCalculator {

    private static final int MAX_CONSECUTIVE_INCOMPLETE = 2;
    private static final int DAILY_COMPLETION_PERCENT = 100;

    public static int calculateWeeklyPerformance(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return calculatePerformance(habit, startOfWeek, endOfWeek);
    }

    public static int calculateMonthlyPerformance(Habit habit) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        return calculatePerformance(habit, startOfMonth, endOfMonth);
    }

    public static int calculateOverallPerformance(Habit habit) {
        LocalDate startDate = habit.getCreationDate();
        LocalDate endDate = LocalDate.now();

        return calculatePerformance(habit, startDate, endDate);
    }

    private static int calculatePerformance(Habit habit, LocalDate startDate, LocalDate endDate) {
        int expectedCompletions = calculateExpectedCompletions(habit, startDate, endDate);
        int actualCompletions = (int) habit.getCompletedDates().stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();

        int performance = expectedCompletions > 0
                ? (int) ((actualCompletions / (double) expectedCompletions) * DAILY_COMPLETION_PERCENT)
                : 0;
        return Math.min(performance, DAILY_COMPLETION_PERCENT);
    }

    public static int calculateExpectedCompletions(Habit habit, LocalDate startDate, LocalDate endDate) {
        switch (habit.getFrequency()) {
            case DAILY:
                return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
            case WEEKLY:
                return (int) ChronoUnit.WEEKS.between(startDate, endDate) + 1;
            case MONTHLY:
                return (int) ChronoUnit.MONTHS.between(startDate, endDate) + 1;
            case CUSTOM:
                return calculateCustomExpectedCompletions(habit, startDate, endDate);
            default:
                return 0;
        }
    }

    private static int calculateCustomExpectedCompletions(Habit habit, LocalDate startDate, LocalDate endDate) {
        int expectedCount = 0;
        Set<DayOfWeek> customDays = Set.copyOf(habit.getCustomDays());
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            if (customDays.contains(date.getDayOfWeek())) {
                expectedCount++;
            }
            date = date.plusDays(1);
        }

        return expectedCount;
    }

    public static int calculateWeeklyConsistency(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        return calculateConsistency(habit, startOfWeek, ChronoUnit.WEEKS);
    }

    public static int calculateMonthlyConsistency(Habit habit) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        return calculateConsistency(habit, currentMonth.atDay(1), ChronoUnit.MONTHS);
    }

    private static int calculateConsistency(Habit habit, LocalDate startDate, ChronoUnit unit) {
        int consistentPeriods = 0;
        int consecutiveIncompletePeriods = 0;

        LocalDate iterationDate = startDate;

        while (!iterationDate.isBefore(habit.getCreationDate())) {
            boolean isConsistent = isPeriodConsistent(habit, iterationDate, unit);

            if (isConsistent) {
                consistentPeriods++;
                consecutiveIncompletePeriods = 0;
            } else {
                consecutiveIncompletePeriods++;
                if (consecutiveIncompletePeriods >= MAX_CONSECUTIVE_INCOMPLETE) {
                    break; // Stop further calculation after enough consecutive inconsistencies
                }
            }

            iterationDate = iterationDate.minus(1, unit);
        }

        return consistentPeriods == 0 ? 0 : consistentPeriods; // Return 0 if no consistent periods
    }


    private static boolean isPeriodConsistent(Habit habit, LocalDate startDate, ChronoUnit unit) {
        LocalDate endDate = unit == ChronoUnit.WEEKS
                ? startDate.with(DayOfWeek.SUNDAY)
                : YearMonth.from(startDate).atEndOfMonth();

        switch (habit.getFrequency()) {
            case DAILY:
                return startDate.datesUntil(endDate.plusDays(1)) // Inclusive
                        .allMatch(date -> habit.getCompletedDates().contains(date));
            case WEEKLY:
                return habit.getCompletedDates().stream()
                        .anyMatch(date -> !date.isBefore(startDate) && !date.isAfter(endDate));
            case CUSTOM:
                return isCustomPeriodConsistent(habit, startDate, endDate);
            case MONTHLY:
                return habit.getCompletedDates().stream()
                        .anyMatch(date -> !date.isBefore(startDate) && !date.isAfter(endDate));
            default:
                return false;
        }
    }

    private static boolean isCustomPeriodConsistent(Habit habit, LocalDate startDate, LocalDate endDate) {
        Set<DayOfWeek> customDays = Set.copyOf(habit.getCustomDays());
        return customDays.stream()
                .allMatch(day -> startDate.datesUntil(endDate.plusDays(1)) // Check all custom days within range
                        .filter(date -> date.getDayOfWeek() == day)
                        .allMatch(habit.getCompletedDates()::contains)); // All should be completed
    }


    public static int calculateLongestStreak(Habit habit) {
        List<LocalDate> completedDates = habit.getCompletedDates().stream().sorted().toList();
        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;

        for (LocalDate date : completedDates) {
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
}
