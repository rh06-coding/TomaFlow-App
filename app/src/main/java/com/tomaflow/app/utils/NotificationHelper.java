package com.tomaflow.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer.Phase;
import com.tomaflow.app.timer.PomodoroTimer.TimerState;
import com.tomaflow.app.timer.TimerEngineService;

public class NotificationHelper {
    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public NotificationHelper(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel timerChannel = new NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_TIMER,
                    "Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            timerChannel.setDescription("Pomodoro timer notifications");
            timerChannel.enableVibration(false);
            timerChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(timerChannel);

            NotificationChannel soundChannel = new NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_SOUND,
                    "Phase Complete",
                    NotificationManager.IMPORTANCE_HIGH
            );
            soundChannel.setDescription("Notifications when phase completes");
            soundChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(soundChannel);
        }
    }

    public Notification buildTimerNotification(TimerState timerState) {
        String timeStr = TimerUtils.formatMillisToMmSs(timerState.remainingMs);
        String phaseLabel = TimerUtils.getPhaseLabel(timerState.phase);
        String contentText = String.format("%s - %s", phaseLabel, timeStr);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, AppConstants.NOTIFICATION_CHANNEL_TIMER)
                .setContentTitle("TomaFlow")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_pause) // Should be a dedicated timer icon
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(timerState.isRunning)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // Add Actions
        if (timerState.isRunning) {
            builder.addAction(R.drawable.ic_pause, "Pause", getServicePendingIntent(AppConstants.COMMAND_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play, "Resume", getServicePendingIntent(AppConstants.COMMAND_RESUME));
        }
        builder.addAction(R.drawable.ic_play, "Skip", getServicePendingIntent(AppConstants.COMMAND_SKIP));

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
        String title = phase == Phase.FOCUS ? "Work Session Complete" : "Break Complete";
        String message = phase == Phase.FOCUS 
                ? String.format("Session %d finished! Time for a break.", sessionCount)
                : "Break is over. Ready to focus?";

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

    public void showPhaseCompleteNotification(Phase phase, int sessionCount) {
        Notification notification = buildPhaseCompleteNotification(phase, sessionCount);
        mNotificationManager.notify(AppConstants.NOTIFICATION_ID_PHASE_COMPLETE, notification);
    }
}
