package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for session data.
 * Called by MainActivity to persist sessions, and by StatsActivity for weekly charts.
 */
public class SessionRepository {

    private final SessionDao      mSessionDao;
    private final ExecutorService mExecutor;

    public SessionRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mSessionDao = db.sessionDao();
        mExecutor   = Executors.newSingleThreadExecutor();
    }

    // Reads
    public LiveData<List<SessionEntity>>              getAllSessions()       { return mSessionDao.getAllSessions(); }
    public LiveData<Integer>                          getWeeklyMinutes()    { return mSessionDao.getWeeklyFocusMinutes(); }
    public LiveData<Integer>                          getWeeklyCycles()     { return mSessionDao.getWeeklyCompletedCycles(); }
    public LiveData<List<SessionDao.DailyStatRow>>    getWeeklyDailyStats() { return mSessionDao.getWeeklyDailyStats(); }

    // Writes
    public void insert(SessionEntity session) { mExecutor.execute(() -> mSessionDao.insert(session)); }
}
