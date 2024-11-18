package org.habittracker.util;

import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.model.Habit;

public class BackupScheduler {
    private static final Logger LOGGER = LogManager.getLogger(BackupScheduler.class);

    private static Timer timer;
    private static boolean autoBackupStatus;

    public static void startAutoBackup(List<Habit> habits) {
        if (autoBackupStatus) {
            LocalDate nextBackupDate = LocalDate.now().plus(1, ChronoUnit.MONTHS);
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        // Generate file path with date
                        String filePath = "backup_" + nextBackupDate + ".json";
                        JsonBackupHelper.backupHabitsToJson(habits, filePath);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Monthly backup created: " + filePath);
                        }

                    });
                }
            }, nextBackupDate.toEpochDay(), ChronoUnit.MONTHS.getDuration().toMillis());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Auto-backup timer scheduled to run every month starting from " + nextBackupDate);
            }
        } else {
            LOGGER.warn("Auto-backup is disabled. Unable to start backup scheduling.");
        }
    }

    public static void enableAutoBackup(List<Habit> habits) {
        autoBackupStatus = true;
        LOGGER.info("Auto-backup enabled.");
        startAutoBackup(habits);
    }

    public static void disableAutoBackup() {
        autoBackupStatus = false;
        if (timer != null) {
            timer.cancel();
            LOGGER.info("Auto-backup disabled and timer cancelled.");
        } else {
            LOGGER.warn("Auto-backup was already disabled; no timer to cancel.");
        }
    }

    public static boolean isAutoBackupEnabled() {
        return autoBackupStatus;
    }
}
