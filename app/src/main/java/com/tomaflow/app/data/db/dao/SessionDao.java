package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SessionEntity session);

    @Query("select * from Sessions order by startTime desc")
    LiveData<List<SessionEntity>> getAllSessions();

    @Query(
            "select sum(duration) from Sessions " +
                    "where strftime('%W', datetime(startTime / 1000, 'unixepoch')) = " +
                    "strftime('%W', 'now')"
    )
    LiveData<Integer> getWeeklyFocusMinutes();

    @Query(
            "select count(*) from Sessions " +
                    "where status = 'Completed' " +
                    "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = " +
                    "strftime('%W', 'now')"
    )
    LiveData<Integer> getWeeklyCompletedCycles();

    @Query(
            "select strftime('%w', datetime(startTime / 1000, 'unixepoch')) as DAY_NUM, " +
                    "sum(duration) as MINUTES, " +
                    "sum(case when status = 'Completed' then 1 else 0 end) as CYCLES " +
                    "from Sessions " +
                    "where strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now') " +
                    "group by DAY_NUM " +
                    "order by DAY_NUM asc"
    )
    LiveData<List<DailyStatRow>> getWeeklyDailyStats();

    class DailyStatRow {
        @ColumnInfo(name = "DAY_NUM")
        public String dayNum;

        @ColumnInfo(name = "MINUTES")
        public int minutes;

        @ColumnInfo(name = "CYCLES")
        public int cycles;
    }
}