package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;

/** Room DAO for the Sessions table. Provides history and weekly aggregation queries. */
@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SessionEntity session);

    @Query("select * from Sessions order by startTime desc")
    LiveData<List<SessionEntity>> getAllSessions();

    /** Total focus minutes for the current week (ISO week number match). */
    @Query("select cast(coalesce(sum(duration), 0) / 60 as integer) from Sessions " +
            "where status = 'Completed' " +
            "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now')")
    LiveData<Integer> getWeeklyFocusMinutes();

    /** Count of completed cycles this week. */
    @Query("select count(*) from Sessions " +
           "where status = 'Completed' " +
           "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now')")
    LiveData<Integer> getWeeklyCompletedCycles();

    /**
     * Per-day breakdown for the current week. Used for the bar chart in StatsActivity.
     * DAY_NUM: "0"=Sun through "6"=Sat.
     */
    @Query("select strftime('%w', datetime(startTime / 1000, 'unixepoch')) as DAY_NUM, " +
            "cast(coalesce(sum(duration), 0) / 60 as integer) as MINUTES, " +
            "sum(case when status = 'Completed' then 1 else 0 end) as CYCLES " +
            "from Sessions " +
            "where status = 'Completed' " +
            "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now') " +
            "group by DAY_NUM order by DAY_NUM asc")
    LiveData<List<DailyStatRow>> getWeeklyDailyStats();

    /** Row type for getWeeklyDailyStats(). */
    class DailyStatRow {
        @ColumnInfo(name = "DAY_NUM")  public String dayNum;   // "0"–"6"
        @ColumnInfo(name = "MINUTES")  public int minutes;
        @ColumnInfo(name = "CYCLES")   public int cycles;
    }
}
