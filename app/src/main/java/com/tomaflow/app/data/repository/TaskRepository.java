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
 * Repository quản lý dữ liệu task.
 * ViewModel gọi Repository thay vì gọi trực tiếp TaskDao.
 * Flow: UI -> ViewModel -> Repository -> TaskDao -> Room -> SQLite
 */
public class TaskRepository {

    private final TaskDao        mTaskDao;

    // Chạy các thao tác ghi database ở background thread.
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

    // Ghi dữ liệu task thông qua TaskDao.
    public void insert(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.insert(task)); }
    public void update(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.update(task)); }
    public void delete(TaskEntity task)  { mExecutor.execute(() -> mTaskDao.delete(task)); }

    public LiveData<TaskEntity> getTaskById(int taskId) {
        return mTaskDao.getTaskById(taskId);
    }

    public void deleteById(int taskId) {
        mExecutor.execute(() -> mTaskDao.deleteById(taskId));
    }

    public void markTaskCompleted(int taskId) {
        mExecutor.execute(() -> mTaskDao.updateTaskStatus(taskId, "Completed"));
    }

    public void markTaskPending(int taskId) {
        mExecutor.execute(() -> mTaskDao.updateTaskStatus(taskId, "Pending"));
    }


    // Cập nhật tag cho task và thời gian chỉnh sửa.
    public LiveData<List<TaskEntity>> getTasksByTag(String tag) {
        return mTaskDao.getTasksByTag(tag);
    }

    public void updateTags(int taskId, String tags) {
        mExecutor.execute(() -> mTaskDao.updateTags(taskId, tags, System.currentTimeMillis()));
    }
}
