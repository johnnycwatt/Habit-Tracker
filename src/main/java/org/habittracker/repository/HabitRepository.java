package org.habittracker.repository;
import org.habittracker.model.Habit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.List;

public class HabitRepository {
    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("habittracker");

    public void addHabit(Habit habit) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(habit);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();

            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                    throw new DuplicateHabitException("A habit with the same name already exists.");
                }
                cause = cause.getCause();
            }

            throw e;
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

    public class DuplicateHabitException extends RuntimeException {
        public DuplicateHabitException(String message) {
            super(message);
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


}
