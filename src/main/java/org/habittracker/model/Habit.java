package org.habittracker.model;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "Habit", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Habit {

    private static final Logger LOGGER = LogManager.getLogger(Habit.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "color", nullable = true)
    private String color;

    private boolean isCompleted;
    private LocalDate creationDate;
    private LocalDate lastCompletedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_milestones", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "milestone")
    private Set<Integer> completedMilestones = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_custom_days", joinColumns = @JoinColumn(name = "habit_id"))
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> customDays;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private int streakCounter;

    @Column(name = "reminder_eligible", nullable = false)
    private boolean reminderEligible = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_completions", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "completion_date")
    private Set<LocalDate> completions = new HashSet<>();

    @Column(name = "best_streak", nullable = false)
    private int bestStreak;

    public Habit() {}

    public Habit(String name, Frequency frequency) {
        this.name = name;
        this.isCompleted = false;
        this.creationDate = LocalDate.now();
        this.frequency = frequency;
        this.color = "#000000";
        this.streakCounter = 0;
    }

    public Habit(String name, Frequency frequency, List<DayOfWeek> customDays) {
        this.name = name;
        this.isCompleted = false;
        this.creationDate = LocalDate.now();
        this.frequency = frequency;
        this.color = "#000000";
        this.streakCounter = 0;
        this.customDays = customDays; // Set custom days here
    }


    public void markAsCompleted() {
        markAsCompletedOnDate(LocalDate.now());
    }

    public void markAsCompletedOnDate(LocalDate date) {
        if (date.isBefore(creationDate)) {
            LOGGER.warn("Cannot mark completion before the habit's start date.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            LOGGER.warn("Cannot mark a future date as completed.");
            return;
        }

        // Add the date only if it is not already present
        if (completions.add(date)) {
            lastCompletedDate = date;
            calculateStreak();
            isCompleted = true;
            LOGGER.info("Habit marked as completed on {}", date);
        }
    }

    private void calculateStreak() {
        int currentStreak = 0;
        int longestStreak = 0;
        LocalDate lastDate = null;

        for (LocalDate date : completions.stream().sorted().toList()) {
            if (lastDate != null) {
                boolean isConsecutive = switch (frequency) {
                    case DAILY -> date.equals(lastDate.plusDays(1));
                    case WEEKLY -> date.equals(lastDate.plusWeeks(1));
                    case MONTHLY -> date.equals(lastDate.plusMonths(1));
                    default -> false;
                };

                if (isConsecutive) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
            } else {
                currentStreak = 1; // First completion starts a streak
            }

            longestStreak = Math.max(longestStreak, currentStreak);
            lastDate = date;
        }

        streakCounter = currentStreak; // Current streak
        bestStreak = Math.max(bestStreak, longestStreak); // Best streak
        LOGGER.info("Streak calculated. Current streak: {}, Best streak: {}", streakCounter, bestStreak);
    }




    public int getCompletionsOnDate(LocalDate date) {
        return completions.contains(date) ? 1 : 0;
    }

    public int getCompletionsInWeek(int week, YearMonth yearMonth) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return (int) completions.stream()
                .filter(date -> YearMonth.from(date).equals(yearMonth) &&
                        date.get(weekFields.weekOfMonth()) == week)
                .count();
    }

    public int getCompletionsInMonth(int year, int month) {
        return (int) completions.stream()
                .filter(date -> date.getYear() == year && date.getMonthValue() == month)
                .count();
    }

    public boolean isCompletedToday() {
        return lastCompletedDate != null && lastCompletedDate.equals(LocalDate.now());
    }

    public Set<LocalDate> getCompletedDates() {
        return new HashSet<>(completions);
    }

    public void addCompletionForTesting(LocalDate date) {
        completions.add(date);
    }

    // Getters and setters for other properties
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public boolean checkCompletion() {
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

    public void setStreakCounter(int streakCounter) {
        this.streakCounter = streakCounter;
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

    public boolean isMilestoneAchieved(int milestone) {
        return completedMilestones.contains(milestone);
    }

    public void addMilestone(int milestone) {
        completedMilestones.add(milestone);
    }

    public LocalDate getLastCompletedDate() {
        return lastCompletedDate;
    }

    public List<DayOfWeek> getCustomDays() {
        return customDays;
    }

    public void setCustomDays(List<DayOfWeek> customDays) {
        this.customDays = customDays;
    }

    public boolean isReminderEligible() {
        return reminderEligible;
    }

    public void setReminderEligible(boolean reminderEligible) {
        this.reminderEligible = reminderEligible;
    }

    public int getLatestMilestone() {
        return completedMilestones.stream()
                .max(Integer::compare)
                .orElse(0);
    }

    public int getBestStreak() {
        return bestStreak;
    }


    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }
}
