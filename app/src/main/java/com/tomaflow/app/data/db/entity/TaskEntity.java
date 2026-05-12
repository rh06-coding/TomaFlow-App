package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Task created by the user. Status lifecycle: Pending -> InProgress -> Completed.
 */
@Entity(tableName = "Tasks")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    public int taskId;

    public int userId;           // Reserved for future Firebase sync

    public String title;
    public String description;
    public int estPomodoros = 1; // Estimated Pomodoro sessions needed

    public String status = "Pending"; // Pending, InProgress, Completed

    public long createdAt;
    public long updatedAt;
}
