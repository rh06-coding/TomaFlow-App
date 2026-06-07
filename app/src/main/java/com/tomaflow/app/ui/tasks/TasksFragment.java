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
    private List<TaskEntity> activeTasks = new ArrayList<>();
    private List<TaskEntity> doneTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        rvActive = view.findViewById(R.id.rv_active);
        rvDone = view.findViewById(R.id.rv_done);
        tvActiveCountLabel = view.findViewById(R.id.tv_active_count_label);
        tvDoneCountLabel = view.findViewById(R.id.tv_done_count_label);

        // Khởi tạo dữ liệu mẫu (mock data) cho màn demo
        if (activeTasks.isEmpty() && doneTasks.isEmpty()) {
            activeTasks.add(new TaskEntity("Finalize Design Tokens", "Establish secondary color mappings", 4));
            activeTasks.add(new TaskEntity("Update README", "Add new screenshots", 2));
            activeTasks.add(new TaskEntity("Fix Navigation Bug", "Issue #42", 1));

            TaskEntity completedTask = new TaskEntity("Setup Project", "Init git and base gradle", 1);
            completedTask.status = "Completed";
            doneTasks.add(completedTask);
        }

        setupRecyclerViews();
        updateCounts();

        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void setupRecyclerViews() {
        activeAdapter = new TaskAdapter(activeTasks, (task, isChecked) -> {
            if (isChecked) {
                task.status = "Completed";
                activeTasks.remove(task);
                doneTasks.add(0, task);
            } else {
                task.status = "Pending";
                doneTasks.remove(task);
                activeTasks.add(task);
            }
            activeAdapter.notifyDataSetChanged();
            doneAdapter.notifyDataSetChanged();
            updateCounts();
        });
        rvActive.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActive.setAdapter(activeAdapter);

        doneAdapter = new TaskAdapter(doneTasks, (task, isChecked) -> {
            if (!isChecked) {
                task.status = "Pending";
                doneTasks.remove(task);
                activeTasks.add(task);
            } else {
                task.status = "Completed";
                activeTasks.remove(task);
                doneTasks.add(0, task);
            }
            activeAdapter.notifyDataSetChanged();
            doneAdapter.notifyDataSetChanged();
            updateCounts();
        });
        rvDone.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDone.setAdapter(doneAdapter);
    }

    private void updateCounts() {
        tvActiveCountLabel.setText(activeTasks.size() + " ITEMS");
        tvDoneCountLabel.setText(doneTasks.size() + " ITEMS");
    }

    private void showAddTaskDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_task, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etNote = view.findViewById(R.id.et_note);
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
                Toast.makeText(getContext(), "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
                return;
            }

            TaskEntity newTask = new TaskEntity(title, note, count[0]);
            activeTasks.add(0, newTask);
            activeAdapter.notifyItemInserted(0);
            rvActive.scrollToPosition(0);
            updateCounts();

            dialog.dismiss();
        });

        dialog.show();
    }
}
