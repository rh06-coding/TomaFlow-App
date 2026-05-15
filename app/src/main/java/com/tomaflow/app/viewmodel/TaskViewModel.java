package com.tomaflow.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.data.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> allTasks;
    private final LiveData<List<TaskEntity>> pendingTasks;
    private final LiveData<Integer> pendingTaskCount;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        pendingTasks = repository.getPendingTasks();
        pendingTaskCount = repository.getPendingCount();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return pendingTasks;
    }

    public LiveData<Integer> getPendingTaskCount() {
        return pendingTaskCount;
    }

    public LiveData<TaskEntity> getTaskById(int taskId) {
        return repository.getTaskById(taskId);
    }

    public void insert(TaskEntity task) {
        repository.insert(task);
    }

    public void update(TaskEntity task) {
        repository.update(task);
    }

    public void delete(TaskEntity task) {
        repository.delete(task);
    }

    public void deleteById(int taskId) {
        repository.deleteById(taskId);
    }
}