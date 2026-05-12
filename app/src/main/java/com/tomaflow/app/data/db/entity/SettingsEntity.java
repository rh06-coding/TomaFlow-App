package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User preferences for timer durations and sound. Prepared for Room-based settings.
 */
@Entity(tableName = "Settings")
public class SettingsEntity {

    @PrimaryKey(autoGenerate = true)
    public int settingId;

    public int userId;

    public int workDuration = 25;       // Focus duration in minutes
    public int shortBreak = 5;          // Short break in minutes
    public int longBreak = 15;          // Long break in minutes
    public int longBreakInterval = 4;   // Cycles before a long break

    public String focusMusic = "rain_sound"; // Background sound asset name
    public long createdAt;
}
