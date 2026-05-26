package com.tomaflow.app.ui.timer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.data.repository.SessionRepository;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.timer.TimerEngineService;

/**
 * MVVM ViewModel bridging TimerEngineService and MainActivity.
 *
 * Binds to TimerEngineService, implements OnTimerEventListener,
 * and exposes timer state as LiveData for the UI.
 *
 * Flow:
 *   Service --[listener]--> ViewModel --[LiveData]--> MainActivity
 *   MainActivity --[sendCommand]--> ViewModel --[Intent]--> Service
 */
public class TimerViewModel extends AndroidViewModel implements PomodoroTimer.OnTimerEventListener {

    private final MutableLiveData<PomodoroTimer.TimerState> mTimerState = new MutableLiveData<>();

    private final SessionRepository mSessionRepository;

    private TimerEngineService mService;
    private boolean mBound = false;

    // Current focus session data
    private long mFocusStartTime = 0L;
    private Integer mCurrentTaskId = null;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerEngineService.TimerBinder binder = (TimerEngineService.TimerBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.getTimer().addTimerEventListener(TimerViewModel.this);

            // Instantly update LiveData with the current service state
            mTimerState.postValue(mService.getTimerState());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public TimerViewModel(@NonNull Application application) {
        super(application);
        mSessionRepository = new SessionRepository(application);
    }

    /** Observe this in MainActivity to receive live timer updates. */
    public LiveData<PomodoroTimer.TimerState> getTimerState() {
        return mTimerState;
    }

    /**
     * Set the task currently attached to the timer.
     * Pass null if the user starts a Pomodoro without selecting a task.
     */
    public void setCurrentTaskId(Integer taskId) {
        mCurrentTaskId = taskId;
    }

    /** Start listening by binding to the Service. Call in onStart(). */
    public void startListening() {
        Intent intent = new Intent(getApplication(), TimerEngineService.class);
        getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** Stop listening by unbinding. Call in onStop(). */
    public void stopListening() {
        if (mBound) {
            if (mService != null) {
                mService.getTimer().removeTimerEventListener(this);
            }
            getApplication().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onTick(PomodoroTimer.TimerState state) {
        mTimerState.postValue(state);
    }

    @Override
    public void onStateChanged(PomodoroTimer.TimerState state) {
        mTimerState.postValue(state);
    }

    @Override
    public void onFocusComplete(int sessionCount) {
        saveCurrentFocusSession("Completed");
    }

    @Override
    public void onBreakComplete(int sessionCount) {
        // Break sessions are not stored for now.
        // Week 4 only requires saving focus sessions.
    }

    /**
     * Send a command to TimerEngineService.
     * Valid commands: COMMAND_START_FOCUS, COMMAND_PAUSE, COMMAND_RESUME, COMMAND_SKIP, COMMAND_RESET.
     */
    public void sendCommand(String command) {
        if (AppConstants.COMMAND_START_FOCUS.equals(command)) {
            mFocusStartTime = System.currentTimeMillis();
        }

        if (AppConstants.COMMAND_SKIP.equals(command) || AppConstants.COMMAND_RESET.equals(command)) {
            saveCurrentFocusSession("Failed");
        }

        Intent intent = new Intent(TimerEngineService.ACTION_COMMAND);
        intent.setClass(getApplication(), TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);

        getApplication().startService(intent);
    }

    private void saveCurrentFocusSession(String status) {
        if (mFocusStartTime <= 0L) {
            return;
        }

        long endTime = System.currentTimeMillis();

        mSessionRepository.saveSession(
                mCurrentTaskId,
                mFocusStartTime,
                endTime,
                status
        );

        mFocusStartTime = 0L;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
}