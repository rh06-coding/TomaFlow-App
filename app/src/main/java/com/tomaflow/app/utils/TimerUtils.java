package com.tomaflow.app.utils;

import android.content.Context;

import com.tomaflow.app.timer.PomodoroTimer.TimerPhase;

/**
 * TimerUtils — Helper utilities for timer operations
 *
 * Provides utility methods for time formatting, phase labeling, and progress calculations.
 * These utilities are used across MainActivity, TimerService, and UI components.
 */
public final class TimerUtils {

    private TimerUtils() {
        // Prevent instantiation
    }

    /**
     * Format milliseconds to "MM:SS" string format.
     * Useful for displaying timer in UI.
     */
    public static String formatMillisToMmSs(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatMillisToHhMmSs(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getPhaseLabel(TimerPhase phase) {
        if (phase == null) {
            return "Unknown";
        }
        return phase.getDisplayName();
    }

    public static String getPhaseLabelLocalized(Context context, TimerPhase phase) {
        if (context == null || phase == null) {
            return "Unknown";
        }

        // For now, return the phase's built-in display name
        // In the future, this can fetch from strings.xml for proper internationalization
        return phase.getDisplayName();
    }

    public static int calculateProgressPercent(long elapsedMs, long totalMs) {
        if (totalMs <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((elapsedMs * 100L) / totalMs));
    }

    public static float calculateProgressFloat(long elapsedMs, long totalMs) {
        if (totalMs <= 0) {
            return 0f;
        }
        return Math.min(1f, (float) elapsedMs / totalMs);
    }

    public static boolean isBreakPhase(TimerPhase phase) {
        return phase == TimerPhase.SHORT_BREAK || phase == TimerPhase.LONG_BREAK;
    }

    public static boolean isWorkPhase(TimerPhase phase) {
        return phase == TimerPhase.WORK;
    }

    public static long secondsToMillis(long seconds) {
        return seconds * 1000L;
    }

    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000L;
    }

    public static long minutesToSeconds(long minutes) {
        return minutes * 60;
    }

    public static int millisToMinutes(long millis) {
        return (int) ((millis / 1000) / 60);
    }

    public static int millisToSeconds(long millis) {
        return (int) (millis / 1000);
    }

    public static int[] getMinutesAndSeconds(long millis) {
        long totalSeconds = millis / 1000;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return new int[]{minutes, seconds};
    }
}

