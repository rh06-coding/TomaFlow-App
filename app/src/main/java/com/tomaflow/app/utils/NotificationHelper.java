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

    /** Required on Android 8.0+ (API 26). */
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

    public Notification buildTimerNotification(TimerState timerState) {
        String timeStr = TimerUtils.formatMillisToMmSs(timerState.remainingMs);
        String phaseLabel = TimerUtils.getPhaseLabel(timerState.phase);
        
        com.tomaflow.app.data.model.BuiltInTrack track = com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().getCurrentTrack();
        boolean isMusicPlaying = com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().isPlaying();
        
        String contentText = track != null ? "🎵 " + track.name : mContext.getString(R.string.focus_music_empty);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, AppConstants.NOTIFICATION_CHANNEL_TIMER)
                .setContentTitle(phaseLabel)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(timerState.isRunning)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (timerState.isRunning) {
            builder.setUsesChronometer(true);
            builder.setWhen(System.currentTimeMillis() + timerState.remainingMs);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setChronometerCountDown(true);
            }
        } else {
            builder.setUsesChronometer(false);
            builder.setContentTitle(phaseLabel + " - " + timeStr);
        }

        // Action 0: Timer Play/Pause
        if (timerState.isRunning) {
            builder.addAction(R.drawable.ic_pause, mContext.getString(R.string.notification_action_pause), getServicePendingIntent(AppConstants.COMMAND_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play, mContext.getString(R.string.notification_action_resume), getServicePendingIntent(AppConstants.COMMAND_RESUME));
        }

        // Action 1: Skip
        builder.addAction(R.drawable.ic_skip_next, mContext.getString(R.string.notification_action_skip), getServicePendingIntent(AppConstants.COMMAND_SKIP));

        // Action 2: Music Play/Pause
        Intent toggleMusicIntent = new Intent(mContext, com.tomaflow.app.ui.music.MusicService.class);
        toggleMusicIntent.setAction(com.tomaflow.app.ui.music.MusicService.ACTION_TOGGLE_PLAY);
        PendingIntent pendingToggleMusic = PendingIntent.getService(mContext, 3, toggleMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        builder.addAction(isMusicPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                "Music", pendingToggleMusic);

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2));

        return builder.build();
    }

    private PendingIntent getServicePendingIntent(String command) {
        Intent intent = new Intent(mContext, TimerEngineService.class);
        intent.setAction(TimerEngineService.ACTION_COMMAND);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, command);
        return PendingIntent.getService(mContext, command.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public Notification buildPhaseCompleteNotification(Phase phase, int sessionCount) {
        String title = phase == Phase.FOCUS 
                ? mContext.getString(R.string.notification_focus_complete_title) 
                : mContext.getString(R.string.notification_break_complete_title);
        String message = phase == Phase.FOCUS 
                ? mContext.getString(R.string.notification_focus_complete_short)
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

    /** Replaces any previous one (same ID). */
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

    public void release() {
        releaseMediaPlayer();
    }
}
