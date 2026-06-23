package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.NoteDao;
import com.tomaflow.app.data.db.entity.NoteEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final LiveData<List<NoteEntity>> allNotes;
    private final ExecutorService executorService;

    public NoteRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void insert(NoteEntity note) {
        executorService.execute(() -> noteDao.insertNote(note));
    }

    public void delete(NoteEntity note) {
        executorService.execute(() -> noteDao.deleteNote(note));
    }
}
