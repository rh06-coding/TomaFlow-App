package com.tomaflow.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class TomaFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Force light mode permanently across the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
