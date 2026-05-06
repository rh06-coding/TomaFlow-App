package com.tomaflow.app.timer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.utils.NotificationHelper;

public class TimerEngineService extends Service {
    public static final String ACTION_COMMAND = "com.tomaflow.TIMER_COMMAND";
    public static final String COMMAND_START_FOCUS = "START_FOCUS";
    public static final String COMMAND_PAUSE = "PAUSE";
    public static final String COMMAND_RESUME = "RESUME";
    public static final String COMMAND_SKIP = "SKIP";
    public static final String COMMAND_RESET = "RESET";

    public static final String ACTION_STATE_CHANGED = "com.tomaflow.TIMER_STATE_CHANGED";
    public static final String ACTION_TICK = "com.tomaflow.TIMER_TICK";
    public static final String EXTRA_TIMER_STATE = "timer_state";

    private PomodoroTimer mTimer;
    private TimerStateManager mStateManager;
    private NotificationHelper mNotificationHelper;
    private LocalBroadcastManager mBroadcastManager;
    private Handler mTickHandler;
    private Runnable mTickRunnable;

    private volatile boolean mTickScheduled = false;
    private long mLastActivityTimeMs = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new PomodoroTimer();
        mStateManager = new TimerStateManager(this);
        mNotificationHelper = new NotificationHelper(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mTickHandler = new Handler(Looper.getMainLooper());

        restoreState();
        setupTimerListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        mLastActivityTimeMs = System.currentTimeMillis();

        String action = intent.getAction();
        if (ACTION_COMMAND.equals(action)) {
            handleCommand(intent);
        }

        // Keep foreground if timer is running
        if (mTimer.getStateValue() != PomodoroTimer.State.IDLE) {
            startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                    mNotificationHelper.buildTimerNotification(buildTimerState()));
        }

        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        String command = intent.getStringExtra(AppConstants.INTENT_EXTRA_COMMAND);
        if (command == null) return;

        switch (command) {
            case COMMAND_START_FOCUS:
                mTimer.startFocus(AppConstants.TIMER_WORK_DURATION_MS);
                break;
            case COMMAND_PAUSE:
                mTimer.pause();
                break;
            case COMMAND_RESUME:
                mTimer.resume();
                break;
            case COMMAND_SKIP:
                mTimer.skip();
                break;
            case COMMAND_RESET:
                mTimer.reset();
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopTick();
        mTimer.destroy();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    private void setupTimerListener() {
        mTimer.setOnTimerEventListener(new PomodoroTimer.OnTimerEventListener() {
            @Override
            public void onTick(PomodoroTimer.TimerState state) {
                broadcastState(ACTION_TICK, state);
                scheduleNextTick();
            }

            @Override
            public void onStateChanged(PomodoroTimer.TimerState state) {
                broadcastState(ACTION_STATE_CHANGED, state);
                mStateManager.saveState(state, mTimer.getFocusDurationMs(), mTimer.getBreakDurationMs());

                if (!mTimer.getStateValue().toString().startsWith("RUNNING")) {
                    stopTick();
                } else if (!mTickScheduled) {
                    scheduleNextTick();
                }

                updateNotification(state);

                if (mTimer.getStateValue() == PomodoroTimer.State.IDLE) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                    checkAutoStop();
                }
            }

            @Override
            public void onFocusComplete(int sessionCount) {
                mNotificationHelper.showPhaseCompleteNotification(PomodoroTimer.Phase.FOCUS, sessionCount);
            }

            @Override
            public void onBreakComplete(int sessionCount) {
                mNotificationHelper.showPhaseCompleteNotification(PomodoroTimer.Phase.BREAK, sessionCount);
            }
        });

        mTickRunnable = () -> {
            mTickScheduled = false;
            mTimer.tick();
        };
    }

    private void scheduleNextTick() {
        if (mTickScheduled || !mTimer.getStateValue().toString().startsWith("RUNNING")) {
            return;
        }
        mTickScheduled = true;
        mTickHandler.postDelayed(mTickRunnable, 1000);
    }

    private void stopTick() {
        mTickScheduled = false;
        mTickHandler.removeCallbacks(mTickRunnable);
    }

    private void updateNotification(PomodoroTimer.TimerState state) {
        if (state.isRunning) {
            startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                    mNotificationHelper.buildTimerNotification(state));
        }
    }

    private void broadcastState(String action, PomodoroTimer.TimerState state) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra(EXTRA_TIMER_STATE, serializeTimerState(state));
        mBroadcastManager.sendBroadcast(broadcast);
    }

    private String serializeTimerState(PomodoroTimer.TimerState state) {
        return String.format("%s|%s|%s|%d|%d|%d",
                state.state.name(),
                state.phase.name(),
                String.valueOf(state.isRunning),
                state.remainingMs,
                state.sessionCount,
                state.updatedAtElapsed);
    }

    private PomodoroTimer.TimerState buildTimerState() {
        return new PomodoroTimer.TimerState(
                mTimer.getStateValue(),
                mTimer.getPhaseValue(),
                mTimer.getStateValue().toString().startsWith("RUNNING"),
                mTimer.getRemainingMs(),
                mTimer.getSessionCount(),
                System.currentTimeMillis()
        );
    }

    private void restoreState() {
        TimerStateManager.RestoredState restored = mStateManager.restoreState();
        mTimer.setDurations(restored.focusDurationMs, restored.breakDurationMs, restored.breakDurationMs);

        // Transition to IDLE if manager says so; otherwise restore exact state
        if (restored.state == PomodoroTimer.State.IDLE || restored.remainingMs == 0) {
            mTimer.reset();
        } else {
            // State restoration wired up here; would need setter methods in PomodoroTimer
            mTimer.reset();
        }
    }

    private void checkAutoStop() {
        long idleTimeMs = System.currentTimeMillis() - mLastActivityTimeMs;
        if (idleTimeMs > AppConstants.SERVICE_AUTO_STOP_DELAY_MS) {
            stopSelf();
        }
    }
}

