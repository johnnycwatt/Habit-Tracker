package org.habittracker.repository;

import org.habittracker.model.Habit;
import org.habittracker.util.JavaFxInitializer;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HabitRepositoryTest {

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
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        habitRepository = HabitRepository.getInstance();// Use test-specific EntityManagerFactory
    }

    @AfterAll
    void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testAddHabit() {
        Habit habit = new Habit("TestHabit", Habit.Frequency.DAILY);
        Habit persistedHabit = habitRepository.addHabit(habit); // Use the returned habit

        // Verify that the habit is added and retrievable by name
        assertNotNull(habitRepository.findHabitByName("TestHabit"));

        // Use the persisted instance for deletion
        habitRepository.deleteHabit(persistedHabit);
    }


    @Test
    void testFindHabitByName() {
        Habit habit = new Habit("TestHabit", Habit.Frequency.WEEKLY);
        Habit persistedHabit = habitRepository.addHabit(habit); // Use the returned habit

        Habit habitExists = habitRepository.findHabitByName("TestHabit");
        assertNotNull(habitExists);
        assertEquals("TestHabit", habitExists.getName());

        // Use the persisted instance for deletion
        habitRepository.deleteHabit(persistedHabit);
    }

    @Test
    void testEditHabit() {
        // Create and add a new habit
        Habit originalHabit = new Habit("TestHabit", Habit.Frequency.DAILY);
        originalHabit.setCreationDate(LocalDate.now().minusDays(10));
        habitRepository.addHabit(originalHabit);

        // Retrieve the habit, modify it, and update it
        Habit habitToEdit = habitRepository.findHabitByName("TestHabit");
        habitToEdit.setName("UpdatedHabit");
        habitToEdit.setFrequency(Habit.Frequency.WEEKLY);
        habitToEdit.setCreationDate(LocalDate.now().minusDays(5));
        habitRepository.updateHabit(habitToEdit);

        // Verify the changes
        Habit updatedHabit = habitRepository.findHabitByName("UpdatedHabit");
        assertNotNull(updatedHabit);
        assertEquals("UpdatedHabit", updatedHabit.getName());
        assertEquals(Habit.Frequency.WEEKLY, updatedHabit.getFrequency());
        assertEquals(LocalDate.now().minusDays(5), updatedHabit.getCreationDate());

        habitRepository.deleteHabit(updatedHabit);
    }
}
