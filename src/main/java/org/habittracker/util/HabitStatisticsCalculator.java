package org.habittracker.util;

import org.habittracker.model.Habit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Set;

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
            expectedCount = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            expectedCount = (int) ChronoUnit.WEEKS.between(startDate, endDate) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
            expectedCount = (int) ChronoUnit.MONTHS.between(startDate, endDate) + 1;
        } else if (habit.getFrequency() == Habit.Frequency.CUSTOM) {
            Set<DayOfWeek> customDays = Set.copyOf(habit.getCustomDays());

            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                if (customDays.contains(date.getDayOfWeek())) {
                    expectedCount++;
                }
                date = date.plusDays(1);
            }
        }

        return expectedCount;
    }


    public static int calculateWeeklyConsistency(Habit habit) {
        LocalDate today = LocalDate.now();
        int consistentWeeks = 0;
        int consecutiveIncompleteWeeks = 0;

        // Set the starting week (current week)
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        while (!startOfWeek.isBefore(habit.getCreationDate())) {
            LocalDate endOfWeek = startOfWeek.with(DayOfWeek.SUNDAY);
            LocalDate currentStartOfWeek = startOfWeek;

            boolean isConsistent = false;

            if (habit.getFrequency() == Habit.Frequency.DAILY) {
                // Daily habits: must be completed every day of the week
                isConsistent = startOfWeek.datesUntil(endOfWeek.plusDays(1)) // Inclusive of endOfWeek
                        .allMatch(date -> habit.getCompletedDates().contains(date));
            } else if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
                // Weekly habits: must have at least one completion in the week
                LocalDate finalStartOfWeek = startOfWeek;
                isConsistent = habit.getCompletedDates().stream()
                        .anyMatch(date -> !date.isBefore(finalStartOfWeek) && !date.isAfter(endOfWeek));
            } else if (habit.getFrequency() == Habit.Frequency.CUSTOM) {
                final Set<DayOfWeek> customDays = Set.copyOf(habit.getCustomDays());
                isConsistent = customDays.stream()
                        .allMatch(day -> habit.getCompletedDates().stream()
                                .anyMatch(date -> date.getDayOfWeek() == day &&
                                        !date.isBefore(currentStartOfWeek) &&
                                        !date.isAfter(endOfWeek)));
            }else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
                // Monthly habits: weekly consistency is N/A
                return 0;
            }

            // Debugging output to confirm weekly check
            //System.out.println("Checking week from " + startOfWeek + " to " + endOfWeek);
            //System.out.println("Weekly habit - " + (isConsistent ? "Week has completion" : "No completion for this week"));

            if (isConsistent) {
                consistentWeeks++;
                consecutiveIncompleteWeeks = 0; // Reset incomplete week counter if this week is consistent
            } else {
                consecutiveIncompleteWeeks++;
                if (consecutiveIncompleteWeeks >= 2) {
                    break; // Stop if two consecutive weeks are incomplete
                }
            }

            // Move to the previous week
            startOfWeek = startOfWeek.minusWeeks(1);
        }

        // Final output
        System.out.println("Total consistent weeks: " + consistentWeeks);
        return consistentWeeks;
    }

    public static int calculateMonthlyConsistency(Habit habit) {
        LocalDate today = LocalDate.now();
        int consistentMonths = 0;
        int consecutiveIncompleteMonths = 0;

        // Start with the current month
        YearMonth currentMonth = YearMonth.from(today);

        while (!currentMonth.isBefore(YearMonth.from(habit.getCreationDate()))) {
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();

            boolean isConsistent = false;

            if (habit.getFrequency() == Habit.Frequency.DAILY) {
                // Daily habits: must be completed every day of the month (for past months)
                if (!currentMonth.equals(YearMonth.from(today)) || today.isAfter(endOfMonth)) {
                    isConsistent = startOfMonth.datesUntil(endOfMonth.plusDays(1))
                            .allMatch(date -> habit.getCompletedDates().contains(date));
                }
            } else if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
                // Weekly habits: must have at least one completion in each week of the month
                isConsistent = startOfMonth.datesUntil(endOfMonth.plusDays(1))
                        .filter(date -> date.getDayOfWeek() == DayOfWeek.MONDAY)
                        .allMatch(startOfWeek -> habit.getCompletedDates().stream()
                                .anyMatch(date -> !date.isBefore(startOfWeek) && !date.isAfter(startOfWeek.with(DayOfWeek.SUNDAY))));
            } else if (habit.getFrequency() == Habit.Frequency.CUSTOM) {
                final Set<DayOfWeek> customDays = Set.copyOf(habit.getCustomDays());
                isConsistent = startOfMonth.datesUntil(endOfMonth.plusDays(1))
                        .filter(date -> date.getDayOfWeek() == DayOfWeek.MONDAY)
                        .allMatch(startOfWeek -> customDays.stream()
                                .allMatch(day -> habit.getCompletedDates().stream()
                                        .anyMatch(date -> date.getDayOfWeek() == day &&
                                                !date.isBefore(startOfWeek) &&
                                                !date.isAfter(startOfWeek.with(DayOfWeek.SUNDAY)))));
            }else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
                // Monthly habits: must have at least one completion in the month
                isConsistent = habit.getCompletedDates().stream()
                        .anyMatch(date -> !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth));
            }

            // Debugging output to confirm monthly check
            //System.out.println("Checking month: " + currentMonth);
            //System.out.println("Habit consistency for this month - " + (isConsistent ? "Consistent" : "Not consistent"));

            if (isConsistent) {
                consistentMonths++;
                consecutiveIncompleteMonths = 0; // Reset the counter if this month is consistent
            } else {
                consecutiveIncompleteMonths++;
                if (consecutiveIncompleteMonths >= 2) {
                    break; // Stop if two consecutive months are incomplete
                }
            }

            // Move to the previous month
            currentMonth = currentMonth.minusMonths(1);
        }

        System.out.println("Total consistent months: " + consistentMonths);
        return consistentMonths;
    }




}