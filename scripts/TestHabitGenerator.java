import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TestHabitGenerator {

    public static void main(String[] args) throws IOException {
        String outputFolder = "generated-json";
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        List<Habit> habits = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Random random = new Random();

        String[] habitNames = {
                "Exercise", "Study", "Meditation", "Reading", "Yoga", "Cooking",
                "Walking", "Gardening", "Journaling", "Painting", "Photography",
                "Cleaning", "Volunteering", "Stretching", "Cycling", "Dancing",
                "Running", "Learning a Language", "Listening to Podcasts", "Writing"
        };

        String[] frequencies = {"DAILY", "WEEKLY", "MONTHLY", "CUSTOM"};
        String[] colors = {
                "#FF0000", "#008000", "#0000FF", "#FF00FF",
                "#CCCC00", "#FFA500", "#009999", "#000000"
        };

        for (int i = 0; i < habitNames.length; i++) {
            Habit habit = new Habit();
            habit.setId(i + 1);
            habit.setName(habitNames[i]);
            habit.setColor(colors[i % colors.length]);
            habit.setCreationDate(LocalDate.now().minusMonths(24).toString());
            habit.setFrequency(frequencies[i % frequencies.length]);
            habit.setReminderEligible(random.nextBoolean());

            // Generate completion dates over the last 24 months
            Set<LocalDate> completions = new HashSet<>();
            LocalDate startDate = LocalDate.now().minusMonths(24);
            LocalDate endDate = LocalDate.now();
            int completionCount = random.nextInt(300); // Random number of completions

            for (int j = 0; j < completionCount; j++) {
                LocalDate randomDate = startDate.plusDays(random.nextInt((int) (endDate.toEpochDay() - startDate.toEpochDay() + 1)));
                completions.add(randomDate);
            }

            habit.setCompletions(completions.stream().map(LocalDate::toString).collect(Collectors.toList()));

            // Calculate realistic streaks
            List<LocalDate> sortedCompletions = completions.stream().sorted().collect(Collectors.toList());
            habit.calculateStreaks(sortedCompletions, habit.getFrequency());

            habits.add(habit);
        }

        try (FileWriter writer = new FileWriter(outputFolder + "/TestHabits.json")) {
            gson.toJson(habits, writer);
        }

        System.out.println("20 habits generated successfully with completions spanning the last 24 months and saved to TestHabits.json");
    }

    static class Habit {
        private int id;
        private String name;
        private String color;
        private String creationDate;
        private String frequency;
        private int streakCounter;
        private boolean reminderEligible;
        private List<String> completions;
        private int bestStreak;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(String creationDate) {
            this.creationDate = creationDate;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public int getStreakCounter() {
            return streakCounter;
        }

        public void setStreakCounter(int streakCounter) {
            this.streakCounter = streakCounter;
        }

        public boolean isReminderEligible() {
            return reminderEligible;
        }

        public void setReminderEligible(boolean reminderEligible) {
            this.reminderEligible = reminderEligible;
        }

        public List<String> getCompletions() {
            return completions;
        }

        public void setCompletions(List<String> completions) {
            this.completions = completions;
        }

        public int getBestStreak() {
            return bestStreak;
        }

        public void setBestStreak(int bestStreak) {
            this.bestStreak = bestStreak;
        }

        public void calculateStreaks(List<LocalDate> sortedCompletions, String frequency) {
            int currentStreak = 0;
            int longestStreak = 0;
            LocalDate lastDate = null;

            for (LocalDate date : sortedCompletions) {
                boolean isConsecutive = lastDate != null && switch (frequency) {
                    case "DAILY" -> date.equals(lastDate.plusDays(1));
                    case "WEEKLY" -> date.equals(lastDate.plusWeeks(1));
                    case "MONTHLY" -> date.equals(lastDate.plusMonths(1));
                    default -> false;
                };

                if (isConsecutive) {
                    currentStreak++;
                } else {
                    currentStreak = 1; // Reset streak if not consecutive
                }

                longestStreak = Math.max(longestStreak, currentStreak);
                lastDate = date;
            }

            // Update streaks
            streakCounter = sortedCompletions.isEmpty() ||
                    !sortedCompletions.get(sortedCompletions.size() - 1).isEqual(LocalDate.now())
                    ? 0
                    : currentStreak;
            bestStreak = longestStreak;
        }
    }
}
