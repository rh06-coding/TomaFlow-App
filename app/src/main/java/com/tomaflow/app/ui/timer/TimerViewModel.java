package com.tomaflow.app.ui.timer;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.timer.TimerEngineService;

public class TimerViewModel extends AndroidViewModel {
    private final MutableLiveData<PomodoroTimer.TimerState> mTimerState = new MutableLiveData<>();
    private final LocalBroadcastManager mBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;

    public TimerViewModel(@NonNull Application application) {
        super(application);
        this.mBroadcastManager = LocalBroadcastManager.getInstance(application);
    }

    public LiveData<PomodoroTimer.TimerState> getTimerState() {
        return mTimerState;
    }

    public void startListening() {
        if (mBroadcastReceiver != null) return;

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                String serialized = intent.getStringExtra(TimerEngineService.EXTRA_TIMER_STATE);
                PomodoroTimer.TimerState state = deserializeTimerState(serialized);
                if (state != null) {
                    mTimerState.setValue(state);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerEngineService.ACTION_STATE_CHANGED);
        filter.addAction(TimerEngineService.ACTION_TICK);
        mBroadcastManager.registerReceiver(mBroadcastReceiver, filter);
    }

    public void stopListening() {
        if (mBroadcastReceiver != null) {
            mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    public void sendCommand(String command) {
        Intent intent = new Intent(TimerEngineService.ACTION_COMMAND);
        intent.setClass(getApplication(), TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication().startForegroundService(intent);
        } else {
            getApplication().startService(intent);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }

    private PomodoroTimer.TimerState deserializeTimerState(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] parts = serialized.split("\\|");
        if (parts.length < 6) return null;

        try {
            PomodoroTimer.State state = PomodoroTimer.State.valueOf(parts[0]);
            PomodoroTimer.Phase phase = PomodoroTimer.Phase.valueOf(parts[1]);
            boolean isRunning = Boolean.parseBoolean(parts[2]);
            long remainingMs = Long.parseLong(parts[3]);
            int sessionCount = Integer.parseInt(parts[4]);
            long updatedAt = Long.parseLong(parts[5]);

            return new PomodoroTimer.TimerState(state, phase, isRunning, remainingMs, sessionCount, updatedAt);
        } catch (Exception e) {
            return null;
        }
    }
}
