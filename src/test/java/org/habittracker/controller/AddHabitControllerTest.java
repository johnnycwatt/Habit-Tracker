package org.habittracker.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.JavaFxInitializer;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import org.habittracker.controller.MainController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.habittracker.Main;

import static org.junit.jupiter.api.Assertions.*;

public class AddHabitControllerTest {

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

    @ParameterizedTest
    @CsvSource({
            // Valid habit cases
            "'Test Habit', Daily, #000000, Black, , true",
            "'Weekly Habit', Weekly, #FF0000, Red, , true",
            "'Monthly Habit', Monthly, #008000, Green, , true",
            "'Custom Habit Mon-Wed', Custom, #0000FF, Blue, MONDAY-WEDNESDAY, true",
            "'Custom Habit Mon-Wed-Fri', Custom, #FFA500, Orange, MONDAY-WEDNESDAY-FRIDAY, true",
            "'Custom Habit All Days', Custom, #00FFFF, Cyan, MONDAY-TUESDAY-WEDNESDAY-THURSDAY-FRIDAY-SATURDAY-SUNDAY, true",
            "'Purple Habit', Daily, #800080, Purple, , true",
            "'Cyan Habit', Weekly, #00FFFF, Cyan, , true",
            "'Magenta Habit', Monthly, #FF00FF, Magenta, , true",

            // Edge cases triggering errors
            "'', Daily, #000000, Black, , false",                        // Empty habit name
            "'Duplicate Habit', Daily, #FF0000, Red, , false",           // Duplicate habit name
            "'No Frequency Habit', , #008000, Green, , false",           // No frequency selected
            "'Custom No Days', Custom, #0000FF, Blue, , true"            // Custom frequency with no selected days (allowed)
    })
    @Tag("JavaFX")
    void testAddHabitWithVariousInputs(String habitName, String frequency, String colorHex, String colorName, String customDays, boolean shouldSucceed) throws Exception {
        // Set up habit details
        TextField habitNameField = (TextField) getPrivateField(addHabitController, "habitNameField");
        ChoiceBox<String> frequencyChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "frequencyChoiceBox");
        DatePicker startDatePicker = (DatePicker) getPrivateField(addHabitController, "startDatePicker");
        ChoiceBox<String> colorChoiceBox = (ChoiceBox<String>) getPrivateField(addHabitController, "colorChoiceBox");
        habitNameField.setText(habitName);

        if (frequency != null) {
            frequencyChoiceBox.setValue(frequency);
        } else {
            frequencyChoiceBox.setValue(null); // Simulate no frequency selected
        }
        startDatePicker.setValue(LocalDate.now());
        colorChoiceBox.setValue(colorName);

        // Simulate duplicate habit name condition
        when(habitRepository.habitExistsByName(habitName)).thenReturn("Duplicate Habit".equals(habitName));

        // Set custom days if specified and frequency is "Custom"
        if ("Custom".equals(frequency) && customDays != null && !customDays.isEmpty()) {
            List<DayOfWeek> days = Arrays.stream(customDays.split("-"))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toList());
            for (DayOfWeek day : days) {
                ToggleButton toggle = (ToggleButton) getPrivateField(addHabitController, day.name().toLowerCase() + "Toggle");
                toggle.setSelected(true);
            }
        }

        // Invoke addHabit method
        Method addHabitMethod = addHabitController.getClass().getDeclaredMethod("addHabit");
        addHabitMethod.setAccessible(true);
        addHabitMethod.invoke(addHabitController);

        // Verification based on whether habit creation should succeed
        if (shouldSucceed) {
            verify(habitRepository, times(1)).addHabit(any(Habit.class));
            verify(notifier, times(1)).showMessage("Habit added successfully!", "green");
        } else {
            verify(habitRepository, never()).addHabit(any(Habit.class));
            verify(notifier, atLeastOnce()).showMessage(anyString(), eq("red"));
        }
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

