package org.habittracker.repository;

import org.habittracker.model.*;
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
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }

    @BeforeEach
    void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        habitRepository = new HabitRepository(entityManagerFactory);
    }

    @AfterEach
    void tearDown() {
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM Habit").executeUpdate();
        entityManager.getTransaction().commit();

        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @AfterAll
    static void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    void testAddAndRetrieveHabit(){
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        List<Habit> habits = habitRepository.getAllHabits();
        assertEquals(1, habits.size());
        assertEquals("Exercise", habits.get(0).getName());
    }

    @Test
    void testUpdateHabitStreak() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit = habitRepository.addHabit(habit);

        habit.incrementStreak();
        habitRepository.updateHabit(habit);

        Habit updatedHabit = habitRepository.findHabitById(habit.getId());
        assertEquals(1, updatedHabit.getStreakCounter());
    }


    @Test
    void testFindHabitByName() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        Habit foundHabit = habitRepository.findHabitByName("Exercise");
        assertNotNull(foundHabit);
        assertEquals("Exercise", foundHabit.getName());

        habitRepository.deleteHabit(foundHabit);
    }

}
