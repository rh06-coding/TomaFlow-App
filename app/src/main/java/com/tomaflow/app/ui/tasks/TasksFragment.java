package com.tomaflow.app.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView rvActive;
    private RecyclerView rvDone;
    private TextView tvActiveCountLabel;
    private TextView tvDoneCountLabel;
    private TaskAdapter activeAdapter;
    private TaskAdapter doneAdapter;
    private final List<TaskEntity> activeTasks = new ArrayList<>();
    private final List<TaskEntity> doneTasks = new ArrayList<>();

    private TaskViewModel mTaskViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        rvActive = view.findViewById(R.id.rv_active);
        rvDone = view.findViewById(R.id.rv_done);
        tvActiveCountLabel = view.findViewById(R.id.tv_active_count_label);
        tvDoneCountLabel = view.findViewById(R.id.tv_done_count_label);

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_profile));
        }

        setupRecyclerViews();
        observeTasks();

        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void setupRecyclerViews() {
        activeAdapter = new TaskAdapter(activeTasks, task -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_task_title)
                .setMessage(R.string.confirm_delete_task_msg)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    mTaskViewModel.delete(task);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
        });
        rvActive.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActive.setAdapter(activeAdapter);

        doneAdapter = new TaskAdapter(doneTasks, task -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_task_title)
                .setMessage(R.string.confirm_delete_task_msg)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    mTaskViewModel.delete(task);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
        });
        rvDone.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDone.setAdapter(doneAdapter);
    }

    /** Drive both lists from Room. Status changes re-emit and rebuild the split. */
    private void observeTasks() {
        mTaskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            activeTasks.clear();
            doneTasks.clear();
            if (tasks != null) {
                for (TaskEntity task : tasks) {
                    if ("Completed".equals(task.status)) {
                        doneTasks.add(task);
                    } else {
                        activeTasks.add(task);
                    }
                }
            }
            activeAdapter.notifyDataSetChanged();
            doneAdapter.notifyDataSetChanged();
            updateCounts();
        });
    }

    private void updateCounts() {
        tvActiveCountLabel.setText(getString(R.string.task_items_count, activeTasks.size()));
        tvDoneCountLabel.setText(getString(R.string.task_items_count, doneTasks.size()));
    }

    private void showAddTaskDialog() {
        com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
        if (!sm.isVip() && activeTasks.size() >= 5) {
            com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getChildFragmentManager(), "PremiumGateDialog");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_task, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etNote = view.findViewById(R.id.et_note);
        EditText etDurationMinutes = view.findViewById(R.id.et_duration_minutes);
        TextView tvPomodoroCount = view.findViewById(R.id.tv_pomodoro_count);
        ImageButton btnMinus = view.findViewById(R.id.btn_minus);
        ImageButton btnPlus = view.findViewById(R.id.btn_plus);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);

        final int[] count = {1};

        btnMinus.setOnClickListener(v -> {
            if (count[0] > 1) {
                count[0]--;
                tvPomodoroCount.setText(String.valueOf(count[0]));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (count[0] < 10) {
                count[0]++;
                tvPomodoroCount.setText(String.valueOf(count[0]));
            }
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            if (title.isEmpty()) {
                com.tomaflow.app.utils.TomaToast.show(getContext(), R.string.task_name_required, true);
                return;
            }
            int durationMinutes = 0;
            String durStr = etDurationMinutes != null ? etDurationMinutes.getText().toString().trim() : "";
            if (!durStr.isEmpty()) {
                try { durationMinutes = Integer.parseInt(durStr); } catch (NumberFormatException ignored) {}
            }

            TaskEntity newTask = new TaskEntity(title, note, count[0], durationMinutes);
            mTaskViewModel.insert(newTask);
            rvActive.scrollToPosition(0);
            dialog.dismiss();
        });

        dialog.show();
    }
}
