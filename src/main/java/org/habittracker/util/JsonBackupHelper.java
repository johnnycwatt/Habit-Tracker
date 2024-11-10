package org.habittracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class JsonBackupHelper {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static void backupHabitsToJson(List<Habit> habits, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(habits, writer);
            System.out.println("Backup saved successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restoreDataFromJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type habitListType = new TypeToken<List<Habit>>() {}.getType();
            List<Habit> habits = gson.fromJson(reader, habitListType);

            HabitRepository habitRepository = HabitRepository.getInstance();
            for (Habit habit : habits) {
                if (!habitRepository.habitExistsByName(habit.getName())) {
                    habitRepository.addHabit(habit);
                }
            }

            System.out.println("Data restored successfully from " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

