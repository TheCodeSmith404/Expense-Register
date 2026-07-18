package com.tcssol.expensetracker;

import android.app.Application;

import com.tcssol.expensetracker.Utils.ThemeManager;

public class ExpenseTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
    }
}
