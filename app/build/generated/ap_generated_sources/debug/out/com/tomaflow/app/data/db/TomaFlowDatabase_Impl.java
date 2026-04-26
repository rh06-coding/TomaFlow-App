package com.tomaflow.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.dao.SessionDao_Impl;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.dao.TaskDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TomaFlowDatabase_Impl extends TomaFlowDatabase {
  private volatile TaskDao _taskDao;

  private volatile SessionDao _sessionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `TASK` (`TASK_ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT, `TAG` TEXT, `IS_COMPLETED` INTEGER NOT NULL, `CREATED_AT` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `SESSION` (`SESSION_ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TASK_ID` INTEGER, `DURATION_MINUTES` INTEGER NOT NULL, `WAS_COMPLETED` INTEGER NOT NULL, `CREATED_AT` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8434043bfa779e3a985acd9ca78211aa')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `TASK`");
        db.execSQL("DROP TABLE IF EXISTS `SESSION`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTASK = new HashMap<String, TableInfo.Column>(5);
        _columnsTASK.put("TASK_ID", new TableInfo.Column("TASK_ID", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTASK.put("TITLE", new TableInfo.Column("TITLE", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTASK.put("TAG", new TableInfo.Column("TAG", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTASK.put("IS_COMPLETED", new TableInfo.Column("IS_COMPLETED", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTASK.put("CREATED_AT", new TableInfo.Column("CREATED_AT", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTASK = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTASK = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTASK = new TableInfo("TASK", _columnsTASK, _foreignKeysTASK, _indicesTASK);
        final TableInfo _existingTASK = TableInfo.read(db, "TASK");
        if (!_infoTASK.equals(_existingTASK)) {
          return new RoomOpenHelper.ValidationResult(false, "TASK(com.tomaflow.app.data.db.entity.TaskEntity).\n"
                  + " Expected:\n" + _infoTASK + "\n"
                  + " Found:\n" + _existingTASK);
        }
        final HashMap<String, TableInfo.Column> _columnsSESSION = new HashMap<String, TableInfo.Column>(5);
        _columnsSESSION.put("SESSION_ID", new TableInfo.Column("SESSION_ID", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSESSION.put("TASK_ID", new TableInfo.Column("TASK_ID", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSESSION.put("DURATION_MINUTES", new TableInfo.Column("DURATION_MINUTES", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSESSION.put("WAS_COMPLETED", new TableInfo.Column("WAS_COMPLETED", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSESSION.put("CREATED_AT", new TableInfo.Column("CREATED_AT", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSESSION = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSESSION = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSESSION = new TableInfo("SESSION", _columnsSESSION, _foreignKeysSESSION, _indicesSESSION);
        final TableInfo _existingSESSION = TableInfo.read(db, "SESSION");
        if (!_infoSESSION.equals(_existingSESSION)) {
          return new RoomOpenHelper.ValidationResult(false, "SESSION(com.tomaflow.app.data.db.entity.SessionEntity).\n"
                  + " Expected:\n" + _infoSESSION + "\n"
                  + " Found:\n" + _existingSESSION);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8434043bfa779e3a985acd9ca78211aa", "9d519ae19eb2c9f3d28c87f48ff97e56");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "TASK","SESSION");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `TASK`");
      _db.execSQL("DELETE FROM `SESSION`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TaskDao.class, TaskDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionDao.class, SessionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TaskDao taskDao() {
    if (_taskDao != null) {
      return _taskDao;
    } else {
      synchronized(this) {
        if(_taskDao == null) {
          _taskDao = new TaskDao_Impl(this);
        }
        return _taskDao;
      }
    }
  }

  @Override
  public SessionDao sessionDao() {
    if (_sessionDao != null) {
      return _sessionDao;
    } else {
      synchronized(this) {
        if(_sessionDao == null) {
          _sessionDao = new SessionDao_Impl(this);
        }
        return _sessionDao;
      }
    }
  }
}
