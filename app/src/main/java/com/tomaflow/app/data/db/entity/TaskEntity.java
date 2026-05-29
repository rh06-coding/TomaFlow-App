package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bảng lưu công việc local.
 * Mỗi dòng là một task dùng cho to-do list và Pomodoro.
 */
@Entity(tableName = "Tasks")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    public int taskId;

    public int userId;           // Reserved for future Firebase sync

    public String title;
    public String description;
    public int estPomodoros = 1; // Estimated Pomodoro sessions needed

    // Trạng thái task: Pending, InProgress hoặc Completed.
    public String status;

    public long createdAt;
    public long updatedAt;

    // Danh sách tag của task, lưu dạng chuỗi: "study,urgent".
    public String tags;
    public TaskEntity() {
    }

    public TaskEntity(String title, String description, int estPomodoros) {
        this.userId = 0;
        this.title = title;
        this.description = description;
        this.estPomodoros = estPomodoros;
        this.status = "Pending";
        this.tags = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
