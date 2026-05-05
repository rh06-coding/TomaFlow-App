package com.tomaflow.app.timer;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    public enum TimerCommand {
        START, PAUSE, RESUME, SKIP, RESET
    }

    private enum TimerState {
        IDLE, RUNNING, PAUSED
    }

    private static final long DEFAULT_WORK_DURATION_MS = 25 * 60 * 1000L;
    private static final long DEFAULT_SHORT_BREAK_MS = 5 * 60 * 1000L;
    private static final long DEFAULT_LONG_BREAK_MS = 15 * 60 * 1000L;
    private static final int CYCLES_BEFORE_LONG_BREAK = 4;
    private static final long COUNTDOWN_INTERVAL = 1000L;

    private long mWorkDurationMs = DEFAULT_WORK_DURATION_MS;
    private long mShortBreakDurationMs = DEFAULT_SHORT_BREAK_MS;
    private long mLongBreakDurationMs = DEFAULT_LONG_BREAK_MS;
    private int mCyclesBeforeLongBreak = CYCLES_BEFORE_LONG_BREAK;

    private TimerState mTimerState = TimerState.IDLE;
    private TimerPhase mCurrentPhase = TimerPhase.IDLE;
    private long mTimeRemainingSavedMs = 0;
    private int mCycleCount = 0;
    private CountDownTimer mCountDownTimer;

    private final MutableLiveData<Long> mTimeRemainingMs = new MutableLiveData<>(mWorkDurationMs);
    private final MutableLiveData<TimerPhase> mPhase = new MutableLiveData<>(mCurrentPhase);
    private final MutableLiveData<Boolean> mIsRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mProgressPercent = new MutableLiveData<>(0);

    public interface OnTimerEventListener {
        void onPhaseChanged(TimerPhase newPhase);
        void onTick(long timeRemainingSec);
        void onPhaseComplete(TimerPhase completedPhase);
        void onSessionFinished();
    }

    private OnTimerEventListener mEventListener;

    public PomodoroTimer() {
        mCurrentPhase = TimerPhase.IDLE;
        mPhase.setValue(mCurrentPhase);
        mTimeRemainingMs.setValue(mWorkDurationMs);
    }

    public void setDurations(long workMs, long shortBreakMs, long longBreakMs) {
        this.mWorkDurationMs = workMs;
        this.mShortBreakDurationMs = shortBreakMs;
        this.mLongBreakDurationMs = longBreakMs;
    }

    public void setCyclesBeforeLongBreak(int cycles) {
        this.mCyclesBeforeLongBreak = cycles;
    }

    public void start() {
        if (mTimerState == TimerState.RUNNING) {
            return;
        }

        // Start a new run from WORK when currently idle.
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

    public void pause() {
        if (mTimerState != TimerState.RUNNING) {
            return;
        }

        mTimerState = TimerState.PAUSED;
        mIsRunning.setValue(false);
        cancelCountdownTimer();
    }

    public void resume() {
        if (mTimerState != TimerState.PAUSED) {
            return;
        }

        mTimerState = TimerState.RUNNING;
        mIsRunning.setValue(true);
        startCountdownTimer();
    }

    public void skip() {
        cancelCountdownTimer();
        transitionToNextPhase();
    }

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

    private void startCountdownTimer() {
        mCountDownTimer = new CountDownTimer(mTimeRemainingSavedMs, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeRemainingSavedMs = millisUntilFinished;
                mTimeRemainingMs.setValue(millisUntilFinished);

                long totalDuration = getDurationForPhase(mCurrentPhase);
                int progress = (int) ((totalDuration - millisUntilFinished) * 100 / totalDuration);
                mProgressPercent.setValue(progress);

                if (mEventListener != null) {
                    mEventListener.onTick(millisUntilFinished / 1000);
                }
            }

            @Override
            public void onFinish() {
                if (mEventListener != null) {
                    mEventListener.onPhaseComplete(mCurrentPhase);
                }
                transitionToNextPhase();
            }
        }.start();
    }

    private void cancelCountdownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void transitionToNextPhase() {
        cancelCountdownTimer();

        TimerPhase nextPhase;

        switch (mCurrentPhase) {
            case WORK:
                mCycleCount++;
                nextPhase = (mCycleCount % mCyclesBeforeLongBreak == 0)
                    ? TimerPhase.LONG_BREAK
                    : TimerPhase.SHORT_BREAK;
                break;

            case SHORT_BREAK:
                nextPhase = TimerPhase.WORK;
                break;

            case LONG_BREAK:
                mCycleCount = 0;
                nextPhase = TimerPhase.WORK;
                // A full Pomodoro session ends after LONG_BREAK.
                if (mEventListener != null) {
                    mEventListener.onSessionFinished();
                }
                break;

            default:
                nextPhase = TimerPhase.WORK;
                break;
        }

        mCurrentPhase = nextPhase;
        mPhase.setValue(mCurrentPhase);
        mTimeRemainingSavedMs = getDurationForPhase(nextPhase);
        mTimeRemainingMs.setValue(mTimeRemainingSavedMs);
        mProgressPercent.setValue(0);

        mTimerState = TimerState.IDLE;
        mIsRunning.setValue(false);

        // Prepare next phase only; caller decides when to start.
        if (mEventListener != null) {
            mEventListener.onPhaseChanged(nextPhase);
        }
    }

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

    public LiveData<Long> getTimeRemaining() {
        return mTimeRemainingMs;
    }

    public LiveData<TimerPhase> getCurrentPhase() {
        return mPhase;
    }

    public LiveData<Boolean> getIsRunning() {
        return mIsRunning;
    }

    public LiveData<Integer> getProgressPercent() {
        return mProgressPercent;
    }

    public TimerState getState() {
        return mTimerState;
    }

    public TimerPhase getPhaseValue() {
        return mCurrentPhase;
    }

    public long getTimeRemainingSex() {
        return mTimeRemainingSavedMs;
    }

    public int getCycleCount() {
        return mCycleCount;
    }

    public void setOnTimerEventListener(OnTimerEventListener listener) {
        this.mEventListener = listener;
    }

    public void removeOnTimerEventListener() {
        this.mEventListener = null;
    }

    public void destroy() {
        cancelCountdownTimer();
        mEventListener = null;
    }
}

