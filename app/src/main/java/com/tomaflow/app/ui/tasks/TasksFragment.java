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
import com.tomaflow.app.databinding.BottomSheetAddTaskBinding;
import com.tomaflow.app.databinding.FragmentTasksBinding;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter activeAdapter;
    private TaskAdapter doneAdapter;
    private final List<TaskEntity> activeTasks = new ArrayList<>();
    private final List<TaskEntity> doneTasks = new ArrayList<>();

    private TaskViewModel mTaskViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.tomaflow.app.utils.HeaderUIHelper.setupHeader(view, getString(R.string.nav_tasks), getViewLifecycleOwner());

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_profile));
        }

        setupRecyclerViews();
        observeTasks();

        binding.fabAdd.setOnClickListener(v -> showAddTaskDialog());
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
        binding.rvActive.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvActive.setAdapter(activeAdapter);

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
        binding.rvDone.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDone.setAdapter(doneAdapter);
    }

    /** Drive both lists from Room. Status changes re-emit and rebuild the split. */
    private void observeTasks() {
        mTaskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            activeTasks.clear();
            doneTasks.clear();
            int estPomos = 0;
            if (tasks != null) {
                for (TaskEntity task : tasks) {
                    if ("Completed".equals(task.status)) {
                        doneTasks.add(task);
                    } else {
                        activeTasks.add(task);
                        estPomos += task.estPomodoros;
                    }
                }
            }
            activeAdapter.notifyDataSetChanged();
            doneAdapter.notifyDataSetChanged();
            updateCounts();

            binding.tvEstPomosValue.setText(String.valueOf(estPomos));
            int total = activeTasks.size() + doneTasks.size();
            if (total == 0) {
                binding.tvCompletionRateValue.setText("0%");
            } else {
                int rate = (int) (((float) doneTasks.size() / total) * 100);
                binding.tvCompletionRateValue.setText(rate + "%");
            }
        });
    }

    private void updateCounts() {
        binding.tvActiveCountLabel.setText(getString(R.string.task_items_count, activeTasks.size()));
        binding.tvDoneCountLabel.setText(getString(R.string.task_items_count, doneTasks.size()));
    }

    private void showAddTaskDialog() {
        com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
        if (!sm.isVip() && activeTasks.size() >= 5) {
            com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getChildFragmentManager(), "PremiumGateDialog");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetAddTaskBinding sheetBinding = BottomSheetAddTaskBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        final int[] count = {1};

        sheetBinding.btnMinus.setOnClickListener(v -> {
            if (count[0] > 1) {
                count[0]--;
                sheetBinding.tvPomodoroCount.setText(String.valueOf(count[0]));
            }
        });

        sheetBinding.btnPlus.setOnClickListener(v -> {
            if (count[0] < 10) {
                count[0]++;
                sheetBinding.tvPomodoroCount.setText(String.valueOf(count[0]));
            }
        });

        sheetBinding.btnSave.setOnClickListener(v -> {
            String title = sheetBinding.etTitle.getText().toString().trim();
            String note = sheetBinding.etNote.getText().toString().trim();
            if (title.isEmpty()) {
                com.tomaflow.app.utils.TomaToast.show(getContext(), R.string.task_name_required, false);
                return;
            }
            int durationMinutes = 0;

            TaskEntity newTask = new TaskEntity(title, note, count[0], durationMinutes);
            mTaskViewModel.insert(newTask);
            binding.rvActive.scrollToPosition(0);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
