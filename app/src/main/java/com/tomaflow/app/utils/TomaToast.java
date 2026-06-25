package com.tomaflow.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.tomaflow.app.R;

public class TomaToast {

    public static void show(Context context, String message) {
        show(context, message, false);
    }

    public static void show(Context context, int messageResId) {
        show(context, context.getString(messageResId), false);
    }

    public static void show(Context context, int messageResId, boolean isSuccess) {
        show(context, context.getString(messageResId), isSuccess);
    }

    public static void show(Context context, String message, boolean isSuccess) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.layout_toma_toast, null);

            TextView text = layout.findViewById(R.id.tv_toast_message);
            ImageView icon = layout.findViewById(R.id.iv_toast_icon);

            text.setText(message);

            if (isSuccess) {
                icon.setImageResource(R.drawable.ic_check);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.toma_success));
            } else {
                icon.setImageResource(R.drawable.ic_close);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.toma_error));
            }

            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
