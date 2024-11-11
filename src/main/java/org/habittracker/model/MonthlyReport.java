package org.habittracker.model;

import java.time.LocalDate;
import java.util.List;

public class MonthlyReport {
    private String period; // Format:"YYYY-MM"
    private List<HabitReportData> habitData;
    private LocalDate reportGeneratedDate;

    public MonthlyReport(String period, List<HabitReportData> habitData, LocalDate reportGeneratedDate) {
        this.period = period;
        this.habitData = habitData;
        this.reportGeneratedDate = reportGeneratedDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<HabitReportData> getHabitData() {
        return habitData;
    }

    public void setHabitData(List<HabitReportData> habitData) {
        this.habitData = habitData;
    }

    public LocalDate getReportGeneratedDate() {
        return reportGeneratedDate;
    }

    public void setReportGeneratedDate(LocalDate reportGeneratedDate) {
        this.reportGeneratedDate = reportGeneratedDate;
    }
}
