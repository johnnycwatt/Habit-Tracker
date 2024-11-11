package org.habittracker.repository;

import org.habittracker.model.Habit;
import org.habittracker.model.Habit.Frequency;
import org.habittracker.util.HabitStatisticsCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HabitStatisticsCalculatorTest {

    private Habit dailyHabit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;
    private Habit customHabit;

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Initialize EntityManagerFactory and EntityManager for the test persistence unit
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        entityManager = entityManagerFactory.createEntityManager();

        dailyHabit = new Habit("Daily Habit", Frequency.DAILY);
        weeklyHabit = new Habit("Weekly Habit", Frequency.WEEKLY);
        monthlyHabit = new Habit("Monthly Habit", Frequency.MONTHLY);

        // Custom habit with Monday, Wednesday, and Friday
        customHabit = new Habit("Custom Habit", Frequency.CUSTOM);
        customHabit.setCustomDays(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        // Persist initial data if needed
        entityManager.getTransaction().begin();
        entityManager.persist(dailyHabit);
        entityManager.persist(weeklyHabit);
        entityManager.persist(monthlyHabit);
        entityManager.persist(customHabit);
        entityManager.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        if (entityManager.isOpen()) {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Habit").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
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
        LocalDate startDate = LocalDate.of(2024, 10, 20);
        LocalDate endDate = LocalDate.of(2024, 11, 3);

        int expectedCompletions = HabitStatisticsCalculator.calculateExpectedCompletions(customHabit, startDate, endDate);
        assertEquals(6, expectedCompletions);
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


    @Test
    void testCalculateWeeklyConsistency_WeeklyHabit() {
        Habit weeklyHabit = new Habit("Weekly Habit for Weekly Consistency", Frequency.WEEKLY);


        LocalDate today = LocalDate.of(2024, 11, 10);
        weeklyHabit.setCreationDate(today.minusWeeks(4));

        weeklyHabit.addCompletionForTesting(today);
        weeklyHabit.addCompletionForTesting(today.minusWeeks(1)); // Last week
        weeklyHabit.addCompletionForTesting(today.minusWeeks(2)); // Two weeks ago
        weeklyHabit.addCompletionForTesting(today.minusWeeks(3)); // Three weeks ago

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(weeklyHabit);
        assertEquals(4, weeklyConsistency); // Expect 4 consistent weeks
    }


    @Test
    void testCalculateWeeklyConsistency_MonthlyHabit() {
        Habit monthlyHabit = new Habit("Monthly Habit for Weekly Consistency", Frequency.MONTHLY);
        LocalDate today = LocalDate.of(2024, 11, 10); // Assume today is a Sunday

        // Monthly habit should not have weekly consistency, so simulate with a monthly completion
        monthlyHabit.addCompletionForTesting(today.minusMonths(1).withDayOfMonth(1)); // One month ago
        monthlyHabit.addCompletionForTesting(today.minusMonths(2).withDayOfMonth(1)); // Two months ago

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(monthlyHabit);
        assertEquals(0, weeklyConsistency); // Weekly consistency not applicable for monthly habits
    }


    @Test
    void testCalculateWeeklyConsistency_WeeklyHabit_WithCurrentWeekIncomplete() {
        Habit weeklyHabit = new Habit("Weekly Habit for Weekly Consistency", Frequency.WEEKLY);

        LocalDate today = LocalDate.now();
        weeklyHabit.setCreationDate(today.minusWeeks(4));

        // Simulate completions for previous weeks but none for the current week
        weeklyHabit.addCompletionForTesting(today.minusWeeks(1));
        weeklyHabit.addCompletionForTesting(today.minusWeeks(2));
        weeklyHabit.addCompletionForTesting(today.minusWeeks(3));

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(weeklyHabit);
        assertEquals(3, weeklyConsistency); // Should count three consistent weeks and ignore current week as current week ongoing
    }

    @Test
    void testCalculateWeeklyConsistency_WeeklyHabit_WithTwoIncompleteWeeks() {
        Habit weeklyHabit = new Habit("Weekly Habit for Weekly Consistency", Frequency.WEEKLY);

        LocalDate today = LocalDate.of(2024, 11, 10);
        weeklyHabit.setCreationDate(today.minusWeeks(4));

        // Simulate completions only in the third and fourth previous weeks
        weeklyHabit.addCompletionForTesting(today.minusWeeks(3));
        weeklyHabit.addCompletionForTesting(today.minusWeeks(4));

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(weeklyHabit);
        assertEquals(0, weeklyConsistency); // Should reset to 0 after two incomplete weeks (this week and last week)
    }

    @Test
    void testCalculateMonthlyConsistency_DailyHabit() {
        Habit dailyHabit = new Habit("Daily Habit for Monthly Consistency", Frequency.DAILY);

        LocalDate today = LocalDate.of(2024, 11, 10);
        dailyHabit.setCreationDate(today.minusMonths(3));

        // Simulate completions for every day of last two months
        for (LocalDate date = today.minusMonths(1).withDayOfMonth(1);
             !date.isAfter(today); date = date.plusDays(1)) {
            dailyHabit.addCompletionForTesting(date);
        }

        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(dailyHabit);
        assertEquals(1, monthlyConsistency); // Expect 1 Consistent Month (October)
    }

    @Test
    void testCalculateMonthlyConsistency_WeeklyHabit() {
        Habit weeklyHabit = new Habit("Weekly Habit for Monthly Consistency", Frequency.WEEKLY);

        LocalDate today = LocalDate.of(2024, 11, 10);
        weeklyHabit.setCreationDate(today.minusMonths(3));

        // Simulate at least one completion per week for last two months
        for (int week = 0; week < 4; week++) {
            weeklyHabit.addCompletionForTesting(today.minusMonths(1).with(DayOfWeek.MONDAY).plusWeeks(week));
            weeklyHabit.addCompletionForTesting(today.with(DayOfWeek.MONDAY).plusWeeks(week));
        }

        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(weeklyHabit);
        assertEquals(2, monthlyConsistency); // Expect 2 consistent months (October and November)
    }

    @Test
    void testCalculateMonthlyConsistency_MonthlyHabit() {
        Habit monthlyHabit = new Habit("Monthly Habit for Monthly Consistency", Frequency.MONTHLY);

        LocalDate today = LocalDate.of(2024, 11, 10);
        monthlyHabit.setCreationDate(today.minusMonths(3));

        // Simulate one completion per month
        monthlyHabit.addCompletionForTesting(today.minusMonths(1).withDayOfMonth(1));
        monthlyHabit.addCompletionForTesting(today.minusMonths(2).withDayOfMonth(1));

        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(monthlyHabit);
        assertEquals(2, monthlyConsistency); // Expect 2 consistent months (September and October)
    }

    @Test
    void testCalculateWeeklyConsistency_CustomHabit_Consistent() {
        Habit customHabit = new Habit("Custom Weekly Habit", Frequency.CUSTOM);
        Set<DayOfWeek> customDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        customHabit.setCustomDays(new ArrayList<>(customDays));

        LocalDate today = LocalDate.now();
        customHabit.setCreationDate(today.minusWeeks(3));

        //Complete custom day in the past week
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        for (DayOfWeek day : customDays) {
            customHabit.addCompletionForTesting(startOfWeek.with(day)); // e.g: Monday, Wednesday, Friday
        }

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(customHabit);
        assertEquals(1, weeklyConsistency); // Should be consistent
    }

    @Test
    void testCalculateWeeklyConsistency_CustomHabit_Inconsistent() {
        Habit customHabit = new Habit("Custom Weekly Habit", Frequency.CUSTOM);
        Set<DayOfWeek> customDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        customHabit.setCustomDays(new ArrayList<>(customDays));

        LocalDate today = LocalDate.now();
        customHabit.setCreationDate(today.minusWeeks(3));

        //Skip one of the specified custom days (e.g., no completion on Friday)
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        customHabit.addCompletionForTesting(startOfWeek.with(DayOfWeek.MONDAY));
        customHabit.addCompletionForTesting(startOfWeek.with(DayOfWeek.WEDNESDAY));

        int weeklyConsistency = HabitStatisticsCalculator.calculateWeeklyConsistency(customHabit);
        assertEquals(0, weeklyConsistency); // Should be inconsistent due to the missing Friday
    }

    @Test
    void testCalculateMonthlyConsistency_CustomHabit_Consistent() {
        Habit customHabit = new Habit("Custom Monthly Habit", Frequency.CUSTOM);
        Set<DayOfWeek> customDays = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        customHabit.setCustomDays(new ArrayList<>(customDays));

        LocalDate today = LocalDate.now();
        customHabit.setCreationDate(today.minusMonths(1));

        // Simulate completions on each custom day over the past month
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate date = startOfMonth;

        while (!date.isAfter(endOfMonth)) {
            if (customDays.contains(date.getDayOfWeek())) {
                customHabit.addCompletionForTesting(date); // Complete on each custom day
            }
            date = date.plusDays(1);
        }

        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(customHabit);
        assertEquals(1, monthlyConsistency); // Should be consistent
    }

    @Test
    void testCalculateMonthlyConsistency_CustomHabit_Inconsistent() {
        Habit customHabit = new Habit("Custom Monthly Habit", Frequency.CUSTOM);
        Set<DayOfWeek> customDays = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        customHabit.setCustomDays(new ArrayList<>(customDays));

        LocalDate today = LocalDate.now();
        customHabit.setCreationDate(today.minusMonths(1));

        // Miss some custom days within the month
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate date = startOfMonth;

        while (!date.isAfter(endOfMonth)) {
            if (customDays.contains(date.getDayOfWeek()) && date.getDayOfMonth() % 2 == 0) {
                customHabit.addCompletionForTesting(date); // Complete on half the custom days
            }
            date = date.plusDays(1);
        }

        int monthlyConsistency = HabitStatisticsCalculator.calculateMonthlyConsistency(customHabit);
        assertEquals(0, monthlyConsistency); // Should be inconsistent
    }

}