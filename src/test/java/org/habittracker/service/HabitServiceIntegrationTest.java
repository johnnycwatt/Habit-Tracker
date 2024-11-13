package org.habittracker.service;

import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Milestones;
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
public class HabitServiceIntegrationTest {

    private static EntityManagerFactory entityManagerFactory;
    private HabitRepository habitRepository;
    private HabitService habitService;
    private Notifier notifier;

    @BeforeAll
    void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        habitRepository = new HabitRepository(entityManagerFactory);
        notifier = mock(Notifier.class);
        habitService = new HabitService(notifier);
    }

    @BeforeEach
    void setUp() {
        if (!entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
            habitRepository = new HabitRepository(entityManagerFactory);
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up any added data after each test, if necessary
        habitRepository.getAllHabits().forEach(habitRepository::deleteHabit);
    }

    @AfterAll
    void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testAddAndCompleteHabit() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitService.markHabitAsCompleted(habit);

        Habit savedHabit = habitRepository.findHabitByName("Exercise");
        assertNotNull(savedHabit);
        assertEquals("Exercise", savedHabit.getName());

        verify(notifier, times(3)).showMessage(anyString(), anyString());

        // Clean up
        habitRepository.deleteHabit(savedHabit);
    }


    @Test
    void testMarkHabitAsCompleted_AlreadyCompletedToday() {
        Habit habit = new Habit("Meditate", Habit.Frequency.DAILY);
        habit.markAsCompleted();  // Mark it as completed today

        habitService.markHabitAsCompleted(habit);

        verify(notifier).showMessage("Habit already marked as completed for today.", "red");
        assertEquals(1, habit.getStreakCounter());  // Ensure streak has not incremented again
    }
    @Test
    void testHabitStreakResetAfterMissedDay() {
        Habit habit = new Habit("Daily Walk", Habit.Frequency.DAILY);
        habit.setLastCompletedDate(LocalDate.now().minusDays(2));  // Missed yesterday
        habit.setStreakCounter(0);

        habitService.markHabitAsCompleted(habit);

        assertEquals(1, habit.getStreakCounter());  // Streak reset to 1
        verify(notifier).showMessage("Great start! Every journey begins with the first step. Keep it up!", "green");
    }



    @Test
    void testFindHabitByName_NonExistent() {
        Habit habit = habitService.findHabitByName("NonExistentHabit");

        assertNull(habit);
    }

    @Test
    void testDeleteHabit() {
        Habit habit = new Habit("Reading", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        // Retrieve the habit to ensure it has been persisted and managed by Hibernate
        Habit persistedHabit = habitRepository.findHabitByName("Reading");

        habitService.deleteHabit(persistedHabit);

        assertNull(habitRepository.findHabitByName("Reading"));
        verify(notifier).showMessage("Habit deleted successfully!", "green");
    }




    @ParameterizedTest
    @CsvSource({
            Milestones.FIRST_DAY + ", Great start! Every journey begins with the first step. Keep it up!",
            Milestones.SEVEN_DAYS + ", One week down! Keep the momentum going!",
            Milestones.TWENTY_ONE_DAYS + ", Three weeks in! You're building a great habit!",
            Milestones.FIFTY_DAYS + ", 50 days! That's some serious dedication!",
            Milestones.SIXTY_SIX_DAYS + ", 66 days—this habit is becoming a part of your life!",
            Milestones.ONE_HUNDRED_DAYS + ", 100 days! You've reached an incredible milestone!",
            "999, Milestone reached! Keep up the good work!"  // Test for default case
    })
    void testGenerateMilestoneMessage(int milestone, String expectedMessage) {
        String actualMessage = habitService.generateMilestoneMessage(milestone);
        assertEquals(expectedMessage, actualMessage, "Message did not match for milestone: " + milestone);
    }


}
