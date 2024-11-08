package org.habittracker.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.Locale;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_completed_dates", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "completed_date")
    private Set<LocalDate> completedDates = new HashSet<>();

    @Column(name = "reminder_eligible", nullable = false)
    private boolean reminderEligible = true;


    public Habit() {
    }

    public Habit(String name, Frequency frequency) {
        this.name = name;
        this.isCompleted = false;
        this.creationDate = LocalDate.now();
        this.frequency = frequency;
        this.color = "#000000";
        this.streakCounter = 0;
    }

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
            streakCounter = 1;
        }

        lastCompletedDate = today;
        isCompleted = true;
    }

    public void markAsCompletedOnDate(LocalDate date) {
        if (date.isBefore(creationDate)) {
            System.out.println("Cannot mark completion before the habit's start date.");
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            System.out.println("Cannot mark a future date as completed.");
            return;
        }

        completedDates.add(date);

        // Update streak only if the date is within range
        if (lastCompletedDate != null) {
            if (date.isAfter(lastCompletedDate)) {
                lastCompletedDate = date;
                calculateStreak();
            } else if (date.isBefore(lastCompletedDate)) {
                calculateStreak();
            }
        } else {
            lastCompletedDate = date;
            streakCounter = 1;
        }

        isCompleted = true;
    }


    private void calculateStreak() {
        int currentStreak = 0;
        int longestStreak = 0;
        LocalDate previousDate = null;

        for (LocalDate completionDate : completedDates.stream().sorted().toList()) {
            if (previousDate != null) {
                boolean isConsecutive = switch (frequency) {
                    case DAILY -> previousDate.plusDays(1).equals(completionDate);
                    case WEEKLY -> previousDate.plusWeeks(1).equals(completionDate);
                    case MONTHLY -> previousDate.plusMonths(1).equals(completionDate);
                    default -> false;
                };

                if (isConsecutive) {
                    currentStreak++;
                } else {
                    longestStreak = Math.max(longestStreak, currentStreak);
                    currentStreak = 1;
                }
            } else {
                currentStreak = 1;
            }
            previousDate = completionDate;
        }

        longestStreak = Math.max(longestStreak, currentStreak);
        streakCounter = longestStreak;
    }


    public int getCompletionsOnDate(LocalDate date) {
        return completedDates.contains(date) ? 1 : 0;
    }

    public int getCompletionsInWeek(int week, YearMonth yearMonth) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int completions = (int) completedDates.stream()
                .filter(date -> YearMonth.from(date).equals(yearMonth) &&
                        date.get(weekFields.weekOfMonth()) == week)
                .count();

        System.out.println("Checking completions for Week " + week + " in " + yearMonth + ": " + completions);
        return completions;
    }

    public int getCompletionsInMonth(int year, int month) {
        return (int) completedDates.stream()
                .filter(date -> date.getYear() == year && date.getMonthValue() == month)
                .count();
    }


    public boolean isCompletedToday() {
        return lastCompletedDate != null && lastCompletedDate.equals(LocalDate.now());
    }

    // Getter for completedDates (if not already present)
    public Set<LocalDate> getCompletedDates() {
        return completedDates;
    }

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

    public void setStreakCounter(int streak) {
        this.streakCounter = streak;
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



    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }
}