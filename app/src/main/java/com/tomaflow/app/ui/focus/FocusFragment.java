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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.databinding.FragmentFocusBinding;
import com.tomaflow.app.timer.PomodoroTimer;
import com.tomaflow.app.ui.timer.TimerView;
import com.tomaflow.app.ui.timer.TimerViewModel;
import com.tomaflow.app.ui.timer.TomatoGrowthView;
import com.tomaflow.app.ui.tasks.TaskViewModel;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Locale;

public class FocusFragment extends Fragment {

    // ── Animation constants ───────────────────────────────────────────────────
    private static final long   PHASE_ANIM_MS        = 400L;
    private static final long   PROGRESS_TICK_MS     = 1100L;
    private static final int    COLOR_FOCUS_ARC      = 0xFFC8324A; // toma_primary
    private static final int    COLOR_SHORT_BREAK_ARC = 0xFF3FA66B; // toma_success
    private static final int    COLOR_LONG_BREAK_ARC  = 0xFF3B82F6; // toma_info

    private FragmentFocusBinding binding;
    private TimerViewModel        mTimerViewModel;
    private TaskViewModel         mTaskViewModel;
    private TaskEntity            mCurrentTask;
    private TimerView             mTimerView;
    private TextView              mTvTime;
    private TextView              mTvSessionLabel;
    private ImageButton           mBtnPlayPause;
    private TomatoGrowthView      mTomatoGrowthView;
    private View                  mTomatoWidget;
    private TextView              mTvTomatoStatus;
    private TextView              mTvTaskTitle;
    private TextView              mTvTaskSubtitle;
    private TextView              mTvTaskPomos;

    /** Pha trước đó — để phát hiện khi nào pha thay đổi. */
    private PomodoroTimer.Phase   mPreviousPhase  = null;
    /** Trạng thái running trước — để phát hiện khi bắt đầu/dừng. */
    private boolean               mPreviousRunning = false;
    private MaterialButtonToggleGroup mSessionToggle;

    private TextView mTvMusicName;
    private com.tomaflow.app.data.model.BuiltInTrack mSelectedBuiltInTrack;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> musicPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                // Everything is now handled by AppMusicPlayer and mMusicListener
            });

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> taskPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    boolean isCleared = result.getData().getBooleanExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_CLEAR_TASK, false);
                    if (isCleared) {
                        mCurrentTask = null;
                        mTimerViewModel.setCurrentTaskId(null);
                        if (mTvTaskTitle != null) mTvTaskTitle.setText(getString(R.string.focus_no_task));
                        if (mTvTaskSubtitle != null) mTvTaskSubtitle.setText(getString(R.string.focus_no_task_sub));
                        if (mTvTaskPomos != null) mTvTaskPomos.setVisibility(View.GONE);
                    } else {
                        String taskId = result.getData().getStringExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_TASK_ID);
                        String taskName = result.getData().getStringExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_TASK_NAME);
                        String taskDesc = result.getData().getStringExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_TASK_DESC);
                        
                        if (taskId != null && FocusFragment.this.mPendingTasks != null) {
                            for (TaskEntity t : FocusFragment.this.mPendingTasks) {
                                if (t.taskId.equals(taskId)) {
                                    mCurrentTask = t;
                                    break;
                                }
                            }
                        }
                        if (mCurrentTask == null && taskId != null && taskName != null) {
                            // Fallback in case not in pending list but returned
                            mCurrentTask = new TaskEntity();
                            mCurrentTask.taskId = taskId;
                            mCurrentTask.title = taskName;
                            mCurrentTask.description = taskDesc;
                        }
                        
                        if (mCurrentTask != null) {
                            mTimerViewModel.setCurrentTaskId(mCurrentTask.taskId);
                        }
                        updateTaskUI();
                    }
                }
            });

    private void updateTaskUI() {
        if (mCurrentTask != null) {
            if (mTvTaskTitle != null) {
                mTvTaskTitle.setText(mCurrentTask.title);
                mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                mTvTaskTitle.setTextColor(getResources().getColor(R.color.toma_text, null));
            }
            if (mTvTaskSubtitle != null) {
                mTvTaskSubtitle.setText(mCurrentTask.description == null || mCurrentTask.description.trim().isEmpty() ? getString(R.string.focus_task_default_sub) : mCurrentTask.description);
            }
            if (mTvTaskPomos != null) {
                mTvTaskPomos.setVisibility(View.VISIBLE);
                mTvTaskPomos.setText(String.valueOf(mCurrentTask.estPomodoros));
            }
        } else {
            if (mTvTaskTitle != null) mTvTaskTitle.setText(getString(R.string.focus_no_task));
            if (mTvTaskSubtitle != null) mTvTaskSubtitle.setText(getString(R.string.focus_no_task_sub));
            if (mTvTaskPomos != null) mTvTaskPomos.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFocusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.tomaflow.app.utils.HeaderUIHelper.setupHeader(view, getString(R.string.nav_focus), getViewLifecycleOwner());

        mTimerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);
        mTaskViewModel  = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_profile));
        }

        bindViews();
        setupTimerObserver();
        setupTaskObserver();
    }

    private boolean isTaskCompleted = false;

    private void bindViews() {
        mTimerView        = binding.timerView;
        mTvTime           = binding.tvTime;
        mTvSessionLabel   = binding.tvSessionLabel;
        mBtnPlayPause     = binding.btnPlayPause;
        mTomatoGrowthView = binding.tomatoGrowthView;
        mTomatoWidget     = binding.layoutTomatoWidget;
        mTvTomatoStatus   = binding.tvTomatoStatus;

        mTvTaskTitle    = binding.tvTaskTitle;
        mTvTaskSubtitle = binding.tvTaskSubtitle;
        mTvTaskPomos    = binding.tvTaskPomos;

        mTvTaskTitle.setText(getString(R.string.focus_no_task));
        mTvTaskSubtitle.setText(getString(R.string.focus_no_task_sub));
        if (mTvTaskPomos != null) mTvTaskPomos.setVisibility(View.GONE);

        mBtnPlayPause.setOnClickListener(v1 -> onPlayPauseClicked());
        binding.btnReset.setOnClickListener(v1 -> onResetClicked());
        binding.btnSkip.setOnClickListener(v1 -> onSkipClicked());
        binding.cardCurrentTask.setOnClickListener(v1 -> onTaskCardClicked());

        // ── Session Tab Switcher ───────────────────────────────────────────────
        mSessionToggle = binding.sessionToggle;
        if (mSessionToggle != null) {
            mSessionToggle.check(R.id.btn_tab_focus); // default
            mSessionToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                if (checkedId == R.id.btn_tab_focus) {
                    mTimerViewModel.jumpToPhase(PomodoroTimer.Phase.FOCUS);
                } else if (checkedId == R.id.btn_tab_short) {
                    mTimerViewModel.jumpToPhase(PomodoroTimer.Phase.SHORT_BREAK);
                } else if (checkedId == R.id.btn_tab_long) {
                    mTimerViewModel.jumpToPhase(PomodoroTimer.Phase.LONG_BREAK);
                }
            });
        }

        mTvMusicName = binding.tvMusicName;
        View btnPickMusic = binding.btnPickMusic;
        View btnPlayMusic = binding.btnPlayMusic;

        if (btnPickMusic != null) {
            btnPickMusic.setOnClickListener(v1 -> {
                android.content.Intent intent = new android.content.Intent(getContext(), com.tomaflow.app.ui.music.MusicPickerActivity.class);
                if (mTvMusicName != null && !mTvMusicName.getText().toString().equals(getString(R.string.focus_music_empty))) {
                    intent.putExtra(com.tomaflow.app.ui.music.MusicPickerActivity.EXTRA_TRACK_NAME, mTvMusicName.getText().toString());
                }
                musicPickerLauncher.launch(intent);
            });
        }

        if (btnPlayMusic != null) {
            btnPlayMusic.setOnClickListener(v1 -> {
                com.tomaflow.app.ui.music.AppMusicPlayer player = com.tomaflow.app.ui.music.AppMusicPlayer.getInstance();
                if (player.isPlaying()) {
                    player.pause(requireContext());
                } else {
                    if (mSelectedBuiltInTrack != null) {
                        player.play(requireContext(), mSelectedBuiltInTrack);
                    } else if (player.getCurrentTrack() != null) {
                        player.resume(requireContext());
                    } else {
                        com.tomaflow.app.utils.TomaToast.show(getContext(), R.string.focus_select_music_first, false);
                    }
                }
            });
        }
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
            if (state.phase == PomodoroTimer.Phase.FOCUS) {
                com.tomaflow.app.timer.SettingsManager settings = new com.tomaflow.app.timer.SettingsManager(requireContext());
                if (settings.isStrictMode()) {
                    com.tomaflow.app.utils.TomaToast.show(requireContext(), R.string.strict_mode_blocked, false);
                    return;
                }
            }
            mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_PAUSE);
        } else {
            if (state.state == PomodoroTimer.State.IDLE) {
                mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_START);
            } else {
                mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_RESUME);
            }
        }
    }

    private void onResetClicked() {
        PomodoroTimer.TimerState cur = mTimerViewModel.getTimerState().getValue();
        if (cur != null && cur.phase == PomodoroTimer.Phase.FOCUS) {
            com.tomaflow.app.timer.SettingsManager settings = new com.tomaflow.app.timer.SettingsManager(requireContext());
            if (settings.isStrictMode()) {
                com.tomaflow.app.utils.TomaToast.show(requireContext(), R.string.strict_mode_blocked, false);
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_reset_title)
                    .setMessage(R.string.confirm_reset_msg)
                    .setPositiveButton(R.string.confirm_yes, (dialog, which) -> {
                        if (cur.isRunning) {
                            wiltTomato();
                        }
                        mTimerViewModel.sendCommand(AppConstants.COMMAND_RESET);
                    })
                    .setNegativeButton(R.string.confirm_no, null)
                    .show();
        } else {
            mTimerViewModel.sendCommand(AppConstants.COMMAND_RESET);
        }
    }

    private void onSkipClicked() {
        PomodoroTimer.TimerState cur = mTimerViewModel.getTimerState().getValue();
        if (cur != null && cur.phase == PomodoroTimer.Phase.FOCUS) {
            com.tomaflow.app.timer.SettingsManager settings = new com.tomaflow.app.timer.SettingsManager(requireContext());
            if (settings.isStrictMode()) {
                com.tomaflow.app.utils.TomaToast.show(requireContext(), R.string.strict_mode_blocked, false);
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_skip_title)
                    .setMessage(R.string.confirm_skip_msg)
                    .setPositiveButton(R.string.confirm_yes_skip, (dialog, which) -> {
                        if (cur.isRunning) {
                            wiltTomato();
                        }
                        mTimerViewModel.sendCommand(AppConstants.COMMAND_SKIP);
                    })
                    .setNegativeButton(R.string.confirm_no, null)
                    .show();
        } else {
            mTimerViewModel.sendCommand(AppConstants.COMMAND_SKIP);
        }
    }

    private List<TaskEntity> mPendingTasks;

    private void setupTaskObserver() {
        mTaskViewModel.getPendingTasks().observe(getViewLifecycleOwner(), tasks -> {
            mPendingTasks = tasks;
            
            // Khôi phục UI của task nếu đang được chọn trong TimerViewModel
            String currentTaskId = mTimerViewModel.getCurrentTaskId();
            if (currentTaskId != null && mCurrentTask == null) {
                for (TaskEntity t : tasks) {
                    if (t.taskId.equals(currentTaskId)) {
                        mCurrentTask = t;
                        break;
                    }
                }
            } else if (currentTaskId != null && mCurrentTask != null) {
                // Update current task pomodoro count dynamically
                for (TaskEntity t : tasks) {
                    if (t.taskId.equals(currentTaskId)) {
                        mCurrentTask = t;
                        break;
                    }
                }
            }
            // Always refresh UI when tasks change to reflect latest state
            updateTaskUI();
        });

        mTimerViewModel.getTaskCompletedEvent().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                TaskCompleteDialog dialog = TaskCompleteDialog.newInstance(mCurrentTask != null ? mCurrentTask.title : "");
                dialog.setListener(() -> {
                    mCurrentTask = null;
                    mTimerViewModel.setCurrentTaskId(null);
                    updateTaskUI();
                });
                dialog.show(getParentFragmentManager(), "task_complete");

                // Consume event
                mTimerViewModel.getTaskCompletedEvent().setValue(false);
            }
        });

        mTimerViewModel.getFocusCompleteEvent().observe(getViewLifecycleOwner(), sessionCount -> {
            if (sessionCount == null || sessionCount <= 0) return;
            // Consume immediately
            mTimerViewModel.getFocusCompleteEvent().setValue(null);
            showPhaseCompleteDialog(true, sessionCount);
        });

        mTimerViewModel.getBreakCompleteEvent().observe(getViewLifecycleOwner(), sessionCount -> {
            if (sessionCount == null || sessionCount <= 0) return;
            // Consume immediately
            mTimerViewModel.getBreakCompleteEvent().setValue(null);
            showPhaseCompleteDialog(false, sessionCount);
        });
    }

    private void showPhaseCompleteDialog(boolean isFocusComplete, int sessionCount) {
        if (!isAdded() || getParentFragmentManager() == null) return;
        PhaseCompleteDialog dialog = PhaseCompleteDialog.newInstance(isFocusComplete, sessionCount);
        dialog.setListener(new PhaseCompleteDialog.OnPhaseCompleteAction() {
            @Override
            public void onPrimaryAction(boolean isFocus) {
                // Start the next appropriate phase
                if (isFocus) {
                    PomodoroTimer.TimerState state = mTimerViewModel.getTimerState().getValue();
                    if (state != null && !state.isRunning) {
                        mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_RESUME);
                    }
                    syncTabWithCurrentPhase();
                } else {
                    mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_START);
                    if (mSessionToggle != null) mSessionToggle.check(R.id.btn_tab_focus);
                }
            }
            @Override
            public void onSkip(boolean isFocus) {
                mTimerViewModel.sendCommand(com.tomaflow.app.constants.AppConstants.COMMAND_SKIP);
                syncTabWithCurrentPhase();
            }
        });
        dialog.show(getParentFragmentManager(), "phase_complete");
    }

    private void syncTabWithCurrentPhase() {
        if (mSessionToggle == null) return;
        PomodoroTimer.TimerState state = mTimerViewModel.getTimerState().getValue();
        if (state == null) return;
        switch (state.phase) {
            case SHORT_BREAK: mSessionToggle.check(R.id.btn_tab_short); break;
            case LONG_BREAK:  mSessionToggle.check(R.id.btn_tab_long);  break;
            default:          mSessionToggle.check(R.id.btn_tab_focus); break;
        }
    }

    private void onTaskCardClicked() {
        android.content.Intent intent = new android.content.Intent(getContext(), com.tomaflow.app.ui.tasks.TaskPickerActivity.class);
        if (mCurrentTask != null) {
            intent.putExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_TASK_ID, mCurrentTask.taskId);
            intent.putExtra(com.tomaflow.app.ui.tasks.TaskPickerActivity.EXTRA_TASK_NAME, mCurrentTask.title);
        }
        taskPickerLauncher.launch(intent);
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
                mTvTomatoStatus.setText(getString(R.string.toma_complete));
            }
        }
    }

    private void updateTomatoLabel(float progress) {
        if (mTvTomatoStatus == null) return;
        String label;
        if (progress < 0.20f)      label = getString(R.string.toma_stage_seed);
        else if (progress < 0.40f) label = getString(R.string.toma_stage_sprout);
        else if (progress < 0.60f) label = getString(R.string.toma_stage_grow);
        else if (progress < 0.80f) label = getString(R.string.toma_stage_flower);
        else                       label = getString(R.string.toma_stage_ripe);
        mTvTomatoStatus.setText(label);
    }

    private void wiltTomato() {
        if (mTomatoGrowthView == null) return;
        mTomatoGrowthView.showDead();
        if (mTvTomatoStatus != null) mTvTomatoStatus.setText(getString(R.string.toma_wilt));
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

        // Sync session toggle tab highlight
        if (mSessionToggle != null && mPreviousPhase != currentPhase) {
            switch (currentPhase) {
                case SHORT_BREAK: mSessionToggle.check(R.id.btn_tab_short); break;
                case LONG_BREAK:  mSessionToggle.check(R.id.btn_tab_long);  break;
                default:          mSessionToggle.check(R.id.btn_tab_focus); break;
            }
        }

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
        if (binding != null) {
            ValueAnimator bgAnimator = ValueAnimator.ofObject(
                    new ArgbEvaluator(), fromBg, toBg);
            bgAnimator.setDuration(PHASE_ANIM_MS);
            bgAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            bgAnimator.addUpdateListener(anim ->
                    binding.getRoot().setBackgroundColor((int) anim.getAnimatedValue()));
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
        // In dark mode, use dark-friendly background colors instead of hardcoded light colors
        boolean isDark = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        if (isDark) {
            switch (phase) {
                case SHORT_BREAK: return 0xFF1B2E24; // toma_success_soft dark
                case LONG_BREAK:  return 0xFF1E293B; // toma_info_soft dark
                default:          return 0xFF0F0F14; // toma_background dark
            }
        } else {
            switch (phase) {
                case SHORT_BREAK: return 0xFFEDF7F1;
                case LONG_BREAK:  return 0xFFEAF3FD;
                default:          return 0xFFF8F5FA;
            }
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
        binding = null;
    }

    private final com.tomaflow.app.ui.music.AppMusicPlayer.OnPlaybackStateChanged mMusicListener = (isPlaying, track) -> {
        if (binding != null) {
            android.widget.ImageView btnPlay = binding.btnPlayMusic;
            if (btnPlay != null) {
                btnPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
            if (track != null) {
                mSelectedBuiltInTrack = track;
                if (mTvMusicName != null) {
                    mTvMusicName.setText(track.name);
                }
            } else {
                mSelectedBuiltInTrack = null;
                if (mTvMusicName != null) {
                    mTvMusicName.setText(R.string.focus_music_empty);
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mTimerViewModel.startListening();
        com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().addListener(mMusicListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().removeListener(mMusicListener);
    }
}
