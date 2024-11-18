package org.habittracker.repository;

import org.habittracker.model.Habit;
import org.habittracker.util.JavaFxInitializer;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HabitRepositoryIntegrationTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private HabitRepository habitRepository;

    @BeforeAll
    static void init() {
        // Initialize JavaFX Toolkit
        JavaFxInitializer.initToolkit();

        // Initialize EntityManagerFactory
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");

        // Initialize the HabitRepository
        HabitRepository.initialize("habittracker-test");
    }

    @BeforeEach
    void setUp() {
        // Create a new EntityManager for each test
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        // Get the instance of HabitRepository
        habitRepository = HabitRepository.getInstance();
    }

    @AfterEach
    void tearDown() {
        // Clear all data from the Habit table after each test
        entityManager.createQuery("DELETE FROM Habit").executeUpdate();

        // Commit the transaction and close the EntityManager
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @AfterAll
    static void close() {
        // Close EntityManagerFactory after all tests
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testAddAndRetrieveHabit() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);

        // Add habit to the repository
        habitRepository.addHabit(habit);

        // Retrieve all habits and verify
        List<Habit> habits = habitRepository.getAllHabits();
        assertEquals(1, habits.size());
        assertEquals("Exercise", habits.get(0).getName());

        // Reload the habit to ensure it is managed
        Habit managedHabit = habitRepository.findHabitByName("Exercise");

        // Cleanup: Delete the managed habit
        habitRepository.deleteHabit(managedHabit);
    }


    @Test
    void testUpdateHabitStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);

        // Add habit to the repository
        habit = habitRepository.addHabit(habit);

        // Update habit streak and save
        habit.incrementStreak();
        habitRepository.updateHabit(habit);

        // Retrieve and verify the updated habit
        Habit updatedHabit = habitRepository.findHabitById(habit.getId());
        assertEquals(1, updatedHabit.getStreakCounter());

        // Cleanup
        habitRepository.deleteHabit(updatedHabit);
    }

    @Test
    void testFindHabitByName() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);

        // Add habit to the repository
        habitRepository.addHabit(habit);

        // Find habit by name and verify
        Habit foundHabit = habitRepository.findHabitByName("Exercise");
        assertNotNull(foundHabit);
        assertEquals("Exercise", foundHabit.getName());

        // Cleanup
        habitRepository.deleteHabit(foundHabit);
    }
}
