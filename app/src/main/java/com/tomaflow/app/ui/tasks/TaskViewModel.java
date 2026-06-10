package com.tomaflow.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.data.repository.TaskRepository;
import com.tomaflow.app.data.repository.UserRepository;

import java.util.List;

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

    public LiveData<TaskEntity> getTaskById(String taskId) {
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

    public void deleteById(String taskId) {
        taskRepository.deleteById(taskId);
    }

    public void markTaskCompleted(String taskId) {
        taskRepository.markTaskCompleted(taskId);
    }

    public void markTaskPending(String taskId) {
        taskRepository.markTaskPending(taskId);
    }

    public String getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }

    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    public LiveData<List<TaskEntity>> getTasksByTag(String tag) {
        return taskRepository.getTasksByTag(tag);
    }

    public void updateTags(String taskId, String tags) {
        taskRepository.updateTags(taskId, tags);
    }

    public void syncTasksFromFirestore() {
        taskRepository.syncTasksFromFirestore();
    }
}