package org.habittracker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.habittracker.model.Habit;
import org.habittracker.util.Milestones;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HabitServiceTest {

    private HabitService habitService;
    private Notifier notifier;

    @BeforeEach
    void setUp() {
        notifier = mock(Notifier.class);
        habitService = new HabitService(notifier);
    }

    @Test
    void testCheckMilestones_MilestoneAlreadyAchieved() {
        Habit habit = new Habit("Reading", Habit.Frequency.DAILY);
        habit.setStreakCounter(Milestones.SEVEN_DAYS);
        habit.addMilestone(Milestones.SEVEN_DAYS); // Mark as already achieved

        boolean milestoneReached = habitService.checkMilestones(habit);

        // Verify no notification since milestone already achieved
        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "Milestone should not be reached again.");
    }

    @Test
    void testCheckMilestones_FirstTimeMilestoneAchievement() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setStreakCounter(Milestones.SEVEN_DAYS);

        boolean milestoneReached = habitService.checkMilestones(habit);

        // Verify notification shown and milestone marked
        verify(notifier).showMessage("One week down! Keep the momentum going!", "green");
        assertTrue(milestoneReached, "Milestone should be reached for the first time.");
        assertTrue(habit.isMilestoneAchieved(Milestones.SEVEN_DAYS), "Milestone should be marked as achieved.");
    }

    @Test
    void testCheckMilestones_NoMilestoneAchieved() {
        Habit habit = new Habit("Study", Habit.Frequency.DAILY);
        habit.setStreakCounter(Milestones.FIRST_DAY - 1);

        boolean milestoneReached = habitService.checkMilestones(habit);

        // Verify no notification and no milestone is marked
        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "No milestone should be achieved.");
    }

    @Test
    void testCheckMilestones_StreakBeyondHighestMilestoneNoExactMatch() {
        Habit habit = new Habit("Coding", Habit.Frequency.DAILY);
        habit.setStreakCounter(Milestones.ONE_HUNDRED_DAYS + 5); // Streak beyond last milestone

        boolean milestoneReached = habitService.checkMilestones(habit);

        // Verify that no notification shown as no milestone matches
        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "No milestone should be achieved for unmatched streak.");
    }
}
