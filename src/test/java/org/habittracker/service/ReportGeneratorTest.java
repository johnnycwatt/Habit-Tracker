package org.habittracker.service;

import org.habittracker.model.Habit;
import org.habittracker.model.MonthlyReport;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ReportGeneratorTest {
    @Mock
    private HabitRepository habitRepository;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportGenerator = new ReportGenerator(habitRepository, notifier);

        // Ensure the reports directory is empty before each test
        File reportsDirectory = new File("reports");
        if (reportsDirectory.exists()) {
            for (File file : reportsDirectory.listFiles()) {
                file.delete();
            }
        }
        reportsDirectory.delete();
    }

    @Test
    void testGenerateMonthlyReport_createReportAndNotifyUser() {
        Habit testHabit = new Habit("Test Habit", Habit.Frequency.DAILY);
        when(habitRepository.getAllHabits()).thenReturn(Collections.singletonList(testHabit));

        YearMonth period = YearMonth.of(2024, 11);
        reportGenerator.generateMonthlyReport(period);

        // Verify that a report file was created
        File reportFile = new File("reports/MonthlyReport-" + period + ".json");
        assertTrue(reportFile.exists(), "Report file should be created");

        // Verify that the notifier was triggered
        verify(notifier).showMessage("New Monthly Report for " + period + " is available!", "green");
    }

    @Test
    void testLoadMonthlyReport_existingReport() {
        // Generate and save a report
        YearMonth period = YearMonth.of(2024, 11);
        reportGenerator.generateMonthlyReport(period);

        // Try load the saved report
        MonthlyReport loadedReport = reportGenerator.loadMonthlyReport(period);
        assertNotNull(loadedReport, "Loaded report should not be null");
        assertEquals(period.toString(), loadedReport.getPeriod(), "Loaded report should match the saved report period");
    }

    @Test
    void testLoadMonthlyReport_nonexistentReport() {
        YearMonth period = YearMonth.of(2024, 12);
        MonthlyReport loadedReport = reportGenerator.loadMonthlyReport(period);
        assertNull(loadedReport, "Loaded report should be null if the file does not exist");
    }

    @Test
    void testCheckForMissedReports_generateMissedReport() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String reportFileName = "reports/MonthlyReport-" + lastMonth + ".json";

        // Check the report file does not exist before running the test
        new File(reportFileName).delete();

        reportGenerator.checkForMissedReports();

        // Verify that the report file was created
        File reportFile = new File(reportFileName);
        assertTrue(reportFile.exists(), "Missed report file should be generated");

        // Verify that the notifier for the missed report was triggered
        verify(notifier).showMessage("New Monthly Report for " + lastMonth.getMonth() + " is available!", "green");
    }

    @Test
    void testCheckForMissedReports_noReportGeneratedIfExists() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String reportFileName = "reports/MonthlyReport-" + lastMonth + ".json";
        File reportFile = new File(reportFileName);

        // Ensure the 'reports' directory exists
        reportFile.getParentFile().mkdirs();

        // Create dummy file to simulate an existing report
        try {
            reportFile.createNewFile();

            reportGenerator.checkForMissedReports();

            // Verify that the notifier was NOT triggered, since the report already exists
            verify(notifier, never()).showMessage(anyString(), anyString());
        } catch (IOException e) {
            fail("Failed to create dummy report file for test: " + e.getMessage());
        } finally {
            // Clean up
            reportFile.delete();
        }
    }



    @Test
    void testSaveReportAsJson_fileWriteException() {
        YearMonth period = YearMonth.of(2024, 11);
        MonthlyReport report = new MonthlyReport(period.toString(), Collections.emptyList(), LocalDate.now());

        // Make the "reports" directory temporarily unwritable
        File reportsDir = new File("reports");
        reportsDir.setWritable(false);

        try {
            reportGenerator.generateMonthlyReport(period);
            // Verify that no exception was thrown and that the method handled it silently
        } finally {
            // Restore the directory's writability for further tests
            reportsDir.setWritable(true);
        }
    }



}
