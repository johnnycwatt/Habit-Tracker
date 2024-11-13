package org.habittracker.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


public class EditHabitControllerTest {
    @BeforeAll
    public static void initToolkit() {
        JavaFxInitializer.initToolkit();
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

    @ParameterizedTest
    @CsvSource({
            "Daily, #000000, Black",
            "Weekly, #FF0000, Red",
            "Monthly, #008000, Green",
            "Custom, #0000FF, Blue"
    })
    @Tag("JavaFX")
    void testEditHabitWithValidData(String frequency, String colorHex, String colorName) throws Exception {
        Habit habit = new Habit("Initial Habit", Habit.Frequency.valueOf(frequency.toUpperCase()));
        habit.setCreationDate(LocalDate.now());
        habit.setColor(colorHex);

        editHabitController.setHabit(habit);

        // Mock user inputs for habit updates
        TextField editHabitNameField = (TextField) getPrivateField(editHabitController, "editHabitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(editHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox");

        // Update values based on parameters
        editHabitNameField.setText("Updated Habit");
        frequencyChoiceBox.setValue(frequency);
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        colorChoiceBox.setValue(colorName);

        //onSaveChanges
        Method onSaveChangesMethod = editHabitController.getClass().getDeclaredMethod("onSaveChanges", ActionEvent.class);
        onSaveChangesMethod.setAccessible(true);
        onSaveChangesMethod.invoke(editHabitController, new ActionEvent());

        // Verify repository interaction and updated habit data
        verify(habitRepository, times(1)).updateHabit(argThat(updatedHabit -> {
            assertEquals("Updated Habit", updatedHabit.getName());
            assertEquals(Habit.Frequency.valueOf(frequency.toUpperCase()), updatedHabit.getFrequency());
            assertEquals(colorHex, updatedHabit.getColor());
            return true;
        }));
        verify(notifier, times(1)).showMessage("Habit updated successfully!", "green");
    }

    @Test
    @Tag("JavaFX")
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
    @Tag("JavaFX")
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
    @Tag("JavaFX")
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
    @Tag("JavaFX")
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
    @Tag("JavaFX")
    void testFrequencyChangeHidesCustomDaysContainer() throws Exception {
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        HBox customDaysContainer = (HBox) getPrivateField(editHabitController, "customDaysContainer");

        frequencyChoiceBox.setValue("Daily");
        Method onFrequencyChangedMethod = editHabitController.getClass().getDeclaredMethod("onFrequencyChanged");
        onFrequencyChangedMethod.setAccessible(true);
        onFrequencyChangedMethod.invoke(editHabitController);

        assertFalse(customDaysContainer.isVisible());
    }

    @ParameterizedTest
    @CsvSource({
            "DAILY, DAILY",
            "WEEKLY, WEEKLY",
            "MONTHLY, MONTHLY",
            "CUSTOM, CUSTOM"
    })
    @Tag("JavaFX")
    void testSetHabitWithFrequencies(Habit.Frequency frequency, String frequencyDisplay) throws Exception {
        // Different frequencies
        Habit habit = new Habit("Test Habit", frequency);
        habit.setCreationDate(LocalDate.now());
        habit.setColor("#0000FF"); // Blue

        // Set custom days only for "Custom" frequency
        if (frequency == Habit.Frequency.CUSTOM) {
            habit.setCustomDays(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        } else {
            habit.setCustomDays(Collections.emptyList());
        }

        editHabitController.setHabit(habit);

        TextField editHabitNameField = (TextField) getPrivateField(editHabitController, "editHabitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(editHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox");

        assertEquals("Test Habit", editHabitNameField.getText());
        assertEquals(frequencyDisplay, frequencyChoiceBox.getValue());
        assertEquals(LocalDate.now(), startDatePicker.getValue());
        assertEquals("Blue", colorChoiceBox.getValue());

    }

    @Test
    @Tag("JavaFX")
    void testGetColorNameFromHex() throws Exception {
        Method getColorNameFromHex = editHabitController.getClass().getDeclaredMethod("getColorNameFromHex", String.class);
        getColorNameFromHex.setAccessible(true);

        assertEquals("Black", getColorNameFromHex.invoke(editHabitController, "#000000"));
        assertEquals("Red", getColorNameFromHex.invoke(editHabitController, "#FF0000"));
        assertEquals("Green", getColorNameFromHex.invoke(editHabitController, "#008000"));
        assertEquals("Blue", getColorNameFromHex.invoke(editHabitController, "#0000FF"));
        assertEquals("Magenta", getColorNameFromHex.invoke(editHabitController, "#FF00FF"));
        assertEquals("Yellow", getColorNameFromHex.invoke(editHabitController, "#FFFF00"));
        assertEquals("Orange", getColorNameFromHex.invoke(editHabitController, "#FFA500"));
        assertEquals("Cyan", getColorNameFromHex.invoke(editHabitController, "#00FFFF"));

        // Test default case
        assertEquals("Black", getColorNameFromHex.invoke(editHabitController, "#ABCDEF")); // Any non-matching color
    }


    @Test
    @Tag("JavaFX")
    void testGetColorHexCode() throws Exception {
        Method getColorHexCode = editHabitController.getClass().getDeclaredMethod("getColorHexCode", String.class);
        getColorHexCode.setAccessible(true);

        assertEquals("#000000", getColorHexCode.invoke(editHabitController, "Black"));
        assertEquals("#FF0000", getColorHexCode.invoke(editHabitController, "Red"));
        assertEquals("#008000", getColorHexCode.invoke(editHabitController, "Green"));
        assertEquals("#0000FF", getColorHexCode.invoke(editHabitController, "Blue"));
        assertEquals("#FF00FF", getColorHexCode.invoke(editHabitController, "Magenta"));
        assertEquals("#FFFF00", getColorHexCode.invoke(editHabitController, "Yellow"));
        assertEquals("#FFA500", getColorHexCode.invoke(editHabitController, "Orange"));
        assertEquals("#00FFFF", getColorHexCode.invoke(editHabitController, "Cyan"));
    }



    @ParameterizedTest
    @CsvSource({
            "DAILY, #000000, Black, ''",          // Daily frequency, no custom days
            "WEEKLY, #FF0000, Red, ''",           // Weekly frequency, no custom days
            "MONTHLY, #008000, Green, ''",        // Monthly frequency, no custom days
            "CUSTOM, #0000FF, Blue, MONDAY-FRIDAY",  // Custom frequency with custom days
            "CUSTOM, #FFA500, Orange, MONDAY-WEDNESDAY-FRIDAY", // Custom frequency with scattered days
            "CUSTOM, #FFA500, Orange, TUESDAY-WEDNESDAY-THURSDAY-SATURDAY-SUNDAY", // Custom frequency with scattered days
            "CUSTOM, #FFFF00, Yellow, MONDAY-TUESDAY-WEDNESDAY-THURSDAY-FRIDAY-SATURDAY-SUNDAY" // Custom frequency with all days
    })
    @Tag("JavaFX")
    void testSetHabitWithDifferentFrequencies(Habit.Frequency frequency, String colorHex, String colorName, String customDays) throws Exception {
        Habit habit = new Habit("Test Habit", frequency);
        habit.setCreationDate(LocalDate.now());
        habit.setColor(colorHex);

        // Set custom days if specified
        if (!customDays.isEmpty()) {
            List<DayOfWeek> days = Arrays.stream(customDays.split("-"))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toList());
            habit.setCustomDays(days);
        }

        editHabitController.setHabit(habit);

        TextField editHabitNameField = (TextField) getPrivateField(editHabitController, "editHabitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(editHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(editHabitController, "colorChoiceBox");
        HBox customDaysContainer = (HBox) getPrivateField(editHabitController, "customDaysContainer");

        // Verify basic fields
        assertEquals("Test Habit", editHabitNameField.getText());
        assertEquals(frequency.toString(), frequencyChoiceBox.getValue());
        assertEquals(LocalDate.now(), startDatePicker.getValue());
        assertEquals(colorName, colorChoiceBox.getValue());

        // Verify custom days container visibility based on frequency
        if (frequency == Habit.Frequency.CUSTOM) {
            assertTrue(customDaysContainer.isVisible());

            // Check toggle buttons for selected days
            verifyToggleButtonState("mondayToggle", customDays.contains("MONDAY"));
            verifyToggleButtonState("tuesdayToggle", customDays.contains("TUESDAY"));
            verifyToggleButtonState("wednesdayToggle", customDays.contains("WEDNESDAY"));
            verifyToggleButtonState("thursdayToggle", customDays.contains("THURSDAY"));
            verifyToggleButtonState("fridayToggle", customDays.contains("FRIDAY"));
            verifyToggleButtonState("saturdayToggle", customDays.contains("SATURDAY"));
            verifyToggleButtonState("sundayToggle", customDays.contains("SUNDAY"));
        } else {
            assertFalse(customDaysContainer.isVisible());
        }
    }

    private void verifyToggleButtonState(String toggleButtonName, boolean expectedState) throws Exception {
        ToggleButton toggleButton = (ToggleButton) getPrivateField(editHabitController, toggleButtonName);
        assertEquals(expectedState, toggleButton.isSelected());
    }

}

