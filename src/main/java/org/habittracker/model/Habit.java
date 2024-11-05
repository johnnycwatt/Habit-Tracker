package org.habittracker.model;

import jdk.jfr.Frequency;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "Habit", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    private boolean isCompleted;
    private LocalDate creationDate;
    private LocalDate lastCompletedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_custom_days", joinColumns = @JoinColumn(name = "habit_id"))
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> customDays;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private int streakCounter;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_completed_dates", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "completed_date")
    private Set<LocalDate> completedDates = new HashSet<>();


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
        completedDates.add(today);
        if (lastCompletedDate != null) {
            switch (frequency) {
                case DAILY:
                    if (lastCompletedDate.plusDays(1).equals(today)) {
                        streakCounter++;
                    } else if (!lastCompletedDate.equals(today)) {
                        streakCounter = 1; // Reset streak if not consecutive
                    }
                    break;
                case WEEKLY:
                    if (lastCompletedDate.plusWeeks(1).equals(today)) {
                        streakCounter++;
                    } else if (!lastCompletedDate.equals(today)) {
                        streakCounter = 1;
                    }
                    break;
                case MONTHLY:
                    if (lastCompletedDate.plusMonths(1).equals(today)) {
                        streakCounter++;
                    } else if (!lastCompletedDate.equals(today)) {
                        streakCounter = 1;
                    }
                    break;
            }
        } else {
            streakCounter = 1; // Start the streak
        }

        lastCompletedDate = today; // Update last completion date
        isCompleted = true; 
    }


    public boolean isCompletedToday() {
        return lastCompletedDate != null && lastCompletedDate.equals(LocalDate.now());
    }

    // Getter for completedDates (if not already present)
    public Set<LocalDate> getCompletedDates() {
        return completedDates;
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

    public int getStreakCounter() {
        return streakCounter;
    }

    public void incrementStreak() {
        this.streakCounter++;
    }

    public void resetStreak() {
        this.streakCounter = 0;
    }

    public void setLastCompletedDate(LocalDate date) {
        incrementStreak();
        this.lastCompletedDate = date;
    }

    public LocalDate getLastCompletedDate(){
        return lastCompletedDate;
    }

    public List<DayOfWeek> getCustomDays() {
        return customDays;
    }

    public void setCustomDays(List<DayOfWeek> customDays) {
        this.customDays = customDays;
    }


    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }

}
