package org.habittracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.model.Habit;
import org.habittracker.model.MonthlyReport;
import org.habittracker.model.HabitReportData;
import org.habittracker.repository.HabitRepository;
import org.habittracker.util.HabitStatisticsCalculator;
import org.habittracker.util.LocalDateAdapter;
import org.habittracker.util.Notifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReportGenerator {
    private static final Logger LOGGER = LogManager.getLogger(ReportGenerator.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Register the LocalDateAdapter
            .setPrettyPrinting()
            .create();

    private final HabitRepository habitRepository;
    private final Notifier notifier;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Reusable list and objects
    private final List<HabitReportData> habitDataList = new ArrayList<>();
    private final HabitReportData reusableHabitReportData = new HabitReportData("", 0, 0, 0, 0);

    public ReportGenerator(HabitRepository habitRepository, Notifier notifier) {
        this.habitRepository = habitRepository;
        this.notifier = notifier;
    }

    public void generateMonthlyReport(YearMonth period) {
        List<Habit> habits = habitRepository.getAllHabits();
        habitDataList.clear(); // Clear the list for reuse

        // Collect data for each habit without creating new HabitReportData objects
        for (Habit habit : habits) {
            reusableHabitReportData.setHabitName(habit.getName());
            reusableHabitReportData.setCompletionRate(HabitStatisticsCalculator.calculateMonthlyPerformance(habit));
            reusableHabitReportData.setLongestStreak(HabitStatisticsCalculator.calculateLongestStreak(habit));
            reusableHabitReportData.setMonthlyConsistency(HabitStatisticsCalculator.calculateMonthlyConsistency(habit));
            reusableHabitReportData.setRanking(0);

            // Clone the reusable object to avoid overwriting data and add to the list
            habitDataList.add(new HabitReportData(
                    reusableHabitReportData.getHabitName(),
                    reusableHabitReportData.getCompletionRate(),
                    reusableHabitReportData.getLongestStreak(),
                    reusableHabitReportData.getMonthlyConsistency(),
                    reusableHabitReportData.getRanking()
            ));
        }

        // Sort habits by completion rate and then assign rankings
        habitDataList.sort(Comparator.comparingInt(HabitReportData::getCompletionRate).reversed());
        for (int i = 0; i < habitDataList.size(); i++) {
            habitDataList.get(i).setRanking(i + 1);
        }

        // Create the report
        MonthlyReport monthlyReport = new MonthlyReport(
                period.toString(),
                new ArrayList<>(habitDataList), // Ensure a separate list is passed
                LocalDate.now()
        );

        // Save the report to JSON
        saveReportAsJson(monthlyReport, period);

        // Notify user about the new report
        notifier.showMessage("New Monthly Report for " + period + " is available!", "green");
    }

    private void saveReportAsJson(MonthlyReport report, YearMonth period) {
        String fileName = "MonthlyReport-" + period.toString() + ".json";
        Path filePath = Paths.get("reports", fileName);

        // Ensure the 'reports' directory exists
        Path directory = filePath.getParent();
        try {
            if (directory != null && !Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            try (var writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(report, writer);
                LOGGER.info("Report saved successfully to {}", filePath);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving report to JSON file: {}", filePath, e);
        }
    }

    public MonthlyReport loadMonthlyReport(YearMonth period) {
        String fileName = "MonthlyReport-" + period.toString() + ".json";
        Path filePath = Paths.get("reports", fileName);

        try (var reader = Files.newBufferedReader(filePath)) {
            return GSON.fromJson(reader, MonthlyReport.class);
        } catch (IOException e) {
            LOGGER.error("Error loading monthly report from JSON file: {}", filePath, e);
            return null;
        }
    }

    public void checkForMissedReports() {
        LocalDate lastDayOfPreviousMonth = LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());
        YearMonth lastMonth = YearMonth.from(lastDayOfPreviousMonth);
        String reportFilename = "MonthlyReport-" + lastMonth.toString() + ".json";
        Path filePath = Paths.get("reports", reportFilename);

        // If the report file doesn’t exist, generate it
        if (!Files.exists(filePath)) {
            LOGGER.info("Generating missed report for {}", lastMonth);
            generateMonthlyReport(lastMonth);
            notifier.showMessage("New Monthly Report for " + lastMonth.getMonth() + " is available!", "green");
        } else {
            LOGGER.info("Report for {} already exists. No action needed.", lastMonth);
        }
    }


    public void startMonthlyReportScheduler() {
        scheduler.scheduleAtFixedRate(this::generateMonthlyReportIfEndOfMonth, 0, 1, TimeUnit.DAYS);
    }

    void generateMonthlyReportIfEndOfMonth() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        if (today.getDayOfMonth() == currentMonth.lengthOfMonth()) {
            LOGGER.info("Generating monthly report for {}", currentMonth);
            generateMonthlyReport(currentMonth);
        }
    }

    public void stopScheduler() {
        scheduler.shutdown();
        LOGGER.info("Scheduler stopped.");
    }
}
