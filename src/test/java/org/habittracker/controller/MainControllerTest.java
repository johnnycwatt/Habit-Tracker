package org.habittracker.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.controller.MainController;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    void testDailyHabitDueToday() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(dailyHabit));

        LocalDate today = LocalDate.of(2024, 11, 8);
        assertTrue(mainController.isHabitDueToday(dailyHabit, today));
    }

    @Test
    void testWeeklyHabitDueTodayOnStartDate() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(weeklyHabit));

        LocalDate today = LocalDate.of(2024, 11, 8);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, today));
    }

    @Test
    void testWeeklyHabitDueAfterOneWeek() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(weeklyHabit));

        LocalDate oneWeekLater = LocalDate.of(2024, 11, 15);
        assertTrue(mainController.isHabitDueToday(weeklyHabit, oneWeekLater));
    }

    @Test
    void testMonthlyHabitDueNextMonth() {
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(monthlyHabit));

        LocalDate oneMonthLater = LocalDate.of(2024, 12, 8);
        assertTrue(mainController.isHabitDueToday(monthlyHabit, oneMonthLater));
    }

    @Test
    void testSetRemindersEnabled() {
        mainController.setRemindersEnabled(true);
        verify(notifier).showMessage("Reminders Enabled", "green");

        mainController.setRemindersEnabled(false);
        verify(notifier).showMessage("Reminders Disabled", "red");
    }



    @Test
    void testPopulateCalendarWithDueHabits() {
        Habit dueTodayHabit = new Habit("Due Today Habit", Habit.Frequency.DAILY);
        dueTodayHabit.setCreationDate(LocalDate.now());
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(dueTodayHabit));

        mainController.populateCalendar(LocalDate.now());
        assertEquals("November 2024", mainController.calendarMonthLabel.getText());
    }

    @Test
    void testShowMainView() {
        mainController.showMainView();

        assertTrue(mainController.mainView.isVisible(), "mainView should be visible");
        assertFalse(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be hidden");
    }

    @Test
    void testShowSettingsView() {
        mainController.showSettingsView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    void testShowAddHabitView() {
        mainController.showAddHabitView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    void testShowEditHabitView() {
        Habit habitToEdit = new Habit("Test Habit", Habit.Frequency.DAILY);
        mainController.showEditHabitView(habitToEdit);

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }

    @Test
    void testShowProgressView() {
        Habit habitToView = new Habit("Test Habit", Habit.Frequency.DAILY);
        mainController.showProgressView(habitToView);

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }


    @Test
    void testShowReportView() {
        mainController.showReportView();

        assertTrue(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be visible");
        assertFalse(mainController.mainView.isVisible(), "mainView should be hidden");
    }



    @Test
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
        Habit dailyHabit = new Habit("Daily Habit", Habit.Frequency.DAILY);
        dailyHabit.setCreationDate(LocalDate.of(2024, 11, 8));

        Habit weeklyHabit = new Habit("Weekly Habit", Habit.Frequency.WEEKLY);
        weeklyHabit.setCreationDate(LocalDate.of(2024, 11, 8));

        Habit monthlyHabit = new Habit("Monthly Habit", Habit.Frequency.MONTHLY);
        monthlyHabit.setCreationDate(LocalDate.of(2024, 11, 8));

        return Stream.of(
                Arguments.of(dailyHabit, LocalDate.of(2024, 11, 8), true),
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 15), true),
                Arguments.of(monthlyHabit, LocalDate.of(2024, 12, 8), true),
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 9), false) // not due
        );
    }

    @ParameterizedTest
    @MethodSource("reminderDueTestCases")
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

        return Stream.of(
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 15), true),
                Arguments.of(monthlyHabit, LocalDate.of(2024, 12, 8), true),
                Arguments.of(weeklyHabit, LocalDate.of(2024, 11, 14), false) // not due
        );
    }

    @Test
    void testStartReminderScheduler() {
        mainController.startReminderScheduler();
        assertFalse(mainController.scheduler.isShutdown(), "Scheduler should not be shut down immediately after start");
    }

    @Test
    void testShutdownScheduler() {
        mainController.startReminderScheduler();
        mainController.shutdownScheduler();
        assertTrue(mainController.scheduler.isShutdown(), "Scheduler should be shut down after calling shutdownScheduler");
    }


}
