package com.tomaflow.app.timer;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * PomodoroTimer — Main Timer Engine
 *
 * Manages the complete Pomodoro timer lifecycle:
 *   - Phases: WORK (25 min) → SHORT_BREAK (5 min) → WORK → LONG_BREAK (15 min)
 *   - State transitions: IDLE → RUNNING → PAUSED → RUNNING → IDLE
 *   - Controls: start, pause, resume, skip, reset
 *   - Observable LiveData for UI reactivity
 *   - Callback system for event notifications
 *
 * Usage:
 *   PomodoroTimer timer = new PomodoroTimer();
 *   timer.setOnTimerEventListener(new OnTimerEventListener() { ... });
 *   timer.start();
 */
public class PomodoroTimer {
    public enum TimerPhase {
        IDLE("Idle"),
        WORK("Deep Work"),
        SHORT_BREAK("Short Break"),
        LONG_BREAK("Long Break");

        private final String displayName;

        TimerPhase(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * TimerCommand — Commands to control the timer
     */
    public enum TimerCommand {
        START, PAUSE, RESUME, SKIP, RESET
    }

    /**
     * TimerState — Internal state machine
     */
    private enum TimerState {
        IDLE, RUNNING, PAUSED
    }

    // Duration Constants (milliseconds) - Default Pomodoro durations

    private static final long DEFAULT_WORK_DURATION_MS = 25 * 60 * 1000L;      // 25 min
    private static final long DEFAULT_SHORT_BREAK_MS = 5 * 60 * 1000L;         // 5 min
    private static final long DEFAULT_LONG_BREAK_MS = 15 * 60 * 1000L;         // 15 min
    private static final int CYCLES_BEFORE_LONG_BREAK = 4;                    // After 4 work sessions
    private static final long COUNTDOWN_INTERVAL = 1000L;                      // 1 second tick

    // Configuration (mutable for settings integration)

    private long mWorkDurationMs = DEFAULT_WORK_DURATION_MS;
    private long mShortBreakDurationMs = DEFAULT_SHORT_BREAK_MS;
    private long mLongBreakDurationMs = DEFAULT_LONG_BREAK_MS;
    private int mCyclesBeforeLongBreak = CYCLES_BEFORE_LONG_BREAK;

    // Internal State

    private TimerState mTimerState = TimerState.IDLE;
    private TimerPhase mCurrentPhase = TimerPhase.IDLE;
    private long mTimeRemainingSavedMs = 0;        // Saved time when paused
    private int mCycleCount = 0;                    // Work cycles completed in current session
    private CountDownTimer mCountDownTimer;

    // LiveData — Observable state for UI binding


    private final MutableLiveData<Long> mTimeRemainingMs = new MutableLiveData<>(mWorkDurationMs);
    private final MutableLiveData<TimerPhase> mPhase = new MutableLiveData<>(mCurrentPhase);
    private final MutableLiveData<Boolean> mIsRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mProgressPercent = new MutableLiveData<>(0);


    // Event Listener


    public interface OnTimerEventListener {
        /** Called when the current phase changes (e.g., WORK → SHORT_BREAK). */
        void onPhaseChanged(TimerPhase newPhase);

        /** Called on each timer tick (~1 second), useful for granular updates. */
        void onTick(long timeRemainingSec);

        /** Called when the current phase completes (time reaches 0). */
        void onPhaseComplete(TimerPhase completedPhase);

        /** Called when an entire Pomodoro session finishes (Work cycle + break). */
        void onSessionFinished();
    }

    private OnTimerEventListener mEventListener;


    // Constructor


    public PomodoroTimer() {
        mCurrentPhase = TimerPhase.IDLE;
        mPhase.setValue(mCurrentPhase);
        mTimeRemainingMs.setValue(mWorkDurationMs);
    }


    // Configuration Methods


    /**
     * Set custom durations for timer phases. Call before starting the timer.
     * @param workMs Work phase duration in milliseconds
     * @param shortBreakMs Short break duration in milliseconds
     * @param longBreakMs Long break duration in milliseconds
     */
    public void setDurations(long workMs, long shortBreakMs, long longBreakMs) {
        this.mWorkDurationMs = workMs;
        this.mShortBreakDurationMs = shortBreakMs;
        this.mLongBreakDurationMs = longBreakMs;
    }

    /**
     * Set the number of work cycles before a long break.
     * @param cycles Number of cycles (typically 4)
     */
    public void setCyclesBeforeLongBreak(int cycles) {
        this.mCyclesBeforeLongBreak = cycles;
    }


    // Control Methods

    public void start() {
        if (mTimerState == TimerState.RUNNING) {
            return; // Already running, ignore
        }

        // Initialize phase if starting from IDLE
        if (mTimerState == TimerState.IDLE) {
            mCurrentPhase = TimerPhase.WORK;
            mPhase.setValue(mCurrentPhase);
            mTimeRemainingSavedMs = mWorkDurationMs;
            if (mEventListener != null) {
                mEventListener.onPhaseChanged(mCurrentPhase);
            }
        }

        mTimerState = TimerState.RUNNING;
        mIsRunning.setValue(true);
        startCountdownTimer();
    }

    /**
     * Pause the timer. Preserves current time.
     * Can be resumed later via resume() or start().
     */
    public void pause() {
        if (mTimerState != TimerState.RUNNING) {
            return; // Not running, ignore
        }

        mTimerState = TimerState.PAUSED;
        mIsRunning.setValue(false);
        cancelCountdownTimer();
    }

    /**
     * Resume the timer from PAUSED state.
     * Continues from where it was paused.
     */
    public void resume() {
        if (mTimerState != TimerState.PAUSED) {
            return; // Not paused, ignore
        }

        mTimerState = TimerState.RUNNING;
        mIsRunning.setValue(true);
        startCountdownTimer();
    }

    /**
     * Skip to the next phase immediately.
     * Current phase ends, transitions to next phase.
     * Useful for manual phase skip or testing.
     */
    public void skip() {
        cancelCountdownTimer();
        transitionToNextPhase();
    }

    /**
     * Reset the timer to initial IDLE state (25 min WORK phase).
     * Clears cycle count, resets time to work duration.
     */
    public void reset() {
        cancelCountdownTimer();
        mTimerState = TimerState.IDLE;
        mIsRunning.setValue(false);

        mCurrentPhase = TimerPhase.IDLE;
        mPhase.setValue(mCurrentPhase);
        mTimeRemainingSavedMs = mWorkDurationMs;
        mTimeRemainingMs.setValue(mWorkDurationMs);
        mProgressPercent.setValue(0);
        mCycleCount = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timer Mechanics
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Start the internal CountDownTimer.
     * Performs ticks every COUNTDOWN_INTERVAL (1 sec) and emits updates.
     */
    private void startCountdownTimer() {
        mCountDownTimer = new CountDownTimer(mTimeRemainingSavedMs, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeRemainingSavedMs = millisUntilFinished;
                mTimeRemainingMs.setValue(millisUntilFinished);

                // Update progress bar (0-100%)
                long totalDuration = getDurationForPhase(mCurrentPhase);
                int progress = (int) ((totalDuration - millisUntilFinished) * 100 / totalDuration);
                mProgressPercent.setValue(progress);

                // Emit tick event
                if (mEventListener != null) {
                    mEventListener.onTick(millisUntilFinished / 1000);
                }
            }

            @Override
            public void onFinish() {
                // Phase completed, transition to next
                if (mEventListener != null) {
                    mEventListener.onPhaseComplete(mCurrentPhase);
                }
                transitionToNextPhase();
            }
        }.start();
    }

    /**
     * Cancel the current countdown timer and prevent memory leaks.
     */
    private void cancelCountdownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    /**
     * Transition to the next phase in the Pomodoro cycle.
     * Handles auto-transitions and cycle counting for long breaks.
     */
    private void transitionToNextPhase() {
        cancelCountdownTimer();

        TimerPhase nextPhase;

        switch (mCurrentPhase) {
            case WORK:
                mCycleCount++;
                // Determine if next is short break or long break
                nextPhase = (mCycleCount % mCyclesBeforeLongBreak == 0)
                    ? TimerPhase.LONG_BREAK
                    : TimerPhase.SHORT_BREAK;
                break;

            case SHORT_BREAK:
                // After short break, return to work
                nextPhase = TimerPhase.WORK;
                break;

            case LONG_BREAK:
                // After long break, reset cycle count and return to work
                mCycleCount = 0;
                nextPhase = TimerPhase.WORK;
                // Emit session finished event
                if (mEventListener != null) {
                    mEventListener.onSessionFinished();
                }
                break;

            default:
                nextPhase = TimerPhase.WORK;
                break;
        }

        // Update to next phase
        mCurrentPhase = nextPhase;
        mPhase.setValue(mCurrentPhase);
        mTimeRemainingSavedMs = getDurationForPhase(nextPhase);
        mTimeRemainingMs.setValue(mTimeRemainingSavedMs);
        mProgressPercent.setValue(0);

        mTimerState = TimerState.IDLE;
        mIsRunning.setValue(false);

        // Emit phase changed event
        if (mEventListener != null) {
            mEventListener.onPhaseChanged(nextPhase);
        }
    }

    /**
     * Get the duration for a given phase in milliseconds.
     * @param phase The timer phase
     * @return Duration in milliseconds
     */
    private long getDurationForPhase(TimerPhase phase) {
        switch (phase) {
            case WORK:
                return mWorkDurationMs;
            case SHORT_BREAK:
                return mShortBreakDurationMs;
            case LONG_BREAK:
                return mLongBreakDurationMs;
            case IDLE:
            default:
                return mWorkDurationMs;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LiveData Accessors (for UI observation)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get LiveData for time remaining in milliseconds.
     * @return Observable time remaining
     */
    public LiveData<Long> getTimeRemaining() {
        return mTimeRemainingMs;
    }

    /**
     * Get LiveData for current timer phase.
     * @return Observable current phase
     */
    public LiveData<TimerPhase> getCurrentPhase() {
        return mPhase;
    }

    /**
     * Get LiveData for running state.
     * @return Observable running state (true = RUNNING, false = IDLE or PAUSED)
     */
    public LiveData<Boolean> getIsRunning() {
        return mIsRunning;
    }

    /**
     * Get LiveData for progress percentage (0-100).
     * @return Observable progress percent
     */
    public LiveData<Integer> getProgressPercent() {
        return mProgressPercent;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // State Accessors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get the current timer state (IDLE, RUNNING, or PAUSED).
     * @return Current state
     */
    public TimerState getState() {
        return mTimerState;
    }

    /**
     * Get the current phase without observing.
     * @return Current phase
     */
    public TimerPhase getPhaseValue() {
        return mCurrentPhase;
    }

    /**
     * Get time remaining without observing (for single queries).
     * @return Time remaining in milliseconds
     */
    public long getTimeRemainingSex() {
        return mTimeRemainingSavedMs;
    }

    /**
     * Get the current cycle count (number of work sessions completed).
     * @return Cycle count
     */
    public int getCycleCount() {
        return mCycleCount;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Event Listener Management
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Set a listener to receive timer events.
     * @param listener The listener to attach, or null to detach
     */
    public void setOnTimerEventListener(OnTimerEventListener listener) {
        this.mEventListener = listener;
    }

    /**
     * Remove the currently attached event listener.
     */
    public void removeOnTimerEventListener() {
        this.mEventListener = null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cleanup
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cleanup and release resources. Call when the component is destroyed.
     */
    public void destroy() {
        cancelCountdownTimer();
        mEventListener = null;
    }
}

