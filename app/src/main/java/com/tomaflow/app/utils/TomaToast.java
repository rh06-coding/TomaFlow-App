package com.tomaflow.app.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomaflow.app.R;

public class TomaToast {

    public static void show(Context context, String message) {
        show(context, message, false);
    }

    public static void show(Context context, int messageResId) {
        show(context, context.getString(messageResId), false);
    }

    public static void show(Context context, int messageResId, boolean isError) {
        show(context, context.getString(messageResId), isError);
    }

    public static void show(Context context, String message, boolean isError) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.layout_toma_toast, null);

            TextView tvMessage = layout.findViewById(R.id.tv_toast_message);
            tvMessage.setText(message);
            
            ImageView icon = layout.findViewById(R.id.iv_toast_icon);
            if (isError) {
                icon.setImageResource(R.drawable.ic_close);
                icon.setColorFilter(context.getResources().getColor(R.color.toma_error, null));
            } else {
                icon.setImageResource(R.drawable.ic_check);
                icon.setColorFilter(context.getResources().getColor(R.color.toma_primary, null));
            }

            Toast toast = new Toast(context.getApplicationContext());
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            // Fallback
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
