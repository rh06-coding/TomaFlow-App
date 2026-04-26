package com.tomaflow.app.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * SessionEntity — Room entity mapped to the SESSION table.
 *
 * Records every completed (or manually skipped) Pomodoro work session.
 * This data feeds the Stats screen charts.
 *
 * Table: SESSION
 * Columns:
 *   SESSION_ID        — auto-generated primary key
 *   TASK_ID           — FK to TASK (nullable if no task was selected)
 *   DURATION_MINUTES  — actual elapsed minutes (may differ from configured duration)
 *   WAS_COMPLETED     — 1 = timer ran to zero naturally, 0 = manually skipped
 *   CREATED_AT        — Unix epoch milliseconds (start time of session)
 *
 * SQL convention: use lowercase commands, UPPERCASE identifiers.
 * Example DAO query for weekly totals:
 *   "select sum(DURATION_MINUTES) from SESSION
 *    where strftime('%W', datetime(CREATED_AT / 1000, 'unixepoch')) =
 *          strftime('%W', 'now')"
 */
@Entity(tableName = "SESSION")
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SESSION_ID")
    public long sessionId;

    @ColumnInfo(name = "TASK_ID")
    public Long taskId;          // nullable — optional task association

    @ColumnInfo(name = "DURATION_MINUTES")
    public int durationMinutes;

    @ColumnInfo(name = "WAS_COMPLETED")
    public boolean wasCompleted;

    @ColumnInfo(name = "CREATED_AT")
    public long createdAt;

    // ── Convenience constructor ────────────────────────────────────────────────

    public SessionEntity(Long taskId, int durationMinutes, boolean wasCompleted) {
        this.taskId          = taskId;
        this.durationMinutes = durationMinutes;
        this.wasCompleted    = wasCompleted;
        this.createdAt       = System.currentTimeMillis();
    }
}
