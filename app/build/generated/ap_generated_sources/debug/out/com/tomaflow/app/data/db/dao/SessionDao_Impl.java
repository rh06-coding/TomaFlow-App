package com.tomaflow.app.data.db.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tomaflow.app.data.db.entity.SessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `SESSION` (`SESSION_ID`,`TASK_ID`,`DURATION_MINUTES`,`WAS_COMPLETED`,`CREATED_AT`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SessionEntity entity) {
        statement.bindLong(1, entity.sessionId);
        if (entity.taskId == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.taskId);
        }
        statement.bindLong(3, entity.durationMinutes);
        final int _tmp = entity.wasCompleted ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.createdAt);
      }
    };
  }

  @Override
  public long insert(final SessionEntity session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfSessionEntity.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<SessionEntity>> getAllSessions() {
    final String _sql = "select * from SESSION order by CREATED_AT desc";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"SESSION"}, false, new Callable<List<SessionEntity>>() {
      @Override
      @Nullable
      public List<SessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "SESSION_ID");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "TASK_ID");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "DURATION_MINUTES");
          final int _cursorIndexOfWasCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "WAS_COMPLETED");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "CREATED_AT");
          final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionEntity _item;
            final Long _tmpTaskId;
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null;
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            }
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final boolean _tmpWasCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasCompleted);
            _tmpWasCompleted = _tmp != 0;
            _item = new SessionEntity(_tmpTaskId,_tmpDurationMinutes,_tmpWasCompleted);
            _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getWeeklyFocusMinutes() {
    final String _sql = "select sum(DURATION_MINUTES) from SESSION where strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = strftime('%W', 'now')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"SESSION"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getWeeklyCompletedCycles() {
    final String _sql = "select count(*) from SESSION where WAS_COMPLETED = 1 and strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = strftime('%W', 'now')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"SESSION"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<SessionDao.DailyStatRow>> getWeeklyDailyStats() {
    final String _sql = "select strftime('%w', datetime(CREATED_AT / 1000, 'unixepoch')) as DAY_NUM, sum(DURATION_MINUTES) as MINUTES, sum(case when WAS_COMPLETED = 1 then 1 else 0 end) as CYCLES from SESSION where strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) = strftime('%W', 'now') group by DAY_NUM order by DAY_NUM asc";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"SESSION"}, false, new Callable<List<SessionDao.DailyStatRow>>() {
      @Override
      @Nullable
      public List<SessionDao.DailyStatRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDayNum = 0;
          final int _cursorIndexOfMinutes = 1;
          final int _cursorIndexOfCycles = 2;
          final List<SessionDao.DailyStatRow> _result = new ArrayList<SessionDao.DailyStatRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionDao.DailyStatRow _item;
            _item = new SessionDao.DailyStatRow();
            if (_cursor.isNull(_cursorIndexOfDayNum)) {
              _item.dayNum = null;
            } else {
              _item.dayNum = _cursor.getString(_cursorIndexOfDayNum);
            }
            _item.minutes = _cursor.getInt(_cursorIndexOfMinutes);
            _item.cycles = _cursor.getInt(_cursorIndexOfCycles);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
