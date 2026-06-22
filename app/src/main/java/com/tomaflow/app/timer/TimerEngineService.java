package com.tomaflow.app.timer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
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
 * Foreground Service chạy Pomodoro ở chế độ nền.
 *
 * CÁC QUYẾT ĐỊNH THIẾT KẾ QUAN TRỌNG để tránh rè/nhiễu âm thanh:
 * 1. Timer ticks chạy trên một HandlerThread ĐỘC LẬP ("TimerThread"), KHÔNG dùng luồng chính.
 *    Điều này ngăn chặn bộ đếm thời gian tranh giành tài nguyên với các callback âm thanh của MediaPlayer.
 * 2. Các callback cập nhật giao diện (UI) được chuyển về luồng CHÍNH thông qua mMainHandler.
 * 3. Thông báo (Notification) chỉ được cập nhật tối đa 5 giây một lần (NOTIF_UPDATE_INTERVAL_MS).
 *    Việc gọi NotificationManager.notify() mỗi giây sẽ làm quá tải hệ thống và gây giật lag âm thanh.
 * 4. startForeground() chỉ được gọi CHÍNH XÁC MỘT LẦN cho mỗi phiên hoạt động.
 */
public class TimerEngineService extends Service {

    private static final String TAG = "TimerEngineService";
    private static final long WAKELOCK_TIMEOUT_MS = 35 * 60 * 1000L;
    private static final long TICK_INTERVAL_MS = 1000L;
    private static final long NOTIF_UPDATE_INTERVAL_MS = 5000L; // update notification max every 5s

    public static final String ACTION_COMMAND = "com.tomaflow.TIMER_COMMAND";

    // ── Fields ───────────────────────────────────────────────────────────────

    private PomodoroTimer        mTimer;
    private TimerStateManager    mStateManager;
    private SettingsManager      mSettingsManager;
    private NotificationHelper   mNotificationHelper;
    private NotificationManager  mNotificationManager;
    private SessionRepository    mSessionRepository;
    private PowerManager.WakeLock mWakeLock;

    /** Dedicated background thread for ALL timer ticking — never touches main thread. */
    private HandlerThread        mTimerThread;
    /** Handler that posts work to mTimerThread. */
    private Handler              mTimerHandler;
    /** Handler for posting listener callbacks + notification updates to main thread. */
    private Handler              mMainHandler;

    private final IBinder mBinder = new TimerBinder();
    private boolean mIsForeground = false;
    private long    mLastNotifUpdateMs = 0;

    // ── Binder ───────────────────────────────────────────────────────────────

    public class TimerBinder extends Binder {
        public TimerEngineService getService() {
            return TimerEngineService.this;
        }
    }

    // ── Public API (called from bound UI) ────────────────────────────────────

    /**
     * Returns the current timer state. Safe to call from any thread.
     * Re-syncs durations from settings when IDLE so the initial display is correct.
     */
    public PomodoroTimer.TimerState getTimerState() {
        if (mTimer.getStateValue() == PomodoroTimer.State.IDLE) {
            mTimer.setDurations(
                    mSettingsManager.getFocusDurationMs(),
                    mSettingsManager.getShortBreakDurationMs(),
                    mSettingsManager.getLongBreakDurationMs()
            );
        }
        return mTimer.buildPublicState();
    }

    public PomodoroTimer getTimer() {
        return mTimer;
    }

    // ── Service lifecycle ─────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();

        // ── Background timer thread (isolated from main thread + audio) ──────
        mTimerThread = new HandlerThread("TomaFlow-TimerThread");
        mTimerThread.start();
        mTimerHandler = new Handler(mTimerThread.getLooper());
        mMainHandler  = new Handler(Looper.getMainLooper());

        mTimer             = new PomodoroTimer();
        mStateManager      = new TimerStateManager(this);
        mSettingsManager   = new SettingsManager(this);
        mNotificationHelper = new NotificationHelper(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mSessionRepository = new SessionRepository(getApplication());

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TomaFlow:TimerWakeLock");

        setupTimerListener();
        restoreState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_COMMAND.equals(intent.getAction())) {
            final String command = intent.getStringExtra(AppConstants.INTENT_EXTRA_COMMAND);
            final String extraPhase = intent.getStringExtra(AppConstants.INTENT_EXTRA_PHASE);
            // Execute command on the timer thread to avoid main-thread contention
            mTimerHandler.post(() -> handleCommand(command, extraPhase));
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mTimerHandler.removeCallbacksAndMessages(null);
        mTimerThread.quitSafely();
        releaseWakeLock();
        mNotificationHelper.release();
        mTimer.destroy();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().stop(this);
        stopForeground(true);
        stopSelf();
    }

    // ── Command handling (runs on mTimerThread) ───────────────────────────────

    private void handleCommand(String command) {
        handleCommand(command, null);
    }

    private void handleCommand(String command, String extraPhase) {
        if (command == null) return;

        switch (command) {
            case AppConstants.COMMAND_START_FOCUS:
                long focusMs = mSettingsManager.getFocusDurationMs();
                if (!mTimer.isRunning()) {
                    mTimer.setDurations(
                            focusMs,
                            mSettingsManager.getShortBreakDurationMs(),
                            mSettingsManager.getLongBreakDurationMs()
                    );
                }
                mNotificationHelper.cancelPhaseCompleteNotification();
                mTimer.startFocus(focusMs);
                // Kick off the tick loop on the timer thread
                scheduleTick();
                break;

            case AppConstants.COMMAND_PAUSE:
                stopTick();
                mTimer.pause();
                break;

            case AppConstants.COMMAND_RESUME:
                mNotificationHelper.cancelPhaseCompleteNotification();
                mTimer.resume();
                // Re-kick the tick loop
                scheduleTick();
                break;

            case AppConstants.COMMAND_SKIP:
                mNotificationHelper.cancelPhaseCompleteNotification();
                mTimer.skip();
                if (mTimer.isRunning()) scheduleTick();
                break;

            case AppConstants.COMMAND_RESET:
                stopTick();
                mNotificationHelper.cancelPhaseCompleteNotification();
                mTimer.reset();
                break;

            case AppConstants.COMMAND_JUMP_TO_PHASE:
                stopTick();
                mNotificationHelper.cancelPhaseCompleteNotification();
                if (PomodoroTimer.Phase.FOCUS.name().equals(extraPhase)) {
                    mTimer.jumpToFocus(mSettingsManager.getFocusDurationMs());
                } else if (PomodoroTimer.Phase.SHORT_BREAK.name().equals(extraPhase)) {
                    mTimer.jumpToBreak(mSettingsManager.getShortBreakDurationMs(), false);
                } else if (PomodoroTimer.Phase.LONG_BREAK.name().equals(extraPhase)) {
                    mTimer.jumpToBreak(mSettingsManager.getLongBreakDurationMs(), true);
                }
                break;
        }

        // Always update foreground status after a command (runs on timer thread, posts notif to main)
        postNotificationUpdate(true /* force */);
    }

    // ── Tick loop (runs entirely on mTimerThread) ─────────────────────────────

    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mTimer.isRunning()) return;

            mTimer.tick(); // advances time, fires onTick() / onPhaseComplete() callbacks

            // Always schedule the next tick AFTER tick() returns
            if (mTimer.isRunning()) {
                mTimerHandler.postDelayed(this, TICK_INTERVAL_MS);
            }
        }
    };

    /**
     * Schedule the next tick on the dedicated timer thread.
     * Call only from mTimerThread context (inside handleCommand or mTickRunnable).
     */
    private void scheduleTick() {
        mTimerHandler.removeCallbacks(mTickRunnable);
        mTimerHandler.postDelayed(mTickRunnable, TICK_INTERVAL_MS);
    }

    private void stopTick() {
        mTimerHandler.removeCallbacks(mTickRunnable);
    }

    // ── Timer listener (callbacks fired from mTimerThread) ───────────────────

    private void setupTimerListener() {
        mTimer.addTimerEventListener(new PomodoroTimer.OnTimerEventListener() {

            @Override
            public void onTick(PomodoroTimer.TimerState state) {
                // Post UI update to main thread — does NOT schedule another tick
                mMainHandler.post(() -> broadcastState(state));
                // Update notification (throttled) on timer thread
                postNotificationUpdate(false);
                // Persist state (cheap, async)
                mStateManager.saveState(state,
                        mTimer.getFocusDurationMs(),
                        mTimer.getShortBreakDurationMs(),
                        mTimer.getLongBreakDurationMs());
            }

            @Override
            public void onStateChanged(PomodoroTimer.TimerState state) {
                // Post UI update to main thread
                mMainHandler.post(() -> broadcastState(state));
                // Update foreground/wakeLock on main thread (required for startForeground)
                mMainHandler.post(() -> updateForegroundStatus(state));
                // Persist state
                mStateManager.saveState(state,
                        mTimer.getFocusDurationMs(),
                        mTimer.getShortBreakDurationMs(),
                        mTimer.getLongBreakDurationMs());
            }

            @Override
            public void onFocusComplete(int sessionCount) {
                mMainHandler.post(() -> {
                    mNotificationHelper.showPhaseCompleteNotification(
                            PomodoroTimer.Phase.FOCUS, sessionCount);
                    mNotificationHelper.playCompletionSound();
                    mNotificationHelper.vibrateForPhaseComplete(PomodoroTimer.Phase.FOCUS);
                });

                // Save session record
                SessionEntity session = new SessionEntity();
                session.startTime = System.currentTimeMillis() - mTimer.getFocusDurationMs();
                session.endTime   = System.currentTimeMillis();
                session.duration  = (int) (mTimer.getFocusDurationMs() / 1000);
                session.status    = "Completed";
                mSessionRepository.insert(session);

                // Badges
                mMainHandler.post(() -> checkAndUnlockBadges(sessionCount));

                // Break starts automatically — re-kick the tick loop
                if (mTimer.isRunning()) scheduleTick();
            }

            @Override
            public void onBreakComplete(int sessionCount) {
                mMainHandler.post(() -> {
                    PomodoroTimer.Phase phase = mTimer.getPhaseValue();
                    mNotificationHelper.showPhaseCompleteNotification(phase, sessionCount);
                    mNotificationHelper.playCompletionSound();
                    mNotificationHelper.vibrateForPhaseComplete(phase);
                });

                // Next focus starts automatically — re-kick the tick loop
                if (mTimer.isRunning()) scheduleTick();
            }
        });
    }

    // ── Listener broadcast (main thread) ─────────────────────────────────────

    /** Called on main thread. The PomodoroTimer listener list is CopyOnWriteArrayList = thread-safe. */
    private void broadcastState(PomodoroTimer.TimerState state) {
        // Timer's CopyOnWriteArrayList is already iterated in PomodoroTimer itself.
        // This method is a hook for any additional main-thread work if needed.
        // State is delivered to TimerViewModel via the addTimerEventListener path.
    }

    // ── Notification / Foreground (main thread) ───────────────────────────────

    /**
     * Called on the timer thread; posts a throttled notification update to main thread.
     * @param force if true, bypasses the throttle (e.g., after state changes)
     */
    private void postNotificationUpdate(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - mLastNotifUpdateMs < NOTIF_UPDATE_INTERVAL_MS)) return;
        mLastNotifUpdateMs = now;

        PomodoroTimer.TimerState snap = mTimer.buildPublicState();
        mMainHandler.post(() -> updateForegroundStatus(snap));
    }

    /** Must be called on main thread. startForeground() requires main thread. */
    private void updateForegroundStatus(PomodoroTimer.TimerState state) {
        boolean isActive = (state.state != PomodoroTimer.State.IDLE
                && state.state != PomodoroTimer.State.COMPLETED);

        if (isActive) {
            android.app.Notification notif = mNotificationHelper.buildTimerNotification(state);
            if (!mIsForeground) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(AppConstants.NOTIFICATION_ID_TIMER, notif,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                } else {
                    startForeground(AppConstants.NOTIFICATION_ID_TIMER, notif);
                }
                mIsForeground = true;
            } else {
                mNotificationManager.notify(AppConstants.NOTIFICATION_ID_TIMER, notif);
            }

            if (state.isRunning) acquireWakeLock();
            else releaseWakeLock();

        } else {
            if (mIsForeground) {
                stopForeground(STOP_FOREGROUND_REMOVE);
                mIsForeground = false;
            }
            releaseWakeLock();
        }
    }

    // ── WakeLock ──────────────────────────────────────────────────────────────

    private void acquireWakeLock() {
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    // ── State restore ─────────────────────────────────────────────────────────

    private void restoreState() {
        TimerStateManager.RestoredState restored = mStateManager.restoreState();

        if (restored.state != PomodoroTimer.State.IDLE) {
            mTimer.setDurations(restored.focusDurationMs,
                    restored.shortBreakDurationMs,
                    restored.longBreakDurationMs);
            mTimer.restoreFromState(restored.toTimerState());
            // If it was running before the service was killed, re-kick the tick loop
            if (mTimer.isRunning()) {
                mTimerHandler.postDelayed(mTickRunnable, TICK_INTERVAL_MS);
            }
        } else {
            mTimer.setDurations(
                    mSettingsManager.getFocusDurationMs(),
                    mSettingsManager.getShortBreakDurationMs(),
                    mSettingsManager.getLongBreakDurationMs()
            );
        }
    }

    // ── Badges ────────────────────────────────────────────────────────────────

    private void checkAndUnlockBadges(int sessionCount) {
        com.tomaflow.app.data.repository.RewardsRepository rewardsRepo =
                new com.tomaflow.app.data.repository.RewardsRepository(getApplication());

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);

        if (hour < 7)        rewardsRepo.unlockBadge("earlybird");
        else if (hour >= 22) rewardsRepo.unlockBadge("nightowl");
        if (sessionCount >= 4) rewardsRepo.unlockBadge("marathon");
    }
}
