package com.tomaflow.app.ui.focus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.tomaflow.app.timer.PomodoroTimer;

/**
 * Custom bottom sheet dialog shown when a Focus or Break phase completes.
 */
public class PhaseCompleteDialog extends BottomSheetDialogFragment {

    private static final String ARG_IS_FOCUS    = "is_focus";
    private static final String ARG_SESSION_COUNT = "session_count";

    public interface OnPhaseCompleteAction {
        void onPrimaryAction(boolean isFocusComplete);
        void onSkip(boolean isFocusComplete);
    }

    private boolean mIsFocusComplete;
    private int mSessionCount;
    private OnPhaseCompleteAction mListener;

    public static PhaseCompleteDialog newInstance(boolean isFocusComplete, int sessionCount) {
        PhaseCompleteDialog d = new PhaseCompleteDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_FOCUS, isFocusComplete);
        args.putInt(ARG_SESSION_COUNT, sessionCount);
        d.setArguments(args);
        return d;
    }

    public void setListener(OnPhaseCompleteAction listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsFocusComplete = getArguments().getBoolean(ARG_IS_FOCUS, true);
            mSessionCount    = getArguments().getInt(ARG_SESSION_COUNT, 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_phase_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle   = view.findViewById(R.id.tv_dialog_title);
        TextView tvSub     = view.findViewById(R.id.tv_dialog_sub);
        ImageView ivIcon   = view.findViewById(R.id.iv_phase_icon);
        View pulseRing     = view.findViewById(R.id.pulse_ring);
        MaterialButton btnPrimary = view.findViewById(R.id.btn_primary_action);
        MaterialButton btnSkip    = view.findViewById(R.id.btn_skip_dialog);

        if (mIsFocusComplete) {
            tvTitle.setText(getString(R.string.phase_complete_focus_title));
            tvSub.setText(getString(R.string.phase_complete_focus_sub, mSessionCount));
            btnPrimary.setText(getString(R.string.phase_complete_start_break));
            ivIcon.setImageResource(R.drawable.ic_tomato);
        } else {
            tvTitle.setText(getString(R.string.phase_complete_break_title));
            tvSub.setText(getString(R.string.phase_complete_break_sub));
            btnPrimary.setText(getString(R.string.phase_complete_start_focus));
            ivIcon.setImageResource(R.drawable.ic_focus);
        }

        // Pulse animation on the ring
        startPulseAnimation(pulseRing);
        // Scale-in animation on icon
        startIconEntrance(ivIcon);

        btnPrimary.setOnClickListener(v -> {
            dismiss();
            if (mListener != null) mListener.onPrimaryAction(mIsFocusComplete);
        });

        btnSkip.setOnClickListener(v -> {
            dismiss();
            if (mListener != null) mListener.onSkip(mIsFocusComplete);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Animations are tied to the view, will stop automatically
    }
}
