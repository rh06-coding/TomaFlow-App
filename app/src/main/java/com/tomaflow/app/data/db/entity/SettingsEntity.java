package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bảng lưu cài đặt Pomodoro local của app.
 */
@Entity(tableName = "Settings")
public class SettingsEntity {

    @PrimaryKey(autoGenerate = true)
    public int settingId;

    public int userId;

    public int workDuration;    // Thời gian focus, tính bằng phút.
    public int shortBreak;      // Thời gian nghỉ ngắn, tính bằng phút.
    public int longBreak;       // Thời gian nghỉ dài, tính bằng phút.
    public int longBreakInterval; // Số chu kỳ Pomodoro trước khi nghỉ dài.
    public boolean focusMusic;  // Bật/tắt nhạc khi tập trung.
    public long createdAt;

    public SettingsEntity() {
    }

    @androidx.room.Ignore
    public SettingsEntity(int workDuration, int shortBreak, int longBreak, int longBreakInterval) {
        this.userId = 0;
        this.workDuration = workDuration;
        this.shortBreak = shortBreak;
        this.longBreak = longBreak;
        this.longBreakInterval = longBreakInterval;
        this.focusMusic = false;
        this.createdAt = System.currentTimeMillis();
    }
}
