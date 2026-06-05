package com.tomaflow.app.data.remote;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Đồng bộ dữ liệu Task từ Room local lên Firestore.
 */
public class FirestoreTaskRemoteDataSource {

    private static final String TAG = "FirestoreSync";
    private final FirebaseFirestore firestore;

    public FirestoreTaskRemoteDataSource() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void uploadTask(String userId, TaskEntity task) {
        if (userId == null || userId.isEmpty() || task == null || task.taskId <= 0) {
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
                .document(String.valueOf(task.taskId))
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Upload task success: " + task.taskId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Upload task failed: " + task.taskId, e)
                );
    }

    public void deleteTask(String userId, int taskId) {
        if (userId == null || userId.isEmpty() || taskId <= 0) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(String.valueOf(taskId))
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Delete task success: " + taskId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Delete task failed: " + taskId, e)
                );
    }
}