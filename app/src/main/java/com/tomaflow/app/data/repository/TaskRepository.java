package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.data.remote.FirestoreTaskRemoteDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository quản lý dữ liệu task.
 * ViewModel gọi Repository thay vì gọi trực tiếp TaskDao.
 * Flow: UI -> ViewModel -> Repository -> TaskDao -> Room -> SQLite/Firestore
 */
public class TaskRepository {

    private final TaskDao mTaskDao;

    // Chạy các thao tác ghi database ở background thread.
    private final ExecutorService mExecutor;

    private final UserRepository mUserRepository;
    private final FirestoreTaskRemoteDataSource mRemoteDataSource;

    public TaskRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mTaskDao = db.taskDao();
        mExecutor = Executors.newSingleThreadExecutor();
        mUserRepository = UserRepository.getInstance();
        mRemoteDataSource = new FirestoreTaskRemoteDataSource();
    }

    // Reads: Room tự xử lý background thread khi trả về LiveData.
    public LiveData<List<TaskEntity>> getAllTasks() {
        return mTaskDao.getAllTasks();
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return mTaskDao.getPendingTasks();
    }

    public LiveData<Integer> getPendingCount() {
        return mTaskDao.getPendingTaskCount();
    }

    public LiveData<TaskEntity> getTaskById(int taskId) {
        return mTaskDao.getTaskById(taskId);
    }

    public LiveData<List<TaskEntity>> getTasksByTag(String tag) {
        return mTaskDao.getTasksByTag(tag);
    }

    public void insert(TaskEntity task) {
        mExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            if (task.createdAt == 0L) {
                task.createdAt = now;
            }

            task.updatedAt = now;

            if (task.status == null || task.status.isEmpty()) {
                task.status = "Pending";
            }

            if (task.tags == null) {
                task.tags = "";
            }

            long generatedId = mTaskDao.insert(task);
            task.taskId = (int) generatedId;

            // Sau khi lưu local thành công thì đồng bộ lên Firestore.
            mRemoteDataSource.uploadTask(
                    mUserRepository.getCurrentUserId(),
                    task
            );
        });
    }

    public void update(TaskEntity task) {
        mExecutor.execute(() -> {
            task.updatedAt = System.currentTimeMillis();

            if (task.tags == null) {
                task.tags = "";
            }

            mTaskDao.update(task);

            // Cập nhật bản mới nhất lên Firestore.
            mRemoteDataSource.uploadTask(
                    mUserRepository.getCurrentUserId(),
                    task
            );
        });
    }

    public void delete(TaskEntity task) {
        mExecutor.execute(() -> {
            mTaskDao.delete(task);

            // Xóa task tương ứng trên Firestore.
            mRemoteDataSource.deleteTask(
                    mUserRepository.getCurrentUserId(),
                    task.taskId
            );
        });
    }

    public void deleteById(int taskId) {
        mExecutor.execute(() -> {
            mTaskDao.deleteById(taskId);

            // Xóa task tương ứng trên Firestore.
            mRemoteDataSource.deleteTask(
                    mUserRepository.getCurrentUserId(),
                    taskId
            );
        });
    }

    public void markTaskCompleted(int taskId) {
        updateTaskStatus(taskId, "Completed");
    }

    public void markTaskPending(int taskId) {
        updateTaskStatus(taskId, "Pending");
    }

    public void updateTags(int taskId, String tags) {
        mExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            String safeTags = tags == null ? "" : tags;

            mTaskDao.updateTags(taskId, safeTags, now);

            // Lấy lại task đầy đủ sau khi update tag để sync Firestore.
            TaskEntity task = mTaskDao.getTaskByIdSync(taskId);

            mRemoteDataSource.uploadTask(
                    mUserRepository.getCurrentUserId(),
                    task
            );
        });
    }

    private void updateTaskStatus(int taskId, String status) {
        mExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            mTaskDao.updateTaskStatus(taskId, status, now);

            // Lấy lại task đầy đủ sau khi đổi trạng thái để sync Firestore.
            TaskEntity task = mTaskDao.getTaskByIdSync(taskId);

            mRemoteDataSource.uploadTask(
                    mUserRepository.getCurrentUserId(),
                    task
            );
        });
    }
}