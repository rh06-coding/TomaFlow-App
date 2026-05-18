package com.tomaflow.app;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.timer.TimerEngineService;
import com.tomaflow.app.ui.timer.TimerView;
import com.tomaflow.app.ui.timer.TimerViewModel;
import com.tomaflow.app.utils.TimerUtils;

public class MainActivity extends AppCompatActivity {
    private TimerView mTimerView;
    private TextView mTvTime;
    private TextView mTvSessionLabel;
    private ImageButton mBtnPlayPause;
    private ImageButton mBtnReset;
    private ImageButton mBtnSkip;
    private CardView mCardCurrentTask;
    private TextView mTvTaskTitle;
    private TextView mTvTaskSubtitle;
    private ImageView mIvTaskIcon;
    private BottomNavigationView mBottomNav;

    private TimerViewModel mTimerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);

        bindViews();
        setupTimerObserver();
        setupBottomNavigation();
        setupBackPressedHandler();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                PomodoroTimer.TimerState state = mTimerViewModel.getTimerState().getValue();
                if (state != null && state.isRunning) {
                    moveTaskToBack(true);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureTimerServiceStarted();
        mTimerViewModel.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimerViewModel.stopListening();
    }

    private void ensureTimerServiceStarted() {
        Intent serviceIntent = new Intent(this, TimerEngineService.class);
        startService(serviceIntent);
    }

    private void bindViews() {
        mTimerView      = findViewById(R.id.timer_view);
        mTvTime         = findViewById(R.id.tv_time);
        mTvSessionLabel = findViewById(R.id.tv_session_label);
        mBtnPlayPause   = findViewById(R.id.btn_play_pause);
        mBtnReset       = findViewById(R.id.btn_reset);
        mBtnSkip        = findViewById(R.id.btn_skip);
        mCardCurrentTask = findViewById(R.id.card_current_task);
        mTvTaskTitle    = findViewById(R.id.tv_task_title);
        mTvTaskSubtitle = findViewById(R.id.tv_task_subtitle);
        mIvTaskIcon     = findViewById(R.id.iv_task_icon);
        mBottomNav      = findViewById(R.id.bottom_navigation);

        mBtnPlayPause.setOnClickListener(v -> onPlayPauseClicked());
        mBtnReset.setOnClickListener(v -> onResetClicked());
        mBtnSkip.setOnClickListener(v -> onSkipClicked());
        mCardCurrentTask.setOnClickListener(v -> onTaskCardClicked());

        mTvTaskTitle.setText(R.string.placeholder_task_title);
        mTvTaskSubtitle.setText(R.string.placeholder_task_subtitle);
    }

    private void setupTimerObserver() {
        mTimerViewModel.getTimerState().observe(this, timerState -> {
            if (timerState == null) return;

            updateTimerDisplay(timerState.remainingMs);
            updatePlayPauseIcon(timerState.state);
            updateProgress(timerState);
            updateSessionLabel(timerState);
        });
    }

    private void onPlayPauseClicked() {
        PomodoroTimer.TimerState currentState = mTimerViewModel.getTimerState().getValue();
        if (currentState == null) {
            sendCommand(AppConstants.COMMAND_START_FOCUS);
        } else if (currentState.isRunning) {
            sendCommand(AppConstants.COMMAND_PAUSE);
        } else if (currentState.state == PomodoroTimer.State.IDLE) {
            sendCommand(AppConstants.COMMAND_START_FOCUS);
        } else {
            sendCommand(AppConstants.COMMAND_RESUME);
        }
    }

    private void onResetClicked() {
        sendCommand(AppConstants.COMMAND_RESET);
        animateProgress(mTimerView.getProgress(), 0f);
    }

    private void onSkipClicked() {
        sendCommand(AppConstants.COMMAND_SKIP);
        Toast.makeText(this, R.string.session_skipped, Toast.LENGTH_SHORT).show();
    }

    private void onTaskCardClicked() {
        boolean isActivated = !mIvTaskIcon.isActivated();
        mIvTaskIcon.setActivated(isActivated);

        if (isActivated) {
            mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        String message = isActivated ? "Task marked as done" : "Task resumed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateTimerDisplay(long millisLeft) {
        mTvTime.setText(TimerUtils.formatMillisToMmSs(millisLeft));
    }

    private void updatePlayPauseIcon(PomodoroTimer.State state) {
        boolean isRunning = state == PomodoroTimer.State.RUNNING_FOCUS || state == PomodoroTimer.State.RUNNING_BREAK;
        if (isRunning) {
            mBtnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void updateProgress(PomodoroTimer.TimerState timerState) {
        long duration = timerState.phase == PomodoroTimer.Phase.FOCUS
                ? AppConstants.TIMER_WORK_DURATION_MS
                : AppConstants.TIMER_SHORT_BREAK_MS;
        float progress = duration > 0 ? 1f - ((float) timerState.remainingMs / duration) : 0f;
        mTimerView.setProgress(Math.max(0, Math.min(1, progress)));
    }

    private void updateSessionLabel(PomodoroTimer.TimerState timerState) {
        int displayCount = timerState.phase == PomodoroTimer.Phase.FOCUS
                ? timerState.sessionCount + 1
                : timerState.sessionCount;
        String label = String.format("%s — Session %d", timerState.phase.getDisplayName(), displayCount);
        mTvSessionLabel.setText(label);
    }

    private void animateProgress(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> mTimerView.setProgress((float) a.getAnimatedValue()));
        animator.start();
    }

    private void sendCommand(String command) {
        mTimerViewModel.sendCommand(command);
    }

    private void setupBottomNavigation() {
        mBottomNav.setSelectedItemId(R.id.nav_focus);
        mBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus) {
                return true;
            } else if (id == R.id.nav_tasks) {
                // TODO: navigate to TasksActivity
                return true;
            } else if (id == R.id.nav_stats) {
                // TODO: navigate to StatsActivity
                return true;
            } else if (id == R.id.nav_rewards) {
                // TODO: navigate to RewardsActivity
                return true;
            } else if (id == R.id.nav_settings) {
                // TODO: navigate to SettingsActivity
                return true;
            }
            return false;
        });
    }
}
