package com.tomaflow.app.data.repository;

import android.app.Application;
import com.tomaflow.app.utils.TomaFlowLog;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.entity.TaskEntity;
import com.tomaflow.app.data.remote.FirestoreTaskRemoteDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Flow: UI -> ViewModel -> Repository -> TaskDao -> Room -> SQLite/Firestore
 */
public class TaskRepository {

    private static final String TAG = "TaskRepository";

    private final TaskDao mTaskDao;

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

    public LiveData<TaskEntity> getTaskById(String taskId) {
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

            task.userId = mUserRepository.getCurrentUserId();

            mTaskDao.insert(task);

            mRemoteDataSource.uploadTask(
                    task.userId,
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

            mRemoteDataSource.uploadTask(
                    mUserRepository.getCurrentUserId(),
                    task
            );
        });
    }

    public void delete(TaskEntity task) {
        mExecutor.execute(() -> {
            mTaskDao.delete(task);

            mRemoteDataSource.deleteTask(
                    mUserRepository.getCurrentUserId(),
                    task.taskId
            );
        });
    }

    public void deleteById(String taskId) {
        mExecutor.execute(() -> {
            mTaskDao.deleteById(taskId);

            mRemoteDataSource.deleteTask(
                    mUserRepository.getCurrentUserId(),
                    taskId
            );
        });
    }

    public void markTaskCompleted(String taskId) {
        updateTaskStatus(taskId, "Completed");
    }

    public void markTaskPending(String taskId) {
        updateTaskStatus(taskId, "Pending");
    }

    public void updateTags(String taskId, String tags) {
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

    private void updateTaskStatus(String taskId, String status) {
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

    public interface OnTaskCompletedCallback {
        void onCompleted();
    }

    /**
     * Giảm số lượng Pomodoro còn lại của công việc đi 1.
     * Nếu số lượng Pomodoro chạm mốc <= 0, tự động đánh dấu công việc là "Completed"
     * và gọi hàm callback để thông báo cho ViewModel.
     */
    public void decrementPomodoro(String taskId, OnTaskCompletedCallback callback) {
        mExecutor.execute(() -> {
            TaskEntity task = mTaskDao.getTaskByIdSync(taskId);
            if (task != null && !"Completed".equals(task.status)) {
                task.estPomodoros -= 1;
                boolean isCompleted = false;
                if (task.estPomodoros <= 0) {
                    task.status = "Completed";
                    isCompleted = true;
                }
                task.updatedAt = System.currentTimeMillis();
                mTaskDao.update(task);
                mRemoteDataSource.uploadTask(
                        mUserRepository.getCurrentUserId(),
                        task
                );
                
                if (isCompleted && callback != null) {
                    callback.onCompleted();
                }
            }
        });
    }

    /**
     * Kéo task từ Firestore về Room sau khi user đăng nhập hoặc mở màn Task.
     */
    public void syncTasksFromFirestore() {
        String userId = mUserRepository.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            TomaFlowLog.e(TAG, "Skip sync because user is not logged in");
            return;
        }

        mRemoteDataSource.fetchTasks(userId, new FirestoreTaskRemoteDataSource.TaskFetchCallback() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                // Guard against wiping local tasks when the remote returns an empty
                // list — that usually means an offline/partial fetch, not "the user has
                // no tasks". Delete-then-replace only when we actually got data.
                if (tasks == null || tasks.isEmpty()) return;
                mExecutor.execute(() -> {
                    mTaskDao.deleteAll();
                    for (TaskEntity task : tasks) {
                        if (task.taskId.isEmpty()) {
                            continue;
                        }

                        if (task.tags == null) {
                            task.tags = "";
                        }

                        if (task.status == null || task.status.isEmpty()) {
                            task.status = "Pending";
                        }

                        // REPLACE giúp task từ Firestore ghi đè task local nếu trùng taskId.
                        mTaskDao.insert(task);
                    }

                    TomaFlowLog.d(TAG, "Synced tasks from Firestore to Room: " + tasks.size());
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Sync tasks from Firestore failed", e);
            }
        });
    }
}