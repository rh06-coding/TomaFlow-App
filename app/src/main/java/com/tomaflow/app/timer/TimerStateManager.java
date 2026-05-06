package com.tomaflow.app.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer.State;
import com.tomaflow.app.timer.PomodoroTimer.Phase;

public class TimerStateManager {
    private static final String PREF_STATE = "timer_state";
    private static final String PREF_PHASE = "timer_phase";
    private static final String PREF_REMAINING_MS = "timer_remaining_ms";
    private static final String PREF_SESSION_COUNT = "timer_session_count";
    private static final String PREF_FOCUS_DURATION = "timer_focus_duration";
    private static final String PREF_BREAK_DURATION = "timer_break_duration";
    private static final String PREF_UPDATED_ELAPSED = "timer_updated_elapsed";

    private final SharedPreferences mPrefs;

    public TimerStateManager(Context context) {
        this.mPrefs = context.getSharedPreferences(AppConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void saveState(PomodoroTimer.TimerState timerState, long focusMs, long breakMs) {
        mPrefs.edit()
                .putString(PREF_STATE, timerState.state.name())
                .putString(PREF_PHASE, timerState.phase.name())
                .putLong(PREF_REMAINING_MS, timerState.remainingMs)
                .putInt(PREF_SESSION_COUNT, timerState.sessionCount)
                .putLong(PREF_FOCUS_DURATION, focusMs)
                .putLong(PREF_BREAK_DURATION, breakMs)
                .putLong(PREF_UPDATED_ELAPSED, timerState.updatedAtElapsed)
                .apply();
    }

    public RestoredState restoreState() {
        String stateStr = mPrefs.getString(PREF_STATE, State.IDLE.name());
        String phaseStr = mPrefs.getString(PREF_PHASE, Phase.FOCUS.name());
        long remainingMs = mPrefs.getLong(PREF_REMAINING_MS, 0);
        int sessionCount = mPrefs.getInt(PREF_SESSION_COUNT, 0);
        long focusDuration = mPrefs.getLong(PREF_FOCUS_DURATION, 25 * 60 * 1000L);
        long breakDuration = mPrefs.getLong(PREF_BREAK_DURATION, 5 * 60 * 1000L);
        long savedElapsed = mPrefs.getLong(PREF_UPDATED_ELAPSED, 0);

        if (savedElapsed == 0) {
            return new RestoredState(State.IDLE, Phase.FOCUS, focusDuration, breakDuration, 0, 0);
        }

        // Calculate drift correction
        long nowElapsed = SystemClock.elapsedRealtime();
        long drift = nowElapsed - savedElapsed;
        long correctedRemaining = remainingMs - drift;

        State state = State.valueOf(stateStr);
        Phase phase = Phase.valueOf(phaseStr);

        if (correctedRemaining <= 0) {
            // Phase expired during offline; transition to next
            return new RestoredState(State.IDLE, phase, focusDuration, breakDuration, sessionCount, 0);
        }

        return new RestoredState(state, phase, focusDuration, breakDuration, sessionCount, correctedRemaining);
    }

    public void clearSavedState() {
        mPrefs.edit()
                .remove(PREF_STATE)
                .remove(PREF_PHASE)
                .remove(PREF_REMAINING_MS)
                .remove(PREF_SESSION_COUNT)
                .remove(PREF_FOCUS_DURATION)
                .remove(PREF_BREAK_DURATION)
                .remove(PREF_UPDATED_ELAPSED)
                .apply();
    }

    public static class RestoredState {
        public final State state;
        public final Phase phase;
        public final long focusDurationMs;
        public final long breakDurationMs;
        public final int sessionCount;
        public final long remainingMs;

        public RestoredState(State state, Phase phase, long focusMs, long breakMs,
                            int sessionCount, long remainingMs) {
            this.state = state;
            this.phase = phase;
            this.focusDurationMs = focusMs;
            this.breakDurationMs = breakMs;
            this.sessionCount = sessionCount;
            this.remainingMs = remainingMs;
        }
    }
}

