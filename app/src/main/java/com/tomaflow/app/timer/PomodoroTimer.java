package com.tomaflow.app.timer;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PomodoroTimer {
    public enum Phase {
        FOCUS("Focus"),
        BREAK("Break");

        private final String displayName;

        Phase(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum State {
        IDLE, RUNNING_FOCUS, PAUSED_FOCUS, RUNNING_BREAK, PAUSED_BREAK, COMPLETED
    }

    private static final long DEFAULT_FOCUS_MS = 25 * 60 * 1000L;
    private static final long DEFAULT_SHORT_BREAK_MS = 5 * 60 * 1000L;
    private static final long DEFAULT_LONG_BREAK_MS = 15 * 60 * 1000L;
    private static final int DEFAULT_CYCLES_BEFORE_LONG = 4;
    private static final long TICK_INTERVAL_MS = 1000L;

    private long mFocusDurationMs = DEFAULT_FOCUS_MS;
    private long mShortBreakDurationMs = DEFAULT_SHORT_BREAK_MS;
    private long mLongBreakDurationMs = DEFAULT_LONG_BREAK_MS;
    private int mCyclesBeforeLongBreak = DEFAULT_CYCLES_BEFORE_LONG;

    private State mState = State.IDLE;
    private Phase mPhase = Phase.FOCUS;
    private long mRemainingMs = DEFAULT_FOCUS_MS;
    private long mStartElapsedMs = 0;
    private int mSessionCount = 0;
    private volatile boolean mIsRunning = false;

    private final MutableLiveData<Long> mTimeRemainingLive = new MutableLiveData<>(DEFAULT_FOCUS_MS);
    private final MutableLiveData<State> mStateLive = new MutableLiveData<>(State.IDLE);
    private final MutableLiveData<Phase> mPhaseLive = new MutableLiveData<>(Phase.FOCUS);
    private final MutableLiveData<Boolean> mIsRunningLive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mProgressLive = new MutableLiveData<>(0);

    public interface OnTimerEventListener {
        void onTick(TimerState state);
        void onStateChanged(TimerState state);
        void onFocusComplete(int sessionCount);
        void onBreakComplete(int sessionCount);
    }

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
        updateStateLive();
    }

    public void setDurations(long focusMs, long shortBreakMs, long longBreakMs) {
        if (focusMs <= 0 || shortBreakMs <= 0 || longBreakMs <= 0) {
            throw new IllegalArgumentException("Durations must be positive");
        }
        this.mFocusDurationMs = focusMs;
        this.mShortBreakDurationMs = shortBreakMs;
        this.mLongBreakDurationMs = longBreakMs;
    }

    public void setCyclesBeforeLongBreak(int cycles) {
        this.mCyclesBeforeLongBreak = cycles;
    }

    public void startFocus(long focusDurationMs) {
        if (focusDurationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK) {
            return;
        }

        mFocusDurationMs = focusDurationMs;
        mState = State.RUNNING_FOCUS;
        mPhase = Phase.FOCUS;
        mRemainingMs = focusDurationMs;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        updateStateLive();
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
        updateStateLive();
    }

    public void resume() {
        if (mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK || mState == State.IDLE) {
            return;
        }

        if (mState == State.PAUSED_FOCUS) {
            mState = State.RUNNING_FOCUS;
            mStartElapsedMs = SystemClock.elapsedRealtime() - (getDurationForPhase(mPhase) - mRemainingMs);
        } else if (mState == State.PAUSED_BREAK) {
            mState = State.RUNNING_BREAK;
            mStartElapsedMs = SystemClock.elapsedRealtime() - (getDurationForPhase(mPhase) - mRemainingMs);
        }
        updateStateLive();
    }

    public void skip() {
        if (mState == State.IDLE || mState == State.COMPLETED) {
            return;
        }

        if (mState == State.RUNNING_FOCUS || mState == State.PAUSED_FOCUS) {
            transitionToBreak();
        } else if (mState == State.RUNNING_BREAK || mState == State.PAUSED_BREAK) {
            transitionToFocus();
        }
    }

    public void reset() {
        mState = State.IDLE;
        mPhase = Phase.FOCUS;
        mRemainingMs = mFocusDurationMs;
        mSessionCount = 0;
        mStartElapsedMs = 0;
        updateStateLive();
    }

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
            updateStateLive();
            if (mEventListener != null) {
                mEventListener.onTick(buildTimerState());
            }
        }
    }

    private void handlePhaseComplete() {
        if (mPhase == Phase.FOCUS) {
            if (mEventListener != null) {
                mEventListener.onFocusComplete(mSessionCount);
            }
            transitionToBreak();
        } else {
            if (mEventListener != null) {
                mEventListener.onBreakComplete(mSessionCount);
            }
            transitionToFocus();
        }
    }

    private void transitionToBreak() {
        long breakDuration = (mSessionCount % mCyclesBeforeLongBreak == 0 && mSessionCount > 0)
                ? mLongBreakDurationMs
                : mShortBreakDurationMs;

        mState = State.RUNNING_BREAK;
        mPhase = Phase.BREAK;
        mRemainingMs = breakDuration;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        updateStateLive();
    }

    private void transitionToFocus() {
        mSessionCount++;

        if (mSessionCount >= mCyclesBeforeLongBreak) {
            mState = State.COMPLETED;
            mSessionCount = 0;
        } else {
            mState = State.RUNNING_FOCUS;
        }

        mPhase = Phase.FOCUS;
        mRemainingMs = mFocusDurationMs;
        mStartElapsedMs = SystemClock.elapsedRealtime();
        updateStateLive();
    }

    private long getDurationForPhase(Phase phase) {
        return (phase == Phase.FOCUS) ? mFocusDurationMs : mShortBreakDurationMs;
    }

    private void updateStateLive() {
        mIsRunningLive.setValue(isRunning());
        mStateLive.setValue(mState);
        mPhaseLive.setValue(mPhase);
        mTimeRemainingLive.setValue(mRemainingMs);

        long totalDuration = getDurationForPhase(mPhase);
        int progress = totalDuration > 0 ? (int) ((totalDuration - mRemainingMs) * 100 / totalDuration) : 0;
        mProgressLive.setValue(Math.min(100, Math.max(0, progress)));

        if (mEventListener != null) {
            mEventListener.onStateChanged(buildTimerState());
        }
    }

    private TimerState buildTimerState() {
        return new TimerState(mState, mPhase, isRunning(), mRemainingMs, mSessionCount,
                SystemClock.elapsedRealtime());
    }

    private boolean isRunning() {
        return mState == State.RUNNING_FOCUS || mState == State.RUNNING_BREAK;
    }

    public LiveData<Long> getTimeRemaining() {
        return mTimeRemainingLive;
    }

    public LiveData<State> getState() {
        return mStateLive;
    }

    public LiveData<Phase> getPhase() {
        return mPhaseLive;
    }

    public LiveData<Boolean> getIsRunning() {
        return mIsRunningLive;
    }

    public LiveData<Integer> getProgress() {
        return mProgressLive;
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

    public long getBreakDurationMs() {
        return mShortBreakDurationMs;
    }

    public void setOnTimerEventListener(OnTimerEventListener listener) {
        this.mEventListener = listener;
    }

    public void removeOnTimerEventListener() {
        this.mEventListener = null;
    }

    public void destroy() {
        mEventListener = null;
    }
}
