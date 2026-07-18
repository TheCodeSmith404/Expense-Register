package com.tcssol.expensetracker.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "earnings_history_table")
public class EarningsHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "monthly_earnings")
    private double monthlyEarnings;

    @ColumnInfo(name = "days_worked")
    private int daysWorked;

    @ColumnInfo(name = "hours_worked")
    private double hoursWorked;

    @ColumnInfo(name = "hourly_rate")
    private double hourlyRate;

    public EarningsHistory(long timestamp, double monthlyEarnings, int daysWorked, double hoursWorked, double hourlyRate) {
        this.timestamp = timestamp;
        this.monthlyEarnings = monthlyEarnings;
        this.daysWorked = daysWorked;
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getMonthlyEarnings() {
        return monthlyEarnings;
    }

    public void setMonthlyEarnings(double monthlyEarnings) {
        this.monthlyEarnings = monthlyEarnings;
    }

    public int getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(int daysWorked) {
        this.daysWorked = daysWorked;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
