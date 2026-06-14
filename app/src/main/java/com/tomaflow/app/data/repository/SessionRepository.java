package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource;


public class SessionRepository {

    private final SessionDao mSessionDao;
    private final FirestoreSessionRemoteDataSource mRemoteDataSource;
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public SessionRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mSessionDao = db.sessionDao();
        mRemoteDataSource = new FirestoreSessionRemoteDataSource();
    }

    public LiveData<List<SessionEntity>> getAllSessions() {
        return mSessionDao.getAllSessions();
    }

    public LiveData<Integer> getWeeklyMinutes() {
        return mSessionDao.getWeeklyFocusMinutes();
    }

    public LiveData<Integer> getWeeklyCycles() {
        return mSessionDao.getWeeklyCompletedCycles();
    }

    public LiveData<List<SessionDao.DailyStatRow>> getWeeklyDailyStats() {
        return mSessionDao.getWeeklyDailyStats();
    }

    public LiveData<List<SessionDao.DailyStatRow>> getDailyStatsSince(long sinceMillis) {
        return mSessionDao.getDailyStatsSince(sinceMillis);
    }

    public LiveData<List<SessionEntity>> getSessionsSince(long sinceMillis) {
        return mSessionDao.getSessionsSince(sinceMillis);
    }

    public void insert(SessionEntity session) {
        sExecutor.execute(() -> {
            mSessionDao.insert(session);

            // Đồng bộ lên Firestore nếu đã đăng nhập
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                mRemoteDataSource.uploadSession(user.getUid(), session);
            }
        });
    }


    /**
     * Save a completed or failed focus session.
     *
     * @param taskId nullable task id, because user may start timer without selecting a task
     * @param startTime session start time in milliseconds
     * @param endTime session end time in milliseconds
     * @param status "Completed" or "Failed"
     */

    public void saveSession(String taskId, long startTime, long endTime, String status) {
        int durationSeconds = (int) Math.max(0, (endTime - startTime) / 1000L);

        SessionEntity session = new SessionEntity();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Cập nhật userId thực tế thay vì 0
        // (Room DB đang dùng int cho userId, nếu muốn lưu ID string của Firebase thì có thể cần migration DB sau này.
        // Tạm thời ta chỉ sync lên cloud bằng FirebaseUser.getUid())
        session.userId = 0; 
        session.taskId = taskId;
        session.startTime = startTime;
        session.endTime = endTime;
        session.duration = durationSeconds;
        session.status = status;

        insert(session);
    }

    /**
     * Kéo lịch sử session từ Firestore về Room (gọi khi đăng nhập)
     */
    public void syncSessionsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        mRemoteDataSource.fetchSessions(user.getUid(), new FirestoreSessionRemoteDataSource.SessionFetchCallback() {
            @Override
            public void onSuccess(List<SessionEntity> sessions) {
                sExecutor.execute(() -> {
                    for (SessionEntity session : sessions) {
                        // Insert/Replace vào Room local
                        mSessionDao.insert(session);
                    }
                    android.util.Log.d("FirestoreSync", "Synced sessions from Firestore to Room: " + sessions.size());
                });
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("FirestoreSync", "Sync sessions from Firestore failed", e);
            }
        });
    }
}
