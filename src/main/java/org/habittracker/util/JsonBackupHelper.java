package org.habittracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.habittracker.model.Habit;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class JsonBackupHelper {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
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
}

