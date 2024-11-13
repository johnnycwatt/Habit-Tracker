package org.habittracker.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainControllerTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private MainController mainController;

    private Habit dailyHabit;
    private Habit weeklyHabit;
    private Habit monthlyHabit;

    @BeforeEach
    void setUp() {
        // Initialize Mockito
        MockitoAnnotations.openMocks(this);

        // Initialize MainController with mocks
        mainController = new MainController();
        mainController.habitRepository = habitRepository;
        mainController.notifier = notifier;

        // Manually initialize FXML components
        mainController.rootStackPane = new StackPane();
        mainController.mainView = new VBox();
        mainController.dynamicViewContainer = new VBox();
        mainController.calendarGrid = new GridPane();
        mainController.calendarMonthLabel = new Label();
        mainController.habitsDueTodayList = new ListView<>();

        // Add a mock scene to test dark mode methods
        mainController.rootStackPane = new StackPane();
        Scene scene = new Scene(mainController.rootStackPane);

        // Initialize test habits
        dailyHabit = new Habit("Daily Habit", Habit.Frequency.DAILY);
        weeklyHabit = new Habit("Weekly Habit", Habit.Frequency.WEEKLY);
        monthlyHabit = new Habit("Monthly Habit", Habit.Frequency.MONTHLY);

        // Set creation dates
        LocalDate startDate = LocalDate.of(2024, 11, 8);
        dailyHabit.setCreationDate(startDate);
        weeklyHabit.setCreationDate(startDate);
        monthlyHabit.setCreationDate(startDate);
    }

    @Test
    @Tag("JavaFX")
    void testDailyHabitDueToday() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(dailyHabit));

        LocalDate today = LocalDate.of(2024, 11, 8);
        assertTrue(mainController.isHabitDueToday(dailyHabit, today));
    }

    @Test
    @Tag("JavaFX")
    void testWeeklyHabitDueTodayOnStartDate() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(weeklyHabit));

        LocalDate today = LocalDate.of(2024, 11, 8);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, today));
    }

    @Test
    @Tag("JavaFX")
    void testWeeklyHabitDueAfterOneWeek() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(weeklyHabit));

        LocalDate oneWeekLater = LocalDate.of(2024, 11, 15);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, oneWeekLater));
    }

    @Test
    @Tag("JavaFX")
    void testMonthlyHabitDueNextMonth() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(monthlyHabit));

        LocalDate oneMonthLater = LocalDate.of(2024, 12, 8);
        assertTrue(mainController.isHabitDueToday(monthlyHabit, oneMonthLater));
    }

    @Test
    @Tag("JavaFX")
    void testSetRemindersEnabled() {
        mainController.setRemindersEnabled(true);
        verify(notifier).showMessage("Reminders Enabled", "green");

        mainController.setRemindersEnabled(false);
        verify(notifier).showMessage("Reminders Disabled", "red");
    }

    @Test
    @Tag("JavaFX")
    void testCheckUpcomingRemindersWhenRemindersDisabled() {
        mainController.setRemindersEnabled(false);
        verify(notifier, times(1)).showMessage("Reminders Disabled", "red");

        mainController.triggerCheckUpcomingReminders();
        verifyNoMoreInteractions(notifier);
    }

    @Test
    @Tag("JavaFX")
    void testEnableDisableDarkMode() {
        mainController.rootStackPane = new StackPane();
        Scene scene = new Scene(mainController.rootStackPane);

        mainController.enableDarkMode();
        assertTrue(mainController.rootStackPane.getScene().getStylesheets().contains(getClass().getResource("/css/dark-theme.css").toExternalForm()));

        mainController.disableDarkMode();
        assertFalse(mainController.rootStackPane.getScene().getStylesheets().contains(getClass().getResource("/css/dark-theme.css").toExternalForm()));
    }





    @Test
    @Tag("JavaFX")
    void testPopulateCalendarWithDueHabits() {
        Habit dueTodayHabit = new Habit("Due Today Habit", Habit.Frequency.DAILY);
        dueTodayHabit.setCreationDate(LocalDate.now());
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(dueTodayHabit));

        mainController.populateCalendar(LocalDate.now());
        assertEquals("November 2024", mainController.calendarMonthLabel.getText());
    }

    @Test
    @Tag("JavaFX")
    void testPopulateCalendarForDifferentMonthConfigurations() {
        mainController.populateCalendar(LocalDate.of(2024, 2, 1)); // Test for February (not leap year)
        assertEquals("February 2024", mainController.calendarMonthLabel.getText());

        mainController.populateCalendar(LocalDate.of(2024, 8, 1)); // Test for August (31 days)
        assertEquals("August 2024", mainController.calendarMonthLabel.getText());
    }


    @Test
    void testOpenSettings() {
        mainController.openSettings();
        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible after opening settings");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden after opening settings");
    }


    @Test
    @Tag("JavaFX")
    void testShowMainView() {
        mainController.showMainView();

        assertTrue(mainController.mainView.isVisible(), "mainView should be visible");
        assertFalse(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be hidden");
    }

    @Test
    @Tag("JavaFX")
    void testShowSettingsView() {
        mainController.showSettingsView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    @Tag("JavaFX")
    void testShowAddHabitView() {
        mainController.showAddHabitView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    @Tag("JavaFX")
    void testShowEditHabitView() {
        Habit habitToEdit = new Habit("Test Habit", Habit.Frequency.DAILY);
        mainController.showEditHabitView(habitToEdit);

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    @Tag("JavaFX")
    void testShowProgressView() {
        Habit habitToView = new Habit("Test Habit", Habit.Frequency.DAILY);
        mainController.showProgressView(habitToView);

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }





    @Test
    @Tag("JavaFX")
    void testShowReportView() {
        mainController.showReportView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }



    @Test
    @Tag("JavaFX")
    void testUpdateHabitsDueToday() {
        Habit dueTodayHabit = new Habit("Habit Due Today", Habit.Frequency.DAILY);
        dueTodayHabit.setCreationDate(LocalDate.now());

        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(dueTodayHabit));

        mainController.updateHabitsDueToday();

        assertEquals(1, mainController.habitsDueTodayList.getItems().size(), "There should be one habit due today");
        assertEquals("Habit Due Today", mainController.habitsDueTodayList.getItems().get(0).getName());
    }

    @ParameterizedTest
    @MethodSource("habitDueTodayTestCases")
    void testIsHabitDueToday(Habit habit, LocalDate today, boolean expected) {
        assertEquals(expected, mainController.isHabitDueToday(habit, today));
    }

    private static Stream<Arguments> habitDueTodayTestCases() {
        LocalDate creationDate = LocalDate.of(2024, 11, 8);

        // DAILY frequency habit
        Habit dailyHabit = new Habit("Daily Habit", Habit.Frequency.DAILY);
        dailyHabit.setCreationDate(creationDate);

        // WEEKLY frequency habits
        Habit weeklyHabitDue = new Habit("Weekly Habit Due", Habit.Frequency.WEEKLY);
        weeklyHabitDue.setCreationDate(creationDate);

        Habit weeklyHabitNotDue = new Habit("Weekly Habit Not Due", Habit.Frequency.WEEKLY);
        weeklyHabitNotDue.setCreationDate(creationDate);

        // MONTHLY frequency habits
        Habit monthlyHabitDue = new Habit("Monthly Habit Due", Habit.Frequency.MONTHLY);
        monthlyHabitDue.setCreationDate(creationDate);

        Habit monthlyHabitNotDue = new Habit("Monthly Habit Not Due", Habit.Frequency.MONTHLY);
        monthlyHabitNotDue.setCreationDate(creationDate.withDayOfMonth(5)); // Different start day to make it not due

        // CUSTOM frequency habits
        Habit customHabitDue = new Habit("Custom Habit Due", Habit.Frequency.CUSTOM);
        customHabitDue.setCreationDate(creationDate);
        customHabitDue.setCustomDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        Habit customHabitNotDue = new Habit("Custom Habit Not Due", Habit.Frequency.CUSTOM);
        customHabitNotDue.setCreationDate(creationDate);
        customHabitNotDue.setCustomDays(Collections.singletonList(DayOfWeek.FRIDAY));

        return Stream.of(
                // DAILY frequency should always return true if creation date is not after today
                Arguments.of(dailyHabit, LocalDate.of(2024, 11, 8), true),
                Arguments.of(dailyHabit, LocalDate.of(2024, 11, 9), true),
                Arguments.of(dailyHabit, LocalDate.of(2024, 11, 10), true),

                // WEEKLY frequency
                Arguments.of(weeklyHabitDue, creationDate, true), // Same day as creation
                Arguments.of(weeklyHabitDue, creationDate.plusDays(7), true), // One week later
                Arguments.of(weeklyHabitDue, creationDate.plusDays(14), true), // Two weeks later
                Arguments.of(weeklyHabitNotDue, creationDate.plusDays(6), false), // Not a multiple of 7 days
                Arguments.of(weeklyHabitNotDue, creationDate.plusDays(13), false), // Not due since it's not 7 days

                // MONTHLY frequency
                Arguments.of(monthlyHabitDue, creationDate.plusMonths(1), true), // One month later
                Arguments.of(monthlyHabitDue, creationDate.plusMonths(2), true), // Two months later
                Arguments.of(monthlyHabitNotDue, creationDate.plusMonths(1), false), // Not due since different day of month
                Arguments.of(monthlyHabitDue, LocalDate.of(2024, 12, 31), false), // Edge case: Different day but same month

                // CUSTOM frequency with specific days (Monday, Wednesday)
                Arguments.of(customHabitDue, LocalDate.of(2024, 11, 11), true), // Monday (due)
                Arguments.of(customHabitDue, LocalDate.of(2024, 11, 13), true), // Wednesday (due)
                Arguments.of(customHabitDue, LocalDate.of(2024, 11, 12), false), // Tuesday (not due)
                Arguments.of(customHabitDue, LocalDate.of(2024, 11, 14), false), // Thursday (not due)

                // CUSTOM frequency with a single day (Friday)
                //Arguments.of(customHabitNotDue, LocalDate.of(2024, 11, 8), false), // Not Friday
                Arguments.of(customHabitNotDue, LocalDate.of(2024, 11, 15), true), // Friday (due)
                Arguments.of(customHabitNotDue, LocalDate.of(2024, 11, 22), true), // Next Friday (due)

                // Leap year case for monthly habit on February 29th
                Arguments.of(monthlyHabitDue, LocalDate.of(2024, 2, 29), false), // Monthly habit not due
                Arguments.of(monthlyHabitDue, LocalDate.of(2024, 3, 29), false), // One month later, but day mismatch

                // Habit with creation date after the "today" date, should return false
                Arguments.of(dailyHabit, creationDate.minusDays(1), false),
                Arguments.of(weeklyHabitDue, creationDate.minusDays(1), false),
                Arguments.of(monthlyHabitDue, creationDate.minusDays(1), false)
        );
    }


    @Test
    @Tag("JavaFX")
    void testCustomHabitDueOnSelectedDays() {
        Habit customHabit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        customHabit.setCreationDate(LocalDate.of(2024, 11, 8));
        customHabit.setCustomDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 11))); // Monday
        assertFalse(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 12))); // Tuesday
        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 13))); // Wednesday
        assertFalse(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 14))); // Thursday
        assertTrue(mainController.isHabitDueToday(customHabit, LocalDate.of(2024, 11, 15))); // Friday
    }



    @ParameterizedTest
    @MethodSource("reminderDueTestCases")
    @Tag("JavaFX")
    void testIsReminderDue(Habit habit, LocalDate date, boolean expected) {
        assertEquals(expected, mainController.isReminderDue(habit, date));
    }

    private static Stream<Arguments> reminderDueTestCases() {
        Habit weeklyHabit = new Habit("Weekly Habit", Habit.Frequency.WEEKLY);
        weeklyHabit.setCreationDate(LocalDate.of(2024, 11, 8));
        weeklyHabit.setLastCompletedDate(LocalDate.of(2024, 11, 8));

        Habit monthlyHabit = new Habit("Monthly Habit", Habit.Frequency.MONTHLY);
        monthlyHabit.setCreationDate(LocalDate.of(2024, 11, 8));
        monthlyHabit.setLastCompletedDate(LocalDate.of(2024, 11, 8));

        Habit weeklyHabitWithDifferentLastCompleted = new Habit("Weekly Habit Different Last Completed", Habit.Frequency.WEEKLY);
        weeklyHabitWithDifferentLastCompleted.setCreationDate(LocalDate.of(2024, 11, 1));
        weeklyHabitWithDifferentLastCompleted.setLastCompletedDate(LocalDate.of(2024, 11, 8));

        Habit monthlyHabitNoLastCompleted = new Habit("Monthly Habit No Last Completed", Habit.Frequency.MONTHLY);
        monthlyHabitNoLastCompleted.setCreationDate(LocalDate.of(2024, 11, 8));

        return Stream.of(
                // Weekly frequency tests
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 15), true), // 7 days after creation
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 16), false), // 8 days after creation
                Arguments.of(weeklyHabitWithDifferentLastCompleted, LocalDate.of(2024, 11, 15), true), // 7 days after last completed
                Arguments.of(weeklyHabitWithDifferentLastCompleted, LocalDate.of(2024, 11, 22), true), // 14 days after last completed
                Arguments.of(weeklyHabitWithDifferentLastCompleted, LocalDate.of(2024, 11, 20), false), // Not a multiple of 7

                // Monthly frequency tests
                Arguments.of(monthlyHabit, LocalDate.of(2024, 12, 8), true), // One month later
                //Arguments.of(monthlyHabit, LocalDate.of(2025, 1, 8), true), // Two months later
                Arguments.of(monthlyHabit, LocalDate.of(2025, 2, 7), false), // One day before two months later
                Arguments.of(monthlyHabitNoLastCompleted, LocalDate.of(2024, 12, 8), true), // One month after creation
                Arguments.of(monthlyHabitNoLastCompleted, LocalDate.of(2024, 11, 7), false) // Before creation
        );
    }


    @Test
    @Tag("JavaFX")
    void testStartReminderScheduler() {
        mainController.startReminderScheduler();
        assertFalse(mainController.scheduler.isShutdown(), "Scheduler should not be shut down immediately after start");
    }

    @Test
    @Tag("JavaFX")
    void testShutdownScheduler() {
        mainController.startReminderScheduler();
        mainController.shutdownScheduler();
        assertTrue(mainController.scheduler.isShutdown(), "Scheduler should be shut down after calling shutdownScheduler");
    }
}
