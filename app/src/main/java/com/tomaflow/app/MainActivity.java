package com.tomaflow.app;

import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tomaflow.app.ui.timer.TimerView;

/**
 * MainActivity — Home Screen (Focus Tab)
 *
 * Displays the circular Pomodoro countdown timer (default 25:00), a Start/Pause
 * button, a Reset button, a Skip button, and a card showing the currently
 * active task. All timer state is kept in-memory; persistence will be wired
 * through the Room database layer once that package is implemented.
 */
public class MainActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Timer constants
    // -------------------------------------------------------------------------
    private static final long WORK_DURATION_MS  = 25 * 60 * 1000L; // 25 minutes
    private static final long COUNTDOWN_INTERVAL = 1_000L;          // 1 second tick

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private enum TimerState { IDLE, RUNNING, PAUSED }

    private TimerState  mTimerState   = TimerState.IDLE;
    private long        mTimeLeftMs   = WORK_DURATION_MS;
    private CountDownTimer mCountDownTimer;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------
    private TimerView       mTimerView;
    private TextView        mTvTime;
    private TextView        mTvSessionLabel;
    private ImageButton     mBtnPlayPause;
    private ImageButton     mBtnReset;
    private ImageButton     mBtnSkip;
    private CardView        mCardCurrentTask;
    private TextView        mTvTaskTitle;
    private TextView        mTvTaskSubtitle;
    private ImageView       mIvTaskIcon;
    private BottomNavigationView mBottomNav;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupBottomNavigation();
        updateTimerDisplay(mTimeLeftMs);
        updatePlayPauseIcon();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel timer to avoid memory leak when activity goes to background.
        // Time left is preserved so the user can resume.
        cancelTimer();
    }

    // -------------------------------------------------------------------------
    // View binding
    // -------------------------------------------------------------------------

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

        // Populate task card with placeholder data (replace with DB query later)
        mTvTaskTitle.setText(R.string.placeholder_task_title);
        mTvTaskSubtitle.setText(R.string.placeholder_task_subtitle);
    }

    // -------------------------------------------------------------------------
    // Timer controls
    // -------------------------------------------------------------------------

    private void onPlayPauseClicked() {
        switch (mTimerState) {
            case IDLE:
            case PAUSED:
                startTimer();
                break;
            case RUNNING:
                pauseTimer();
                break;
        }
    }

    private void onResetClicked() {
        cancelTimer();
        mTimerState = TimerState.IDLE;
        mTimeLeftMs = WORK_DURATION_MS;
        updateTimerDisplay(mTimeLeftMs);
        animateProgress(mTimerView.getProgress(), 0f);
        updatePlayPauseIcon();
    }

    private void onSkipClicked() {
        cancelTimer();
        mTimerState = TimerState.IDLE;
        mTimeLeftMs = 0;
        updateTimerDisplay(0);
        mTimerView.setProgress(1f); // full ring = session complete
        updatePlayPauseIcon();
        Toast.makeText(this, R.string.session_skipped, Toast.LENGTH_SHORT).show();
    }

    private void onTaskCardClicked() {
        // Toggle the 'activated' state of the icon
        boolean isActivated = !mIvTaskIcon.isActivated();
        mIvTaskIcon.setActivated(isActivated);

        // Optional: Strike through the title when completed
        if (isActivated) {
            mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        String message = isActivated ? "Task marked as done" : "Task resumed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        mTimerState = TimerState.RUNNING;
        updatePlayPauseIcon();

        mCountDownTimer = new CountDownTimer(mTimeLeftMs, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftMs = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);

                float progress = 1f - ((float) millisUntilFinished / WORK_DURATION_MS);
                mTimerView.setProgress(progress);
            }

            @Override
            public void onFinish() {
                mTimerState = TimerState.IDLE;
                mTimeLeftMs = 0;
                updateTimerDisplay(0);
                mTimerView.setProgress(1f);
                updatePlayPauseIcon();
                onTimerFinished();
            }
        }.start();
    }

    private void pauseTimer() {
        cancelTimer();
        mTimerState = TimerState.PAUSED;
        updatePlayPauseIcon();
    }

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    // -------------------------------------------------------------------------
    // Timer finished callback
    // -------------------------------------------------------------------------

    private void onTimerFinished() {
        Toast.makeText(this, R.string.session_complete, Toast.LENGTH_LONG).show();
        // TODO: Trigger notification, save session to DB via repository
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    /** Formats milliseconds as MM:SS and pushes to the time TextView. */
    private void updateTimerDisplay(long millisLeft) {
        long totalSeconds = millisLeft / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        mTvTime.setText(timeText);
    }

    /** Swaps the play/pause icon depending on the current state. */
    private void updatePlayPauseIcon() {
        if (mTimerState == TimerState.RUNNING) {
            mBtnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    /**
     * Smoothly animates the circular progress arc from {@code from} to {@code to}.
     * Used when resetting so the ring doesn't jump abruptly.
     */
    private void animateProgress(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> mTimerView.setProgress((float) a.getAnimatedValue()));
        animator.start();
    }

    // -------------------------------------------------------------------------
    // Bottom navigation
    // -------------------------------------------------------------------------

    private void setupBottomNavigation() {
        mBottomNav.setSelectedItemId(R.id.nav_focus);
        mBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus) {
                return true; // already here
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
