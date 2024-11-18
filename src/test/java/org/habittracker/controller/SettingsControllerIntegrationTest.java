package org.habittracker.controller;

import javafx.scene.control.Label;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.BackupScheduler;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingsControllerIntegrationTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private HabitRepository habitRepository;
    private SettingsController settingsController;
    private Label notificationLabel;

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void setUpEntityManagerFactory() {
        JavaFxInitializer.initToolkit();
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }

    @AfterAll
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        habitRepository = HabitRepository.getInstance();


        notificationLabel = new Label();
        Notifier notifier = new NotificationHelper(notificationLabel);
        settingsController = new SettingsController();
        settingsController.notifier = notifier;

        MainController mainController = new MainController();
        //mainController.habitRepository = habitRepository;
        settingsController.setMainController(mainController);
    }

    @AfterEach
    void tearDown() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @Test
    void testBackupDataToJson() throws IOException {
        // Prepare some sample habits and a temp file
        Habit habit1 = new Habit("Test Habit 1", Habit.Frequency.DAILY);
        Habit habit2 = new Habit("Test Habit 2", Habit.Frequency.WEEKLY);

        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit1);
        habitRepository.addHabit(habit2);
        entityManager.getTransaction().commit();

        File backupFile = new File(tempDir.toFile(), "backup.json");

        // Perform the backup
        JsonBackupHelper.backupHabitsToJson(habitRepository.getAllHabits(), backupFile.getAbsolutePath());

        // Verify the backup file was created
        assertTrue(backupFile.exists(), "Backup file should be created.");
        assertTrue(Files.size(backupFile.toPath()) > 0, "Backup file should not be empty.");
    }

    @Test
    @Tag("Integration")
    void testRestoreDataFromJson() throws IOException {
        // Create a temp file with sample habit data for restoration
        File backupFile = new File(tempDir.toFile(), "backup.json");
        Habit habit1 = new Habit("Restored Habit 1", Habit.Frequency.DAILY);
        Habit habit2 = new Habit("Restored Habit 2", Habit.Frequency.MONTHLY);
        List<Habit> habits = Arrays.asList(habit1, habit2);
        JsonBackupHelper.backupHabitsToJson(habits, backupFile.getAbsolutePath());

        // Clear existing habits before restoring
        entityManager.getTransaction().begin();
        habitRepository.getAllHabits().forEach(habitRepository::deleteHabit);
        entityManager.getTransaction().commit();

        // Perform the restore
        JsonBackupHelper.restoreDataFromJson(backupFile.getAbsolutePath());

        // Re-instantiate HabitRepository to ensure it fetches the latest state from the database
        habitRepository = HabitRepository.getInstance();
        List<Habit> restoredHabits = habitRepository.getAllHabits();

        // Verify that the habits were restored correctly
        assertTrue(restoredHabits.stream().anyMatch(habit -> "Restored Habit 1".equals(habit.getName())), "Habit 1 should be restored.");
        assertTrue(restoredHabits.stream().anyMatch(habit -> "Restored Habit 2".equals(habit.getName())), "Habit 2 should be restored.");
    }

    @Test
    void testEnableAutoBackup() {
        BackupScheduler.enableAutoBackup(habitRepository.getAllHabits());
        assertTrue(BackupScheduler.isAutoBackupEnabled(), "Auto backup should be enabled.");
    }

    @Test
    void testDisableAutoBackup() {
        // Enable auto-backup first
        BackupScheduler.enableAutoBackup(habitRepository.getAllHabits());

        // Disable auto-backup
        BackupScheduler.disableAutoBackup();

        // Verify auto-backup is disabled
        assertFalse(BackupScheduler.isAutoBackupEnabled(), "Auto backup should be disabled.");
    }
}
