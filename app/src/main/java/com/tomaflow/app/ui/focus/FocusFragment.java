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


import com.tomaflow.app.R;
import com.tomaflow.app.constants.AppConstants;
import com.tomaflow.app.data.db.entity.TaskEntity;
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
    private static final int    COLOR_FOCUS_BG       = 0xFFF8F5FA; // toma_background
    private static final int    COLOR_SHORT_BREAK_BG  = 0xFFEDF7F1;
    private static final int    COLOR_LONG_BREAK_BG   = 0xFFEAF3FD;

    private TimerViewModel        mTimerViewModel;
    private TaskViewModel         mTaskViewModel;
    private TaskEntity            mCurrentTask;
    private TimerView             mTimerView;
    private TextView              mTvTime;
    private TextView              mTvSessionLabel;
    private ImageButton           mBtnPlayPause;
    private View                  mRootScrollView;
    private TomatoGrowthView      mTomatoGrowthView;
    private View                  mTomatoWidget;
    private TextView              mTvTomatoStatus;
    private TextView              mTvTaskTitle;
    private TextView              mTvTaskSubtitle;

    /** Pha trước đó — để phát hiện khi nào pha thay đổi. */
    private PomodoroTimer.Phase   mPreviousPhase  = null;
    /** Trạng thái running trước — để phát hiện khi bắt đầu/dừng. */
    private boolean               mPreviousRunning = false;

    private TextView mTvMusicName;
    private com.tomaflow.app.data.model.BuiltInTrack mSelectedBuiltInTrack;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> musicPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    boolean isCleared = result.getData().getBooleanExtra(com.tomaflow.app.ui.music.MusicPickerActivity.EXTRA_CLEAR_TRACK, false);
                    if (isCleared) {
                        if (mTvMusicName != null) {
                            mTvMusicName.setText(R.string.focus_music_empty);
                        }
                        mSelectedBuiltInTrack = null;
                        com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().stop(requireContext());
                    } else {
                        String trackName = result.getData().getStringExtra(com.tomaflow.app.ui.music.MusicPickerActivity.EXTRA_TRACK_NAME);
                        String trackId = result.getData().getStringExtra(com.tomaflow.app.ui.music.MusicPickerActivity.EXTRA_BUILTIN_TRACK_ID);
                        if (trackName != null && mTvMusicName != null) {
                            mTvMusicName.setText(trackName);
                        }
                        if (trackId != null) {
                            for (com.tomaflow.app.data.model.BuiltInTrack track : com.tomaflow.app.ui.music.BuiltInTrackCatalog.TRACKS) {
                                if (track.id.equals(trackId)) {
                                    mSelectedBuiltInTrack = track;
                                    // Tự động phát khi chọn xong
                                    com.tomaflow.app.ui.music.AppMusicPlayer.getInstance().play(requireContext(), mSelectedBuiltInTrack);
                                    break;
                                }
                            }
                        } else {
                            mSelectedBuiltInTrack = null;
                        }
                    }
                } else if (result.getResultCode() == android.app.Activity.RESULT_CANCELED) {
                    // Do nothing when user cancels picker without explicitly clearing
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_focus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);
        mTaskViewModel  = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_profile));
        }

        bindViews(view);
        setupTimerObserver();
        setupTaskObserver();
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
        mTvTaskTitle    = v.findViewById(R.id.tv_task_title);
        mTvTaskSubtitle = v.findViewById(R.id.tv_task_subtitle);

        mTvTaskTitle.setText("Chọn công việc");
        mTvTaskSubtitle.setText("Nhấn để chọn");

        mBtnPlayPause.setOnClickListener(v1 -> onPlayPauseClicked());
        btnReset.setOnClickListener(v1 -> onResetClicked());
        btnSkip.setOnClickListener(v1 -> onSkipClicked());
        cardCurrentTask.setOnClickListener(v1 -> onTaskCardClicked());

        mTvMusicName = v.findViewById(R.id.tv_music_name);
        View btnPickMusic = v.findViewById(R.id.btn_pick_music);
        View btnPlayMusic = v.findViewById(R.id.btn_play_music);

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
                        android.widget.Toast.makeText(getContext(), "Vui lòng chọn nhạc trước", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        btnCompleteTask.setOnClickListener(v1 -> {
            if (mCurrentTask != null) {
                mTaskViewModel.markTaskCompleted(mCurrentTask.taskId);
                android.widget.Toast.makeText(getContext(), "Tuyệt vời! Task đã hoàn thành.", android.widget.Toast.LENGTH_SHORT).show();
                mCurrentTask = null;
                mTimerViewModel.setCurrentTaskId(null);
                mTvTaskTitle.setText("Chọn công việc");
                mTvTaskSubtitle.setText("Nhấn để chọn");
            } else {
                onTaskCardClicked(); // Mở picker nếu chưa có task
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

    private List<TaskEntity> mPendingTasks;

    private void setupTaskObserver() {
        mTaskViewModel.getPendingTasks().observe(getViewLifecycleOwner(), tasks -> {
            mPendingTasks = tasks;
        });
    }

    private void onTaskCardClicked() {
        if (mPendingTasks == null || mPendingTasks.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Không có công việc nào đang chờ.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_task_picker_sheet, null);
        android.widget.LinearLayout layoutTaskList = sheetView.findViewById(R.id.layout_task_list);

        for (TaskEntity task : mPendingTasks) {
            View itemView = getLayoutInflater().inflate(R.layout.item_task_picker, layoutTaskList, false);
            TextView tvTitle = itemView.findViewById(R.id.tv_title);
            TextView tvDesc = itemView.findViewById(R.id.tv_desc);
            TextView tvPomos = itemView.findViewById(R.id.tv_pomos);

            tvTitle.setText(task.title);
            if (task.description == null || task.description.trim().isEmpty()) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setText(task.description);
                tvDesc.setVisibility(View.VISIBLE);
            }
            if (task.estimatedMinutes > 0) {
                tvPomos.setText(task.estimatedMinutes + "m");
            } else {
                tvPomos.setText(String.valueOf(task.estPomodoros));
            }

            itemView.setOnClickListener(v -> {
                mCurrentTask = task;
                mTimerViewModel.setCurrentTaskId(mCurrentTask.taskId);
                mTvTaskTitle.setText(mCurrentTask.title);
                mTvTaskSubtitle.setText(mCurrentTask.description == null || mCurrentTask.description.trim().isEmpty() ? "Đang tập trung" : mCurrentTask.description);
                
                // Đảm bảo icon hoàn thành hiển thị chuẩn
                View btnCompleteTask = mRootScrollView.findViewById(R.id.btn_complete_task);
                mTvTaskTitle.setPaintFlags(mTvTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                mTvTaskTitle.setTextColor(getResources().getColor(R.color.toma_text, null));
                if (btnCompleteTask != null) btnCompleteTask.setAlpha(1.0f);
                
                dialog.dismiss();
            });

            layoutTaskList.addView(itemView);
        }

        dialog.setContentView(sheetView);
        dialog.show();
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

    private final com.tomaflow.app.ui.music.AppMusicPlayer.OnPlaybackStateChanged mMusicListener = (isPlaying, track) -> {
        if (getView() != null) {
            android.widget.ImageView btnPlay = getView().findViewById(R.id.btn_play_music);
            if (btnPlay != null) {
                btnPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
            if (track != null) {
                mSelectedBuiltInTrack = track;
                if (mTvMusicName != null) {
                    mTvMusicName.setText(track.name);
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
