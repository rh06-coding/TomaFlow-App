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
 * Lắng nghe TimerEngineService và lưu session khi focus kết thúc.
 */
public class TimerViewModel extends AndroidViewModel implements PomodoroTimer.OnTimerEventListener {

    private final MutableLiveData<PomodoroTimer.TimerState> mTimerState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mTaskCompletedEvent = new MutableLiveData<>();
    private final MutableLiveData<Integer> mFocusCompleteEvent = new MutableLiveData<>(); // carries sessionCount
    private final MutableLiveData<Integer> mBreakCompleteEvent = new MutableLiveData<>();

    private final SessionRepository mSessionRepository;
    private final com.tomaflow.app.data.repository.TaskRepository mTaskRepository;

    private TimerEngineService mService;
    private boolean mBound = false;

    private long mFocusStartTime = 0L;// Lưu thời điểm bắt đầu focus để tính duration thực tế.
    private String mCurrentTaskId = null;// Task đang được focus; null nếu user không chọn task.

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
        mTaskRepository = new com.tomaflow.app.data.repository.TaskRepository(application);
    }

    /** Observe this in MainActivity to receive live timer updates. */
    public LiveData<PomodoroTimer.TimerState> getTimerState() {
        return mTimerState;
    }

    public MutableLiveData<Boolean> getTaskCompletedEvent() {
        return mTaskCompletedEvent;
    }

    public MutableLiveData<Integer> getFocusCompleteEvent() {
        return mFocusCompleteEvent;
    }

    public MutableLiveData<Integer> getBreakCompleteEvent() {
        return mBreakCompleteEvent;
    }

    /**
     * Set the task currently attached to the timer.
     * Pass null if the user starts a Pomodoro without selecting a task.
     */
    public void setCurrentTaskId(String taskId) {
        mCurrentTaskId = taskId;
    }

    public String getCurrentTaskId() {
        return mCurrentTaskId;
    }

    public void startListening() {
        if (mBound && mService != null) {
            // Already bound (e.g. returning from another screen): re-pull the current
            // state so a settings change made while away is reflected immediately.
            mTimerState.postValue(mService.getTimerState());
            return;
        }
        Intent intent = new Intent(getApplication(), TimerEngineService.class);
        getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

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
        mFocusCompleteEvent.postValue(sessionCount);
        saveCurrentFocusSession("Completed");
        if (mCurrentTaskId != null) {
            mTaskRepository.decrementPomodoro(mCurrentTaskId, () -> {
                mTaskCompletedEvent.postValue(true);
            });
        }
    }

    @Override
    public void onBreakComplete(int sessionCount) {
        mBreakCompleteEvent.postValue(sessionCount);
        // Break sessions are not stored for now.
        // Week 4 only requires saving focus sessions.
    }

    /**
     * Send a command to TimerEngineService.
     * Valid commands: COMMAND_START_FOCUS, COMMAND_PAUSE, COMMAND_RESUME, COMMAND_SKIP, COMMAND_RESET.
     */
    public void sendCommand(String command) {
        if (AppConstants.COMMAND_START.equals(command)) {
            PomodoroTimer.TimerState state = mTimerState.getValue();
            if (state == null || state.phase == PomodoroTimer.Phase.FOCUS) {
                mFocusStartTime = System.currentTimeMillis();
            }
        }

        if (AppConstants.COMMAND_SKIP.equals(command) || AppConstants.COMMAND_RESET.equals(command)) {
            saveCurrentFocusSession("Failed");
        }

        Intent intent = new Intent(TimerEngineService.ACTION_COMMAND);
        intent.setClass(getApplication(), TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);

        getApplication().startService(intent);
    }

    /** Jump directly to a specified phase without completing the current session. */
    public void jumpToPhase(PomodoroTimer.Phase phase) {
        Intent intent = new Intent(TimerEngineService.ACTION_COMMAND);
        intent.setClass(getApplication(), TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, AppConstants.COMMAND_JUMP_TO_PHASE);
        intent.putExtra(AppConstants.INTENT_EXTRA_PHASE, phase.name());
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