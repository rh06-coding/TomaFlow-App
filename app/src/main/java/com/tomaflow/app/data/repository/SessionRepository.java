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
 * SessionRepository — single source of truth for SESSION data.
 *
 * Called by MainActivity after a Pomodoro session finishes to persist the record,
 * and by StatsActivity to fetch aggregated weekly data for the bar charts.
 */
public class SessionRepository {

    private final SessionDao      mSessionDao;
    private final ExecutorService mExecutor;

    public SessionRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        mSessionDao = db.sessionDao();
        mExecutor   = Executors.newSingleThreadExecutor();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public LiveData<List<SessionEntity>>              getAllSessions()        { return mSessionDao.getAllSessions(); }
    public LiveData<Integer>                          getWeeklyMinutes()     { return mSessionDao.getWeeklyFocusMinutes(); }
    public LiveData<Integer>                          getWeeklyCycles()      { return mSessionDao.getWeeklyCompletedCycles(); }
    public LiveData<List<SessionDao.DailyStatRow>>    getWeeklyDailyStats()  { return mSessionDao.getWeeklyDailyStats(); }

    // ── Write ─────────────────────────────────────────────────────────────────

    public void insert(SessionEntity session) {
        mExecutor.execute(() -> mSessionDao.insert(session));
    }
}
