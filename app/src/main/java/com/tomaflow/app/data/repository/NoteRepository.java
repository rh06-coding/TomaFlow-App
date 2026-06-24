package com.tomaflow.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.NoteDao;
import com.tomaflow.app.data.db.entity.NoteEntity;
import com.tomaflow.app.data.remote.FirestoreNoteRemoteDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final LiveData<List<NoteEntity>> allNotes;
    private final ExecutorService executorService;
    
    private final UserRepository mUserRepository;
    private final FirestoreNoteRemoteDataSource mRemoteDataSource;

    public NoteRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
        mUserRepository = UserRepository.getInstance();
        mRemoteDataSource = new FirestoreNoteRemoteDataSource();
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void insert(NoteEntity note) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            if (note.createdAt == 0L) {
                note.createdAt = now;
            }
            note.updatedAt = now;
            note.userId = mUserRepository.getCurrentUserId();

            noteDao.insertNote(note);

            mRemoteDataSource.uploadNote(note.userId, note);
        });
    }

    public void delete(NoteEntity note) {
        executorService.execute(() -> {
            noteDao.deleteNote(note);
            mRemoteDataSource.deleteNote(mUserRepository.getCurrentUserId(), note.noteId);
        });
    }

    public void syncNotesFromFirestore() {
        String userId = mUserRepository.getCurrentUserId();
        if (userId == null || userId.isEmpty()) return;

        mRemoteDataSource.fetchNotes(userId, new FirestoreNoteRemoteDataSource.NoteFetchCallback() {
            @Override
            public void onSuccess(List<NoteEntity> remoteNotes) {
                executorService.execute(() -> {
                    noteDao.deleteAll();
                    for (NoteEntity note : remoteNotes) {
                        noteDao.insertNote(note);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Log sync error
            }
        });
    }
}
