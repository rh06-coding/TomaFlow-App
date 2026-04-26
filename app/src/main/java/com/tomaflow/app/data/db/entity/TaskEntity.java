package com.tomaflow.app.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * TaskEntity — Room entity mapped to the TASK table.
 *
 * Table: TASK
 * Columns:
 *   TASK_ID      — auto-generated primary key
 *   TITLE        — display name of the task
 *   TAG          — category tag (e.g. "Design", "Dev", "Research")
 *   IS_COMPLETED — 0 = pending, 1 = done
 *   CREATED_AT   — Unix epoch milliseconds
 *
 * SQL convention: use lowercase commands, UPPERCASE identifiers.
 * Example DAO query:
 *   "select * from TASK where IS_COMPLETED = 0 order by CREATED_AT desc"
 */
@Entity(tableName = "TASK")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TASK_ID")
    public long taskId;

    @ColumnInfo(name = "TITLE")
    public String title;

    @ColumnInfo(name = "TAG")
    public String tag;

    @ColumnInfo(name = "IS_COMPLETED")
    public boolean isCompleted;

    @ColumnInfo(name = "CREATED_AT")
    public long createdAt;

    // ── Convenience constructor ────────────────────────────────────────────────

    public TaskEntity(String title, String tag) {
        this.title       = title;
        this.tag         = tag;
        this.isCompleted = false;
        this.createdAt   = System.currentTimeMillis();
    }
}
