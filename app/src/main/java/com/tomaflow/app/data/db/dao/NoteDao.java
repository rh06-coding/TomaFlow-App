package com.tomaflow.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tomaflow.app.data.db.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteEntity note);

    @Delete
    void deleteNote(NoteEntity note);

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    LiveData<List<NoteEntity>> getAllNotes();
}
