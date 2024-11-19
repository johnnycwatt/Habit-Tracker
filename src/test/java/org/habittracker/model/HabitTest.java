package org.habittracker.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.YearMonth;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


import static org.junit.jupiter.api.Assertions.*;

public class HabitTest {

    private Habit habit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeAll
    static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }

    @BeforeEach
    void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        // Initialize test habits
        habit = new Habit("TestHabitOne", Habit.Frequency.DAILY);
        weeklyHabit = new Habit("TestHabitTwo", Habit.Frequency.WEEKLY);
        monthlyHabit = new Habit("TestHabitThree", Habit.Frequency.MONTHLY);

        // Persist habits
        entityManager.persist(habit);
        entityManager.persist(weeklyHabit);
        entityManager.persist(monthlyHabit);

        entityManager.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM Habit").executeUpdate();
        entityManager.getTransaction().commit();

        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @AfterAll
    static void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testHabitName() {
        habit.setName("Read");
        assertEquals("Read", habit.getName());
    }

    @ParameterizedTest
    @CsvSource({
            "DAILY, 2",
            "WEEKLY, 2",
            "MONTHLY, 2"
    })
    void testStreakCalculation(String frequency, int expectedStreak) {
        Habit testHabit = new Habit("TestHabit", Habit.Frequency.valueOf(frequency));

        // Simulate completions based on frequency
        switch (frequency) {
            case "DAILY" -> {
                testHabit.addCompletionForTesting(LocalDate.now().minusDays(1));
                testHabit.markAsCompleted();
            }
            case "WEEKLY" -> {
                testHabit.addCompletionForTesting(LocalDate.now().minusWeeks(1));
                testHabit.markAsCompleted();
            }
            case "MONTHLY" -> {
                testHabit.addCompletionForTesting(LocalDate.now().minusMonths(1));
                testHabit.markAsCompleted();
            }
        }

        assertEquals(expectedStreak, testHabit.getStreakCounter(), frequency + " habit should have the expected streak.");
    }

    @ParameterizedTest
    @CsvSource({
            "2024, 11, 1, 2", // Week 1 has 2 completions
            "2024, 11, 2, 2", // Week 2 has 2 completions
            "2024, 11, 3, 2", // Week 3 has 2 completions
            "2024, 11, 4, 1", // Week 4 has 1 completion
            "2024, 11, 5, 1"  // Week 5 has 1 completion
    })
    void testGetCompletionsInWeek(int year, int month, int week, int expectedCompletions) {
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 1));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 2));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 5));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 7));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 12));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 15));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 20));
        weeklyHabit.addCompletionForTesting(LocalDate.of(year, month, 28));

        YearMonth testMonth = YearMonth.of(year, month);
        assertEquals(expectedCompletions, weeklyHabit.getCompletionsInWeek(week, testMonth),
                "Week " + week + " should have the expected number of completions.");
    }


    @ParameterizedTest
    @CsvSource({
            "2024, 11, 4", // 4 completions in November
            "2024, 10, 0"  // 0 completions in October
    })
    void testGetCompletionsInMonth(int year, int month, int expectedCompletions) {
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 1));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 7));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 15));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 29));

        assertEquals(expectedCompletions, monthlyHabit.getCompletionsInMonth(year, month),
                "Month " + month + " in year " + year + " should have the expected number of completions.");
    }

    @Test
    void testBestStreakUpdates() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        habit.setCreationDate(startDate);

        // Simulate completions
        habit.markAsCompletedOnDate(startDate); // Day -6
        habit.markAsCompletedOnDate(startDate.plusDays(1)); // Day -5
        habit.markAsCompletedOnDate(startDate.plusDays(2)); // Day -4: Best streak = 3

        // Break streak
        habit.markAsCompletedOnDate(startDate.plusDays(5)); // Day -1

        // Start a new streak
        habit.markAsCompletedOnDate(startDate.plusDays(6)); // Day 0

        // Assert current streak
        assertEquals(2, habit.getStreakCounter(), "Current streak should reflect the latest streak of consecutive completions.");

        // Assert best streak
        assertEquals(3, habit.getBestStreak(), "Best streak should reflect the longest streak of consecutive completions.");
    }


    @Test
    void testMarkAsCompletedOnFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        habit.markAsCompletedOnDate(futureDate);

        assertFalse(habit.getCompletedDates().contains(futureDate),
                "Completed dates should not include a future date.");
    }


}