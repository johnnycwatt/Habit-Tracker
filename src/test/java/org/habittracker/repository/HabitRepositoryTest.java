package org.habittracker.repository;

import org.habittracker.model.Habit;
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
    void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        habitRepository = new HabitRepository(entityManagerFactory);
    }

    @BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
        habitRepository = new HabitRepository(entityManagerFactory);  // Use test-specific EntityManagerFactory
    }

    @AfterAll
    void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testAddHabit() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        // Verify that the habit is added and retrievable by name
        assertNotNull(habitRepository.findHabitByName("Exercise"));
    }

    @Test
    void testFindHabitByName() {
        Habit habit = new Habit("Read", Habit.Frequency.WEEKLY);
        habitRepository.addHabit(habit);

        Habit habitExists = habitRepository.findHabitByName("Read");
        assertNotNull(habitExists);
        assertEquals("Read", habitExists.getName());
    }

    @Test
    void testEditHabit() {
        // Create and add a new habit
        Habit originalHabit = new Habit("Exercise", Habit.Frequency.DAILY);
        originalHabit.setCreationDate(LocalDate.now().minusDays(10));
        habitRepository.addHabit(originalHabit);

        // Retrieve the habit, modify it, and update it
        Habit habitToEdit = habitRepository.findHabitByName("Exercise");
        habitToEdit.setName("Exercise Updated");
        habitToEdit.setFrequency(Habit.Frequency.WEEKLY);
        habitToEdit.setCreationDate(LocalDate.now().minusDays(5));
        habitRepository.updateHabit(habitToEdit);

        // Verify the changes
        Habit updatedHabit = habitRepository.findHabitByName("Exercise Updated");
        assertNotNull(updatedHabit);
        assertEquals("Exercise Updated", updatedHabit.getName());
        assertEquals(Habit.Frequency.WEEKLY, updatedHabit.getFrequency());
        assertEquals(LocalDate.now().minusDays(5), updatedHabit.getCreationDate());

        habitRepository.deleteHabit(updatedHabit);
    }
}
