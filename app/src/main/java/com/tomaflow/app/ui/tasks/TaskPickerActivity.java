package com.tomaflow.app.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout layoutTaskList;
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
        layoutTaskList = findViewById(R.id.layout_task_list);
        tvEmptyTasks = findViewById(R.id.tv_empty_tasks);

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Khôi phục task hiện tại
        mPrevId = getIntent().getStringExtra(EXTRA_TASK_ID);
        String prevName = getIntent().getStringExtra(EXTRA_TASK_NAME);
        if (prevName != null && !prevName.isEmpty()) {
            tvSelectedTaskName.setText(prevName);
        }

        findViewById(R.id.btn_clear_task).setOnClickListener(v -> {
            tvSelectedTaskName.setText("Chưa chọn công việc");
            Intent result = new Intent();
            result.putExtra(EXTRA_CLEAR_TASK, true);
            setResult(RESULT_OK, result);
            finish();
        });

        mTaskViewModel.getPendingTasks().observe(this, this::populateTasks);
    }

    private void populateTasks(List<TaskEntity> tasks) {
        layoutTaskList.removeAllViews();
        if (tasks == null || tasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            return;
        }

        boolean hasVisibleTasks = false;
        LayoutInflater inflater = getLayoutInflater();

        for (TaskEntity task : tasks) {
            // Không hiển thị công việc đang được chọn ở danh sách Pending
            if (mPrevId != null && mPrevId.equals(task.taskId)) {
                continue;
            }
            
            hasVisibleTasks = true;
            View itemView = inflater.inflate(R.layout.item_task_picker, layoutTaskList, false);
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
            tvPomos.setText(String.valueOf(task.estPomodoros));

            itemView.setOnClickListener(v -> {
                handlePickedTask(task);
            });

            layoutTaskList.addView(itemView);
        }
        
        tvEmptyTasks.setVisibility(hasVisibleTasks ? View.GONE : View.VISIBLE);
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
