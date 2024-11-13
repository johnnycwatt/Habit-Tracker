package org.habittracker.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.HabitReportData;
import org.habittracker.model.MonthlyReport;
import org.habittracker.util.JsonBackupHelper;
import org.habittracker.util.LocalDateAdapter;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;



public class ReportViewController {
    @FXML
    ComboBox<String> monthSelector;
    @FXML
    Label reportPeriodLabel;
    @FXML
    TableView<HabitReportData> habitReportTable;
    @FXML
    TableColumn<HabitReportData, String> habitNameColumn;
    @FXML
    TableColumn<HabitReportData, Integer> completionRateColumn;
    @FXML
    TableColumn<HabitReportData, Integer> longestStreakColumn;
    @FXML
    TableColumn<HabitReportData, Integer> consistencyColumn;

    @FXML
    VBox reportContent;

    private Path reportsDirectory = Paths.get("reports");

    public void setReportsDirectory(Path path) {
        this.reportsDirectory = path;
    }


    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Register the LocalDateAdapter
            .setPrettyPrinting()
            .create();


    private MainController mainController;
    private Main mainApp;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        System.out.println("MainController set in ReportViewController: " + (mainController != null));
    }


    @FXML
    public void initialize() {
        habitNameColumn.setCellValueFactory(new PropertyValueFactory<>("habitName"));
        completionRateColumn.setCellValueFactory(new PropertyValueFactory<>("completionRate"));
        longestStreakColumn.setCellValueFactory(new PropertyValueFactory<>("longestStreak"));
        consistencyColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyConsistency"));

        loadAvailableMonths();
    }

    void loadAvailableMonths() {
        monthSelector.getItems().clear();
        try {
            Files.list(reportsDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(filename -> filename.endsWith(".json"))
                    .map(filename -> filename.replace(".json", ""))
                    .forEach(monthSelector.getItems()::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void onMonthSelected() {
        String selectedMonth = monthSelector.getValue();
        if (selectedMonth != null) {
            loadReportData(selectedMonth);
            reportContent.setVisible(true);
        }
    }

    public void loadReportData(String month) {
        Path reportFilePath = reportsDirectory.resolve(month + ".json");
        try (FileReader reader = new FileReader(reportFilePath.toFile())) {
            MonthlyReport report = gson.fromJson(reader, MonthlyReport.class);
            displayReportData(report);
            reportPeriodLabel.setText("Report for " + month);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void displayReportData(MonthlyReport report) {
        reportPeriodLabel.setText("Report for " + report.getPeriod());
        habitReportTable.getItems().setAll(report.getHabitData());
    }


    @FXML
    void goBack() {
            mainController.showHabitListView();
    }
}
