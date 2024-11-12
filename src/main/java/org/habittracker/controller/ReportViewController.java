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
    private ComboBox<String> monthSelector;
    @FXML
    private Label reportPeriodLabel;
    @FXML
    private TableView<HabitReportData> habitReportTable;
    @FXML
    private TableColumn<HabitReportData, String> habitNameColumn;
    @FXML
    private TableColumn<HabitReportData, Integer> completionRateColumn;
    @FXML
    private TableColumn<HabitReportData, Integer> longestStreakColumn;
    @FXML
    private TableColumn<HabitReportData, Integer> consistencyColumn;

    @FXML
    private VBox reportContent;

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

    private void loadAvailableMonths() {
        monthSelector.getItems().clear();
        try {
            Files.list(Paths.get("reports"))
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
    private void onMonthSelected() {
        String selectedMonth = monthSelector.getValue();
        if (selectedMonth != null) {
            loadReportData(selectedMonth);
            reportContent.setVisible(true);
        }
    }

    private void loadReportData(String period) {
        Path reportPath = Paths.get("reports", period + ".json");
        try (FileReader reader = new FileReader(reportPath.toFile())) {
            MonthlyReport monthlyReport = gson.fromJson(reader, MonthlyReport.class);
            displayReportData(monthlyReport);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayReportData(MonthlyReport report) {
        reportPeriodLabel.setText("Report for " + report.getPeriod());
        habitReportTable.getItems().setAll(report.getHabitData());
    }


    @FXML
    private void goBack() {
            mainController.showHabitListView();
    }
}
