package com.tomaflow.app.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.tomaflow.app.data.db.dao.SettingsDao;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.db.entity.SettingsEntity;
import com.tomaflow.app.data.db.entity.TaskEntity;

/**
 * Room database singleton. Tables: Tasks, Sessions, Settings.
 *
 * Uses double-checked locking so only one instance exists across threads.
 * fallbackToDestructiveMigration simplifies schema changes during development.
 */
@Database(entities = {TaskEntity.class, SessionEntity.class, SettingsEntity.class}, version = 1, exportSchema = false)
public abstract class TomaFlowDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract SessionDao sessionDao();
    public abstract SettingsDao settingsDao();

    private static volatile TomaFlowDatabase INSTANCE;

    public static TomaFlowDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (TomaFlowDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TomaFlowDatabase.class, "tomaflow_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
