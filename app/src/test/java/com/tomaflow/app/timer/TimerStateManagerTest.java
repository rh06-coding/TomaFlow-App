package com.tomaflow.app.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.constants.AppConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TimerStateManagerTest {

    private TimerStateManager mManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        // Clear any leftover prefs from previous tests
        context.getSharedPreferences(AppConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
        mManager = new TimerStateManager(context);
    }

    // ── restoreState defaults ─────────────────────────────────────────────────

    @Test
    public void restoreState_withNothingSaved_returnsDefaults() {
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.IDLE, restored.state);
        assertEquals(PomodoroTimer.Phase.FOCUS, restored.phase);
        assertEquals(AppConstants.TIMER_WORK_DURATION_MS, restored.focusDurationMs);
        assertEquals(AppConstants.TIMER_SHORT_BREAK_MS, restored.shortBreakDurationMs);
        assertEquals(AppConstants.TIMER_LONG_BREAK_MS, restored.longBreakDurationMs);
        assertEquals(0, restored.sessionCount);
        assertEquals(AppConstants.TIMER_WORK_DURATION_MS, restored.remainingMs);
        assertEquals(0, restored.updatedAtElapsed);
        assertFalse(restored.isRunning);
    }

    // ── saveState + restoreState round-trip ───────────────────────────────────

    @Test
    public void saveAndRestore_runningFocus() {
        PomodoroTimer.TimerState state = new PomodoroTimer.TimerState(
                PomodoroTimer.State.RUNNING_FOCUS,
                PomodoroTimer.Phase.FOCUS,
                true,
                1200000,
                1500000,
                1,
                50000
        );

        mManager.saveState(state, 1500000, 300000, 900000);
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, restored.state);
        assertEquals(PomodoroTimer.Phase.FOCUS, restored.phase);
        assertEquals(1500000, restored.focusDurationMs);
        assertEquals(300000, restored.shortBreakDurationMs);
        assertEquals(900000, restored.longBreakDurationMs);
        assertEquals(1, restored.sessionCount);
        assertEquals(1200000, restored.remainingMs);
        assertEquals(50000, restored.updatedAtElapsed);
        assertTrue(restored.isRunning);
    }

    @Test
    public void saveAndRestore_pausedBreak() {
        PomodoroTimer.TimerState state = new PomodoroTimer.TimerState(
                PomodoroTimer.State.PAUSED_BREAK,
                PomodoroTimer.Phase.SHORT_BREAK,
                false,
                180000,
                300000,
                3,
                120000
        );

        mManager.saveState(state, 1500000, 300000, 900000);
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.PAUSED_BREAK, restored.state);
        assertEquals(PomodoroTimer.Phase.SHORT_BREAK, restored.phase);
        assertEquals(3, restored.sessionCount);
        assertEquals(180000, restored.remainingMs);
        assertEquals(120000, restored.updatedAtElapsed);
        assertFalse(restored.isRunning);
    }

    @Test
    public void saveAndRestore_longBreak() {
        PomodoroTimer.TimerState state = new PomodoroTimer.TimerState(
                PomodoroTimer.State.RUNNING_BREAK,
                PomodoroTimer.Phase.LONG_BREAK,
                true,
                600000,
                900000,
                4,
                200000
        );

        mManager.saveState(state, 1500000, 300000, 900000);
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.RUNNING_BREAK, restored.state);
        assertEquals(PomodoroTimer.Phase.LONG_BREAK, restored.phase);
        assertEquals(4, restored.sessionCount);
        assertEquals(600000, restored.remainingMs);
        assertTrue(restored.isRunning);
    }

    // ── clearSavedState ───────────────────────────────────────────────────────

    @Test
    public void clearSavedState_resetsToDefaults() {
        PomodoroTimer.TimerState state = new PomodoroTimer.TimerState(
                PomodoroTimer.State.RUNNING_FOCUS,
                PomodoroTimer.Phase.FOCUS,
                true,
                800000,
                1500000,
                2,
                30000
        );

        mManager.saveState(state, 1500000, 300000, 900000);
        mManager.clearSavedState();
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.IDLE, restored.state);
        assertEquals(PomodoroTimer.Phase.FOCUS, restored.phase);
        assertEquals(0, restored.sessionCount);
        assertFalse(restored.isRunning);
    }

    // ── RestoredState.toTimerState ────────────────────────────────────────────

    @Test
    public void toTimerState_convertsCorrectly() {
        PomodoroTimer.TimerState original = new PomodoroTimer.TimerState(
                PomodoroTimer.State.RUNNING_FOCUS,
                PomodoroTimer.Phase.FOCUS,
                true,
                900000,
                1500000,
                2,
                75000
        );

        mManager.saveState(original, 1500000, 300000, 900000);
        TimerStateManager.RestoredState restored = mManager.restoreState();
        PomodoroTimer.TimerState converted = restored.toTimerState();

        assertEquals(original.state, converted.state);
        assertEquals(original.phase, converted.phase);
        assertEquals(original.isRunning, converted.isRunning);
        assertEquals(original.remainingMs, converted.remainingMs);
        assertEquals(original.sessionCount, converted.sessionCount);
        assertEquals(original.updatedAtElapsed, converted.updatedAtElapsed);
    }

    // ── Multiple save/restore cycles ──────────────────────────────────────────

    @Test
    public void saveOverwritesPreviousState() {
        PomodoroTimer.TimerState first = new PomodoroTimer.TimerState(
                PomodoroTimer.State.RUNNING_FOCUS,
                PomodoroTimer.Phase.FOCUS,
                true,
                1000000,
                1500000,
                1,
                10000
        );
        PomodoroTimer.TimerState second = new PomodoroTimer.TimerState(
                PomodoroTimer.State.PAUSED_BREAK,
                PomodoroTimer.Phase.LONG_BREAK,
                false,
                500000,
                900000,
                4,
                99000
        );

        mManager.saveState(first, 1500000, 300000, 900000);
        mManager.saveState(second, 1500000, 300000, 900000);
        TimerStateManager.RestoredState restored = mManager.restoreState();

        assertEquals(PomodoroTimer.State.PAUSED_BREAK, restored.state);
        assertEquals(PomodoroTimer.Phase.LONG_BREAK, restored.phase);
        assertEquals(4, restored.sessionCount);
        assertEquals(500000, restored.remainingMs);
    }
}
