package com.tomaflow.app.timer;

import android.os.SystemClock;

import com.tomaflow.app.constants.AppConstants;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bộ đếm thời gian (State machine) cốt lõi của Pomodoro.
 *
 * LUỒNG XỬ LÝ (THREADING): Tất cả các phương thức đều được gọi từ một HandlerThread riêng biệt của
 * TimerEngineService ("TomaFlow-TimerThread"). Vì vậy, các callback của listener cũng được kích hoạt
 * trên luồng đó. TimerEngineService có trách nhiệm đẩy các callback UI lên luồng chính (Main Thread).
 *
 * KHÔNG sử dụng hệ số nhân tốc độ, KHÔNG phụ thuộc vào luồng chính, KHÔNG có AudioFocus tại đây.
 */
public class PomodoroTimer {

    // ── Enums ─────────────────────────────────────────────────────────────────

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

    // ── Defaults ─────────────────────────────────────────────────────────────

    private static final long DEFAULT_FOCUS_MS        = AppConstants.TIMER_WORK_DURATION_MS;
    private static final long DEFAULT_SHORT_BREAK_MS  = AppConstants.TIMER_SHORT_BREAK_MS;
    private static final long DEFAULT_LONG_BREAK_MS   = AppConstants.TIMER_LONG_BREAK_MS;
    private static final int  DEFAULT_CYCLES_BEFORE_LONG = AppConstants.TIMER_CYCLES_BEFORE_LONG_BREAK;

    // ── Mutable state ─────────────────────────────────────────────────────────

    private long mFocusDurationMs      = DEFAULT_FOCUS_MS;
    private long mShortBreakDurationMs = DEFAULT_SHORT_BREAK_MS;
    private long mLongBreakDurationMs  = DEFAULT_LONG_BREAK_MS;
    private int  mCyclesBeforeLongBreak = DEFAULT_CYCLES_BEFORE_LONG;
    private final int mTargetSessions  = AppConstants.TIMER_DEFAULT_TARGET_SESSIONS;

    private State mState          = State.IDLE;
    private Phase mPhase          = Phase.FOCUS;
    private long  mRemainingMs    = DEFAULT_FOCUS_MS;
    private long  mStartElapsedMs = 0;
    private int   mSessionCount   = 0;

    // ── Listener interface ────────────────────────────────────────────────────

    public interface OnTimerEventListener {
        /** Fired once per second while running. Use for UI countdown display. */
        void onTick(TimerState state);
        /** Fired whenever state changes (start, pause, resume, reset, phase transition). */
        void onStateChanged(TimerState state);
        /** Fired when a focus phase completes. */
        void onFocusComplete(int sessionCount);
        /** Fired when a break phase completes. */
        void onBreakComplete(int sessionCount);
    }

    // ── Immutable snapshot ────────────────────────────────────────────────────

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
            this.state            = state;
            this.phase            = phase;
            this.isRunning        = isRunning;
            this.remainingMs      = remainingMs;
            this.totalDurationMs  = totalDurationMs;
            this.sessionCount     = sessionCount;
            this.updatedAtElapsed = updatedAtElapsed;
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    /** CopyOnWriteArrayList is thread-safe for iteration while another thread modifies it. */
    private final List<OnTimerEventListener> mListeners = new CopyOnWriteArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public PomodoroTimer() {}

    // ── Configuration ─────────────────────────────────────────────────────────

    public void setDurations(long focusMs, long shortBreakMs, long longBreakMs) {
        if (focusMs <= 0 || shortBreakMs <= 0 || longBreakMs <= 0) {
            throw new IllegalArgumentException("Durations must be positive");
        }
        mFocusDurationMs      = focusMs;
        mShortBreakDurationMs = shortBreakMs;
        mLongBreakDurationMs  = longBreakMs;
        if (mState == State.IDLE) {
            mRemainingMs = mFocusDurationMs;
            notifyStateChanged();
        }
    }

    public void setCyclesBeforeLongBreak(int cycles) {
        mCyclesBeforeLongBreak = cycles;
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    public void startFocus(long focusDurationMs) {
        if (focusDurationMs <= 0) throw new IllegalArgumentException("Duration must be positive");
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK) return;

        mFocusDurationMs = focusDurationMs;
        mState           = State.RUNNING_FOCUS;
        mPhase           = Phase.FOCUS;
        mRemainingMs     = focusDurationMs;
        mStartElapsedMs  = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    public void pause() {
        if (mState == State.PAUSED_FOCUS || mState == State.PAUSED_BREAK || mState == State.IDLE) return;

        if (mState == State.RUNNING_FOCUS)  mState = State.PAUSED_FOCUS;
        else if (mState == State.RUNNING_BREAK) mState = State.PAUSED_BREAK;
        notifyStateChanged();
    }

    /**
     * Resume after pause. Re-anchors mStartElapsedMs so the remaining time is preserved exactly.
     * elapsed = duration - remaining  →  newStart = now - elapsed
     */
    public void resume() {
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK || mState == State.IDLE) return;

        if (mState == State.PAUSED_FOCUS)       mState = State.RUNNING_FOCUS;
        else if (mState == State.PAUSED_BREAK)  mState = State.RUNNING_BREAK;

        // Re-anchor: how much time was already consumed before pause?
        long consumed   = getDurationForPhase(mPhase) - mRemainingMs;
        mStartElapsedMs = SystemClock.elapsedRealtime() - consumed;

        notifyStateChanged();
    }

    public void skip() {
        if (mState == State.IDLE || mState == State.COMPLETED) return;

        if (mPhase == Phase.FOCUS) transitionToBreak();
        else                       transitionToFocus();
    }

    public void reset() {
        mState          = State.IDLE;
        mPhase          = Phase.FOCUS;
        mRemainingMs    = mFocusDurationMs;
        mSessionCount   = 0;
        mStartElapsedMs = 0;
        notifyStateChanged();
    }

    // ── Tick (called once per second from TimerEngineService's HandlerThread) ─

    /**
     * Advances the timer by recalculating remaining time from the wall-clock anchor.
     * This approach is immune to Handler timing drift — if a tick fires late,
     * the next tick still computes the correct remaining time.
     *
     * IMPORTANT: Only fires onTick(), never onStateChanged().
     * State changes (phase transitions) fire their own onStateChanged via the transition methods.
     */
    public void tick() {
        if (!isRunning()) return;

        long nowElapsed       = SystemClock.elapsedRealtime();
        long elapsedSinceStart = nowElapsed - mStartElapsedMs;
        long currentDuration   = getDurationForPhase(mPhase);
        mRemainingMs           = currentDuration - elapsedSinceStart;

        if (mRemainingMs <= 0) {
            mRemainingMs = 0;
            handlePhaseComplete();
        } else {
            // Fire onTick ONLY — not onStateChanged (which would confuse service scheduling)
            TimerState state = buildTimerState();
            for (OnTimerEventListener listener : mListeners) {
                listener.onTick(state);
            }
        }
    }

    // ── Restore from persisted state ──────────────────────────────────────────

    public void restoreFromState(TimerState state) {
        mState        = state.state;
        mPhase        = state.phase;
        mSessionCount = state.sessionCount;
        mRemainingMs  = state.remainingMs;

        if (state.isRunning) {
            // Drift-correct: subtract time elapsed since the state was saved
            long nowElapsed          = SystemClock.elapsedRealtime();
            long timePassedSinceSave = nowElapsed - state.updatedAtElapsed;
            mRemainingMs            -= timePassedSinceSave;

            if (mRemainingMs <= 0) {
                mRemainingMs = 0;
                handlePhaseComplete();
            } else {
                long consumed   = getDurationForPhase(mPhase) - mRemainingMs;
                mStartElapsedMs = nowElapsed - consumed;
                notifyStateChanged();
            }
        } else {
            mStartElapsedMs = 0;
            notifyStateChanged();
        }
    }

    // ── Internal transitions ──────────────────────────────────────────────────

    private void handlePhaseComplete() {
        if (mPhase == Phase.FOCUS) {
            // Notify before transition so listeners know which phase completed
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

    private void transitionToBreak() {
        mSessionCount++;
        boolean isLongBreak = (mSessionCount % mCyclesBeforeLongBreak == 0);

        mPhase           = isLongBreak ? Phase.LONG_BREAK : Phase.SHORT_BREAK;
        long breakDuration = isLongBreak ? mLongBreakDurationMs : mShortBreakDurationMs;
        mState           = State.RUNNING_BREAK;
        mRemainingMs     = breakDuration;
        mStartElapsedMs  = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    private void transitionToFocus() {
        if (mSessionCount >= mTargetSessions) {
            mState          = State.COMPLETED;
            mPhase          = Phase.FOCUS;
            mRemainingMs    = 0;
            mStartElapsedMs = 0;
            notifyStateChanged();
            return;
        }

        mState          = State.RUNNING_FOCUS;
        mPhase          = Phase.FOCUS;
        mRemainingMs    = mFocusDurationMs;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        notifyStateChanged();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long getDurationForPhase(Phase phase) {
        switch (phase) {
            case SHORT_BREAK: return mShortBreakDurationMs;
            case LONG_BREAK:  return mLongBreakDurationMs;
            case FOCUS:
            default:          return mFocusDurationMs;
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

    /** Public snapshot — safe to call from any thread. */
    public TimerState buildPublicState() {
        return buildTimerState();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean isRunning() {
        return mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK;
    }

    public State getStateValue()    { return mState; }
    public Phase getPhaseValue()    { return mPhase; }
    public long  getRemainingMs()   { return mRemainingMs; }
    public int   getSessionCount()  { return mSessionCount; }

    public long getFocusDurationMs()      { return mFocusDurationMs; }
    public long getShortBreakDurationMs() { return mShortBreakDurationMs; }
    public long getLongBreakDurationMs()  { return mLongBreakDurationMs; }
    public int  getCyclesBeforeLongBreak(){ return mCyclesBeforeLongBreak; }

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
