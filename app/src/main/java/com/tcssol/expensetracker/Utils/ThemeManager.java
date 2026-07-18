package com.tcssol.expensetracker.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/** Persists and applies the user's light/dark theme choice. */
public final class ThemeManager {

    private static final String PREFS = "ui_prefs";
    private static final String KEY_MODE = "theme_mode";

    private ThemeManager() {
    }

    /** Applies the saved mode; call once at app start. */
    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(savedMode(context));
    }

    /** Flips light/dark, saves the choice, and re-applies it. */
    public static void toggle(Context context) {
        int next = isDark(context)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;
        prefs(context).edit().putInt(KEY_MODE, next).apply();
        AppCompatDelegate.setDefaultNightMode(next);
    }

    /** Whether the app is currently rendering in dark mode. */
    public static boolean isDark(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private static int savedMode(Context context) {
        return prefs(context).getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
