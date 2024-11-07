package org.habittracker.service;

import javafx.scene.control.Label;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Milestones;
import java.util.List;

public class HabitService {
    private final HabitRepository habitRepository;
    private final NotificationHelper notificationHelper;

    public HabitService(Label notificationLabel) {
        this.habitRepository = HabitRepository.getInstance(); // Assuming singleton
        this.notificationHelper = new NotificationHelper(notificationLabel);
    }

    // Retrieve all habits from the repository
    public List<Habit> getAllHabits() {
        return habitRepository.getAllHabits();
    }

    // Find a habit by its name
    public Habit findHabitByName(String name) {
        return habitRepository.findHabitByName(name);
    }

    public void markHabitAsCompleted(Habit habit) {
        if (habit.isCompletedToday()) {
            notificationHelper.showTemporaryMessage("Habit already marked as completed for today.", "red");
            return;
        }

        habit.markAsCompleted();

        // Check for milestones
        boolean milestoneReached = checkMilestones(habit);

        // If no milestone was reached, show the regular completion message
        if (!milestoneReached) {
            notificationHelper.showTemporaryMessage(
                    "Habit marked as completed for today! Streak: " + habit.getStreakCounter(), "green"
            );
        }

        habitRepository.updateHabit(habit);
    }

    public void deleteHabit(Habit habit) {
        habitRepository.deleteHabit(habit);
        notificationHelper.showTemporaryMessage("Habit deleted successfully!", "green");
    }

    // Checks milestones and shows a milestone notification if reached
    public boolean checkMilestones(Habit habit) {
        int streak = habit.getStreakCounter();
        boolean milestoneReached = false;

        for (int milestone : Milestones.MILESTONES) {
            if (streak == milestone && !habit.isMilestoneAchieved(milestone)) {
                String message = generateMilestoneMessage(milestone);
                notificationHelper.showMilestoneNotification(message, "green");
                habit.addMilestone(milestone); // Mark milestone as achieved
                milestoneReached = true;
                break;
            }
        }
        return milestoneReached;
    }

    private String generateMilestoneMessage(int milestone) {
        return switch (milestone) {
            case Milestones.FIRST_DAY -> "Great start! Every journey begins with the first step. Keep it up!";
            case Milestones.SEVEN_DAYS -> "One week down! Keep the momentum going!";
            case Milestones.TWENTY_ONE_DAYS -> "Three weeks in! You're building a great habit!";
            case Milestones.FIFTY_DAYS -> "50 days! That's some serious dedication!";
            case Milestones.SIXTY_SIX_DAYS -> "66 days—this habit is becoming a part of your life!";
            case Milestones.ONE_HUNDRED_DAYS -> "100 days! You've reached an incredible milestone!";
            default -> "Milestone reached! Keep up the good work!";
        };
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }
}


