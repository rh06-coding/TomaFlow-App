package com.tomaflow.app.data.remote;

import android.util.Log;
import com.tomaflow.app.utils.TomaFlowLog;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mục đích: Đồng bộ lịch sử Pomodoro (Sessions) với Firebase Firestore.
 * 
 * Các method quan trọng:
 * - uploadSession: Đẩy 1 session mới lên cloud. Dùng `startTime` làm ID tài liệu.
 * - fetchSessions: Tải toàn bộ lịch sử session từ cloud về máy.
 */
public class FirestoreSessionRemoteDataSource {

    private static final String TAG = "FirestoreSession";
    private FirebaseFirestore firestore;

    public FirestoreSessionRemoteDataSource() {
        // Lấy instance Firestore lười (lazy) — không gọi trong constructor để
        // SessionRepository có thể dựng trong unit test mà không cần Firestore component.
    }

    private FirebaseFirestore db() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    public interface SessionFetchCallback {
        void onSuccess(List<SessionEntity> sessions);
        void onFailure(Exception e);
    }

    public void uploadSession(String userId, SessionEntity session) {
        if (userId == null || userId.isEmpty() || session == null) {
            return;
        }

        // Dùng startTime làm Document ID vì nó là duy nhất (timestamp)
        String docId = String.valueOf(session.startTime);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId_local", session.sessionId);
        data.put("userId", userId);
        data.put("taskId", session.taskId != null ? session.taskId : "");
        data.put("startTime", session.startTime);
        data.put("endTime", session.endTime);
        data.put("duration", session.duration);
        data.put("status", session.status);

        db().collection("users")
                .document(userId)
                .collection("sessions")
                .document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> TomaFlowLog.d(TAG, "Upload session success: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Upload session failed: " + docId, e));
    }

    public void fetchSessions(String userId, SessionFetchCallback callback) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        db().collection("users")
                .document(userId)
                .collection("sessions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SessionEntity> sessions = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        SessionEntity session = new SessionEntity();
                        // Bỏ qua sessionId cục bộ (để Room tự sinh)
                        
                        // Fix Type Mismatch issues from Firebase
                        Long startTime = document.getLong("startTime");
                        Long endTime = document.getLong("endTime");
                        Long duration = document.getLong("duration");
                        
                        session.taskId = document.getString("taskId");
                        session.startTime = startTime == null ? 0L : startTime;
                        session.endTime = endTime == null ? 0L : endTime;
                        session.duration = duration == null ? 0 : duration.intValue();
                        session.status = document.getString("status");

                        sessions.add(session);
                    }

                    TomaFlowLog.d(TAG, "Fetch sessions success: " + sessions.size());
                    callback.onSuccess(sessions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fetch sessions failed", e);
                    callback.onFailure(e);
                });
    }
}
