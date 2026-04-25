package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.List;

/**
 * TaskDao — Data Access Object for the TASK table.
 *
 * SQL convention (project rule):
 *   • All SQL commands are lowercase (select, from, where, insert, etc.)
 *   • All table/column names are UPPERCASE
 *   • Do NOT use row_number() with cast()
 */
@Dao
public interface TaskDao {

    // ── Insert ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TaskEntity task);

    // ── Update ────────────────────────────────────────────────────────────────

    @Update
    void update(TaskEntity task);

    // ── Delete ────────────────────────────────────────────────────────────────

    @Delete
    void delete(TaskEntity task);

    @Query("delete from TASK where TASK_ID = :taskId")
    void deleteById(long taskId);

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All tasks ordered newest-first. LiveData auto-updates the UI on change. */
    @Query("select * from TASK order by CREATED_AT desc")
    LiveData<List<TaskEntity>> getAllTasks();

    /** Only incomplete tasks — used on the Focus screen task card picker. */
    @Query("select * from TASK where IS_COMPLETED = 0 order by CREATED_AT desc")
    LiveData<List<TaskEntity>> getPendingTasks();

    /** Single task by its primary key. */
    @Query("select * from TASK where TASK_ID = :taskId limit 1")
    LiveData<TaskEntity> getTaskById(long taskId);

    /** Count of pending tasks — useful for the badge on the Tasks tab icon. */
    @Query("select count(*) from TASK where IS_COMPLETED = 0")
    LiveData<Integer> getPendingTaskCount();
}
