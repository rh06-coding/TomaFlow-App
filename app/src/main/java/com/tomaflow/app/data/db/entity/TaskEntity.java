package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public int taskId;

    public int userId; // Khóa ngoại chuẩn bị cho Firebase

    public String title;
    public String description;
    public int estPomodoros = 1;

    public String status = "Pending"; // Pending, InProgress, Completed

    public long createdAt;
    public long updatedAt;
}