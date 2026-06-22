package com.tomaflow.app.ui.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.BuiltInTrack;

/**
 * Foreground Service giữ thông báo nhạc luôn hiển thị khi người dùng
 * thoát ứng dụng hoặc chuyển tab. Đối tượng MediaPlayer thực sự nằm trong AppMusicPlayer.
 * Service này chỉ quản lý thông báo (Notification) và MediaSession token.
 */
public class MusicService extends Service {

    public static final String ACTION_UPDATE_NOTIFICATION = "com.tomaflow.app.action.UPDATE_MUSIC_NOTIF";
    public static final String ACTION_TOGGLE_PLAY         = "com.tomaflow.app.action.TOGGLE_PLAY";
    public static final String ACTION_STOP                = "com.tomaflow.app.action.STOP_MUSIC";

    private static final String CHANNEL_ID      = "tomaflow_music_channel";
    private static final int    NOTIFICATION_ID = 2002;

    private MediaSessionCompat mMediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mMediaSession = new MediaSessionCompat(this, "TomaFlowMusic");
        mMediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;

        AppMusicPlayer player = AppMusicPlayer.getInstance();

        switch (intent.getAction()) {
            case ACTION_UPDATE_NOTIFICATION:
                updateNotification(player);
                break;
            case ACTION_TOGGLE_PLAY:
                if (player.isPlaying()) player.pause(this);
                else                    player.resume(this);
                updateNotification(player);
                break;
            case ACTION_STOP:
                player.stop(this);
                stopForeground(true);
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        AppMusicPlayer.getInstance().stop(this);
        stopForeground(true);
        stopSelf();
    }

    private void updateNotification(AppMusicPlayer player) {
        BuiltInTrack track = player.getCurrentTrack();
        if (track == null) {
            stopForeground(true);
            stopSelf();
            return;
        }

        boolean isPlaying = player.isPlaying();

        // Open app intent
        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent pendingOpen = PendingIntent.getActivity(this, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Toggle intent
        Intent toggleIntent = new Intent(this, MusicService.class);
        toggleIntent.setAction(ACTION_TOGGLE_PLAY);
        PendingIntent pendingToggle = PendingIntent.getService(this, 1, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Stop intent
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStop = PendingIntent.getService(this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(track.name)
                .setContentText(isPlaying ? "Đang phát nhạc nền" : "Đã tạm dừng")
                .setContentIntent(pendingOpen)
                .setOngoing(isPlaying)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play", pendingToggle)
                .addAction(R.drawable.ic_reset, "Stop", pendingStop)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notif,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notif);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Nhạc nền Focus", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Điều khiển nhạc nền Pomodoro");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaSession != null) mMediaSession.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
