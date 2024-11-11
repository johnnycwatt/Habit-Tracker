package org.habittracker.model;

public class HabitReportData {
    private String habitName;
    private int completionRate;
    private int longestStreak;
    private int monthlyConsistency;
    private int ranking;

    public HabitReportData(String habitName, int completionRate, int longestStreak, int monthlyConsistency, int ranking) {
        this.habitName = habitName;
        this.completionRate = completionRate;
        this.longestStreak = longestStreak;
        this.monthlyConsistency = monthlyConsistency;
        this.ranking = ranking;
    }


    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
    }

    public int getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(int completionRate) {
        this.completionRate = completionRate;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getMonthlyConsistency() {
        return monthlyConsistency;
    }

    public void setMonthlyConsistency(int monthlyConsistency) {
        this.monthlyConsistency = monthlyConsistency;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
