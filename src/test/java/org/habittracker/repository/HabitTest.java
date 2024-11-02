package org.habittracker.repository;

import org.junit.jupiter.api.Test;
import org.habittracker.model.Habit;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HabitTest {

    @Test
    void testHabitName() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setName("Read");
        assertEquals("Read", habit.getName());
    }

    @Test
    void testIncrementStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        assertEquals(0, habit.getStreakCounter());
        habit.incrementStreak();
        assertEquals(1, habit.getStreakCounter());
    }

    @Test
    void testGetFrequency() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        assertEquals(Habit.Frequency.DAILY, habit.getFrequency());
    }

    @Test
    void testSetFrequency() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setFrequency(Habit.Frequency.WEEKLY);
        assertEquals(Habit.Frequency.WEEKLY, habit.getFrequency());
    }

    @Test
    void testDailyHabitStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setLastCompletedDate(LocalDate.now().minusDays(1)); // Complete yesterday

        habit.markAsCompleted(); // complete today
        assertEquals(2, habit.getStreakCounter()); // should increase to 2
    }

    @Test
    void testWeeklyHabitStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.WEEKLY);
        habit.setLastCompletedDate(LocalDate.now().minusWeeks(1)); // Complete last week

        habit.markAsCompleted(); // complete today
        assertEquals(2, habit.getStreakCounter()); // should increase to 2
    }

    @Test
    void testMonthlyHabitStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.MONTHLY);
        habit.setLastCompletedDate(LocalDate.now().minusMonths(1)); // Complete last month

        habit.markAsCompleted(); // complete today
        assertEquals(2, habit.getStreakCounter()); // should increase to 2
    }


}
