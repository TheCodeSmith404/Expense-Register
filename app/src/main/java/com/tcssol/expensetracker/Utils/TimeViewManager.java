package com.tcssol.expensetracker.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import java.util.Currency;
import java.util.Locale;

public class TimeViewManager {
    private static final String PREF_NAME = "TimeViewPrefs";
    private static final String KEY_MONTHLY_EARNINGS = "earnings_monthly";
    private static final String KEY_DAYS_WORKED = "days_worked";
    private static final String KEY_HOURS_WORKED = "hours_worked";
    private static final String KEY_TIME_VIEW_MODE = "time_view_mode";

    private static final MutableLiveData<Boolean> timeViewModeLiveData = new MutableLiveData<>(false);
    private static double hourlyRate = 0.0;
    private static double monthlyEarnings = 0.0;
    private static int daysWorked = 0;
    private static double hoursWorked = 0;

    public static void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        monthlyEarnings = prefs.getFloat(KEY_MONTHLY_EARNINGS, 0f);
        daysWorked = prefs.getInt(KEY_DAYS_WORKED, 0);
        hoursWorked = prefs.getFloat(KEY_HOURS_WORKED, 0f);
        boolean mode = prefs.getBoolean(KEY_TIME_VIEW_MODE, false);
        timeViewModeLiveData.setValue(mode);
        recalculateHourlyRate();
    }

    public static MutableLiveData<Boolean> getTimeViewModeLiveData() {
        return timeViewModeLiveData;
    }

    public static boolean isTimeViewMode() {
        return Boolean.TRUE.equals(timeViewModeLiveData.getValue());
    }

    public static void setTimeViewMode(Context context, boolean active) {
        timeViewModeLiveData.setValue(active);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TIME_VIEW_MODE, active).apply();
    }

    public static boolean isConfigured() {
        return monthlyEarnings > 0 && daysWorked > 0 && hoursWorked > 0;
    }

    public static void saveConfig(Context context, double earnings, int days, double hours) {
        monthlyEarnings = earnings;
        daysWorked = days;
        hoursWorked = hours;
        recalculateHourlyRate();

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_MONTHLY_EARNINGS, (float) earnings)
                .putInt(KEY_DAYS_WORKED, days)
                .putFloat(KEY_HOURS_WORKED, (float) hours)
                .apply();
    }

    public static double getMonthlyEarnings() {
        return monthlyEarnings;
    }

    public static int getDaysWorked() {
        return daysWorked;
    }

    public static double getHoursWorked() {
        return hoursWorked;
    }

    public static double getHourlyRate() {
        return hourlyRate;
    }

    private static void recalculateHourlyRate() {
        double totalHours = daysWorked * hoursWorked;
        if (totalHours > 0) {
            hourlyRate = monthlyEarnings / totalHours;
        } else {
            hourlyRate = 0.0;
        }
    }

    public static String formatAmount(double amount, boolean showSign) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        boolean isNegative = amount < 0;
        double absAmt = Math.abs(amount);

        if (!isTimeViewMode() || hourlyRate <= 0.0) {
            String formatted = symbol + String.format(Locale.getDefault(), "%,.0f", absAmt);
            if (showSign) {
                return (isNegative ? "-" : "+") + formatted;
            } else {
                return (isNegative ? "-" : "") + formatted;
            }
        }

        return formatAmountAsTime(amount, showSign);
    }

    public static String formatAmountAsTime(double amount, boolean showSign) {
        if (hourlyRate <= 0.0) {
            return "0s";
        }
        boolean isNegative = amount < 0;
        double absAmt = Math.abs(amount);

        double totalHours = absAmt / hourlyRate;
        double hoursPerDay = hoursWorked;
        double hoursPerWeek = (daysWorked / 4.0) * hoursWorked;
        double hoursPerMonth = daysWorked * hoursWorked;

        String timeStr;
        if (totalHours >= hoursPerMonth) {
            double months = totalHours / hoursPerMonth;
            timeStr = formatDecimal(months) + "mo";
        } else if (totalHours >= hoursPerWeek) {
            double weeks = totalHours / hoursPerWeek;
            timeStr = formatDecimal(weeks) + "w";
        } else if (totalHours >= hoursPerDay) {
            double days = totalHours / hoursPerDay;
            timeStr = formatDecimal(days) + "d";
        } else if (totalHours >= 1.0) {
            timeStr = formatDecimal(totalHours) + "h";
        } else if (totalHours >= 1.0 / 60.0) {
            double minutes = totalHours * 60.0;
            timeStr = formatDecimal(minutes) + "m";
        } else {
            double seconds = totalHours * 3600.0;
            timeStr = String.format(Locale.getDefault(), "%.0fs", seconds);
        }

        if (showSign) {
            return (isNegative ? "-" : "+") + timeStr;
        } else {
            return (isNegative ? "-" : "") + timeStr;
        }
    }

    private static String formatDecimal(double value) {
        if (value == (long) value) {
            return String.format(Locale.getDefault(), "%d", (long) value);
        } else {
            return String.format(Locale.getDefault(), "%.1f", value);
        }
    }
}
