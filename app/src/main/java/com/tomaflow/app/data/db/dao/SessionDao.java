package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;

/**
 * SessionDao — Data Access Object for the SESSION table.
 *
 * SQL convention (project rule):
 *   • All SQL commands are lowercase (select, from, sum, insert, etc.)
 *   • All table/column names are UPPERCASE
 *   • Do NOT use row_number() with cast()
 */
@Dao
public interface SessionDao {

    // ── Insert ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SessionEntity session);

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All sessions ordered newest-first. */
    @Query("select * from SESSION order by CREATED_AT desc")
    LiveData<List<SessionEntity>> getAllSessions();

    /**
     * Total focus minutes in the current calendar week.
     * Used for the "Total Hours" stat card.
     *
     * Note: strftime('%W') returns ISO week number (00–53).
     */
    @Query(
        "select sum(DURATION_MINUTES) from SESSION " +
        "where strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = " +
        "strftime('%W', 'now')"
    )
    LiveData<Integer> getWeeklyFocusMinutes();

    /**
     * Count of completed sessions in the current calendar week.
     * Used for the "Total Cycles" stat card.
     */
    @Query(
        "select count(*) from SESSION " +
        "where WAS_COMPLETED = 1 " +
        "and strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = " +
        "strftime('%W', 'now')"
    )
    LiveData<Integer> getWeeklyCompletedCycles();

    /**
     * Per-day totals for the current week, grouped by day-of-week.
     * Used to feed the bar chart on the Stats screen.
     * Returns rows: (DAY_LABEL text, MINUTES integer, CYCLES integer)
     */
    @Query(
        "select strftime('%w', datetime(CREATED_AT / 1000, 'unixepoch')) as DAY_NUM, " +
        "sum(DURATION_MINUTES) as MINUTES, " +
        "sum(case when WAS_COMPLETED = 1 then 1 else 0 end) as CYCLES " +
        "from SESSION " +
        "where strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = strftime('%W', 'now') " +
        "group by DAY_NUM " +
        "order by DAY_NUM asc"
    )
    LiveData<List<DailyStatRow>> getWeeklyDailyStats();

    // ── Inner projection class ─────────────────────────────────────────────────

    /** Projection for the per-day bar-chart query result. */
    class DailyStatRow {
        @ColumnInfo(name = "DAY_NUM")
        public String dayNum;    // "0" = Sun … "6" = Sat
        
        @ColumnInfo(name = "MINUTES")
        public int    minutes;
        
        @ColumnInfo(name = "CYCLES")
        public int    cycles;
    }
}
