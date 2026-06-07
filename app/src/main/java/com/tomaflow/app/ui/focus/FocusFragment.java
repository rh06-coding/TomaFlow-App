package com.tomaflow.app.ui.focus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.ui.timer.TimerView;
import com.tomaflow.app.ui.timer.TimerViewModel;

import java.util.Locale;

public class FocusFragment extends Fragment {

    private TimerViewModel mTimerViewModel;
    private TimerView mTimerView;
    private TextView mTvTime;
    private TextView mTvSessionLabel;
    private ImageButton mBtnPlayPause;

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
        mTimerView      = v.findViewById(R.id.timer_view);
        mTvTime         = v.findViewById(R.id.tv_time);
        mTvSessionLabel = v.findViewById(R.id.tv_session_label);
        mBtnPlayPause   = v.findViewById(R.id.btn_play_pause);
        ImageButton btnReset = v.findViewById(R.id.btn_reset);
        ImageButton btnSkip = v.findViewById(R.id.btn_skip);
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
    }

    private void setupTimerObserver() {
        mTimerViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            updateTimerDisplay(state.remainingMs);
            updatePlayPauseIcon(state.state);
            updateProgress(state);
            updateSessionLabel(state);
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
        mTimerViewModel.sendCommand(AppConstants.COMMAND_RESET);
    }

    private void onSkipClicked() {
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
        float progress = 0;
        if (state.totalDurationMs > 0) {
            progress = (float) (state.totalDurationMs - state.remainingMs) / state.totalDurationMs;
        }
        mTimerView.setProgress(progress);
    }

    private void updateSessionLabel(PomodoroTimer.TimerState state) {
        if (mTvSessionLabel == null) return;
        mTvSessionLabel.setText(state.phase.getDisplayName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTimerView = null;
        mTvTime = null;
        mTvSessionLabel = null;
        mBtnPlayPause = null;
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
