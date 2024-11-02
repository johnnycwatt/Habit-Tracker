package org.habittracker.repository;
import org.habittracker.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HabitRepositoryTest {

    private HabitRepository habitRepository;

    @BeforeEach
    void setUp() {
        habitRepository =new HabitRepository();
    }

    @Test
    void testAddHabit() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);
        assertNotNull(habitRepository.findHabitByName("Exercise"));
    }

    @Test
    void testFindHabitByName() {
        Habit habit = new Habit("Read", Habit.Frequency.WEEKLY);
        habitRepository.addHabit(habit);
        Habit habitExists = habitRepository.findHabitByName("Read");
        assertNotNull(habitExists);
        assertEquals("Read", habitExists.getName());
    }

    @Test
    void testUpdateHabit() {
        Habit habit = new Habit("Exercise", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);
        habit.setFrequency(Habit.Frequency.MONTHLY);
        habitRepository.updateHabit(habit);
        Habit updatedHabit = habitRepository.findHabitByName("Exercise");
        assertEquals(Habit.Frequency.MONTHLY, updatedHabit.getFrequency());
    }

    @Test
    void testDeleteHabit() {
        Habit habit = new Habit("Meditate", Habit.Frequency.DAILY);
        habitRepository.addHabit(habit);

        habitRepository.deleteHabit(habit);
        assertNull(habitRepository.findHabitByName("Meditate"));
    }
}
