/*
 * MIT License
 *
 * Copyright (c) 2024 Johnny Chadwick-Watt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.habittracker.service;

import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.habittracker.util.MilestoneManager;
import java.util.List;

public class HabitService {
    private final HabitRepository habitRepository;
    private final Notifier notifier;  // Using Notifier instead of NotificationHelper directly

    // Constructor that accepts a Notifier instead of Label or NotificationHelper
    public HabitService(Notifier notifier) {
        this.habitRepository = HabitRepository.getInstance(); // Assuming singleton
        this.notifier = notifier;
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
            notifier.showMessage("Habit already marked as completed for today.", "red");
            return;
        }

        habit.markAsCompleted();

        // Check for milestones and send only milestone-related notifications
        if (!checkMilestones(habit)) {
            notifier.showMessage(
                    "Habit marked as completed for today! Streak: " + habit.getStreakCounter(), "green"
            );
        }

        habitRepository.updateHabit(habit);
    }



    public void deleteHabit(Habit habit) {
        habitRepository.deleteHabit(habit);
        notifier.showMessage("Habit deleted successfully!", "green");
    }

    // Checks milestones and shows a milestone notification if reached
    public boolean checkMilestones(Habit habit) {
        int streak = habit.getStreakCounter();
        boolean milestoneReached = false;

        for (int milestone : MilestoneManager.MILESTONES) {
            if (streak == milestone && !habit.isMilestoneAchieved(milestone)) {
                habit.addMilestone(milestone); // Mark milestone as achieved
                milestoneReached = true;
                break; // Stop further checks once a milestone is achieved
            }
        }

        // If a milestone was reached, notify the user
        if (milestoneReached) {
            int lastMilestone = habit.getLatestMilestone(); // Assume this retrieves the most recent milestone
            String message = generateMilestoneMessage(lastMilestone);
            notifier.showMessage(message, "green");
        }

        return milestoneReached;
    }


    String generateMilestoneMessage(int milestone) {
        return switch (milestone) {
            case MilestoneManager.FIRST_DAY -> "Great start! Every journey begins with the first step. Keep it up!";
            case MilestoneManager.SEVEN_DAYS -> "One week down! Keep the momentum going!";
            case MilestoneManager.TWENTY_ONE_DAYS -> "Three weeks in! You're building a great habit!";
            case MilestoneManager.FIFTY_DAYS -> "50 days! That's some serious dedication!";
            case MilestoneManager.SIXTY_SIX_DAYS -> "66 days—this habit is becoming a part of your life!";
            case MilestoneManager.ONE_HUNDRED_DAYS -> "100 days! You've reached an incredible milestone!";
            default -> "Milestone reached! Keep up the good work!";
        };
    }

    // Getter for the notifier (if needed elsewhere)
    public Notifier getNotifier() {
        return notifier;
    }
}
