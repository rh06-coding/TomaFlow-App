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
        View parentView = null;
        if (context instanceof Activity) {
            parentView = ((Activity) context).findViewById(android.R.id.content);
        } else if (context instanceof ContextWrapper) {
            Context base = ((ContextWrapper) context).getBaseContext();
            if (base instanceof Activity) {
                parentView = ((Activity) base).findViewById(android.R.id.content);
            }
        }

        if (parentView == null) {
            // Fallback
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Snackbar snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_LONG);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            
            layout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            layout.setPadding(0, 0, 0, 0);
            if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                layout.setLayoutParams(params);
            }

            LayoutInflater inflater = LayoutInflater.from(context);
            View customView = inflater.inflate(R.layout.layout_toma_snackbar, null);

            TextView tvMessage = customView.findViewById(R.id.snackbar_text);
            tvMessage.setText(message);

            ImageView icon = customView.findViewById(R.id.snackbar_icon);
            View container = customView.findViewById(R.id.snackbar_container);

            if (!isSuccess) {
                icon.setImageResource(R.drawable.ic_close);
                container.setBackgroundResource(R.drawable.bg_toma_snackbar_error);
            } else {
                icon.setImageResource(R.drawable.ic_check);
                container.setBackgroundResource(R.drawable.bg_toma_snackbar_success);
            }

            layout.addView(customView, 0);
            snackbar.show();
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
