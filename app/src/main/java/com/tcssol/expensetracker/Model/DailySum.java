package com.tcssol.expensetracker.Model;

import java.time.LocalDate;

/**
 * Data class for Bar Chart data points.
 */
public class DailySum {
    private LocalDate date;
    private double totalSpent;
    private double totalEarned;

    public DailySum(LocalDate date, double totalSpent, double totalEarned) {
        this.date = date;
        this.totalSpent = totalSpent;
        this.totalEarned = totalEarned;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public double getTotalEarned() { return totalEarned; }
    public void setTotalEarned(double totalEarned) { this.totalEarned = totalEarned; }
}
