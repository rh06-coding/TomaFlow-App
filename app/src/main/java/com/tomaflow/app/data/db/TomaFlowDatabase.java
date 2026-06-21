

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
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                TaskEntity.class,
                SessionEntity.class,
                SettingsEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class TomaFlowDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();

    public abstract SessionDao sessionDao();

    public abstract SettingsDao settingsDao();

    private static volatile TomaFlowDatabase INSTANCE;


    public static TomaFlowDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (TomaFlowDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TomaFlowDatabase.class,
                                    "tomaflow_database"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }


    // Migration v2 thêm cột tags vào bảng Tasks mà không xóa dữ liệu cũ.
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table Tasks add column tags TEXT not null default ''");
        }
    };

    // Migration v3: add estimatedMinutes column to Tasks
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Tasks ADD COLUMN estimatedMinutes INTEGER NOT NULL DEFAULT 0");
        }
    };
}