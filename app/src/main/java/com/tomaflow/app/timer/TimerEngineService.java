package com.tomaflow.app.timer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.repository.SessionRepository;
import com.tomaflow.app.utils.NotificationHelper;

/**
 * Foreground Service that runs the Pomodoro timer in the background.
 *
 * Why a Service? The timer must keep running even when the user leaves the app.
 * Foreground services require a visible notification (Android requirement).
 *
 * Communication:
 *   UI -> Service: Bound connection (TimerBinder) or Intent with ACTION_COMMAND
 *   Service -> UI: Callback interface (OnTimerEventListener) via bound connection
 */
public class TimerEngineService extends Service {

    private static final String TAG = "TimerEngineService";
    private static final long WAKELOCK_TIMEOUT_MS = 35 * 60 * 1000L; // 35 minutes
    private static final long TICK_INTERVAL_MS = 1000L;
    private static final long AUTO_STOP_CHECK_INTERVAL_MS = 60000L;

    // Intent action for commands FROM the UI
    public static final String ACTION_COMMAND = "com.tomaflow.TIMER_COMMAND";

    private PomodoroTimer mTimer;
    private TimerStateManager mStateManager;
    private NotificationHelper mNotificationHelper;
    private NotificationManager mNotificationManager;
    private SessionRepository mSessionRepository;
    private Handler mTickHandler;
    private Runnable mTickRunnable;
    private PowerManager.WakeLock mWakeLock;
    private AudioManager mAudioManager;
    private AudioFocusRequest mFocusRequest;

    private final IBinder mBinder = new TimerBinder();
    private volatile boolean mTickScheduled = false;
    private long mLastActivityTimeMs = 0;

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mTimer.isRunning()) {
                    mTimer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!mTimer.isRunning() && mTimer.getStateValue() != PomodoroTimer.State.IDLE) {
                    mTimer.resume();
                }
                break;
        }
    };

    /** Class used for the client Binder. */
    public class TimerBinder extends Binder {
        public TimerEngineService getService() {
            return TimerEngineService.this;
        }
    }

    /**
     * Exposes the current timer state for the bound UI to perform instant initialization.
     */
    public PomodoroTimer.TimerState getTimerState() {
        return buildTimerState();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new PomodoroTimer();
        mStateManager = new TimerStateManager(this);
        mNotificationHelper = new NotificationHelper(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mSessionRepository = new SessionRepository(getApplication());
        mTickHandler = new Handler(Looper.getMainLooper());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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
        
        if (state.state != PomodoroTimer.State.IDLE && state.state != PomodoroTimer.State.COMPLETED) {
            // Only start/maintain foreground service if active (running or paused)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                        mNotificationHelper.buildTimerNotification(state),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(AppConstants.NOTIFICATION_ID_TIMER,
                        mNotificationHelper.buildTimerNotification(state));
            }
            
            if (mTimer.isRunning()) {
                acquireWakeLock();
                requestAudioFocus();
            } else {
                releaseWakeLock();
                abandonAudioFocus();
            }
        } else {
            // If IDLE or COMPLETED, we do not need a foreground service or persistent notification
            releaseWakeLock();
            abandonAudioFocus();
            stopForeground(STOP_FOREGROUND_REMOVE);
            checkAutoStop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mTickHandler.removeCallbacksAndMessages(null);
        releaseWakeLock();
        abandonAudioFocus();
        mNotificationHelper.release();
        mTimer.destroy();
        super.onDestroy();
    }

    private void acquireWakeLock() {
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
            Log.d(TAG, "WakeLock acquired");
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }

    private void requestAudioFocus() {
        if (mFocusRequest == null) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    .build();
        }

        int res = mAudioManager.requestAudioFocus(mFocusRequest);
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus granted");
        }
    }

    private void abandonAudioFocus() {
        if (mFocusRequest != null) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest);
            Log.d(TAG, "Audio focus abandoned");
        }
    }

    private void setupTimerListener() {
        mTimer.addTimerEventListener(new PomodoroTimer.OnTimerEventListener() {

            @Override
            public void onTick(PomodoroTimer.TimerState state) {
                updateForegroundStatus();
                scheduleNextTick();
            }

            @Override
            public void onStateChanged(PomodoroTimer.TimerState state) {
                mStateManager.saveState(state, mTimer.getFocusDurationMs(), 
                        mTimer.getShortBreakDurationMs(), mTimer.getLongBreakDurationMs());

                if (!state.isRunning) {
                    stopTick();
                } else if (!mTickScheduled) {
                    scheduleNextTick();
                }

                updateForegroundStatus();
            }

            @Override
            public void onFocusComplete(int sessionCount) {
                mNotificationHelper.showPhaseCompleteNotification(PomodoroTimer.Phase.FOCUS, sessionCount);
                mNotificationHelper.playCompletionSound();
                mNotificationHelper.vibrateForPhaseComplete(PomodoroTimer.Phase.FOCUS);

                // Insert session into database
                SessionEntity session = new SessionEntity();
                session.startTime = System.currentTimeMillis() - mTimer.getFocusDurationMs();
                session.endTime = System.currentTimeMillis();
                session.duration = (int) (mTimer.getFocusDurationMs() / 1000);
                session.status = "Completed";
                mSessionRepository.insert(session);
            }

            @Override
            public void onBreakComplete(int sessionCount) {
                PomodoroTimer.Phase phase = mTimer.getPhaseValue();
                mNotificationHelper.showPhaseCompleteNotification(phase, sessionCount);
                mNotificationHelper.playCompletionSound();
                mNotificationHelper.vibrateForPhaseComplete(phase);
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
        mTickHandler.postDelayed(mTickRunnable, TICK_INTERVAL_MS);
    }

    private void stopTick() {
        mTickScheduled = false;
        mTickHandler.removeCallbacks(mTickRunnable);
    }

    public PomodoroTimer getTimer() {
        return mTimer;
    }

    private PomodoroTimer.TimerState buildTimerState() {
        return new PomodoroTimer.TimerState(
                mTimer.getStateValue(),
                mTimer.getPhaseValue(),
                mTimer.isRunning(),
                mTimer.getRemainingMs(),
                mTimer.getPhaseValue() == PomodoroTimer.Phase.FOCUS ? mTimer.getFocusDurationMs() :
                        (mTimer.getPhaseValue() == PomodoroTimer.Phase.SHORT_BREAK ? mTimer.getShortBreakDurationMs() : mTimer.getLongBreakDurationMs()),
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
            mTickHandler.postDelayed(this::checkAutoStop, AUTO_STOP_CHECK_INTERVAL_MS);
        }
    }
}
