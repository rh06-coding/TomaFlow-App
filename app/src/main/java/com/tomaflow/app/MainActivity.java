package com.tomaflow.app;

import android.animation.ValueAnimator;
import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.timer.TimerEngineService;
import com.tomaflow.app.ui.timer.TimerView;
import com.tomaflow.app.ui.timer.TimerViewModel;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView mBottomNav;
    private TimerViewModel mTimerViewModel;

    // These views will be moved to FocusFragment eventually.
    // Keeping them here for now to avoid breaking the build if they are referenced elsewhere.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);

        bindViews();
        setupBottomNavigation();
        setupBackPressedHandler();
        setupTimerObserver();

        ensureTimerServiceStarted();
    }

    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            private long lastPressedTime;
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - lastPressedTime < 2000) {
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                    lastPressedTime = System.currentTimeMillis();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void ensureTimerServiceStarted() {
        Intent serviceIntent = new Intent(this, TimerEngineService.class);
        startService(serviceIntent);
    }

    private void bindViews() {
        mBottomNav = findViewById(R.id.bottom_nav);
        
        // These are in fragment_focus.xml, so they will be null here.
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

        if (mBtnPlayPause != null) mBtnPlayPause.setOnClickListener(v -> onPlayPauseClicked());
        if (mBtnReset != null) mBtnReset.setOnClickListener(v -> onResetClicked());
        if (mBtnSkip != null) mBtnSkip.setOnClickListener(v -> onSkipClicked());
        if (mCardCurrentTask != null) mCardCurrentTask.setOnClickListener(v -> onTaskCardClicked());
    }

    private void setupTimerObserver() {
        mTimerViewModel.getTimerState().observe(this, state -> {
            if (state == null) return;
            updateTimerDisplay(state.remainingMs);
            updatePlayPauseIcon(state.state);
            updateProgress(state);
            updateSessionLabel(state);
        });
    }

    private void onPlayPauseClicked() {
        // Toggle logic placeholder
    }

    private void onResetClicked() {
        sendCommand(AppConstants.COMMAND_RESET);
    }

    private void onSkipClicked() {
        sendCommand(AppConstants.COMMAND_SKIP);
    }

    private void onTaskCardClicked() {
        mBottomNav.setSelectedItemId(R.id.nav_tasks);
    }

    private void updateTimerDisplay(long millis) {
        if (mTvTime == null) return;
    }

    private void updatePlayPauseIcon(com.tomaflow.app.timer.PomodoroTimer.State state) {
        if (mBtnPlayPause == null) return;
    }

    private void updateProgress(com.tomaflow.app.timer.PomodoroTimer.TimerState state) {
        if (mTimerView == null) return;
    }

    private void updateSessionLabel(com.tomaflow.app.timer.PomodoroTimer.TimerState state) {
        if (mTvSessionLabel == null) return;
    }

    private void animateProgress(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            if (mTimerView != null) {
                mTimerView.setProgress((float) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void sendCommand(String action) {
        Intent intent = new Intent(this, TimerEngineService.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_COMMAND, action);
        startService(intent);
    }

    private void setupBottomNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(mBottomNav, navController);
        }
    }
}
