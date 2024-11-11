package org.habittracker.controller;
import org.habittracker.model.Habit;
import org.habittracker.util.EntityManagerFactoryUtil;
import org.habittracker.util.TestNotifier;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import java.time.DayOfWeek;
import java.util.Arrays;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainControllerTest {
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private MainController mainController;
    private Habit dailyHabit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;
    private TestNotifier testNotifier;

    @BeforeAll
    void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }

    @BeforeEach
    void setUp() {
        if (!entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        }
        entityManager = entityManagerFactory.createEntityManager();
        mainController = new MainController();
        testNotifier = new TestNotifier();

        // Initialize habits
        dailyHabit = new Habit("Daily Habit", Habit.Frequency.DAILY);
        weeklyHabit = new Habit("Weekly Habit", Habit.Frequency.WEEKLY);
        monthlyHabit = new Habit("Monthly Habit", Habit.Frequency.MONTHLY);

        // Set start date
        LocalDate startDate = LocalDate.of(2024, 11, 8);
        dailyHabit.setCreationDate(startDate);
        weeklyHabit.setCreationDate(startDate);
        monthlyHabit.setCreationDate(startDate);

        // Start transaction for setting up test data
        entityManager.getTransaction().begin();
        entityManager.persist(dailyHabit);
        entityManager.persist(weeklyHabit);
        entityManager.persist(monthlyHabit);
        entityManager.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }

        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM Habit").executeUpdate();
        entityManager.getTransaction().commit();

        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @AfterAll
    void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
    @Test
    void testDailyHabitDueToday() {
        LocalDate today = LocalDate.of(2024, 11, 8); // same as start date
        assertTrue(mainController.isHabitDueToday(dailyHabit, today));

        today = LocalDate.of(2024, 11, 9); // next day
        assertTrue(mainController.isHabitDueToday(dailyHabit, today));
    }

    @Test
    void testWeeklyHabitDueTodayOnStartDate() {
        LocalDate today = LocalDate.of(2024, 11, 8); // same as start date
        assertTrue(mainController.isHabitDueToday(weeklyHabit, today));
    }

    @Test
    void testWeeklyHabitDueAfterOneWeek() {
        LocalDate oneWeekLater = LocalDate.of(2024, 11, 15);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, oneWeekLater));

        LocalDate twoWeeksLater = LocalDate.of(2024, 11, 22);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, twoWeeksLater));

        LocalDate notDueDay = LocalDate.of(2024, 11, 14); // not a multiple of 7 days
        assertFalse(mainController.isHabitDueToday(weeklyHabit, notDueDay));
    }

    @Test
    void testMonthlyHabitDueTodayOnStartDate() {
        LocalDate today = LocalDate.of(2024, 11, 8); // same as start date
        assertTrue(mainController.isHabitDueToday(monthlyHabit, today));
    }

    @Test
    void testMonthlyHabitDueNextMonth() {
        LocalDate oneMonthLater = LocalDate.of(2024, 12, 8);
        assertTrue(mainController.isHabitDueToday(monthlyHabit, oneMonthLater));

        LocalDate twoMonthsLater = LocalDate.of(2025, 1, 8);
        assertTrue(mainController.isHabitDueToday(monthlyHabit, twoMonthsLater));

        LocalDate notDueDay = LocalDate.of(2024, 12, 7); // one day before due
        assertFalse(mainController.isHabitDueToday(monthlyHabit, notDueDay));
    }

    @Test
    void testMonthlyHabitDueEndOfMonth() {
        LocalDate endOfMonthStartDate = LocalDate.of(2024, 1, 31);
        Habit endOfMonthHabit = new Habit("End of Month Habit", Habit.Frequency.MONTHLY);
        endOfMonthHabit.setCreationDate(endOfMonthStartDate);

        // Check due on months without 31st
        LocalDate endOfFebDueDate = LocalDate.of(2024, 2, 29); // Leap year
        assertTrue(mainController.isHabitDueToday(endOfMonthHabit, endOfFebDueDate));

        LocalDate endOfAprilDueDate = LocalDate.of(2024, 4, 30); // April has only 30 days
        assertTrue(mainController.isHabitDueToday(endOfMonthHabit, endOfAprilDueDate));
    }

    @Test
    void testCustomHabitDueOnSelectedDays() {
        // habit set to occur on Monday, Wednesday, and Friday
        Habit customHabit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        customHabit.setCreationDate(LocalDate.of(2024, 11, 8));
        customHabit.setCustomDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        // Check if the habit is due
        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 8))); // Friday
        assertFalse(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 9))); // Saturday
        assertFalse(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 10))); // Sunday
        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 11))); // Monday
        assertFalse(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 12))); // Tuesday
        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 13))); // Wednesday
    }



    @Test
    void testCustomHabitDueOnWeekendsOnly() {
        //habit set to occur on Saturday and Sunday
        Habit weekendHabit = new Habit("Weekend Habit", Habit.Frequency.CUSTOM);
        weekendHabit.setCreationDate(LocalDate.of(2024, 11, 8));
        weekendHabit.setCustomDays(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        // Check if habit is due on Saturday and Sunday
        assertTrue(mainController.isHabitDueToday(weekendHabit, LocalDate.of(2024, 11, 9))); // Saturday
        assertTrue(mainController.isHabitDueToday(weekendHabit, LocalDate.of(2024, 11, 10))); // Sunday
        assertFalse(mainController.isHabitDueToday(weekendHabit, LocalDate.of(2024, 11, 11))); // Monday
        assertFalse(mainController.isHabitDueToday(weekendHabit, LocalDate.of(2024, 11, 12))); // Tuesday
    }


    @Test
    void testCustomHabitDueOnSpecificDaysWithOffsetStartDate() {
        // starting on a non-selected day, but due on Tuesday and Friday
        Habit offsetHabit = new Habit("Offset Habit", Habit.Frequency.CUSTOM);
        offsetHabit.setCreationDate(LocalDate.of(2024, 11, 6)); // Start date is Wednesday
        offsetHabit.setCustomDays(Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

        // Check if the habit is due on Thursday and Friday and not Wednesday
        assertFalse(mainController.isHabitDueToday(offsetHabit, LocalDate.of(2024, 11, 6))); // Wednesday (start date)
        assertTrue(mainController.isHabitDueToday(offsetHabit, LocalDate.of(2024, 11, 7))); // Thursday
        assertTrue(mainController.isHabitDueToday(offsetHabit, LocalDate.of(2024, 11, 8))); // Friday
        assertFalse(mainController.isHabitDueToday(offsetHabit, LocalDate.of(2024, 11, 9))); // Saturday
    }





    @Test
    void testWeeklyHabitReminder() {
        LocalDate startDate = LocalDate.of(2024, 11, 8); // Habit start date
        weeklyHabit.setCreationDate(startDate);

        LocalDate lastCompletedDate = startDate.plusWeeks(1); // Completed after one week
        weeklyHabit.setLastCompletedDate(lastCompletedDate);

        // Next reminder should be one week after the last completion
        LocalDate nextReminderDate = lastCompletedDate.plusWeeks(1);
        assertTrue(mainController.isReminderDue(weeklyHabit, nextReminderDate),
                "Expected reminder for weekly habit on " + nextReminderDate);

        // Ensure no reminder one day before the expected reminder
        LocalDate dayBeforeReminder = nextReminderDate.minusDays(1);
        assertFalse(mainController.isReminderDue(weeklyHabit, dayBeforeReminder),
                "No reminder expected for weekly habit on " + dayBeforeReminder);
    }


    @Test
    void testMonthlyHabitReminder() {
        LocalDate startDate = LocalDate.of(2024, 11, 8);
        monthlyHabit.setCreationDate(startDate);

        LocalDate lastCompletedDate = startDate.plusMonths(1);
        monthlyHabit.setLastCompletedDate(lastCompletedDate);

        // Next reminder should be one month after the last completion
        LocalDate nextReminderDate = lastCompletedDate.plusMonths(1);
        assertTrue(mainController.isReminderDue(monthlyHabit, nextReminderDate),
                "Expected reminder for monthly habit on " + nextReminderDate);

        // Ensure no reminder one day before the expected reminder
        LocalDate dayBeforeReminder = nextReminderDate.minusDays(1);
        assertFalse(mainController.isReminderDue(monthlyHabit, dayBeforeReminder),
                "No reminder expected for monthly habit on " + dayBeforeReminder);
    }

}
