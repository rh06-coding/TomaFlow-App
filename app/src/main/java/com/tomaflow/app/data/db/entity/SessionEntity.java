package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One completed Pomodoro session record. Used for stats and history.
 */
@Entity(tableName = "Sessions")
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    public int sessionId;

    public int userId;           // Reserved for Firebase sync
    public Integer taskId;       // Nullable — user may not pick a task

    public long startTime;       // System.currentTimeMillis()
    public long endTime;
    public int duration;         // Actual duration in seconds (may differ from 25min if skipped)
    public String status;        // "Completed" or "Failed"
}
