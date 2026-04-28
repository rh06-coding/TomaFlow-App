package com.tomaflow.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Settings")
public class SettingsEntity {
    @PrimaryKey(autoGenerate = true)
    public int settingId;

    public int userId;

    public int workDuration = 25;
    public int shortBreak = 5;
    public int longBreak = 15;
    public int longBreakInterval = 4;

    public String focusMusic = "rain_sound";
    public long createdAt;
}