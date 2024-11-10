package org.habittracker.util;

import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.habittracker.model.Habit;

public class BackupScheduler {
    private static Timer timer;
    private static boolean isAutoBackupEnabled = false;

    public static void startAutoBackup(List<Habit> habits) {
        if (isAutoBackupEnabled) {
            LocalDate nextBackupDate = LocalDate.now().plus(1, ChronoUnit.MONTHS);
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        // Generate file path with date
                        String filePath = "backup_" + nextBackupDate + ".json";
                        JsonBackupHelper.backupHabitsToJson(habits, filePath);
                        System.out.println("Monthly backup created: " + filePath);
                    });
                }
            }, nextBackupDate.toEpochDay(), ChronoUnit.MONTHS.getDuration().toMillis());
        }
    }

    public static void enableAutoBackup(List<Habit> habits) {
        isAutoBackupEnabled = true;
        startAutoBackup(habits);
    }

    public static void disableAutoBackup() {
        isAutoBackupEnabled = false;
        if (timer != null) {
            timer.cancel();
            System.out.println("Auto backup disabled.");
        }
    }

    public static boolean isAutoBackupEnabled() {
        return isAutoBackupEnabled;
    }
}
