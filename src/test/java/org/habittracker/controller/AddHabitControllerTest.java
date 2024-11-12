package org.habittracker.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import org.habittracker.controller.MainController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.habittracker.Main;

import static org.junit.jupiter.api.Assertions.*;

public class AddHabitControllerTest {

    static {
        // Initialize JavaFX toolkit for testing
        Platform.startup(() -> {});
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
    private AddHabitController addHabitController;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Initialize AddHabitController with the mocked Notifier
        addHabitController = new AddHabitController(habitRepository,notifier);
        addHabitController.setMainApp(mainApp); // Set the mocked mainApp in AddHabitController

        when(mainApp.getMainController()).thenReturn(mainController);

        // Set other private fields using reflection
        setPrivateField(addHabitController, "habitNameField", new TextField());
        setPrivateField(addHabitController, "frequencyChoiceBox", new ChoiceBox<>());
        setPrivateField(addHabitController, "startDatePicker", new DatePicker());
        setPrivateField(addHabitController, "notificationLabel", new Label());
        setPrivateField(addHabitController, "colorChoiceBox", new ChoiceBox<>());
        setPrivateField(addHabitController, "mondayToggle", new ToggleButton());
        setPrivateField(addHabitController, "tuesdayToggle", new ToggleButton());
        setPrivateField(addHabitController, "wednesdayToggle", new ToggleButton());
        setPrivateField(addHabitController, "thursdayToggle", new ToggleButton());
        setPrivateField(addHabitController, "fridayToggle", new ToggleButton());
        setPrivateField(addHabitController, "saturdayToggle", new ToggleButton());
        setPrivateField(addHabitController, "sundayToggle", new ToggleButton());

        // Initialize ChoiceBox options
        ((ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox")).getItems().addAll("Daily", "Weekly", "Monthly", "Custom");
        ((ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox")).getItems().addAll("Black", "Red", "Green", "Blue");
    }



    // Helper methods to set and get private fields using reflection
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
    @Tag("JavaFX")
    void testAddHabitWithValidData() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        //Test data for habit creation
        habitNameField.setText("Test Habit");
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Black");

        //Avoid duplicate habit detection
        when(habitRepository.habitExistsByName("Test Habit")).thenReturn(false);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        // Verify repository and notifier interactions
        verify(habitRepository, times(1)).addHabit(any(Habit.class));
        verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
        verify(mainApp, times(1)).getMainController(); // Verify that mainApp interaction occurred
        verify(mainController, times(1)).updateHabitsDueToday(); // Verify that the updateHabitsDueToday was called
    }


    @Test
    @Tag("JavaFX")
    void testAddHabitWithEmptyName() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        //Test data for habit creation
        habitNameField.setText(""); // Empty name
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Black");

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        // Verify that the notifier shows the error message and addHabit is not called on repository
        verify(notifier, times(1)).showMessage("Habit name is required!", "red");
        verify(habitRepository, never()).addHabit(any(Habit.class));
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithNoFrequencySelected() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        //Test data with no frequency selected
        habitNameField.setText("Test Habit");
        startDatePicker.setValue(LocalDate.now());
        setPrivateField(addHabitController, "frequencyChoiceBox", new ChoiceBox<>()); // Empty choice box
        colorChoiceBox.setValue("Black");

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(notifier, times(1)).showMessage("Please select a frequency.", "red");
        verify(habitRepository, never()).addHabit(any(Habit.class));
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithExistingName() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        habitNameField.setText("Existing Habit");
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Blue");

        //Simulate an existing habit
        when(habitRepository.habitExistsByName("Existing Habit")).thenReturn(true);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(notifier, times(1)).showMessage("A habit with this name already exists. Please choose a different name.", "red");
        verify(habitRepository, never()).addHabit(any(Habit.class));
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithCustomFrequencyAndSelectedDays() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        // Set habit details
        habitNameField.setText("Custom Habit");
        frequencyChoiceBox.setValue("Custom");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Blue");

        // Set selected days
        ToggleButton mondayToggle = (ToggleButton) getPrivateField(addHabitController, "mondayToggle");
        ToggleButton wednesdayToggle = (ToggleButton) getPrivateField(addHabitController, "wednesdayToggle");
        mondayToggle.setSelected(true);
        wednesdayToggle.setSelected(true);

        when(habitRepository.habitExistsByName("Custom Habit")).thenReturn(false);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(habitRepository, times(1)).addHabit(argThat(habit -> {
            assertEquals("Custom Habit", habit.getName());
            assertTrue(habit.getCustomDays().containsAll(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithColorSelection() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        habitNameField.setText("Color Habit");
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Green");

        when(habitRepository.habitExistsByName("Color Habit")).thenReturn(false);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(habitRepository, times(1)).addHabit(argThat(habit -> {
            assertEquals("#008000", habit.getColor()); // Green
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithFarFutureStartDate() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        habitNameField.setText("Future Habit");
        frequencyChoiceBox.setValue("Daily");
        startDatePicker.setValue(LocalDate.now().plusYears(10)); // Far future start date
        colorChoiceBox.setValue("Red");

        when(habitRepository.habitExistsByName("Future Habit")).thenReturn(false);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(habitRepository, times(1)).addHabit(any(Habit.class));
        verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
    }

    @Test
    @Tag("JavaFX")
    void testAddHabitWithUndefinedColor() throws Exception {
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");

        habitNameField.setText("Undefined Color Habit");
        frequencyChoiceBox.setValue("Monthly");
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue("Purple"); //"Purple" not being defined in the color options

        when(habitRepository.habitExistsByName("Undefined Color Habit")).thenReturn(false);

        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        verify(habitRepository, times(1)).addHabit(argThat(habit -> {
            assertEquals("#000000", habit.getColor()); //Fallback color being black
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
    }

}

