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
 * DAO thao tác với bảng Sessions.
 * Dùng để lưu lịch sử Pomodoro và đọc thống kê theo tuần.
 */
@Dao
public interface SessionDao {

    // Lưu một phiên Pomodoro đã hoàn thành hoặc bị hủy.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SessionEntity session);

    @Query("select * from Sessions order by startTime desc")
    LiveData<List<SessionEntity>> getAllSessions();

    // Tổng số phút focus đã hoàn thành trong tuần hiện tại.
    @Query("select cast(coalesce(sum(duration), 0) / 60 as integer) from Sessions " +
            "where status = 'Completed' " +
            "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now')")
    LiveData<Integer> getWeeklyFocusMinutes();

    // Số phiên Pomodoro hoàn thành trong tuần hiện tại.
    @Query("select count(*) from Sessions " +
           "where status = 'Completed' " +
           "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now')")
    LiveData<Integer> getWeeklyCompletedCycles();


    // Thống kê focus theo từng ngày trong tuần để vẽ biểu đồ.
    @Query("select strftime('%w', datetime(startTime / 1000, 'unixepoch')) as DAY_NUM, " +
            "cast(coalesce(sum(duration), 0) / 60 as integer) as MINUTES, " +
            "sum(case when status = 'Completed' then 1 else 0 end) as CYCLES " +
            "from Sessions " +
            "where status = 'Completed' " +
            "and strftime('%W', datetime(startTime / 1000, 'unixepoch')) = strftime('%W', 'now') " +
            "group by DAY_NUM order by DAY_NUM asc")
    LiveData<List<DailyStatRow>> getWeeklyDailyStats();

    // Thống kê focus theo ngày trong N ngày gần nhất (rolling window) để lọc 7/30 ngày.
    // Gom 30 ngày vào 7 bucket thứ trong tuần, tái sử dụng cùng cấu trúc biểu đồ.
    @Query("select strftime('%w', datetime(startTime / 1000, 'unixepoch')) as DAY_NUM, " +
            "cast(coalesce(sum(duration), 0) / 60 as integer) as MINUTES, " +
            "sum(case when status = 'Completed' then 1 else 0 end) as CYCLES " +
            "from Sessions " +
            "where status = 'Completed' and startTime >= :sinceMillis " +
            "group by DAY_NUM order by DAY_NUM asc")
    LiveData<List<DailyStatRow>> getDailyStatsSince(long sinceMillis);

    // Các session kể từ mốc thời gian, dùng cho biểu đồ tròn theo khoảng đã chọn.
    @Query("select * from Sessions where startTime >= :sinceMillis order by startTime desc")
    LiveData<List<SessionEntity>> getSessionsSince(long sinceMillis);

    /** Row type for getWeeklyDailyStats(). */
    class DailyStatRow {
        @ColumnInfo(name = "DAY_NUM")      public String dayNum;        // "0"–"6"
        @ColumnInfo(name = "MINUTES")      public int minutes;           // focus minutes
        @ColumnInfo(name = "CYCLES")       public int cycles;
    }
}
