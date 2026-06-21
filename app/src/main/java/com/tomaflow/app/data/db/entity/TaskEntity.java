package com.tomaflow.app.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Bảng lưu công việc local.
 * Mỗi dòng là một task dùng cho to-do list và Pomodoro.
 */
@Entity(tableName = "Tasks")
public class TaskEntity {

    @PrimaryKey
    @NonNull
    public String taskId;

    public String userId;        // Firebase UID

    public String title;
    public String description;
    public int estPomodoros = 1; // Estimated Pomodoro sessions needed
    public int estimatedMinutes = 0; // Estimated duration in minutes (0 = not set)

    // Trạng thái task: Pending, InProgress hoặc Completed.
    public String status;

    public long createdAt;
    public long updatedAt;

    // Danh sách tag của task, lưu dạng chuỗi: "study,urgent".
    public String tags;

    public TaskEntity() {
        this.taskId = UUID.randomUUID().toString();
    }

    public TaskEntity(String title, String description, int estPomodoros) {
        this();
        this.userId = "";
        this.title = title;
        this.description = description;
        this.estPomodoros = estPomodoros;
        this.estimatedMinutes = 0;
        this.status = "Pending";
        this.tags = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public TaskEntity(String title, String description, int estPomodoros, int estimatedMinutes) {
        this(title, description, estPomodoros);
        this.estimatedMinutes = estimatedMinutes;
    }
}
