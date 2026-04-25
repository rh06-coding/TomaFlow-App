package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TaskRepository — single source of truth for TASK data.
 *
 * Abstracts the Room DAO behind a clean interface so ViewModels never
 * touch the database directly. All write operations are dispatched to a
 * background thread via the {@link ExecutorService}.
 */
public class TaskRepository {

    private final TaskDao        mTaskDao;
    private final ExecutorService mExecutor;

    public TaskRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mTaskDao  = db.taskDao();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    // ── Read (LiveData — auto-dispatched on background thread by Room) ──────────

    public LiveData<List<TaskEntity>> getAllTasks()    { return mTaskDao.getAllTasks(); }
    public LiveData<List<TaskEntity>> getPendingTasks() { return mTaskDao.getPendingTasks(); }
    public LiveData<Integer>          getPendingCount() { return mTaskDao.getPendingTaskCount(); }

    // ── Write (must run off the main thread) ────────────────────────────────────

    public void insert(TaskEntity task) {
        mExecutor.execute(() -> mTaskDao.insert(task));
    }

    public void update(TaskEntity task) {
        mExecutor.execute(() -> mTaskDao.update(task));
    }

    public void delete(TaskEntity task) {
        mExecutor.execute(() -> mTaskDao.delete(task));
    }
}
