package com.tomaflow.app;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class TomaFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences themePrefs = getSharedPreferences("user_theme_prefs", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("last_dark", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
