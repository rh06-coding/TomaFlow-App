package com.tomaflow.app.timer;

import android.os.SystemClock;

import com.tomaflow.app.constants.AppConstants;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class PomodoroTimer {

    public enum Phase {
        FOCUS("Focus"),
        SHORT_BREAK("Short Break"),
        LONG_BREAK("Long Break");

        private final String displayName;

        Phase(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

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
    /** Speed multiplier for debug/testing: 1x/2x/4x/8x/16x. */
    private int mSpeedMultiplier = 1;

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
        public final long totalDurationMs;
        public final int sessionCount;
        public final long updatedAtElapsed;

        public TimerState(State state, Phase phase, boolean isRunning, long remainingMs,
                         long totalDurationMs, int sessionCount, long updatedAtElapsed) {
            this.state = state;
            this.phase = phase;
            this.isRunning = isRunning;
            this.remainingMs = remainingMs;
            this.totalDurationMs = totalDurationMs;
            this.sessionCount = sessionCount;
            this.updatedAtElapsed = updatedAtElapsed;
        }
    }

    private final List<OnTimerEventListener> mListeners = new CopyOnWriteArrayList<>();

    public PomodoroTimer() {
        notifyStateChanged();
    }

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
        long elapsedSinceStart = (nowElapsed - mStartElapsedMs) * mSpeedMultiplier;
        long currentDuration = getDurationForPhase(mPhase);
        mRemainingMs = currentDuration - elapsedSinceStart;

        if (mRemainingMs <= 0) {
            mRemainingMs = 0;
            handlePhaseComplete();
        } else {
            notifyStateChanged();
            TimerState state = buildTimerState();
            for (OnTimerEventListener listener : mListeners) {
                listener.onTick(state);
            }
        }
    }

    /**
     * [DEBUG] Set timer speed multiplier. Safe to call at any time.
     * Recalculates mStartElapsedMs so remaining time is preserved at the moment of change.
     *
     * @param multiplier 1 = real time, 2 = 2x, 4 = 4x, 8 = 8x, 16 = 16x
     */
    public void setSpeedMultiplier(int multiplier) {
        if (multiplier < 1) multiplier = 1;
        if (mSpeedMultiplier == multiplier) return;

        if (isRunning()) {
            // Preserve mRemainingMs: adjust virtual start so remaining stays the same
            // virtualElapsed = duration - remaining
            // realElapsed    = virtualElapsed / newMultiplier
            // mStartElapsed  = now - realElapsed
            long nowElapsed = SystemClock.elapsedRealtime();
            long consumed   = getDurationForPhase(mPhase) - mRemainingMs;
            mSpeedMultiplier = multiplier;
            mStartElapsedMs  = nowElapsed - (consumed / mSpeedMultiplier);
        } else {
            mSpeedMultiplier = multiplier;
        }
    }

    public int getSpeedMultiplier() { return mSpeedMultiplier; }

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
        TimerState state = buildTimerState();
        if (mPhase == Phase.FOCUS) {
            for (OnTimerEventListener listener : mListeners) {
                listener.onFocusComplete(mSessionCount + 1);
            }
            transitionToBreak();
        } else {
            for (OnTimerEventListener listener : mListeners) {
                listener.onBreakComplete(mSessionCount);
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

    private long getDurationForPhase(Phase phase) {
        switch (phase) {
            case SHORT_BREAK: return mShortBreakDurationMs;
            case LONG_BREAK: return mLongBreakDurationMs;
            case FOCUS:
            default: return mFocusDurationMs;
        }
    }

    private void notifyStateChanged() {
        TimerState state = buildTimerState();
        for (OnTimerEventListener listener : mListeners) {
            listener.onStateChanged(state);
        }
    }

    private TimerState buildTimerState() {
        return new TimerState(mState, mPhase, isRunning(), mRemainingMs,
                getDurationForPhase(mPhase), mSessionCount, SystemClock.elapsedRealtime());
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

    public void addTimerEventListener(OnTimerEventListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeTimerEventListener(OnTimerEventListener listener) {
        mListeners.remove(listener);
    }

    public void destroy() {
        mListeners.clear();
    }
}
