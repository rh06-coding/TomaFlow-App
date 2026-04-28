package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("delete from Tasks where taskId = :taskId")
    void deleteById(long taskId);

    @Query("select * from Tasks order by createdAt desc")
    LiveData<List<TaskEntity>> getAllTasks();

    @Query("select * from Tasks where status != 'Completed' order by createdAt desc")
    LiveData<List<TaskEntity>> getPendingTasks();

    @Query("select * from Tasks where taskId = :taskId limit 1")
    LiveData<TaskEntity> getTaskById(long taskId);

    @Query("select count(*) from Tasks where status != 'Completed'")
    LiveData<Integer> getPendingTaskCount();
}