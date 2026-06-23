package com.tomaflow.app.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey
    @NonNull
    public String noteId;

    public String title;
    public String content;
    public String mood;
    public long createdAt;

    public NoteEntity() {
        this.noteId = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }
}
