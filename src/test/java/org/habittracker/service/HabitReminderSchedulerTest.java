package org.habittracker.service;

import javafx.application.Platform;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HabitReminderSchedulerTest {

    private HabitReminderScheduler scheduler;
    private Notifier mockNotifier;
    private HabitRepository mockHabitRepository;

    @BeforeEach
    void setUp() {
        JavaFxInitializer.initToolkit();
        mockNotifier = mock(Notifier.class);
        mockHabitRepository = mock(HabitRepository.class);
        scheduler = new HabitReminderScheduler(mockNotifier, mockHabitRepository);
    }

    @AfterEach
    void tearDown() {
        scheduler.stop(); // Ensure the scheduler is stopped after each test
    }

    @Test
    void testStartAndStopScheduler() {
        scheduler.start();
        assertFalse(scheduler.scheduler.isShutdown());
        scheduler.stop();
        assertTrue(scheduler.scheduler.isShutdown());
    }

    @Test
    void testSetRemindersEnabled() {
        scheduler.setRemindersEnabled(false);
        verify(mockNotifier).showMessage("Reminders Disabled", "red");

        scheduler.setRemindersEnabled(true);
        verify(mockNotifier).showMessage("Reminders Enabled", "green");
    }


    @Test
    void testCheckUpcomingReminders_NoHabits() {
        when(mockHabitRepository.getAllHabits()).thenReturn(Collections.emptyList());
        scheduler.start();

        scheduler.checkUpcomingReminders(); // Manually invoke the method
        verify(mockNotifier, never()).showMessage(anyString(), anyString());
    }



    @ParameterizedTest
    @CsvSource({
            "2024-11-17, WEEKLY, 2024-11-15, false",
            "2024-11-17, MONTHLY, 2024-10-17, true",
            "2024-11-17, MONTHLY, 2024-09-17, false",
            "2024-11-17, DAILY, 2024-11-16, false"
    })
    void testIsReminderDue(String currentDate, String frequency, String dueDate, boolean expected) {
        Habit.Frequency habitFrequency = Habit.Frequency.valueOf(frequency);
        Habit habit = new Habit("Test Habit", habitFrequency);
        habit.setLastCompletedDate(LocalDate.parse(dueDate));

        boolean result = scheduler.isReminderDue(habit, LocalDate.parse(currentDate));
        assertEquals(expected, result);
    }

    @Test
    void testIsReminderDue_UnsupportedFrequency() {
        Habit habit = new Habit("Test Habit", Habit.Frequency.DAILY); // Not supported in isReminderDue
        boolean result = scheduler.isReminderDue(habit, LocalDate.now());
        assertFalse(result); // Should return false
    }
}
