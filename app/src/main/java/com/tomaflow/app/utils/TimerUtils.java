package com.tomaflow.app.utils;

import android.content.Context;

import com.tomaflow.app.timer.PomodoroTimer.Phase;

import java.util.Locale;

/** Utility methods for time formatting, progress calculation, and unit conversion. */
public final class TimerUtils {

    private TimerUtils() {}

    /** 1500000ms -> "25:00", 65000ms -> "01:05" */
    public static String formatMillisToMmSs(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    /** 3661000ms -> "01:01:01" */
    public static String formatMillisToHhMmSs(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getPhaseLabel(Phase phase) {
        if (phase == null) {
            return "Unknown";
        }
        return phase.getDisplayName();
    }

    public static String getPhaseLabelLocalized(Context context, Phase phase) {
        if (context == null || phase == null) {
            return "Unknown";
        }

        return phase.getDisplayName();
    }

    /** Progress as integer percent (0-100). */
    public static int calculateProgressPercent(long elapsedMs, long totalMs) {
        if (totalMs <= 0) return 0;
        return Math.min(100, (int) ((elapsedMs * 100L) / totalMs));
    }

    /** Progress as float (0.0-1.0) for ProgressBar. */
    public static float calculateProgressFloat(long elapsedMs, long totalMs) {
        if (totalMs <= 0) return 0f;
        return Math.min(1f, (float) elapsedMs / totalMs);
    }

    public static boolean isBreakPhase(Phase phase) {
        return phase == Phase.SHORT_BREAK || phase == Phase.LONG_BREAK;
    }

    public static boolean isWorkPhase(Phase phase) {
        return phase == Phase.FOCUS;
    }

    public static long secondsToMillis(long seconds) { return seconds * 1000L; }
    public static long minutesToMillis(long minutes) { return minutes * 60 * 1000L; }
    public static long minutesToSeconds(long minutes) { return minutes * 60; }
    public static int millisToMinutes(long millis) { return (int) ((millis / 1000) / 60); }
    public static int millisToSeconds(long millis) { return (int) (millis / 1000); }

    /** 1500000ms -> [25, 0], 65000ms -> [1, 5] */
    public static int[] getMinutesAndSeconds(long millis) {
        long totalSeconds = millis / 1000;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return new int[]{minutes, seconds};
    }
}
