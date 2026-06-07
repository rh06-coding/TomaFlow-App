package com.tomaflow.app.data.db.dao;
/**
 * DAO thao tác với bảng Settings.
 * Dùng để đọc và cập nhật cài đặt Pomodoro local.
 */
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tomaflow.app.data.db.entity.SettingsEntity;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SettingsEntity settings);

    @Update
    void update(SettingsEntity settings);


    // Hiện tại app dùng một bản cài đặt local chung.
    @Query("SELECT * FROM Settings LIMIT 1")
    LiveData<SettingsEntity> getSettings();

    @Query("SELECT * FROM Settings LIMIT 1")
    SettingsEntity getSettingsSync();

    @Query("DELETE FROM Settings")
    void deleteAll();
}