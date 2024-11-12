package org.habittracker.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


public class EditHabitControllerTest {
    @BeforeAll
    static void initToolkit() {
        if (!Platform.isFxApplicationThread() && !Platform.isImplicitExit()) {
            Platform.startup(() -> {});
        }
    }





    @Mock
    private HabitRepository habitRepository;

    @Mock
    private MainController mainController;

    @Mock
    private Notifier notifier;

    @Mock
    private Main mainApp;

    @InjectMocks
    private EditHabitController editHabitController;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Initialize EditHabitController and set required fields
        editHabitController = new EditHabitController();
        editHabitController.setHabitRepository(habitRepository);
        editHabitController.setMainApp(mainApp);

        // Set up private fields for test accessibility
        setPrivateField(editHabitController, "editHabitNameField", new TextField());
        setPrivateField(editHabitController, "frequencyChoiceBox", new ChoiceBox<>());
        setPrivateField(editHabitController, "colorChoiceBox", new ChoiceBox<>());
        setPrivateField(editHabitController, "startDatePicker", new DatePicker());

        Label notificationLabel = new Label();
        setPrivateField(editHabitController, "notificationLabel", notificationLabel);

        editHabitController.initialize();
        setPrivateField(editHabitController, "customDaysContainer", new HBox());
        setPrivateField(editHabitController, "mondayToggle", new ToggleButton());
        setPrivateField(editHabitController, "tuesdayToggle", new ToggleButton());
        setPrivateField(editHabitController, "wednesdayToggle", new ToggleButton());
        setPrivateField(editHabitController, "thursdayToggle", new ToggleButton());
        setPrivateField(editHabitController, "fridayToggle", new ToggleButton());
        setPrivateField(editHabitController, "saturdayToggle", new ToggleButton());
        setPrivateField(editHabitController, "sundayToggle", new ToggleButton());
        setPrivateField(editHabitController, "notifier", notifier);

        when(mainApp.getMainController()).thenReturn(mainController);

        // Initialize ChoiceBox options
        ((ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox")).getItems().addAll("Daily", "Weekly", "Monthly", "Custom");
        ((ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox")).getItems().addAll("Black", "Red", "Green", "Blue");
    }




    // Helper methods for reflection
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    void testEditHabitWithValidData() throws Exception {
        Habit habit = new Habit("Initial Habit", Habit.Frequency.DAILY);
        habit.setCreationDate(LocalDate.now());
        habit.setColor("#000000"); // Black

        editHabitController.setHabit(habit);

        // Mock user inputs for habit updates
        TextField editHabitNameField = (TextField) getPrivateField(editHabitController, "editHabitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(editHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox");

        editHabitNameField.setText("Updated Habit");
        frequencyChoiceBox.setValue("Weekly");
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        colorChoiceBox.setValue("Red");

        //onSaveChanges
        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        // Verify repository interaction and updated habit data
        verify(habitRepository, times(1)).updateHabit(argThat(updatedHabit -> {
            assertEquals("Updated Habit", updatedHabit.getName());
            assertEquals(Habit.Frequency.WEEKLY, updatedHabit.getFrequency());
            assertEquals("#FF0000", updatedHabit.getColor()); // Red
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit updated successfully!", "green");
    }

    @Test
    void testEditHabitWithEmptyName() throws Exception {
        Habit habit = new Habit("Initial Habit", Habit.Frequency.DAILY);
        editHabitController.setHabit(habit);

        // Set empty name
        TextField editHabitNameField = (TextField) getPrivateField(editHabitController, "editHabitNameField");
        editHabitNameField.setText("");

        //onSaveChanges
        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        // Verify that error is shown and repository method is not called
        verify(notifier, times(1)).showMessage("Habit name is required!", "red");
        verify(habitRepository, never()).updateHabit(any(Habit.class));
    }


    @Test
    void testEditHabitWithCustomFrequencyAndSelectedDays() throws Exception {
        Habit habit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        editHabitController.setHabit(habit);

        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        frequencyChoiceBox.setValue("Custom");

        ToggleButton mondayToggle = (ToggleButton) getPrivateField(editHabitController, "mondayToggle");
        ToggleButton fridayToggle = (ToggleButton) getPrivateField(editHabitController, "fridayToggle");
        mondayToggle.setSelected(true);
        fridayToggle.setSelected(true);

        //onSaveChanges
        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        verify(habitRepository, times(1)).updateHabit(argThat(updatedHabit -> {
            List<DayOfWeek> expectedDays = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            assertEquals(expectedDays, updatedHabit.getCustomDays());
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit updated successfully!", "green");
    }

    @Test
    void testEditHabitWithCustomFrequencyNoDaysSelected() throws Exception {
        Habit habit = new Habit("Custom Habit", Habit.Frequency.CUSTOM);
        editHabitController.setHabit(habit);

        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        frequencyChoiceBox.setValue("Custom");

        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        verify(habitRepository, times(1)).updateHabit(argThat(updatedHabit -> {
            assertTrue(updatedHabit.getCustomDays().isEmpty());
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit updated successfully!", "green");
    }

    @Test
    void testEditHabitWithColorSelection() throws Exception {
        Habit habit = new Habit("Color Habit", Habit.Frequency.DAILY);
        habit.setColor("#008000"); // Green
        editHabitController.setHabit(habit);

        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox");
        colorChoiceBox.setValue("Blue");

        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        verify(habitRepository, times(1)).updateHabit(argThat(updatedHabit -> {
            assertEquals("#0000FF", updatedHabit.getColor()); // Blue
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit updated successfully!", "green");
    }


    @Test
    void testFrequencyChangeHidesCustomDaysContainer() throws Exception {
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        HBox customDaysContainer = (HBox) getPrivateField(editHabitController, "customDaysContainer");

        frequencyChoiceBox.setValue("Daily");
        Method onFrequencyChangedMethod = editHabitController.getClass().getDeclaredMethod("onFrequencyChanged");
        onFrequencyChangedMethod.setAccessible(true);
        onFrequencyChangedMethod.invoke(editHabitController);

        assertFalse(customDaysContainer.isVisible());
    }


}
