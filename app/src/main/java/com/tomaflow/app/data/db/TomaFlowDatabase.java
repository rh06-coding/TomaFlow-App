package com.tomaflow.app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.dao.TaskDao;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.db.entity.TaskEntity;

/**
 * TomaFlowDatabase — Room database entry point.
 *
 * Contains two entities:
 *   • TASK    — user-created to-do items
 *   • SESSION — completed Pomodoro session records
 *
 * SQL convention (project rule):
 *   All query strings in DAOs must use lowercase SQL commands and
 *   UPPERCASE table/column names. Do NOT use row_number() with cast().
 *
 *   Correct example:
 *     @Query("select * from TASK where IS_COMPLETED = 0 order by CREATED_AT desc")
 *
 * Access pattern: singleton via {@link #getInstance(Context)}.
 */
@Database(
        entities = {TaskEntity.class, SessionEntity.class},
        version  = 1,
        exportSchema = false
)
public abstract class TomaFlowDatabase extends RoomDatabase {

    private static volatile TomaFlowDatabase sInstance;

    // ── DAOs ─────────────────────────────────────────────────────────────────

    public abstract TaskDao    taskDao();
    public abstract SessionDao sessionDao();

    // ── Singleton factory ─────────────────────────────────────────────────────

    public static TomaFlowDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TomaFlowDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TomaFlowDatabase.class,
                                    "tomaflow.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }
}
