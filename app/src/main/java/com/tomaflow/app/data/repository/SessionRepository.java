package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SessionRepository {

    private final SessionDao mSessionDao;
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public SessionRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mSessionDao = db.sessionDao();
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
        sExecutor.execute(() -> mSessionDao.insert(session));
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
        session.userId = 0; // Reserved for cloud/Firebase user later
        session.taskId = taskId;
        session.startTime = startTime;
        session.endTime = endTime;
        session.duration = durationSeconds;
        session.status = status;

        insert(session);
    }
}
