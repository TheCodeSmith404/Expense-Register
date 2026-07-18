package com.tcssol.expensetracker.Model;

import androidx.room.ColumnInfo;

public class MonthlySum {
    @ColumnInfo(name = "month_year")
    private String monthYear;

    @ColumnInfo(name = "total")
    private double total;

    public MonthlySum(String monthYear, double total) {
        this.monthYear = monthYear;
        this.total = total;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
