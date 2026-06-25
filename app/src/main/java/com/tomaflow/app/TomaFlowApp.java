package com.tomaflow.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.tomaflow.app.timer.SettingsManager;

public class TomaFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SettingsManager settingsManager = new SettingsManager(this);
        AppCompatDelegate.setDefaultNightMode(
                settingsManager.isDarkMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
