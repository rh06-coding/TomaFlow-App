package com.tomaflow.app.ui.focus;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.tomaflow.app.R;

public class TaskCompleteDialog extends BottomSheetDialogFragment {

    private static final String ARG_TASK_NAME = "task_name";

    public interface OnTaskCompleteAction {
        void onDismiss();
    }

    private String mTaskName;
    private OnTaskCompleteAction mListener;

    public static TaskCompleteDialog newInstance(String taskName) {
        TaskCompleteDialog d = new TaskCompleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        d.setArguments(args);
        return d;
    }

    public void setListener(OnTaskCompleteAction listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTaskName = getArguments().getString(ARG_TASK_NAME, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_task_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivIcon   = view.findViewById(R.id.iv_task_icon);
        View pulseRing     = view.findViewById(R.id.pulse_ring);
        MaterialButton btnPrimary = view.findViewById(R.id.btn_primary_action);

        // Pulse animation on the ring
        startPulseAnimation(pulseRing);
        // Scale-in animation on icon
        startIconEntrance(ivIcon);

        btnPrimary.setOnClickListener(v -> {
            dismiss();
            if (mListener != null) mListener.onDismiss();
        });
    }

    private void startPulseAnimation(View target) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, "scaleX", 0.8f, 1.3f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, "scaleY", 0.8f, 1.3f, 0.8f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(target, "alpha", 0.4f, 0.1f, 0.4f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.RESTART);
        scaleX.setDuration(2000);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.RESTART);
        scaleY.setDuration(2000);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatMode(ObjectAnimator.RESTART);
        alpha.setDuration(2000);
        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(scaleX, scaleY, alpha);
        pulse.start();
    }

    private void startIconEntrance(View target) {
        target.setScaleX(0f);
        target.setScaleY(0f);
        target.animate()
              .scaleX(1f).scaleY(1f)
              .setDuration(400)
              .setInterpolator(new FastOutSlowInInterpolator())
              .start();
    }
}
