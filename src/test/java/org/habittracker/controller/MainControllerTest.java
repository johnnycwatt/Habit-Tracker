package org.habittracker.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.service.HabitReminderScheduler;
import org.habittracker.util.HabitCalendarPopulator;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainControllerTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private Notifier notifier;

    @Mock
    private HabitReminderScheduler reminderScheduler;

    @Mock
    private HabitCalendarPopulator calendarPopulator;

    @InjectMocks
    private MainController mainController;

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeAll
    public static void initToolkit() {
        JavaFxInitializer.initToolkit();
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }

    @BeforeEach
    void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        // Initialize Mockito
        MockitoAnnotations.openMocks(this);

        // Initialize MainController with mocks
        mainController = new MainController();
        mainController.setNotifier(notifier);
        mainController.setReminderScheduler(reminderScheduler);
        mainController.setCalendarPopulator(calendarPopulator);

        // Initialize FXML components manually
        mainController.rootStackPane = new StackPane();
        mainController.mainView = new VBox();
        mainController.dynamicViewContainer = new VBox();
        mainController.calendarGrid = new GridPane();
        mainController.calendarMonthLabel = new Label();
        mainController.habitsDueTodayList = new ListView<>();

        // Add a mock scene for dark mode testing
        Scene scene = new Scene(mainController.rootStackPane);
        mainController.rootStackPane.getScene();

    }

    @Test
    void testShowMainView() {
        mainController.showMainView();

        verify(calendarPopulator).populateCalendar(LocalDate.now(), false); // Dark mode off by default
        assertTrue(mainController.mainView.isVisible(), "mainView should be visible");
        assertFalse(mainController.dynamicViewContainer.isVisible(), "dynamicViewContainer should be hidden");
    }


    @Test
    void testEnableDarkMode() {
        mainController.enableDarkMode();

        assertTrue(mainController.isDarkModeEnabled(), "Dark mode should be enabled");
        assertTrue(mainController.rootStackPane.getScene().getStylesheets()
                        .contains(getClass().getResource("/css/dark-theme.css").toExternalForm()),
                "Dark theme stylesheet should be applied");
    }

    @Test
    void testDisableDarkMode() {
        mainController.enableDarkMode(); // Enable first
        mainController.disableDarkMode(); // Then disable

        assertFalse(mainController.isDarkModeEnabled(), "Dark mode should be disabled");
        assertFalse(mainController.rootStackPane.getScene().getStylesheets()
                        .contains(getClass().getResource("/css/dark-theme.css").toExternalForm()),
                "Dark theme stylesheet should be removed");
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
}
