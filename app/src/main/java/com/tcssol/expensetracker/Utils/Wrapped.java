package com.tcssol.expensetracker.Utils;

public class Wrapped {
    private int month=-12;
    private int year=-2024;
    public Wrapped(int month, int year){
        this.month=month;
        this.year=year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
