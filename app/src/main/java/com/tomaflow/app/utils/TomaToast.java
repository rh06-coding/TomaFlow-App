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

        if (parentView != null) {
            try {
                Snackbar snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_LONG);
                if (!isSuccess) {
                    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.toma_error));
                } else {
                    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.toma_success));
                }
                snackbar.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                
                View snackbarView = snackbar.getView();
                float dp = 4f; // Tighter shadow
                snackbarView.setElevation(android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    snackbarView.setOutlineSpotShadowColor(android.graphics.Color.parseColor("#4D000000")); // Darker spot shadow (30% black)
                    snackbarView.setOutlineAmbientShadowColor(android.graphics.Color.parseColor("#80000000")); // Darker ambient shadow (50% black)
                }

                snackbar.show();
            } catch (Exception e) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
