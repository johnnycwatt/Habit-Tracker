package org.habittracker.repository;
import org.habittracker.controller.MainController;
import org.habittracker.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import java.time.DayOfWeek;
import java.util.Arrays;


public class MainControllerTest {

    private MainController mainController;
    private Habit dailyHabit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;

    @BeforeEach
    void setUp() {
        mainController = new MainController();

        // Create daily, weekly, and monthly habits starting on a specific date
        dailyHabit = new Habit("Daily Habit", Habit.Frequency.DAILY);
        weeklyHabit = new Habit("Weekly Habit", Habit.Frequency.WEEKLY);
        monthlyHabit = new Habit("Monthly Habit", Habit.Frequency.MONTHLY);
        LocalDate startDate = LocalDate.of(2024, 11, 8);
        dailyHabit.setCreationDate(startDate);
        weeklyHabit.setCreationDate(startDate);
        monthlyHabit.setCreationDate(startDate);
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

}
