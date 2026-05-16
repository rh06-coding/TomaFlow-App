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
    private TimerEngineService mService;
    private boolean mBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerEngineService.TimerBinder binder = (TimerEngineService.TimerBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.getTimer().setOnTimerEventListener(TimerViewModel.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }

    /** Observe this in MainActivity to receive live timer updates. */
    public LiveData<PomodoroTimer.TimerState> getTimerState() {
        return mTimerState;
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
                mService.getTimer().destroy(); // Cleanup listener
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
        // ViewModel can handle extra logic here if needed
    }

    @Override
    public void onBreakComplete(int sessionCount) {
        // ViewModel can handle extra logic here if needed
    }

    /**
     * Send a command to TimerEngineService.
     * Valid commands: COMMAND_START_FOCUS, COMMAND_PAUSE, COMMAND_RESUME, COMMAND_SKIP, COMMAND_RESET.
     */
    public void sendCommand(String command) {
        Intent intent = new Intent(TimerEngineService.ACTION_COMMAND);
        intent.setClass(getApplication(), TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);
        
        getApplication().startForegroundService(intent);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
}
