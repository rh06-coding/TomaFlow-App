package com.tomaflow.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class TomaFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Ép ứng dụng luôn chạy ở chế độ giao diện sáng (Light Mode), bất kể cài đặt của hệ thống.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
