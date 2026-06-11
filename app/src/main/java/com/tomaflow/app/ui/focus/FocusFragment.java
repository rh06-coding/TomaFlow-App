package com.tomaflow.app.ui.focus;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;

import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.ui.timer.TimerView;
import com.tomaflow.app.ui.timer.TimerViewModel;
import com.tomaflow.app.ui.timer.TomatoGrowthView;

import java.util.Locale;

public class FocusFragment extends Fragment {

    // ── Animation constants ───────────────────────────────────────────────────
    private static final long   PHASE_ANIM_MS        = 400L;
    private static final long   PROGRESS_TICK_MS     = 1100L;
    private static final int    COLOR_FOCUS_ARC      = 0xFFC8324A; // toma_primary
    private static final int    COLOR_SHORT_BREAK_ARC = 0xFF3FA66B; // toma_success
    private static final int    COLOR_LONG_BREAK_ARC  = 0xFF3B82F6; // toma_info
    private static final int    COLOR_FOCUS_BG       = 0xFFF8F5FA; // toma_background
    private static final int    COLOR_SHORT_BREAK_BG  = 0xFFEDF7F1;
    private static final int    COLOR_LONG_BREAK_BG   = 0xFFEAF3FD;

    private TimerViewModel        mTimerViewModel;
    private TimerView             mTimerView;
    private TextView              mTvTime;
    private TextView              mTvSessionLabel;
    private ImageButton           mBtnPlayPause;
    private View                  mRootScrollView;
    private TomatoGrowthView      mTomatoGrowthView;
    private View                  mTomatoWidget;
    private TextView              mTvTomatoStatus;

    /** Pha trước đó — để phát hiện khi nào pha thay đổi. */
    private PomodoroTimer.Phase   mPreviousPhase  = null;
    /** Trạng thái running trước — để phát hiện khi bắt đầu/dừng. */
    private boolean               mPreviousRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_focus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        bindViews(view);
        setupTimerObserver();
    }

    private boolean isTaskCompleted = false;

    private void bindViews(View v) {
        mRootScrollView   = v;
        mTimerView        = v.findViewById(R.id.timer_view);
        mTvTime           = v.findViewById(R.id.tv_time);
        mTvSessionLabel   = v.findViewById(R.id.tv_session_label);
        mBtnPlayPause     = v.findViewById(R.id.btn_play_pause);
        mTomatoGrowthView = v.findViewById(R.id.tomato_growth_view);
        mTomatoWidget     = v.findViewById(R.id.layout_tomato_widget);
        mTvTomatoStatus   = v.findViewById(R.id.tv_tomato_status);
        ImageButton btnReset     = v.findViewById(R.id.btn_reset);
        ImageButton btnSkip      = v.findViewById(R.id.btn_skip);
        CardView cardCurrentTask = v.findViewById(R.id.card_current_task);

        View btnCompleteTask = v.findViewById(R.id.btn_complete_task);
        TextView tvTaskTitle = v.findViewById(R.id.tv_task_title);

        mBtnPlayPause.setOnClickListener(v1 -> onPlayPauseClicked());
        btnReset.setOnClickListener(v1 -> onResetClicked());
        btnSkip.setOnClickListener(v1 -> onSkipClicked());
        cardCurrentTask.setOnClickListener(v1 -> onTaskCardClicked());

        btnCompleteTask.setOnClickListener(v1 -> {
            isTaskCompleted = !isTaskCompleted;
            if (isTaskCompleted) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskTitle.setTextColor(getResources().getColor(R.color.toma_text_muted, null));
                btnCompleteTask.setAlpha(0.5f);
                android.widget.Toast.makeText(getContext(), "Tuyệt vời! Task đã hoàn thành.", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskTitle.setTextColor(getResources().getColor(R.color.toma_text, null));
                btnCompleteTask.setAlpha(1.0f);
            }
        });

        // ── Speed Toggle (debug) ───────────────────────────────────────────────
        MaterialButtonToggleGroup speedToggle = v.findViewById(R.id.speed_toggle);
        speedToggle.check(R.id.btn_speed_1x); // default 1x
        speedToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int speed = 1;
            if      (checkedId == R.id.btn_speed_2x)  speed = 2;
            else if (checkedId == R.id.btn_speed_4x)  speed = 4;
            else if (checkedId == R.id.btn_speed_8x)  speed = 8;
            else if (checkedId == R.id.btn_speed_16x) speed = 16;
            mTimerViewModel.setTimerSpeed(speed);
        });
    }

    private void setupTimerObserver() {
        mTimerViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            updateTimerDisplay(state.remainingMs);
            updatePlayPauseIcon(state.state);
            updateProgress(state);
            updateSessionLabel(state);
            updateTomatoGrowth(state);
        });
    }

    private void onPlayPauseClicked() {
        PomodoroTimer.TimerState state = mTimerViewModel.getTimerState().getValue();
        if (state == null) return;

        if (state.isRunning) {
            mTimerViewModel.sendCommand(AppConstants.COMMAND_PAUSE);
        } else {
            if (state.state == PomodoroTimer.State.IDLE || state.state == PomodoroTimer.State.COMPLETED) {
                mTimerViewModel.sendCommand(AppConstants.COMMAND_START_FOCUS);
            } else {
                mTimerViewModel.sendCommand(AppConstants.COMMAND_RESUME);
            }
        }
    }

    private void onResetClicked() {
        // Nếu đang chạy Focus thì cây héo
        PomodoroTimer.TimerState cur = mTimerViewModel.getTimerState().getValue();
        if (cur != null && cur.isRunning && cur.phase == PomodoroTimer.Phase.FOCUS) {
            wiltTomato();
        }
        mTimerViewModel.sendCommand(AppConstants.COMMAND_RESET);
    }

    private void onSkipClicked() {
        // Nếu đang chạy Focus thì cây héo (skip = bỏ dở)
        PomodoroTimer.TimerState cur = mTimerViewModel.getTimerState().getValue();
        if (cur != null && cur.isRunning && cur.phase == PomodoroTimer.Phase.FOCUS) {
            wiltTomato();
        }
        mTimerViewModel.sendCommand(AppConstants.COMMAND_SKIP);
    }

    private void onTaskCardClicked() {
        // Navigation logic for tasks
    }

    private void updateTimerDisplay(long millis) {
        if (mTvTime == null) return;
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        mTvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updatePlayPauseIcon(PomodoroTimer.State state) {
        if (mBtnPlayPause == null) return;
        boolean isRunning = (state == PomodoroTimer.State.RUNNING_FOCUS || state == PomodoroTimer.State.RUNNING_BREAK);
        mBtnPlayPause.setImageResource(isRunning ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void updateProgress(PomodoroTimer.TimerState state) {
        if (mTimerView == null) return;
        float progress = 0f;
        if (state.totalDurationMs > 0) {
            progress = (float) (state.totalDurationMs - state.remainingMs) / state.totalDurationMs;
        }
        if (state.state == PomodoroTimer.State.IDLE || state.state == PomodoroTimer.State.COMPLETED) {
            // Reset: set ngay, không animate
            mTimerView.setProgress(progress);
        } else {
            // Đang chạy: animate mượt mà đến target, duration hơi dài hơn 1 tick một chút
            mTimerView.animateTo(progress, PROGRESS_TICK_MS);
        }
    }

    // ── Tomato Growth ─────────────────────────────────────────────────────────

    private void updateTomatoGrowth(PomodoroTimer.TimerState state) {
        if (mTomatoGrowthView == null || mTomatoWidget == null) return;

        boolean isRunningFocus = state.isRunning && state.phase == PomodoroTimer.Phase.FOCUS;
        boolean justStarted    = isRunningFocus && !mPreviousRunning;
        boolean justCompleted  = state.state == PomodoroTimer.State.COMPLETED;
        boolean isIdle         = state.state == PomodoroTimer.State.IDLE;

        mPreviousRunning = isRunningFocus;

        if (isIdle) {
            // Reset về ẩn
            hideTomatoWidget();
            mTomatoGrowthView.reset();
            return;
        }

        if (isRunningFocus || state.state == PomodoroTimer.State.PAUSED_FOCUS) {
            // Hiện widget và cập nhật stage
            if (mTomatoWidget.getVisibility() != View.VISIBLE) {
                showTomatoWidget();
            }
            float progress = 0f;
            if (state.totalDurationMs > 0) {
                progress = (float)(state.totalDurationMs - state.remainingMs) / state.totalDurationMs;
            }
            mTomatoGrowthView.setProgress(progress);
            // Cập nhật label
            updateTomatoLabel(progress);
        }

        if (justCompleted) {
            // Quả chín hoàn toàn + celebrate
            mTomatoGrowthView.setProgress(1f);
            mTomatoGrowthView.celebrateComplete();
            if (mTvTomatoStatus != null) {
                mTvTomatoStatus.setText("🍅 Pomodoro hoàn thành!");
            }
        }
    }

    private void updateTomatoLabel(float progress) {
        if (mTvTomatoStatus == null) return;
        String label;
        if (progress < 0.20f)      label = "Hạt giống vừa được gieo...";
        else if (progress < 0.40f) label = "Mầm xanh đang nhú lên!";
        else if (progress < 0.60f) label = "Cây đang lớn mạnh...";
        else if (progress < 0.80f) label = "Hoa nở rồi, sắp có quả!";
        else                       label = "Cà chua sắp chín rồi! 🍅";
        mTvTomatoStatus.setText(label);
    }

    private void wiltTomato() {
        if (mTomatoGrowthView == null) return;
        mTomatoGrowthView.showDead();
        if (mTvTomatoStatus != null) mTvTomatoStatus.setText("Cây đã chết... 😢 Hãy cố lần sau!");
        // Ẩn widget sau 2 giây
        if (mTomatoWidget != null) {
            mTomatoWidget.postDelayed(this::hideTomatoWidget, 2500);
        }
    }

    private void showTomatoWidget() {
        if (mTomatoWidget == null) return;
        mTomatoWidget.setVisibility(View.VISIBLE);
        mTomatoWidget.setAlpha(0f);
        mTomatoWidget.animate().alpha(1f).setDuration(400).start();
    }

    private void hideTomatoWidget() {
        if (mTomatoWidget == null) return;
        mTomatoWidget.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (mTomatoWidget != null) {
                        mTomatoWidget.setVisibility(View.GONE);
                        mTomatoGrowthView.reset();
                    }
                })
                .start();
    }

    private void updateSessionLabel(PomodoroTimer.TimerState state) {
        if (mTvSessionLabel == null) return;

        PomodoroTimer.Phase currentPhase = state.phase;

        // Chỉ trigger animation khi pha thực sự thay đổi
        if (mPreviousPhase != null && mPreviousPhase != currentPhase) {
            animatePhaseTransition(mPreviousPhase, currentPhase);
        }
        mPreviousPhase = currentPhase;

        // Fade text label khi đổi pha
        mTvSessionLabel.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    mTvSessionLabel.setText(currentPhase.getDisplayName());
                    mTvSessionLabel.animate().alpha(1f).setDuration(150).start();
                })
                .start();
    }

    /**
     * Animate màu nền màn hình và màu arc khi chuyển pha.
     */
    private void animatePhaseTransition(PomodoroTimer.Phase from, PomodoroTimer.Phase to) {
        int fromArc = arcColorForPhase(from);
        int toArc   = arcColorForPhase(to);
        int fromBg  = bgColorForPhase(from);
        int toBg    = bgColorForPhase(to);

        // Arc color animation (qua TimerView)
        if (mTimerView != null) {
            mTimerView.animateProgressColor(fromArc, toArc, PHASE_ANIM_MS);
        }

        // Background color animation
        if (mRootScrollView != null) {
            ValueAnimator bgAnimator = ValueAnimator.ofObject(
                    new ArgbEvaluator(), fromBg, toBg);
            bgAnimator.setDuration(PHASE_ANIM_MS);
            bgAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            bgAnimator.addUpdateListener(anim ->
                    mRootScrollView.setBackgroundColor((int) anim.getAnimatedValue()));
            bgAnimator.start();
        }
    }

    private int arcColorForPhase(PomodoroTimer.Phase phase) {
        switch (phase) {
            case SHORT_BREAK: return COLOR_SHORT_BREAK_ARC;
            case LONG_BREAK:  return COLOR_LONG_BREAK_ARC;
            default:          return COLOR_FOCUS_ARC;
        }
    }

    private int bgColorForPhase(PomodoroTimer.Phase phase) {
        switch (phase) {
            case SHORT_BREAK: return COLOR_SHORT_BREAK_BG;
            case LONG_BREAK:  return COLOR_LONG_BREAK_BG;
            default:          return COLOR_FOCUS_BG;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTimerView        = null;
        mTvTime           = null;
        mTvSessionLabel   = null;
        mBtnPlayPause     = null;
        mTomatoGrowthView = null;
        mTomatoWidget     = null;
        mTvTomatoStatus   = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimerViewModel.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
