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
 * Single source of truth for task data.
 * Reads return LiveData (Room handles background threading).
 * Writes are dispatched to a single-thread ExecutorService.
 *
 * Flow: UI -> ViewModel -> Repository -> TaskDao -> Room -> SQLite
 */
public class TaskRepository {

    private final TaskDao        mTaskDao;
    private final ExecutorService mExecutor;

    public TaskRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mTaskDao  = db.taskDao();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    // Reads (LiveData — auto-dispatched by Room)
    public LiveData<List<TaskEntity>> getAllTasks()     { return mTaskDao.getAllTasks(); }
    public LiveData<List<TaskEntity>> getPendingTasks() { return mTaskDao.getPendingTasks(); }
    public LiveData<Integer>          getPendingCount() { return mTaskDao.getPendingTaskCount(); }

    // Writes (background thread via ExecutorService)
    public void insert(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.insert(task)); }
    public void update(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.update(task)); }
    public void delete(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.delete(task)); }
}
