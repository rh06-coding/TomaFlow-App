package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bảng lưu lịch sử Pomodoro.
 * Mỗi dòng là một phiên focus đã hoàn thành hoặc bị hủy.
 */
@Entity(tableName = "Sessions")
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    public int sessionId;

    public int userId;           // Reserved for Firebase sync
    public Integer taskId;       // Có thể null nếu user chạy timer mà không chọn task.

    public long startTime;       // System.currentTimeMillis()
    public long endTime;
    public int duration;         // Thời lượng focus thực tế, tính bằng giây.
    public String status;        // "Completed" or "Failed"
}
