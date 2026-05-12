package com.tomaflow.app.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SettingsActivity — Preferences Screen (stub).
 *
 * Corresponds to the Settings tab in the bottom navigation.
 * Will allow the user to configure Work Duration (slider 15–60 min),
 * Break Duration (slider 3–15 min), Sound Notifications toggle,
 * and Strict Mode toggle.  All values are persisted via SharedPreferences
 * (simple settings) or Room (if richer query capability is needed).
 *
 * Prototype reference: SettingsScreen.tsx
 *
 * TODO: Inflate activity_settings.xml, bind SeekBars and Switch views,
 *       wire SettingsRepository.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_settings);
    }
}
