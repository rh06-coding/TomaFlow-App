package com.tomaflow.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.data.repository.TaskRepository;
import com.tomaflow.app.data.repository.UserRepository;

import java.util.List;

/**
 * ViewModel cho màn hình task.
 * Cung cấp dữ liệu task cho UI và gọi TaskRepository để xử lý database.
 *
 * UI chỉ nên gọi ViewModel, không gọi trực tiếp Repository/DAO.
 * Sau này nếu đổi Room sang Cloud Database thì ưu tiên sửa Repository,
 * hạn chế sửa UI.
 */
public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        userRepository = UserRepository.getInstance();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return taskRepository.getAllTasks();
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return taskRepository.getPendingTasks();
    }

    public LiveData<Integer> getPendingCount() {
        return taskRepository.getPendingCount();
    }

    public LiveData<TaskEntity> getTaskById(int taskId) {
        return taskRepository.getTaskById(taskId);
    }

    public void insert(TaskEntity task) {
        taskRepository.insert(task);
    }

    public void update(TaskEntity task) {
        taskRepository.update(task);
    }

    public void delete(TaskEntity task) {
        taskRepository.delete(task);
    }

    public void deleteById(int taskId) {
        taskRepository.deleteById(taskId);
    }

    // Lấy user hiện tại thông qua UserRepository để dễ đổi sang cloud/auth sau này.
    public String getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }

    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    public LiveData<List<TaskEntity>> getTasksByTag(String tag) {
        return taskRepository.getTasksByTag(tag);
    }

    public void updateTags(int taskId, String tags) {
        taskRepository.updateTags(taskId, tags);
    }
}