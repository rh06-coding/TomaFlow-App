package com.tomaflow.app.timer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.utils.NotificationHelper;

/**
 * Foreground Service that runs the Pomodoro timer in the background.
 *
 * Why a Service? The timer must keep running even when the user leaves the app.
 * Foreground services require a visible notification (Android requirement).
 *
 * Communication:
 *   UI -> Service: Intent with ACTION_COMMAND + extra COMMAND_*
 *   Service -> UI: LocalBroadcast with ACTION_TICK / ACTION_STATE_CHANGED
 */
public class TimerEngineService extends Service {

    // Intent action for commands FROM the UI
    public static final String ACTION_COMMAND = "com.tomaflow.TIMER_COMMAND";

    // Broadcast actions sent TO the UI
    public static final String ACTION_STATE_CHANGED = "com.tomaflow.TIMER_STATE_CHANGED";
    public static final String ACTION_TICK = "com.tomaflow.TIMER_TICK";
    public static final String EXTRA_TIMER_STATE = "timer_state";

    private PomodoroTimer mTimer;
    private TimerStateManager mStateManager;
    private NotificationHelper mNotificationHelper;
    private NotificationManager mNotificationManager;
    private LocalBroadcastManager mBroadcastManager;
    private Handler mTickHandler;
    private Runnable mTickRunnable;
    private PowerManager.WakeLock mWakeLock;


    private volatile boolean mTickScheduled = false;
    private long mLastActivityTimeMs = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new PomodoroTimer();
        mStateManager = new TimerStateManager(this);
        mNotificationHelper = new NotificationHelper(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mTickHandler = new Handler(Looper.getMainLooper());

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TomaFlow:TimerWakeLock");

        setupTimerListener();
        restoreState();
    }

    /**
     * Handles incoming intents from the UI layer.
     * Returns START_STICKY so the system restarts this service if killed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLastActivityTimeMs = System.currentTimeMillis();

        if (intent != null && ACTION_COMMAND.equals(intent.getAction())) {
            handleCommand(intent);
        }

        updateForegroundStatus();

        return START_STICKY;
    }

    /** Dispatch command string to the appropriate PomodoroTimer method. */
    private void handleCommand(Intent intent) {
        String command = intent.getStringExtra(AppConstants.INTENT_EXTRA_COMMAND);
        if (command == null) return;

        switch (command) {
            case AppConstants.COMMAND_START_FOCUS:
                mTimer.startFocus(AppConstants.TIMER_WORK_DURATION_MS);
                break;
            case AppConstants.COMMAND_PAUSE:
                mTimer.pause();
                break;
            case AppConstants.COMMAND_RESUME:
                mTimer.resume();
                break;
            case AppConstants.COMMAND_SKIP:
                mTimer.skip();
                break;
            case AppConstants.COMMAND_RESET:
                mTimer.reset();
                break;
        }
    }

    private void updateForegroundStatus() {
        PomodoroTimer.TimerState state = buildTimerState();
        if (mTimer.isRunning() || mTimer.getStateValue() != PomodoroTimer.State.IDLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                        mNotificationHelper.buildTimerNotification(state),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                        mNotificationHelper.buildTimerNotification(state));
            }
            acquireWakeLock();
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE);
            releaseWakeLock();
            checkAutoStop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        stopTick();
        releaseWakeLock();
        mTimer.destroy();
        super.onDestroy();
    }

    private void acquireWakeLock() {
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes safety timeout*/);
            Log.d("TimerEngineService", "WakeLock acquired");
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d("TimerEngineService", "WakeLock released");
        }
    }


    private void setupTimerListener() {
        mTimer.setOnTimerEventListener(new PomodoroTimer.OnTimerEventListener() {

            @Override
            public void onTick(PomodoroTimer.TimerState state) {
                broadcastState(ACTION_TICK, state);
                updateNotification(state);
                scheduleNextTick();
            }

            @Override
            public void onStateChanged(PomodoroTimer.TimerState state) {
                broadcastState(ACTION_STATE_CHANGED, state);
                mStateManager.saveState(state, mTimer.getFocusDurationMs(), 
                        mTimer.getShortBreakDurationMs(), mTimer.getLongBreakDurationMs());

                if (!state.isRunning) {
                    stopTick();
                    releaseWakeLock();
                } else if (!mTickScheduled) {
                    scheduleNextTick();
                    acquireWakeLock();
                }

                updateNotification(state);
                
                if (state.state == PomodoroTimer.State.IDLE || state.state == PomodoroTimer.State.COMPLETED) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                    releaseWakeLock();
                    checkAutoStop();
                }
            }

            @Override
            public void onFocusComplete(int sessionCount) {
                mNotificationHelper.showPhaseCompleteNotification(PomodoroTimer.Phase.FOCUS, sessionCount);
                mNotificationHelper.playCompletionSound();
                mNotificationHelper.vibrateForPhaseComplete(PomodoroTimer.Phase.FOCUS);
            }

            @Override
            public void onBreakComplete(int sessionCount) {
                mNotificationHelper.showPhaseCompleteNotification(PomodoroTimer.Phase.SHORT_BREAK, sessionCount);
                mNotificationHelper.playCompletionSound();
                mNotificationHelper.vibrateForPhaseComplete(PomodoroTimer.Phase.SHORT_BREAK);
            }
        });

        mTickRunnable = () -> {
            mTickScheduled = false;
            mTimer.tick();
        };
    }

    /** Post the next tick() call after 1 second. Skips if already scheduled. */
    private void scheduleNextTick() {
        if (mTickScheduled || !mTimer.isRunning()) {
            return;
        }
        mTickScheduled = true;
        mTickHandler.postDelayed(mTickRunnable, 1000);
    }

    private void stopTick() {
        mTickScheduled = false;
        mTickHandler.removeCallbacks(mTickRunnable);
    }

    /** Refresh the foreground notification with current timer state. */
    private void updateNotification(PomodoroTimer.TimerState state) {
        if (mTimer.isRunning() || mTimer.getStateValue() != PomodoroTimer.State.IDLE) {
             mNotificationManager.notify(AppConstants.NOTIFICATION_ID_TIMER, 
                     mNotificationHelper.buildTimerNotification(state));
        }
    }

    /** Broadcast serialized timer state to the UI via LocalBroadcastManager. */
    private void broadcastState(String action, PomodoroTimer.TimerState state) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra(EXTRA_TIMER_STATE, serializeTimerState(state));
        mBroadcastManager.sendBroadcast(broadcast);
    }

    /**
     * Serialize TimerState to a pipe-delimited string for Intent transport.
     * Format: "STATE|PHASE|isRunning|remainingMs|sessionCount|updatedAt"
     */
    private String serializeTimerState(PomodoroTimer.TimerState state) {
        return String.format("%s|%s|%b|%d|%d|%d",
                state.state.name(),
                state.phase.name(),
                state.isRunning,
                state.remainingMs,
                state.sessionCount,
                state.updatedAtElapsed);
    }

    private PomodoroTimer.TimerState buildTimerState() {
        return new PomodoroTimer.TimerState(
                mTimer.getStateValue(),
                mTimer.getPhaseValue(),
                mTimer.isRunning(),
                mTimer.getRemainingMs(),
                mTimer.getSessionCount(),
                SystemClock.elapsedRealtime()
        );
    }

    /** Restore timer state from SharedPreferences in case the service was killed. */
    private void restoreState() {
        TimerStateManager.RestoredState restored = mStateManager.restoreState();
        mTimer.setDurations(restored.focusDurationMs, restored.shortBreakDurationMs, restored.longBreakDurationMs);

        if (restored.state != PomodoroTimer.State.IDLE) {
             mTimer.restoreFromState(restored.toTimerState());
        }
    }

    /** Stop the service if idle for longer than SERVICE_AUTO_STOP_DELAY_MS. */
    private void checkAutoStop() {
        if (mTimer.isRunning()) return;
        
        long idleTimeMs = System.currentTimeMillis() - mLastActivityTimeMs;
        if (idleTimeMs > AppConstants.SERVICE_AUTO_STOP_DELAY_MS) {
            stopSelf();
        } else {
            mTickHandler.postDelayed(this::checkAutoStop, 60000);
        }
    }
}
