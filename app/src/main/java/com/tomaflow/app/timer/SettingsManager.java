package com.tomaflow.app.timer;

import android.content.Context;
import android.content.SharedPreferences;

import com.tomaflow.app.constants.AppConstants;

/**
 * Single source of truth for user-configurable timer durations.
 *
 * Each getter returns the user-set value when one exists, otherwise the
 * {@link AppConstants} default — so "AppConstant || user-set" is resolved here.
 * Values are stored as whole minutes (matching the {@code *_MIN} pref keys and
 * the Settings sliders) and exposed to the timer as milliseconds.
 */
public class SettingsManager {

    private final SharedPreferences mPrefs;

    public SettingsManager(Context context) {
        this.mPrefs = context.getSharedPreferences(
                AppConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public long getFocusDurationMs() {
        return resolveMs(AppConstants.PREF_WORK_DURATION_MIN, AppConstants.TIMER_WORK_DURATION_MS);
    }

    public long getShortBreakDurationMs() {
        return resolveMs(AppConstants.PREF_SHORT_BREAK_MIN, AppConstants.TIMER_SHORT_BREAK_MS);
    }

    public long getLongBreakDurationMs() {
        return resolveMs(AppConstants.PREF_LONG_BREAK_MIN, AppConstants.TIMER_LONG_BREAK_MS);
    }

    public void setFocusDurationMinutes(int minutes) {
        putMinutes(AppConstants.PREF_WORK_DURATION_MIN, minutes);
    }

    public void setShortBreakDurationMinutes(int minutes) {
        putMinutes(AppConstants.PREF_SHORT_BREAK_MIN, minutes);
    }

    public void setLongBreakDurationMinutes(int minutes) {
        putMinutes(AppConstants.PREF_LONG_BREAK_MIN, minutes);
    }

    /** Stored minutes → ms, or the AppConstants default when nothing is saved. */
    private long resolveMs(String key, long defaultMs) {
        int minutes = mPrefs.getInt(key, 0);
        return minutes > 0 ? minutes * 60_000L : defaultMs;
    }

    private void putMinutes(String key, int minutes) {
        mPrefs.edit().putInt(key, minutes).apply();
    }
}
