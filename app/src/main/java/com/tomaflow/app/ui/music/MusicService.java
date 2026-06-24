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

    @Override
    public void onCreate() {
        super.onCreate();
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
        if (player.getCurrentTrack() == null) {
            stopForeground(true);
            stopSelf();
            return;
        }

        com.tomaflow.app.utils.NotificationHelper helper = new com.tomaflow.app.utils.NotificationHelper(this);
        Notification notif = helper.buildCombinedNotification(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(com.tomaflow.app.constants.AppConstants.NOTIFICATION_ID_TIMER, notif,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(com.tomaflow.app.constants.AppConstants.NOTIFICATION_ID_TIMER, notif);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
