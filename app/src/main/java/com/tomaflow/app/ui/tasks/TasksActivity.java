package com.tomaflow.app.ui.tasks;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * Màn hình danh sách công việc.
 * Sau này sẽ hiển thị task từ Room và cho phép thêm/sửa/xóa task.
 */
public class TasksActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_tasks);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Kéo task từ Firestore về Room khi user mở màn Task.
        // UI sau này chỉ cần observe Room là sẽ thấy dữ liệu đã đồng bộ.
        taskViewModel.syncTasksFromFirestore();
    }
}