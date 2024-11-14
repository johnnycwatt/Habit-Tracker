package org.habittracker.controller;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.habittracker.model.HabitReportData;
import org.habittracker.model.MonthlyReport;
import org.habittracker.util.JavaFxInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportViewControllerTest {

    private ReportViewController reportViewController;
    private MainController mainControllerMock;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        JavaFxInitializer.initToolkit();

        // Create the ReportViewController and mock its dependencies
        reportViewController = new ReportViewController();

        mainControllerMock = Mockito.mock(MainController.class);
        reportViewController.setMainController(mainControllerMock);

        // Manually initialize FXML-injected fields
        reportViewController.monthSelector = new ComboBox<>();
        reportViewController.reportPeriodLabel = new Label();
        reportViewController.habitReportTable = new TableView<>();
        reportViewController.reportContent = new VBox();

        // Manually initialize TableColumn fields
        reportViewController.habitNameColumn = new TableColumn<>("Habit Name");
        reportViewController.completionRateColumn = new TableColumn<>("Completion Rate");
        reportViewController.longestStreakColumn = new TableColumn<>("Longest Streak");
        reportViewController.consistencyColumn = new TableColumn<>("Consistency");

        // Add columns to the TableView
        reportViewController.habitReportTable.getColumns().addAll(
                reportViewController.habitNameColumn,
                reportViewController.completionRateColumn,
                reportViewController.longestStreakColumn,
                reportViewController.consistencyColumn
        );

        // Set up the "reports" folder in the temporary directory
        File reportsDir = tempDir.resolve("reports").toFile();
        reportsDir.mkdir();

        // Set the custom reports directory path in the controller for testing
        reportViewController.setReportsDirectory(reportsDir.toPath());

        // Sample JSON file to simulate a monthly report
        File sampleReport = new File(reportsDir, "2023-10.json");
        try (FileWriter writer = new FileWriter(sampleReport)) {
            writer.write("{ \"period\": \"2023-10\", \"habitData\": ["
                    + "{\"habitName\": \"Exercise\", \"completionRate\": 80, \"longestStreak\": 10, \"monthlyConsistency\": 85, \"ranking\": 1},"
                    + "{\"habitName\": \"Reading\", \"completionRate\": 90, \"longestStreak\": 15, \"monthlyConsistency\": 88, \"ranking\": 2}"
                    + "], \"reportGeneratedDate\": \"2023-10-15\"}");
        }

        // Initialize the controller, which sets up the TableView column bindings and loads available months
        reportViewController.initialize();
    }

    @Test
    @Tag("JavaFX")
    void testDisplayReportData() {
        // Create a sample MonthlyReport
        HabitReportData exerciseData = new HabitReportData("Exercise", 80, 10, 85, 1);
        HabitReportData readingData = new HabitReportData("Reading", 90, 15, 88, 2);
        MonthlyReport report = new MonthlyReport("2023-10", Arrays.asList(exerciseData, readingData), LocalDate.of(2023, 10, 15));

        // Display the report data
        reportViewController.displayReportData(report);

        // Verify that the correct report period label is set
        assertEquals("Report for 2023-10", reportViewController.reportPeriodLabel.getText());

        // Check that the table is populated with the habit report data
        assertEquals(2, reportViewController.habitReportTable.getItems().size(), "Habit report table should contain two entries");
    }

    @Test
    @Tag("JavaFX")
    void testGoBack() {
        // Simulate the goBack action
        reportViewController.goBack();

        // Verify that the mainController's showHabitListView is called
        Mockito.verify(mainControllerMock).showHabitListView();
    }

    @Test
    @Tag("JavaFX")
    void testLoadAvailableMonths() {
        // Verify that the monthSelector is populated with the available months
        assertEquals(1, reportViewController.monthSelector.getItems().size(), "Month selector should contain one entry");
        assertEquals("2023-10", reportViewController.monthSelector.getItems().get(0), "Month selector should contain '2023-10'");
    }

    @Test
    @Tag("JavaFX")
    void testOnMonthSelected() {
        // Select a month from the ComboBox
        reportViewController.monthSelector.getItems().add("2023-10");
        reportViewController.monthSelector.setValue("2023-10");

        // Ensure the report content is initially not visible
        reportViewController.reportContent.setVisible(false);

        // Call onMonthSelected and check that the report content is displayed
        reportViewController.onMonthSelected();
        assertTrue(reportViewController.reportContent.isVisible(), "Report content should be visible after a month is selected");
        assertEquals("Report for 2023-10", reportViewController.reportPeriodLabel.getText(), "Report period label should be set correctly");
        assertEquals(2, reportViewController.habitReportTable.getItems().size(), "Habit report table should contain two entries");
    }

    @Test
    @Tag("JavaFX")
    void testLoadReportData() {
        // Directly load the report data for the period "2023-10"
        reportViewController.loadReportData("2023-10");

        // Verify that the label is updated and data is loaded into the TableView
        assertEquals("Report for 2023-10", reportViewController.reportPeriodLabel.getText(), "Report period label should be set correctly");
        assertEquals(2, reportViewController.habitReportTable.getItems().size(), "Habit report table should contain two entries");
        assertEquals("Exercise", reportViewController.habitReportTable.getItems().get(0).getHabitName(), "First entry in table should be 'Exercise'");
        assertEquals("Reading", reportViewController.habitReportTable.getItems().get(1).getHabitName(), "Second entry in table should be 'Reading'");
    }
}
