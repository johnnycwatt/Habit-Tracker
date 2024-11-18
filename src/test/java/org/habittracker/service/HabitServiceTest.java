package org.habittracker.service;

import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.MilestoneManager;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HabitServiceTest {

    private static EntityManagerFactory entityManagerFactory;
    private HabitRepository habitRepository;
    private HabitService habitService;
    private Notifier notifier;

    @BeforeAll
    public void init() {
        // Initialize JavaFX Toolkit for UI-related operations
        JavaFxInitializer.initToolkit();

        // Initialize the HabitRepository and EntityManagerFactory
        HabitRepository.initialize("habittracker-test");
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");

        notifier = mock(Notifier.class);
        habitService = new HabitService(notifier);

        habitRepository = HabitRepository.getInstance();
    }

    @AfterEach
    public void setUp() {
        habitRepository.clearAll(); // Clear test data before each test
        reset(notifier);
    }

    @AfterAll
    public void tearDown() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    // Unit Tests for checkMilestones
    @Test
    void testCheckMilestones_MilestoneAlreadyAchieved() {
        Habit habit = new Habit("Reading", Habit.Frequency.DAILY);
        habit.setStreakCounter(MilestoneManager.SEVEN_DAYS);
        habit.addMilestone(MilestoneManager.SEVEN_DAYS); // Mark as already achieved

        boolean milestoneReached = habitService.checkMilestones(habit);

        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "Milestone should not be reached again.");
    }

    @Test
    void testCheckMilestones_FirstTimeMilestoneAchievement() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setStreakCounter(MilestoneManager.SEVEN_DAYS);

        boolean milestoneReached = habitService.checkMilestones(habit);

        verify(notifier).showMessage("One week down! Keep the momentum going!", "green");
        assertTrue(milestoneReached, "Milestone should be reached for the first time.");
        assertTrue(habit.isMilestoneAchieved(MilestoneManager.SEVEN_DAYS), "Milestone should be marked as achieved.");
    }

    @Test
    void testCheckMilestones_NoMilestoneAchieved() {
        Habit habit = new Habit("Study", Habit.Frequency.DAILY);
        habit.setStreakCounter(MilestoneManager.FIRST_DAY - 1);

        boolean milestoneReached = habitService.checkMilestones(habit);

        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "No milestone should be achieved.");
    }

    @Test
    void testCheckMilestones_StreakBeyondHighestMilestoneNoExactMatch() {
        Habit habit = new Habit("Coding", Habit.Frequency.DAILY);
        habit.setStreakCounter(MilestoneManager.ONE_HUNDRED_DAYS + 5);

        boolean milestoneReached = habitService.checkMilestones(habit);

        verify(notifier, never()).showMessage(anyString(), anyString());
        assertFalse(milestoneReached, "No milestone should be achieved for unmatched streak.");
    }

    @Test
    public void testMarkHabitAsCompleted_AlreadyCompletedToday() {
        Habit habit = new Habit("Meditate", Habit.Frequency.DAILY);
        habit.setLastCompletedDate(LocalDate.now());
        habitRepository.addHabit(habit);

        habitService.markHabitAsCompleted(habit);

        verify(notifier).showMessage("Habit already marked as completed for today.", "red");
        assertEquals(1, habit.getStreakCounter(), "Streak should not increment again.");
    }

    @Test
    public void testHabitStreakResetAfterMissedDay() {
        Habit habit = new Habit("TestHabit", Habit.Frequency.DAILY);
        habit.setLastCompletedDate(LocalDate.now().minusDays(2));
        habit.setStreakCounter(0);

        Habit persistedHabit = habitRepository.addHabit(habit);

        assertNotNull(persistedHabit.getId(), "Habit ID should not be null after persisting.");

        habitService.markHabitAsCompleted(persistedHabit);

        assertEquals(1, persistedHabit.getStreakCounter(), "Streak should reset to 1 after missing a day.");
        verify(notifier).showMessage("Great start! Every journey begins with the first step. Keep it up!", "green");

    }

    @Test
    public void testFindHabitByName_NonExistent() {
        Habit habit = habitService.findHabitByName("NonExistentHabit");

        assertNull(habit, "Non-existent habit should return null.");
    }

    @Test
    public void testDeleteHabit() {
        Habit habit = new Habit("Reading", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        Habit persistedHabit = habitRepository.findHabitByName("Reading");
        assertNotNull(persistedHabit, "Habit should exist before deletion.");

        habitService.deleteHabit(persistedHabit);

        assertNull(habitRepository.findHabitByName("Reading"), "Habit should be deleted.");
        verify(notifier).showMessage("Habit deleted successfully!", "green");
    }

    @ParameterizedTest
    @CsvSource({
            MilestoneManager.FIRST_DAY + ", Great start! Every journey begins with the first step. Keep it up!",
            MilestoneManager.SEVEN_DAYS + ", One week down! Keep the momentum going!",
            MilestoneManager.TWENTY_ONE_DAYS + ", Three weeks in! You're building a great habit!",
            MilestoneManager.FIFTY_DAYS + ", 50 days! That's some serious dedication!",
            MilestoneManager.SIXTY_SIX_DAYS + ", 66 days—this habit is becoming a part of your life!",
            MilestoneManager.ONE_HUNDRED_DAYS + ", 100 days! You've reached an incredible milestone!",
            "999, Milestone reached! Keep up the good work!"
    })
    public void testGenerateMilestoneMessage(int milestone, String expectedMessage) {
        String actualMessage = habitService.generateMilestoneMessage(milestone);
        assertEquals(expectedMessage, actualMessage, "Message did not match for milestone: " + milestone);
    }
}
