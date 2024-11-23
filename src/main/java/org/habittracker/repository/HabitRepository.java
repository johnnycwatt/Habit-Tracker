package org.habittracker.repository;

import org.habittracker.model.Habit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;

public class HabitRepository {

    private static EntityManagerFactory entityManagerFactory;
    private static HabitRepository instance;

    // Private constructor to enforce singleton usage
    private HabitRepository() {}

    /**
     * Initialize the repository with the specified persistence unit.
     * This method must be called before getInstance() is used.
     *
     * @param persistenceUnitName The name of the persistence unit to initialize.
     */
    public static void initialize(String persistenceUnitName) {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
            instance = new HabitRepository();
        }
    }

    /**
     * Get the singleton instance of the HabitRepository.
     *
     * @return HabitRepository instance.
     */
    public static HabitRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HabitRepository has not been initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Close the EntityManagerFactory when the application shuts down.
     */
    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    public Habit addHabit(Habit habit) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            Habit managedHabit = em.merge(habit);
            em.getTransaction().commit();
            return managedHabit;
        } catch (PersistenceException e) {
            em.getTransaction().rollback();

            // Handle constraint violation exception
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                    throw new DuplicateHabitException("A habit with the same name already exists.", e);
                }
                cause = cause.getCause();
            }

            throw new RuntimeException("An error occurred while adding the habit", e);
        } finally {
            em.close();
        }
    }

    public List<Habit> getAllHabits() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT h FROM Habit h", Habit.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Habit findHabitById(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.find(Habit.class, id);
        } finally {
            em.close();
        }
    }

    public Habit findHabitByName(String name) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<Habit> habits = em.createQuery("SELECT h FROM Habit h WHERE h.name = :name", Habit.class)
                    .setParameter("name", name)
                    .getResultList();
            return habits.isEmpty() ? null : habits.get(0);
        } finally {
            em.close();
        }
    }

    public void updateHabit(Habit habit) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(habit);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void deleteHabit(Habit habit) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            Habit managedHabit = em.find(Habit.class, habit.getId());
            if (managedHabit != null) {
                em.remove(managedHabit);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public boolean habitExistsByName(String name) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(h) FROM Habit h WHERE h.name = :name", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    // Custom exception for duplicate habits
    public static class DuplicateHabitException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DuplicateHabitException(String message) {
            super(message);
        }

        public DuplicateHabitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void clearAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Habit").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public int getHabitCount() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(h) FROM Habit h", Long.class).getSingleResult().intValue();
        } finally {
            em.close();
        }
    }

    public int getEarliestCompletionYear(Habit habit) {
        return habit.getCompletedDates()
                .stream()
                .mapToInt(LocalDate::getYear)
                .min()
                .orElse(LocalDate.now().getYear()); // Default to current year if no completions
    }

}
