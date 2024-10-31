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
    private EntityManagerFactory entityManagerFactory;
    private HabitRepository habitRepository;

    @BeforeAll
    void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker");
        habitRepository = new HabitRepository();
    }

    @BeforeEach
    void startTransaction() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Habit").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    void tearDown() {
        entityManagerFactory.close();
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
    void testUpdateHabitStreak(){
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        habit.incrementStreak();
        habitRepository.updateHabit(habit);

        Habit updatedHabit = habitRepository.findHabitById(habit.getId());
        assertEquals(1, updatedHabit.getStreakCount());
    }

}
