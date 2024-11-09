package org.habittracker.repository;

import org.habittracker.model.Habit;
import org.habittracker.model.Habit.Frequency;
import org.habittracker.util.HabitStatisticsCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HabitStatisticsCalculatorTest {

    private Habit dailyHabit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;
    private Habit customHabit;

    @BeforeEach
    void setUp() {
        dailyHabit = new Habit("Daily Habit", Frequency.DAILY);
        weeklyHabit = new Habit("Weekly Habit", Frequency.WEEKLY);
        monthlyHabit = new Habit("Monthly Habit", Frequency.MONTHLY);

        // Custom habit with Monday, Wednesday, and Friday
        customHabit = new Habit("Custom Habit", Frequency.CUSTOM);
        customHabit.setCustomDays(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
    }

    @Test
    void testCalculateExpectedCompletions_DailyFrequency() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        int expectedCompletions = HabitStatisticsCalculator.calculateExpectedCompletions(dailyHabit, startDate, endDate);
        assertEquals(7, expectedCompletions); // 7 days in range
    }

    @Test
    void testCalculateExpectedCompletions_WeeklyFrequency() {
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalDate endDate = LocalDate.now();

        int expectedCompletions = HabitStatisticsCalculator.calculateExpectedCompletions(weeklyHabit, startDate, endDate);
        assertEquals(5, expectedCompletions); // 5 weeks in range
    }

    @Test
    void testCalculateExpectedCompletions_MonthlyFrequency() {
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();

        int expectedCompletions = HabitStatisticsCalculator.calculateExpectedCompletions(monthlyHabit, startDate, endDate);
        assertEquals(4, expectedCompletions); // 4 months in range
    }

    @Test
    void testCalculateExpectedCompletions_CustomFrequency() {
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalDate endDate = LocalDate.now();

        int expectedCompletions = HabitStatisticsCalculator.calculateExpectedCompletions(customHabit, startDate, endDate);
        assertEquals(9, expectedCompletions); // 3 weeks with 3 custom days per week = 9 expected completions
    }

    @Test
    void testCalculateWeeklyPerformance_CustomHabit() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate wednesday = today.with(DayOfWeek.WEDNESDAY);

        customHabit.addCompletionForTesting(monday);
        customHabit.addCompletionForTesting(wednesday);

        int weeklyPerformance = HabitStatisticsCalculator.calculateWeeklyPerformance(customHabit);
        assertEquals(66, weeklyPerformance); // 2 out of 3 custom days completed, around 66%
    }

    @Test
    void testCalculateOverallPerformance_DailyHabit() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(19); // 20 days including today
        dailyHabit.setCreationDate(startDate);

        // Set up completions for 10 out of 20 days
        for (int i = 0; i < 10; i++) {
            dailyHabit.markAsCompletedOnDate(today.minusDays(i * 2)); // Completes every other day
        }

        int overallPerformance = HabitStatisticsCalculator.calculateOverallPerformance(dailyHabit);
        assertEquals(50, overallPerformance); // 10 out of 20 days completed, around 50%
    }

    @Test
    void testCalculateOverallPerformance_WeeklyHabit() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(9); // 9 weeks including this week
        weeklyHabit.setCreationDate(startDate);

        // Set up completions for 5 out of 10 weeks
        for (int i = 0; i < 5; i++) {
            weeklyHabit.markAsCompletedOnDate(today.minusWeeks(i * 2)); // Completes every other week
        }

        int overallPerformance = HabitStatisticsCalculator.calculateOverallPerformance(weeklyHabit);
        assertEquals(50, overallPerformance); // 5 out of 10 weeks completed, around 50%
    }

    @Test
    void testCalculateOverallPerformance_MonthlyHabit() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(5); // 6 months including this month
        monthlyHabit.setCreationDate(startDate);

        // Set up completion for 3 out of 6 months
        for (int i = 0; i < 3; i++) {
            monthlyHabit.markAsCompletedOnDate(today.minusMonths(i * 2)); // Completes every other month
        }

        int overallPerformance = HabitStatisticsCalculator.calculateOverallPerformance(monthlyHabit);
        assertEquals(50, overallPerformance); // 3 out of 6 months completed, around 50%
    }
}