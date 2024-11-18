package org.habittracker.controller;

import javafx.scene.control.*;
import javafx.application.Platform;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.service.HabitService;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.MockNotifier;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HabitListControllerIntegrationTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private HabitRepository habitRepository;
    private HabitService habitService;
    private HabitListController controller;
    private MockNotifier mockNotifier;

    @BeforeAll
    public static void initToolkit() {
        JavaFxInitializer.initToolkit();
        HabitRepository.initialize("habittracker-test");
        entityManagerFactory = Persistence.createEntityManagerFactory("habittracker-test");
    }
    @BeforeEach
    public void setUp() throws Exception {
        entityManager = entityManagerFactory.createEntityManager();
        habitRepository = HabitRepository.getInstance();
        mockNotifier = new MockNotifier();
        habitService = new HabitService(mockNotifier);

        controller = new HabitListController();
        controller.setMainApp(new Main());

        // Use reflection to set private fields
        setPrivateField(controller, "habitListView", new ListView<>());
        setPrivateField(controller, "notificationLabel", new Label());
        setPrivateField(controller, "editHabitNameField", new TextField());
        setPrivateField(controller, "editFrequencyChoiceBox", new ChoiceBox<>());
        setPrivateField(controller, "editStartDatePicker", new DatePicker());

        // Use reflection to invoke private initialize method
        invokePrivateMethod(controller, "initialize");
    }

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private Object getPrivateField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }


    private Object invokePrivateMethod(Object instance, String methodName) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(instance);
    }

    @AfterEach
    public void tearDown() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @AfterAll
    public static void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }



    //Tests


    @Test
    @Tag("JavaFX")
    public void testLoadHabitList() throws Exception {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habit.setCreationDate(LocalDate.now());

        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                // Use reflection to invoke private loadHabitList method
                invokePrivateMethod(controller, "loadHabitList");
                assertEquals(1, ((ListView<String>) getPrivateField(controller, "habitListView")).getItems().size());
                assertTrue(((ListView<String>) getPrivateField(controller, "habitListView")).getItems().get(0).contains("Exercise"));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking loadHabitList through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testOnEditHabitNoSelection() throws Exception {
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "onEditHabit");
                assertEquals("Please select a habit to edit.", mockNotifier.getMessages().get(0));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onEditHabit through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testOnDeleteHabit() throws Exception {
        Habit habit = new Habit("Meditate", Habit.Frequency.WEEKLY);

        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);  // Select the first (and only) habit

                invokePrivateMethod(controller, "onDeleteHabit");

                entityManager.getTransaction().begin();
                Habit deletedHabit = habitRepository.findHabitByName("Meditate");
                entityManager.getTransaction().commit();

                assertNull(deletedHabit);  // Verify the habit was deleted
                assertEquals(0, habitListView.getItems().size());  // Verify UI update
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onDeleteHabit through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testOnDeleteHabitNoSelection() throws Exception {
        Platform.runLater(() -> {
            try {
                // Invoke onDeleteHabit with no selection
                invokePrivateMethod(controller, "onDeleteHabit");

                // Verify message
                assertEquals("No habit selected for deletion.", mockNotifier.getMessages().get(0));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onDeleteHabit through reflection");
            }
        });
    }


    @Test
    @Tag("JavaFX")
    public void testOnMarkAsCompleted() throws Exception {
        Habit habit = new Habit("Read", Habit.Frequency.DAILY);
        habit.setCreationDate(LocalDate.now().minusDays(5));

        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);

                invokePrivateMethod(controller, "onMarkAsCompleted");

                entityManager.getTransaction().begin();
                Habit updatedHabit = habitRepository.findHabitByName("Read");
                entityManager.getTransaction().commit();

                assertNotNull(updatedHabit);
                assertTrue(updatedHabit.getStreakCounter() > 0);  // Check if streak is updated
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onMarkAsCompleted through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testOnMarkAsCompletedCustomHabitInsideDays() throws Exception {
        Habit habit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        habit.setCustomDays(List.of(LocalDate.now().getDayOfWeek())); // Set custom day to today

        entityManager.getTransaction().begin(); // Start a transaction
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit(); // Commit the transaction after adding the habit

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);

                invokePrivateMethod(controller, "onMarkAsCompleted");

                // Check that no warning message was shown
                assertTrue(mockNotifier.getMessages().isEmpty());  // Ensure no messages were added
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onMarkAsCompleted through reflection");
            }
        });
    }


    @Test
    @Tag("JavaFX")
    public void testOnViewProgressNoSelection() throws Exception {
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "onViewProgress");
                assertEquals("Please select a habit to view progress.", mockNotifier.getMessages().get(0));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onViewProgress through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testOnViewProgressWithSelection() throws Exception {
        // Mock Main and MainController
        Main mockMain = mock(Main.class);
        MainController mockMainController = mock(MainController.class);
        when(mockMain.getMainController()).thenReturn(mockMainController);
        controller.setMainApp(mockMain);

        // Add a habit to the list and select it
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);
                invokePrivateMethod(controller, "onViewProgress");

                // Verify showProgressView was called
                verify(mockMainController, times(1)).showProgressView(any(Habit.class));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onViewProgress through reflection");
            }
        });
    }


    @Test
    @Tag("JavaFX")
    public void testOnHabitSelected() throws Exception {
        // Add a habit to the list
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);

                //Verify selectedHabit
                invokePrivateMethod(controller, "onHabitSelected");
                Habit selectedHabit = (Habit) getPrivateField(controller, "selectedHabit");
                assertNotNull(selectedHabit);
                assertEquals("Exercise", selectedHabit.getName());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onHabitSelected through reflection");
            }
        });
    }



    @Test
    @Tag("JavaFX")
    public void testOnMarkAsCompletedCustomHabitOutsideDays() throws Exception {
        Habit habit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        habit.setCustomDays(List.of(LocalDate.now().minusDays(1).getDayOfWeek())); // Set custom day to yesterday
        entityManager.getTransaction().begin();
        habitRepository.addHabit(habit);
        entityManager.getTransaction().commit();

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "loadHabitList");

                // Select the habit in the list
                ListView<String> habitListView = (ListView<String>) getPrivateField(controller, "habitListView");
                habitListView.getSelectionModel().select(0);

                // Test mark as completed
                invokePrivateMethod(controller, "onMarkAsCompleted");
                assertEquals("Today is not part of your specified habit days. Marking completion may affect statistics. Good work on getting the habit done! That is the much more important!",
                        mockNotifier.getMessages().get(0));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception while invoking onMarkAsCompleted through reflection");
            }
        });
    }

    @Test
    @Tag("JavaFX")
    public void testGoBack() throws Exception {
        Main mockMain = mock(Main.class);
        MainController mockMainController = mock(MainController.class);
        when(mockMain.getMainController()).thenReturn(mockMainController);
        controller.setMainApp(mockMain);
        invokePrivateMethod(controller, "goBack");

        // Verify showMainView was called
        verify(mockMainController, times(1)).showMainView();
    }

    @Test
    @Tag("JavaFX")
    public void testShowReportView() throws Exception {
        Main mockMain = mock(Main.class);
        MainController mockMainController = mock(MainController.class);
        when(mockMain.getMainController()).thenReturn(mockMainController);
        controller.setMainApp(mockMain);
        invokePrivateMethod(controller, "showReportView");

        // Verify showReportView called
        verify(mockMainController, times(1)).showReportView();
    }


}