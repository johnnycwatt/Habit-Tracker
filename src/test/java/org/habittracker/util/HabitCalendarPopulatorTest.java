package org.habittracker.util;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HabitCalendarPopulatorTest {
    private GridPane mockGridPane;
    private Label mockMonthLabel;
    private HabitRepository mockHabitRepository;
    private HabitCalendarPopulator calendarPopulator;

    @BeforeEach
    void setUp() {
        JavaFxInitializer.initToolkit();
        mockGridPane = new GridPane();
        mockMonthLabel = new Label();
        mockHabitRepository = mock(HabitRepository.class);

        calendarPopulator = new HabitCalendarPopulator(mockGridPane, mockMonthLabel, mockHabitRepository);
    }

    @Test
    void testConstructorInitializesFields() {
        assertNotNull(calendarPopulator);
    }

    @Test
    void testInitializeDayNameLabels() throws Exception {
        HabitCalendarPopulator populator = new HabitCalendarPopulator(mockGridPane, mockMonthLabel, mockHabitRepository);

        // Access private field dayNameLabels using reflection
        Label[] dayNameLabels = (Label[]) getPrivateField(populator, "dayNameLabels");
        assertEquals(7, dayNameLabels.length);
        assertEquals("Mon", dayNameLabels[0].getText());
        assertEquals("Sun", dayNameLabels[6].getText());
    }

    @Test
    void testPopulateCalendar() {
        List<Habit> mockHabits = List.of(
                new Habit("Daily Habit", Habit.Frequency.DAILY),
                new Habit("Weekly Habit", Habit.Frequency.CUSTOM, Collections.singletonList(DayOfWeek.MONDAY))
        );
        when(mockHabitRepository.getAllHabits()).thenReturn(mockHabits);

        LocalDate referenceDate = LocalDate.of(2024, 11, 1);
        calendarPopulator.populateCalendar(referenceDate, false);

        // Verify the month label text
        assertEquals("November 2024", mockMonthLabel.getText());

        // Verify that days are added to the grid
        List<Label> days = mockGridPane.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .collect(Collectors.toList());

        assertEquals(37, days.size());  // November has 30 days + 7 day labels (Mon - Sun)
        assertTrue(days.stream().anyMatch(label -> label.getText().equals("1")));
        assertTrue(days.stream().anyMatch(label -> label.getText().equals("30")));
    }


    @Test
    void testAddDayNamesToCalendar() {
        calendarPopulator.populateCalendar(LocalDate.of(2024, 11, 1), false);

        // Verify day names are added to the grid
        List<String> dayNames = mockGridPane.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .collect(Collectors.toList());

        assertTrue(dayNames.containsAll(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")));
    }


    @ParameterizedTest
    @CsvSource({
            "DAILY, 2024-11-01, true",
            "CUSTOM, 2024-11-06, false"
    })
    void testIsHabitDueToday(Habit.Frequency frequency, LocalDate today, boolean expected) {
        Habit mockHabit = new Habit("Test Habit", frequency, List.of(DayOfWeek.MONDAY));
        boolean isDue = calendarPopulator.isHabitDueToday(mockHabit, today);
        assertEquals(expected, isDue);
    }

    private Object getPrivateField(Object instance, String fieldName) throws Exception {
        var field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }
}
