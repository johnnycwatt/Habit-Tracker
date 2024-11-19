import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HabitJsonGenerator {
    public static void main(String[] args) throws IOException {
        // Ensure the output folder exists
        String outputFolder = "generated-json";
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Generate JSON files with different habit counts
        generateHabitJson(50, outputFolder);
        generateHabitJson(100, outputFolder);
        generateHabitJson(250, outputFolder);
        generateHabitJson(1000, outputFolder);

        System.out.println("All JSON files generated successfully in the folder: " + outputFolder);
    }

    private static void generateHabitJson(int count, String outputFolder) throws IOException {
        List<Habit> habits = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Random random = new Random();

        String[] habitNames = {
                "Exercise", "Study", "Meditation", "Fasting", "Reading", "Yoga", "Work on Project",
                "Clean the House", "Write a Journal", "Learn a New Skill", "Go for a Walk",
                "Drink Water", "Cook a Meal", "Plan the Day", "Stretch", "Call a Friend",
                "Learn a Language", "Organize Desk", "Practice Coding", "Play an Instrument",
                "Gardening", "Go Hiking", "Listen to a Podcast", "Watch a Documentary",
                "Declutter Room", "Practice Gratitude", "Volunteer", "Draw or Paint",
                "Explore a New Place", "Practice Photography", "Try a New Recipe", "Take Deep Breaths"
        };

        String[] colors = {
                "#FF0000", "#008000", "#0000FF", "#FF00FF",
                "#CCCC00", "#FFA500", "#009999", "#000000"
        };
        String[] frequencies = {"DAILY", "WEEKLY", "MONTHLY", "CUSTOM"};

        for (int i = 1; i <= count; i++) {
            Habit habit = new Habit();
            habit.setId(i);
            habit.setName(habitNames[random.nextInt(habitNames.length)] + " " + i);
            habit.setColor(colors[random.nextInt(colors.length)]);
            habit.setCreationDate(LocalDate.now().minusDays(random.nextInt(30)).toString());
            habit.setFrequency(frequencies[random.nextInt(frequencies.length)]);
            habit.setStreakCounter(random.nextInt(50));
            habit.setReminderEligible(random.nextBoolean());

            // Add completions for DAILY/WEEKLY
            List<String> completions = new ArrayList<>();
            for (int j = 0; j < habit.getStreakCounter(); j++) {
                completions.add(LocalDate.now().minusDays(j).toString());
            }
            habit.setCompletions(completions);

            habit.setBestStreak(habit.getStreakCounter());
            habits.add(habit);
        }

        String fileName = outputFolder + "/Habits_" + count + ".json";
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(habits, writer);
        }

        System.out.println(count + " habits generated successfully and saved to " + fileName);
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

        // Getters and Setters for all fields
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getCreationDate() { return creationDate; }
        public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public int getStreakCounter() { return streakCounter; }
        public void setStreakCounter(int streakCounter) { this.streakCounter = streakCounter; }
        public boolean isReminderEligible() { return reminderEligible; }
        public void setReminderEligible(boolean reminderEligible) { this.reminderEligible = reminderEligible; }
        public List<String> getCompletions() { return completions; }
        public void setCompletions(List<String> completions) { this.completions = completions; }
        public int getBestStreak() { return bestStreak; }
        public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
    }
}
