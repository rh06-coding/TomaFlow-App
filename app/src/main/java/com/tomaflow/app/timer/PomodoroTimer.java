package com.tomaflow.app.timer;

import android.os.SystemClock;

import com.tomaflow.app.constants.AppConstants;


public class PomodoroTimer {

    /** Current phase of the Pomodoro cycle. */
    public enum Phase {
        FOCUS("Focus"),
        SHORT_BREAK("Short Break"),
        LONG_BREAK("Long Break");

        private final String displayName;

        Phase(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    /** Timer state — tracks pause/running/completed status. */
    public enum State {
        IDLE, RUNNING_FOCUS, PAUSED_FOCUS, RUNNING_BREAK, PAUSED_BREAK, COMPLETED
    }

    private static final long DEFAULT_FOCUS_MS = AppConstants.TIMER_WORK_DURATION_MS;
    private static final long DEFAULT_SHORT_BREAK_MS = AppConstants.TIMER_SHORT_BREAK_MS;
    private static final long DEFAULT_LONG_BREAK_MS = AppConstants.TIMER_LONG_BREAK_MS;
    private static final int DEFAULT_CYCLES_BEFORE_LONG = AppConstants.TIMER_CYCLES_BEFORE_LONG_BREAK;

    private long mFocusDurationMs = DEFAULT_FOCUS_MS;
    private long mShortBreakDurationMs = DEFAULT_SHORT_BREAK_MS;
    private long mLongBreakDurationMs = DEFAULT_LONG_BREAK_MS;
    private int mCyclesBeforeLongBreak = DEFAULT_CYCLES_BEFORE_LONG;
    private final int mTargetSessions = AppConstants.TIMER_DEFAULT_TARGET_SESSIONS;

    private State mState = State.IDLE;
    private Phase mPhase = Phase.FOCUS;
    private long mRemainingMs = DEFAULT_FOCUS_MS;
    private long mStartElapsedMs = 0;
    private int mSessionCount = 0;

    /** Callback interface for TimerEngineService to react to timer events. */
    public interface OnTimerEventListener {
        void onTick(TimerState state);
        void onStateChanged(TimerState state);
        void onFocusComplete(int sessionCount);
        void onBreakComplete(int sessionCount);
    }

    /** Immutable snapshot of timer state at a point in time. Serializable via Intent. */
    public static class TimerState {
        public final State state;
        public final Phase phase;
        public final boolean isRunning;
        public final long remainingMs;
        public final int sessionCount;
        public final long updatedAtElapsed;

        public TimerState(State state, Phase phase, boolean isRunning, long remainingMs,
                         int sessionCount, long updatedAtElapsed) {
            this.state = state;
            this.phase = phase;
            this.isRunning = isRunning;
            this.remainingMs = remainingMs;
            this.sessionCount = sessionCount;
            this.updatedAtElapsed = updatedAtElapsed;
        }
    }

    private OnTimerEventListener mEventListener;

    public PomodoroTimer() {
        notifyStateChanged();
    }

    /** Set custom durations for focus, short break, and long break. All must be > 0. */
    public void setDurations(long focusMs, long shortBreakMs, long longBreakMs) {
        if (focusMs <= 0 || shortBreakMs <= 0 || longBreakMs <= 0) {
            throw new IllegalArgumentException("Durations must be positive");
        }
        this.mFocusDurationMs = focusMs;
        this.mShortBreakDurationMs = shortBreakMs;
        this.mLongBreakDurationMs = longBreakMs;
        if (mState == State.IDLE) {
            mRemainingMs = mFocusDurationMs;
            notifyStateChanged();
        }
    }

    /** Number of focus+break cycles before a long break kicks in. */
    public void setCyclesBeforeLongBreak(int cycles) {
        this.mCyclesBeforeLongBreak = cycles;
    }

    /** Start a focus session. No-op if already running. */
    public void startFocus(long focusDurationMs) {
        if (focusDurationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        // Prevent restart mid-session
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK) {
            return;
        }

        mFocusDurationMs = focusDurationMs;
        mState = State.RUNNING_FOCUS;
        mPhase = Phase.FOCUS;
        mRemainingMs = focusDurationMs;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    /** Pause the timer. No-op if already paused or idle. */
    public void pause() {
        if (mState == State.PAUSED_FOCUS || mState == State.PAUSED_BREAK || mState == State.IDLE) {
            return;
        }

        if (mState == State.RUNNING_FOCUS) {
            mState = State.PAUSED_FOCUS;
        } else if (mState == State.RUNNING_BREAK) {
            mState = State.PAUSED_BREAK;
        }
        notifyStateChanged();
    }

    /**
     * Resume after pause. Recalculates mStartElapsedMs so remaining time
     * doesn't jump: elapsed = duration - remaining.
     */
    public void resume() {
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK || mState == State.IDLE) {
            return;
        }

        if (mState == State.PAUSED_FOCUS) {
            mState = State.RUNNING_FOCUS;
        } else if (mState == State.PAUSED_BREAK) {
            mState = State.RUNNING_BREAK;
        }
        mStartElapsedMs = SystemClock.elapsedRealtime() - (getDurationForPhase(mPhase) - mRemainingMs);
        notifyStateChanged();
    }

    /** Skip the current phase: focus->break, break->focus (increments session count). */
    public void skip() {
        if (mState == State.IDLE || mState == State.COMPLETED) {
            return;
        }

        if (mPhase == Phase.FOCUS) {
            transitionToBreak();
        } else {
            // Skipping a break counts as finishing it
            transitionToFocus();
        }
    }

    /** Reset to IDLE. Clears session count and remaining time. */
    public void reset() {
        mState = State.IDLE;
        mPhase = Phase.FOCUS;
        mRemainingMs = mFocusDurationMs;
        mSessionCount = 0;
        mStartElapsedMs = 0;
        notifyStateChanged();
    }

    /**
     * Called every second from TimerEngineService.
     * Calculates elapsed time via SystemClock (immune to user changing device clock).
     * If remaining <= 0, triggers phase completion.
     */
    public void tick() {
        if (!isRunning() || mState == State.IDLE || mState == State.COMPLETED) {
            return;
        }

        long nowElapsed = SystemClock.elapsedRealtime();
        long elapsedSinceStart = nowElapsed - mStartElapsedMs;
        long currentDuration = getDurationForPhase(mPhase);
        mRemainingMs = currentDuration - elapsedSinceStart;

        if (mRemainingMs <= 0) {
            mRemainingMs = 0;
            handlePhaseComplete();
        } else {
            notifyStateChanged();
            if (mEventListener != null) {
                mEventListener.onTick(buildTimerState());
            }
        }
    }

    public void restoreFromState(TimerState state) {
        this.mState = state.state;
        this.mPhase = state.phase;
        this.mSessionCount = state.sessionCount;
        this.mRemainingMs = state.remainingMs;

        if (state.isRunning) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long timePassedSinceSave = nowElapsed - state.updatedAtElapsed;
            mRemainingMs -= timePassedSinceSave;

            if (mRemainingMs <= 0) {
                mRemainingMs = 0;
                handlePhaseComplete();
            } else {
                mStartElapsedMs = nowElapsed - (getDurationForPhase(mPhase) - mRemainingMs);
                notifyStateChanged();
            }
        } else {
            mStartElapsedMs = 0;
            notifyStateChanged();
        }
    }


    private void handlePhaseComplete() {
        if (mPhase == Phase.FOCUS) {
            if (mEventListener != null) {
                mEventListener.onFocusComplete(mSessionCount + 1);
            }
            transitionToBreak();
        } else {
            if (mEventListener != null) {
                mEventListener.onBreakComplete(mSessionCount);
            }
            transitionToFocus();
        }
    }

    /**
     * Transition to break. Picks long break if sessionCount is a multiple of
     * mCyclesBeforeLongBreak (and > 0), otherwise short break.
     */
    private void transitionToBreak() {
        mSessionCount++;
        boolean isLongBreak = (mSessionCount % mCyclesBeforeLongBreak == 0);
        
        mPhase = isLongBreak ? Phase.LONG_BREAK : Phase.SHORT_BREAK;
        long breakDuration = isLongBreak ? mLongBreakDurationMs : mShortBreakDurationMs;

        mState = State.RUNNING_BREAK;
        mRemainingMs = breakDuration;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    /** Transition to focus. Increments session count; enters COMPLETED if all cycles done. */
    private void transitionToFocus() {
        if (mSessionCount >= mTargetSessions) {
            mState = State.COMPLETED;
            mPhase = Phase.FOCUS;
            mRemainingMs = 0;
            mStartElapsedMs = 0;
            notifyStateChanged();
            return;
        }
        
        mState = State.RUNNING_FOCUS;
        mPhase = Phase.FOCUS;
        mRemainingMs = mFocusDurationMs;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    /** Returns the configured duration for the given phase. */
    private long getDurationForPhase(Phase phase) {
        switch (phase) {
            case SHORT_BREAK: return mShortBreakDurationMs;
            case LONG_BREAK: return mLongBreakDurationMs;
            case FOCUS:
            default: return mFocusDurationMs;
        }
    }

    /** Push current state to listener. */
    private void notifyStateChanged() {
        if (mEventListener != null) {
            mEventListener.onStateChanged(buildTimerState());
        }
    }

    private TimerState buildTimerState() {
        return new TimerState(mState, mPhase, isRunning(), mRemainingMs, mSessionCount,
                SystemClock.elapsedRealtime());
    }

    public boolean isRunning() {
        return mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK;
    }

    public State getStateValue() {
        return mState;
    }

    public Phase getPhaseValue() {
        return mPhase;
    }

    public long getRemainingMs() {
        return mRemainingMs;
    }

    public int getSessionCount() {
        return mSessionCount;
    }

    public long getFocusDurationMs() {
        return mFocusDurationMs;
    }

    public long getShortBreakDurationMs() {
        return mShortBreakDurationMs;
    }

    public long getLongBreakDurationMs() {
        return mLongBreakDurationMs;
    }

    public int getCyclesBeforeLongBreak() {
        return mCyclesBeforeLongBreak;
    }

    public void setOnTimerEventListener(OnTimerEventListener listener) {
        this.mEventListener = listener;
    }

    public void destroy() {
        mEventListener = null;
    }
}
