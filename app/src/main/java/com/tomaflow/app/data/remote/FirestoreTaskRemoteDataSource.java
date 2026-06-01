package com.tomaflow.app.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Đồng bộ dữ liệu Task từ Room local lên Firestore.
 */
public class FirestoreTaskRemoteDataSource {

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

        // Lưu task theo từng user để sau này đổi sang Firebase Auth dễ hơn.
        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(String.valueOf(task.taskId))
                .set(data, SetOptions.merge());
    }

    public void deleteTask(String userId, int taskId) {
        if (userId == null || userId.isEmpty() || taskId <= 0) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(String.valueOf(taskId))
                .delete();
    }
}