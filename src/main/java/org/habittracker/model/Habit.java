package org.habittracker.model;

import jdk.jfr.Frequency;

import javax.persistence.*;
import java.time.LocalDate;




@Entity
@Table(name = "Habit", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    private boolean isCompleted;
    private LocalDate creationDate;
    private LocalDate lastCompletedDate;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private int streakCounter;


    public Habit() {
    }

    public Habit(String name, Frequency frequency){
        this.name = name;
        this.isCompleted = false;
        this.creationDate = LocalDate.now();
        this.frequency = frequency;
        this.streakCounter = 0;
    }

    //mark the habit as complete for the day
    public void markAsCompleted() {
        LocalDate today = LocalDate.now();
        if (lastCompletedDate == null || !lastCompletedDate.equals(today)) {
            lastCompletedDate = today;
            streakCounter++;
        }
    }


    public boolean isCompletedToday() {
        return lastCompletedDate != null && lastCompletedDate.equals(LocalDate.now());
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public int getStreakCount() {
        return streakCounter;
    }

    public void incrementStreak() {
        this.streakCounter++;
    }

    public void resetStreak() {
        this.streakCounter = 0;
    }

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY
    }

}
