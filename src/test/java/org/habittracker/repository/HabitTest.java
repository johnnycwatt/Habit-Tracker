package org.habittracker.repository;

import org.habittracker.util.EntityManagerFactoryUtil;
import org.junit.jupiter.api.*;
import org.habittracker.model.Habit;
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

    @Test
    void testIncrementStreak() {
        assertEquals(0, habit.getStreakCounter());
        habit.incrementStreak();
        assertEquals(1, habit.getStreakCounter());
    }

    @Test
    void testGetFrequency() {
        assertEquals(Habit.Frequency.DAILY, habit.getFrequency());
    }

    @Test
    void testSetFrequency() {
        habit.setFrequency(Habit.Frequency.WEEKLY);
        assertEquals(Habit.Frequency.WEEKLY, habit.getFrequency());
    }

    @Test
    void testDailyHabitStreak() {
        habit.addCompletionForTesting(LocalDate.now().minusDays(1)); // Complete yesterday

        habit.markAsCompleted(); // complete today
        assertEquals(2, habit.getStreakCounter()); // should increase to 2
    }

    @Test
    void testWeeklyHabitStreak() {
        weeklyHabit.addCompletionForTesting(LocalDate.now().minusWeeks(1)); // Complete last week

        weeklyHabit.markAsCompleted(); // complete today
        assertEquals(2, weeklyHabit.getStreakCounter()); // should increase to 2
    }

    @Test
    void testMonthlyHabitStreak() {
        monthlyHabit.addCompletionForTesting(LocalDate.now().minusMonths(1)); // Complete last month

        monthlyHabit.markAsCompleted(); // complete today
        assertEquals(2, monthlyHabit.getStreakCounter()); // should increase to 2
    }
    @Test
    void testHabitDefaultColor() {
        assertEquals("#000000", habit.getColor(), "Default color should be black (#000000)");
    }


    @Test
    void testSetHabitColor_Blue() {
        habit.setColor("#0000FF"); //blue
        assertEquals("#0000FF", habit.getColor(), "The color should be blue (#0000FF)");
    }

    @Test
    void testEditHabitColor() {
        habit.setColor("#0000FF"); // set to blue

        // Change color to green
        habit.setColor("#008000");
        assertEquals("#008000", habit.getColor(), "The color should be updated to green (#008000)");

        //Change color to red
        habit.setColor("#FF0000");
        assertEquals("#FF0000", habit.getColor(), "The color should be updated to red (#FF0000)");
    }

    @Test
    void testReminderEligibilityForWeeklyHabit() {
        weeklyHabit.setCreationDate(LocalDate.now().minusWeeks(1).plusDays(6)); // Set it to be eligible tomorrow

        assertTrue(weeklyHabit.isReminderEligible(), "Weekly habit should be eligible for a reminder tomorrow.");
    }

    @Test
    void testReminderEligibilityForMonthlyHabit() {
        monthlyHabit.setCreationDate(LocalDate.now().minusMonths(1).plusDays(29)); // Set it to be eligible tomorrow

        assertTrue(monthlyHabit.isReminderEligible(), "Monthly habit should be eligible for a reminder tomorrow.");
    }

    @Test
    void testGetCompletionsInWeek() {
        YearMonth testMonth = YearMonth.of(2024, 11); // November 2024

        // Simulate completion on specific dates in November 2024
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 1)); // Expected in Week 1
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 2)); // Expected in Week 1
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 5)); // Expected in Week 2
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 7)); // Expected in Week 2
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 12)); // Expected in Week 3
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 15)); // Expected in Week 3
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 20)); // Expected in Week 4
        weeklyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 28)); // Expected in Week 5

        // Verify completions in each week
        assertEquals(2, weeklyHabit.getCompletionsInWeek(1, testMonth), "Week 1 should have 2 completions");
        assertEquals(2, weeklyHabit.getCompletionsInWeek(2, testMonth), "Week 2 should have 2 completions");
        assertEquals(2, weeklyHabit.getCompletionsInWeek(3, testMonth), "Week 3 should have 2 completions");
        assertEquals(1, weeklyHabit.getCompletionsInWeek(4, testMonth), "Week 4 should have 1 completion");
        assertEquals(1, weeklyHabit.getCompletionsInWeek(5, testMonth), "Week 5 should have 1 completion");
    }

    @Test
    void testGetCompletionsInMonth() {
        int testYear = 2024;
        int testMonth = 11; // November

        // Simulate completion on specific dates in November 2024
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 1));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 7));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 15));
        monthlyHabit.addCompletionForTesting(LocalDate.of(2024, 11, 29));

        assertEquals(4, monthlyHabit.getCompletionsInMonth(testYear, testMonth), "November 2024 should have 4 completions");

        // Test for a different month with no completions
        int otherMonth = 10; // October
        assertEquals(0, monthlyHabit.getCompletionsInMonth(testYear, otherMonth), "October 2024 should have 0 completions");
    }

    @Test
    void testMarkAsCompletedOnSpecificDate() {

        LocalDate startDate = LocalDate.now().minusDays(3); // Set start date to three days ago
        habit.setCreationDate(startDate);

        LocalDate specificDate = LocalDate.now().minusDays(3);

        habit.markAsCompletedOnDate(specificDate);
        assertTrue(habit.getCompletedDates().contains(specificDate),
                "Completed dates should include the specified date.");
    }
    @Test
    void testBackdatedCompletionWithStreak() {

        // Mark as completed for today and three days ago
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        habit.markAsCompletedOnDate(threeDaysAgo);
        habit.markAsCompleted(); // Mark for today

        assertEquals(1, habit.getStreakCounter(),
                "Backdated completion shouldn't affect the current streak counter.");
    }


    @Test
    void testConsecutiveCompletionWithStreak() {
        LocalDate startDate = LocalDate.now().minusDays(2);
        habit.setCreationDate(startDate);

        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();

        habit.markAsCompletedOnDate(twoDaysAgo);
        habit.markAsCompletedOnDate(yesterday);
        habit.markAsCompletedOnDate(today);

        assertEquals(3, habit.getStreakCounter(),
                "Streak counter should reflect three consecutive days of completion.");
    }

    @Test
    void testNonConsecutiveCompletionResetsStreak() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        LocalDate today = LocalDate.now();

        habit.markAsCompletedOnDate(threeDaysAgo);
        habit.markAsCompletedOnDate(today);

        assertEquals(1, habit.getStreakCounter(),
                "Streak should reset after a non-consecutive completion.");
    }


    @Test
    void testWeeklyHabitBackdatedCompletion() {

        LocalDate startDate = LocalDate.now().minusWeeks(2);
        weeklyHabit.setCreationDate(startDate);

        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);

        weeklyHabit.markAsCompletedOnDate(twoWeeksAgo);
        weeklyHabit.markAsCompletedOnDate(oneWeekAgo);

        assertEquals(2, weeklyHabit.getStreakCounter(),
                "Weekly habit should maintain a streak for consecutive weekly completions.");
    }

    @Test
    void testMonthlyHabitBackdatedCompletion() {
        LocalDate startDate = LocalDate.now().minusMonths(2); // Set start date to two months ago
        monthlyHabit.setCreationDate(startDate);

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        monthlyHabit.markAsCompletedOnDate(twoMonthsAgo);
        monthlyHabit.markAsCompletedOnDate(oneMonthAgo);

        assertEquals(2, monthlyHabit.getStreakCounter(),
                "Monthly habit should maintain a streak for consecutive monthly completions.");
    }

    @Test
    void testMarkAsCompletedOnFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        
        habit.markAsCompletedOnDate(futureDate);

        assertFalse(habit.getCompletedDates().contains(futureDate),
                "Completed dates should not include a future date.");
    }

}
