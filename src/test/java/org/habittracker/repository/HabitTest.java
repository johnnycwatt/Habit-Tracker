package org.habittracker.repository;

import org.junit.jupiter.api.Test;
import org.habittracker.model.Habit;

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
        assertEquals(0, habit.getStreakCount());
        habit.incrementStreak();
        assertEquals(1, habit.getStreakCount());
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

}
