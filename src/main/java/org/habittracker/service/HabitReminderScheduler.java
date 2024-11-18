package org.habittracker.service;

import javafx.application.Platform;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HabitReminderScheduler {
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Notifier notifier;
    private final HabitRepository habitRepository;

    private boolean remindersEnabled = true;

    public HabitReminderScheduler(Notifier notifier, HabitRepository habitRepository) {
        this.notifier = notifier;
        this.habitRepository = habitRepository;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkUpcomingReminders, 0, 24, TimeUnit.HOURS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void setRemindersEnabled(boolean enabled) {
        this.remindersEnabled = enabled;
        String message = remindersEnabled ? "Reminders Enabled" : "Reminders Disabled";
        String color = remindersEnabled ? "green" : "red";
        notifier.showMessage(message, color);
    }

    void checkUpcomingReminders() {
        if (!remindersEnabled) {
            return;
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Habit> habits = habitRepository.getAllHabits();

        for (Habit habit : habits) {
            if (isReminderDue(habit, tomorrow)) {
                Platform.runLater(() -> notifier.showMessage(
                        "Reminder: '" + habit.getName() + "' is due tomorrow!", "blue"
                ));
            }
        }
    }

    boolean isReminderDue(Habit habit, LocalDate date) {
        LocalDate dueDate = habit.getLastCompletedDate() != null ? habit.getLastCompletedDate() : habit.getCreationDate();

        if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            return ChronoUnit.DAYS.between(dueDate, date) % 7 == 0 && !dueDate.isAfter(date);
        } else if (habit.getFrequency() == Habit.Frequency.MONTHLY) {
            LocalDate nextDueDate = dueDate.plusMonths(1).withDayOfMonth(
                    Math.min(dueDate.getDayOfMonth(), date.lengthOfMonth()));
            return nextDueDate.equals(date);
        }
        return false;
    }
}
