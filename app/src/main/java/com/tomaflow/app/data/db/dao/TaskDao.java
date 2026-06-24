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

/**
 * DAO thao tác với bảng Tasks.
 * Dùng cho CRUD task và các query lọc task.
 */
@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("delete from Tasks")
    void deleteAll();

    @Query("delete from Tasks where taskId = :taskId")
    void deleteById(String taskId);

    @Query("select * from Tasks order by createdAt desc")
    LiveData<List<TaskEntity>> getAllTasks();

    // Lấy các task chưa hoàn thành để hiển thị hoặc chọn khi focus.
    @Query("select * from Tasks where status != 'Completed' order by createdAt desc")
    LiveData<List<TaskEntity>> getPendingTasks();

    @Query("select * from Tasks where taskId = :taskId limit 1")
    LiveData<TaskEntity> getTaskById(String taskId);

    // Lấy task đồng bộ trong background thread để sync Firestore.
    @Query("select * from Tasks where taskId = :taskId limit 1")
    TaskEntity getTaskByIdSync(String taskId);

    // Đếm số task chưa hoàn thành.
    @Query("select count(*) from Tasks where status != 'Completed'")
    LiveData<Integer> getPendingTaskCount();

    // Cập nhật trạng thái task và thời gian chỉnh sửa.
    @Query("update Tasks set status = :status, updatedAt = :updatedAt where taskId = :taskId")
    void updateTaskStatus(String taskId, String status, long updatedAt);

    // Lọc các task có chứa tag được truyền vào.
    @Query("select * from Tasks where tags like '%' || :tag || '%' order by createdAt desc")
    LiveData<List<TaskEntity>> getTasksByTag(String tag);

    // Cập nhật tag cho task và thời gian chỉnh sửa.
    @Query("update Tasks set tags = :tags, updatedAt = :updatedAt where taskId = :taskId")
    void updateTags(String taskId, String tags, long updatedAt);
}