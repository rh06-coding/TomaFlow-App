package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sessions")
public class SessionEntity {
    @PrimaryKey(autoGenerate = true)
    public int sessionId;

    public int userId; // Khóa ngoại chuẩn bị cho Firebase
    public Integer taskId; // Có thể null nếu user không chọn task

    public long startTime;
    public long endTime;
    public int duration; // Thời lượng thực tế

    public String status; // Completed, Failed
}