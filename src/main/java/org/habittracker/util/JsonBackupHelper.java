package org.habittracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class JsonBackupHelper {
    private static final Logger LOGGER = LogManager.getLogger(JsonBackupHelper.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static void backupHabitsToJson(List<Habit> habits, String filePath) {
        Path path = Path.of(filePath);
        try (var writer = Files.newBufferedWriter(path)) {
            GSON.toJson(habits, writer);
            LOGGER.info("Backup saved successfully to {}", filePath);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Backup encountered an issue", e);
            }

        }
    }

    public static void restoreDataFromJson(String filePath) {
        Path path = Path.of(filePath);
        try (var reader = Files.newBufferedReader(path)) {
            Type habitListType = new TypeToken<List<Habit>>() {}.getType();
            List<Habit> habits = GSON.fromJson(reader, habitListType);

            HabitRepository habitRepository = HabitRepository.getInstance();
            for (Habit habit : habits) {
                if (!habitRepository.habitExistsByName(habit.getName())) {
                    habitRepository.addHabit(habit);
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Restored data from " + filePath);
            }

        } catch (IOException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Failed to restore data from " + filePath);
            }
        }
    }
}
