package com.tomaflow.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer.Phase;
import com.tomaflow.app.timer.PomodoroTimer.TimerState;
import com.tomaflow.app.timer.TimerEngineService;

/**
 * Manages notifications, completion sound, and vibration.
 *
 * Two notification channels:
 *   - TIMER_CHANNEL (LOW): persistent countdown notification for foreground service
 *   - SOUND_CHANNEL (HIGH): phase-complete notification with sound + vibration
 */
public class NotificationHelper {

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;

    public NotificationHelper(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        createNotificationChannels();
    }

    /** Create the two channels. Required on Android 8.0+ (API 26). */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Low importance — no sound, used for the persistent timer notification
            NotificationChannel timerChannel = new NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_TIMER,
                    mContext.getString(R.string.notification_channel_timer_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            timerChannel.setDescription(mContext.getString(R.string.notification_channel_timer_desc));
            timerChannel.enableVibration(false);
            timerChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(timerChannel);

            // High importance — sound + vibration, used for phase-complete alerts
            NotificationChannel soundChannel = new NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_SOUND,
                    mContext.getString(R.string.notification_channel_sound_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            soundChannel.setDescription(mContext.getString(R.string.notification_channel_sound_desc));
            soundChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(soundChannel);
        }
    }

    /**
     * Build the persistent foreground notification showing countdown.
     * Ongoing = true (can't swipe away), auto-cancel = false.
     */
    public Notification buildTimerNotification(TimerState timerState) {
        String timeStr = TimerUtils.formatMillisToMmSs(timerState.remainingMs);
        String phaseLabel = TimerUtils.getPhaseLabel(timerState.phase);
        String contentText = String.format("%s - %s", phaseLabel, timeStr);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, AppConstants.NOTIFICATION_CHANNEL_TIMER)
                .setContentTitle(mContext.getString(R.string.notification_timer_title))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_pause) // Should be a dedicated timer icon
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(timerState.isRunning)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // Add Actions
        if (timerState.isRunning) {
            builder.addAction(R.drawable.ic_pause, mContext.getString(R.string.notification_action_pause), getServicePendingIntent(AppConstants.COMMAND_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play, mContext.getString(R.string.notification_action_resume), getServicePendingIntent(AppConstants.COMMAND_RESUME));
        }
        builder.addAction(R.drawable.ic_play, mContext.getString(R.string.notification_action_skip), getServicePendingIntent(AppConstants.COMMAND_SKIP));

        return builder.build();
    }

    private PendingIntent getServicePendingIntent(String command) {
        Intent intent = new Intent(mContext, TimerEngineService.class);
        intent.setAction(TimerEngineService.ACTION_COMMAND);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);
        return PendingIntent.getService(mContext, command.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Build the one-shot notification shown when a phase completes. */
    public Notification buildPhaseCompleteNotification(Phase phase, int sessionCount) {
        String title = phase == Phase.FOCUS 
                ? mContext.getString(R.string.notification_focus_complete_title) 
                : mContext.getString(R.string.notification_break_complete_title);
        String message = phase == Phase.FOCUS 
                ? mContext.getString(R.string.notification_focus_complete_msg, sessionCount)
                : mContext.getString(R.string.notification_break_complete_msg);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(mContext, AppConstants.NOTIFICATION_CHANNEL_SOUND)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();
    }

    /** Show the phase-complete notification. Replaces any previous one (same ID). */
    public void showPhaseCompleteNotification(Phase phase, int sessionCount) {
        Notification notification = buildPhaseCompleteNotification(phase, sessionCount);
        mNotificationManager.notify(AppConstants.NOTIFICATION_ID_PHASE_COMPLETE, notification);
    }

    /**
     * Dismiss the phase-complete notification. Called when the user moves into a new
     * active phase (Start/Resume/Skip/Reset) so it doesn't linger next to the
     * persistent timer notification.
     */
    public void cancelPhaseCompleteNotification() {
        mNotificationManager.cancel(AppConstants.NOTIFICATION_ID_PHASE_COMPLETE);
    }

    /**
     * Play a completion sound. Tries res/raw/session_complete first,
     * falls back to the system default notification sound.
     * MediaPlayer auto-releases via OnCompletionListener.
     */
    public void playCompletionSound() {
        try {
            releaseMediaPlayer();

            // Play on the notification stream, not the media stream — the default
            // MediaPlayer.create() attributes (USAGE_MEDIA) made the sound depend on
            // media volume, which is often low even when ringer volume is maxed.
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            int soundResId = mContext.getResources().getIdentifier(
                    "session_complete", "raw", mContext.getPackageName());

            if (soundResId != 0) {
                mMediaPlayer = MediaPlayer.create(mContext, soundResId, attributes, 0);
            } else {
                Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mMediaPlayer = MediaPlayer.create(mContext, defaultSound, null, attributes, 0);
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
            } catch (Exception e) {
                // Ignore release errors
            }
            mMediaPlayer = null;
        }
    }

    /**
     * Vibrate the device on phase complete.
     * Focus: double pulse [0, 200, 100, 200]. Break: single pulse [0, 100].
     * Silently no-ops if the device has no vibrator.
     */
    @SuppressWarnings("deprecation")
    public void vibrateForPhaseComplete(Phase phase) {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }

        try {
            long[] pattern = (phase == Phase.FOCUS)
                    ? AppConstants.VIBRATION_PATTERN_SESSION_COMPLETE
                    : AppConstants.VIBRATION_PATTERN_PHASE_COMPLETE;
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            mVibrator.vibrate(effect);
        } catch (Exception e) {
            // Fallback for older API levels
            try {
                long[] pattern = (phase == Phase.FOCUS)
                        ? AppConstants.VIBRATION_PATTERN_SESSION_COMPLETE
                        : AppConstants.VIBRATION_PATTERN_PHASE_COMPLETE;
                mVibrator.vibrate(pattern, -1);
            } catch (Exception ignored) {
            }
        }
    }

    /** Release all held resources. Call from TimerEngineService.onDestroy(). */
    public void release() {
        releaseMediaPlayer();
    }
}
