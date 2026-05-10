package com.tomaflow.app.timer;

import android.content.Context;
import android.content.SharedPreferences;

import com.tomaflow.app.constants.AppConstants;

public class TimerStateManager {
    private static final String PREF_STATE = "timer_state";
    private static final String PREF_PHASE = "timer_phase";
    private static final String PREF_REMAINING_MS = "timer_remaining_ms";
    private static final String PREF_SESSION_COUNT = "timer_session_count";
    private static final String PREF_FOCUS_DURATION = "timer_focus_duration";
    private static final String PREF_SHORT_BREAK_DURATION = "timer_short_break_duration";
    private static final String PREF_LONG_BREAK_DURATION = "timer_long_break_duration";
    private static final String PREF_UPDATED_AT_ELAPSED = "timer_updated_at_elapsed";
    private static final String PREF_IS_RUNNING = "timer_is_running";

    private final SharedPreferences mPrefs;

    public TimerStateManager(Context context) {
        this.mPrefs = context.getSharedPreferences(AppConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void saveState(PomodoroTimer.TimerState state, long focusDuration, long shortBreakDuration, long longBreakDuration) {
        mPrefs.edit()
                .putString(PREF_STATE, state.state.name())
                .putString(PREF_PHASE, state.phase.name())
                .putLong(PREF_REMAINING_MS, state.remainingMs)
                .putInt(PREF_SESSION_COUNT, state.sessionCount)
                .putLong(PREF_FOCUS_DURATION, focusDuration)
                .putLong(PREF_SHORT_BREAK_DURATION, shortBreakDuration)
                .putLong(PREF_LONG_BREAK_DURATION, longBreakDuration)
                .putLong(PREF_UPDATED_AT_ELAPSED, state.updatedAtElapsed)
                .putBoolean(PREF_IS_RUNNING, state.isRunning)
                .apply();
    }

    public RestoredState restoreState() {
        String stateStr = mPrefs.getString(PREF_STATE, PomodoroTimer.State.IDLE.name());
        String phaseStr = mPrefs.getString(PREF_PHASE, PomodoroTimer.Phase.FOCUS.name());
        
        return new RestoredState(
                PomodoroTimer.State.valueOf(stateStr),
                PomodoroTimer.Phase.valueOf(phaseStr),
                mPrefs.getLong(PREF_FOCUS_DURATION, AppConstants.TIMER_WORK_DURATION_MS),
                mPrefs.getLong(PREF_SHORT_BREAK_DURATION, AppConstants.TIMER_SHORT_BREAK_MS),
                mPrefs.getLong(PREF_LONG_BREAK_DURATION, AppConstants.TIMER_LONG_BREAK_MS),
                mPrefs.getInt(PREF_SESSION_COUNT, 0),
                mPrefs.getLong(PREF_REMAINING_MS, AppConstants.TIMER_WORK_DURATION_MS),
                mPrefs.getLong(PREF_UPDATED_AT_ELAPSED, 0),
                mPrefs.getBoolean(PREF_IS_RUNNING, false)
        );
    }

    public void clearSavedState() {
        mPrefs.edit()
                .remove(PREF_STATE)
                .remove(PREF_PHASE)
                .remove(PREF_REMAINING_MS)
                .remove(PREF_SESSION_COUNT)
                .remove(PREF_UPDATED_AT_ELAPSED)
                .remove(PREF_IS_RUNNING)
                .apply();
    }

    public static class RestoredState {
        public final PomodoroTimer.State state;
        public final PomodoroTimer.Phase phase;
        public final long focusDurationMs;
        public final long shortBreakDurationMs;
        public final long longBreakDurationMs;
        public final int sessionCount;
        public final long remainingMs;
        public final long updatedAtElapsed;
        public final boolean isRunning;

        public RestoredState(PomodoroTimer.State state, PomodoroTimer.Phase phase, 
                            long focusDurationMs, long shortBreakDurationMs, long longBreakDurationMs,
                            int sessionCount, long remainingMs, long updatedAtElapsed, boolean isRunning) {
            this.state = state;
            this.phase = phase;
            this.focusDurationMs = focusDurationMs;
            this.shortBreakDurationMs = shortBreakDurationMs;
            this.longBreakDurationMs = longBreakDurationMs;
            this.sessionCount = sessionCount;
            this.remainingMs = remainingMs;
            this.updatedAtElapsed = updatedAtElapsed;
            this.isRunning = isRunning;
        }

        public PomodoroTimer.TimerState toTimerState() {
            return new PomodoroTimer.TimerState(state, phase, isRunning, remainingMs, sessionCount, updatedAtElapsed);
        }
    }
}
