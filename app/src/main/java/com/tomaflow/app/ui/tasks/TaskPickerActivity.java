package com.tomaflow.app.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.List;

public class TaskPickerActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_NAME = "extra_task_name";
    public static final String EXTRA_TASK_DESC = "extra_task_desc";
    public static final String EXTRA_CLEAR_TASK = "extra_clear_task";

    private TaskViewModel mTaskViewModel;
    private TextView tvSelectedTaskName;
    private View cardSelectedTask;
    private RecyclerView rvTaskList;
    private TaskPickerAdapter mAdapter;
    private TextView tvEmptyTasks;

    private String mPrevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSelectedTaskName = findViewById(R.id.tv_selected_task_name);
        cardSelectedTask = findViewById(R.id.card_selected_task);
        rvTaskList = findViewById(R.id.rv_task_list);
        tvEmptyTasks = findViewById(R.id.tv_empty_tasks);

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Khôi phục task hiện tại
        mPrevId = getIntent().getStringExtra(EXTRA_TASK_ID);
        String prevName = getIntent().getStringExtra(EXTRA_TASK_NAME);
        if (prevName != null && !prevName.isEmpty()) {
            tvSelectedTaskName.setText(prevName);
        }

        findViewById(R.id.btn_clear_task).setOnClickListener(v -> {
            tvSelectedTaskName.setText(getString(R.string.task_picker_none_selected));
            Intent result = new Intent();
            result.putExtra(EXTRA_CLEAR_TASK, true);
            setResult(RESULT_OK, result);
            finish();
        });

        mAdapter = new TaskPickerAdapter(mPrevId, this::handlePickedTask);
        rvTaskList.setAdapter(mAdapter);

        mTaskViewModel.getPendingTasks().observe(this, this::populateTasks);
    }

    private void populateTasks(List<TaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            rvTaskList.setVisibility(View.GONE);
            mAdapter.submitList(null);
            return;
        }

        // Không hiển thị công việc đang được chọn ở danh sách Pending
        List<TaskEntity> filteredTasks = new java.util.ArrayList<>();
        for (TaskEntity task : tasks) {
            if (mPrevId == null || !mPrevId.equals(task.taskId)) {
                filteredTasks.add(task);
            }
        }
        
        boolean hasVisibleTasks = !filteredTasks.isEmpty();
        tvEmptyTasks.setVisibility(hasVisibleTasks ? View.GONE : View.VISIBLE);
        rvTaskList.setVisibility(hasVisibleTasks ? View.VISIBLE : View.GONE);
        mAdapter.submitList(filteredTasks);
    }

    private void handlePickedTask(TaskEntity task) {
        tvSelectedTaskName.setText(task.title);

        Intent result = new Intent();
        result.putExtra(EXTRA_TASK_ID, task.taskId);
        result.putExtra(EXTRA_TASK_NAME, task.title);
        result.putExtra(EXTRA_TASK_DESC, task.description);
        setResult(RESULT_OK, result);
        finish();
    }
}
