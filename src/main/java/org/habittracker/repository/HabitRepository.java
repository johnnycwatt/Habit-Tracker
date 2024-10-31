package org.habittracker.repository;
import org.habittracker.model.Habit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class HabitRepository {
    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("habittracker");

    public void addHabit(Habit habit){
        EntityManager em = entityManagerFactory.createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(habit);
            em.getTransaction().commit();
        }finally{
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

}
