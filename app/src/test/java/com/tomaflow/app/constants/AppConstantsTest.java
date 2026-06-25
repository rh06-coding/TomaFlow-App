package com.tomaflow.app.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppConstantsTest {

    // ── Timer durations ───────────────────────────────────────────────────────

    @Test
    public void workDuration_is25Minutes() {
        assertEquals(25 * 60 * 1000L, AppConstants.TIMER_WORK_DURATION_MS);
    }

    @Test
    public void shortBreak_is5Minutes() {
        assertEquals(5 * 60 * 1000L, AppConstants.TIMER_SHORT_BREAK_MS);
    }

    @Test
    public void longBreak_is15Minutes() {
        assertEquals(15 * 60 * 1000L, AppConstants.TIMER_LONG_BREAK_MS);
    }

    @Test
    public void cyclesBeforeLongBreak_is4() {
        assertEquals(4, AppConstants.TIMER_CYCLES_BEFORE_LONG_BREAK);
    }

    @Test
    public void countdownInterval_is1Second() {
        assertEquals(1000L, AppConstants.TIMER_COUNTDOWN_INTERVAL_MS);
    }

    @Test
    public void defaultTargetSessions_is8() {
        assertEquals(8, AppConstants.TIMER_DEFAULT_TARGET_SESSIONS);
    }

    // ── Service / vibration ───────────────────────────────────────────────────

    @Test
    public void autoStopDelay_is5Minutes() {
        assertEquals(5 * 60 * 1000L, AppConstants.SERVICE_AUTO_STOP_DELAY_MS);
    }

    @Test
    public void vibrationPhaseComplete_singleVibration() {
        assertEquals(2, AppConstants.VIBRATION_PATTERN_PHASE_COMPLETE.length);
        assertEquals(0L, AppConstants.VIBRATION_PATTERN_PHASE_COMPLETE[0]);
        assertEquals(100L, AppConstants.VIBRATION_PATTERN_PHASE_COMPLETE[1]);
    }

    @Test
    public void vibrationSessionComplete_doubleVibration() {
        assertEquals(4, AppConstants.VIBRATION_PATTERN_SESSION_COMPLETE.length);
    }

    @Test
    public void animationDuration_is400ms() {
        assertEquals(400L, AppConstants.ANIMATION_DURATION_MS);
    }

    // ── Notification ids ──────────────────────────────────────────────────────

    @Test
    public void notificationIds_areDistinct() {
        assertTrue(AppConstants.NOTIFICATION_ID_TIMER != AppConstants.NOTIFICATION_ID_PHASE_COMPLETE);
    }

    @Test
    public void notificationChannels_areDistinct() {
        assertTrue(!AppConstants.NOTIFICATION_CHANNEL_TIMER.equals(AppConstants.NOTIFICATION_CHANNEL_SOUND));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    @Test
    public void commands_areDistinct() {
        assertTrue(!AppConstants.COMMAND_START.equals(AppConstants.COMMAND_PAUSE));
        assertTrue(!AppConstants.COMMAND_PAUSE.equals(AppConstants.COMMAND_RESUME));
        assertTrue(!AppConstants.COMMAND_RESUME.equals(AppConstants.COMMAND_SKIP));
        assertTrue(!AppConstants.COMMAND_SKIP.equals(AppConstants.COMMAND_RESET));
    }

    @Test
    public void databaseVersion_isPositive() {
        assertTrue(AppConstants.DATABASE_VERSION > 0);
    }
}
