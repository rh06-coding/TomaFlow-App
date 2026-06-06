package com.tomaflow.app.data.remote;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đồng bộ dữ liệu Task giữa Room local và Firestore.
 */
public class FirestoreTaskRemoteDataSource {

    private static final String TAG = "FirestoreSync";

    private final FirebaseFirestore firestore;

    public FirestoreTaskRemoteDataSource() {
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Callback dùng khi lấy task từ Firestore về Room.
     */
    public interface TaskFetchCallback {
        void onSuccess(List<TaskEntity> tasks);

        void onFailure(Exception e);
    }

    /**
     * Đẩy task từ Room local lên Firestore.
     */
    public void uploadTask(String userId, TaskEntity task) {
        if (userId == null || userId.isEmpty() || task == null || task.taskId.isEmpty()) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.taskId);
        data.put("userId", userId);
        data.put("title", task.title);
        data.put("description", task.description);
        data.put("estPomodoros", task.estPomodoros);
        data.put("status", task.status);
        data.put("tags", task.tags);
        data.put("createdAt", task.createdAt);
        data.put("updatedAt", task.updatedAt);

        // Lưu task theo user đang đăng nhập.
        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.taskId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Upload task success: " + task.taskId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Upload task failed: " + task.taskId, e)
                );
    }

    /**
     * Xóa task tương ứng trên Firestore.
     */
    public void deleteTask(String userId, String taskId) {
        if (userId == null || userId.isEmpty() || taskId == null || taskId.isEmpty()) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Delete task success: " + taskId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Delete task failed: " + taskId, e)
                );
    }

    /**
     * Kéo toàn bộ task của user từ Firestore về để lưu lại vào Room.
     */
    public void fetchTasks(String userId, TaskFetchCallback callback) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TaskEntity> tasks = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        TaskEntity task = new TaskEntity();

                        String taskId = document.getString("taskId");
                        String remoteUserId = document.getString("userId");
                        Long estPomodoros = document.getLong("estPomodoros");
                        Long createdAt = document.getLong("createdAt");
                        Long updatedAt = document.getLong("updatedAt");

                        task.taskId = taskId == null ? "" : taskId;
                        task.userId = remoteUserId;
                        task.title = document.getString("title");
                        task.description = document.getString("description");
                        task.estPomodoros = estPomodoros == null ? 1 : estPomodoros.intValue();
                        task.status = document.getString("status");
                        task.tags = document.getString("tags");
                        task.createdAt = createdAt == null ? 0L : createdAt;
                        task.updatedAt = updatedAt == null ? 0L : updatedAt;

                        tasks.add(task);
                    }

                    Log.d(TAG, "Fetch tasks success: " + tasks.size());
                    callback.onSuccess(tasks);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fetch tasks failed", e);
                    callback.onFailure(e);
                });
    }
}