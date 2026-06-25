package com.tomaflow.app.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.tomaflow.app.R;
import com.tomaflow.app.timer.SettingsManager;

public class DndManager {

    public static void checkAndEnableDnd(Context context) {
        SettingsManager settings = new SettingsManager(context);
        if (!settings.isDndMode()) return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Need permission
            com.tomaflow.app.utils.TomaToast.show(context, R.string.dnd_permission_needed, false);
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void checkAndDisableDnd(Context context) {
        SettingsManager settings = new SettingsManager(context);
        if (!settings.isDndMode()) return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
