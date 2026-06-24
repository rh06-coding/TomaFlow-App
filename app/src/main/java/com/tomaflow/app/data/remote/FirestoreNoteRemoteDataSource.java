package com.tomaflow.app.data.remote;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.db.entity.NoteEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreNoteRemoteDataSource {

    private static final String TAG = "FirestoreNoteSync";

    private final FirebaseFirestore firestore;

    public FirestoreNoteRemoteDataSource() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface NoteFetchCallback {
        void onSuccess(List<NoteEntity> notes);

        void onFailure(Exception e);
    }

    public void uploadNote(String userId, NoteEntity note) {
        if (userId == null || userId.isEmpty() || note == null || note.noteId.isEmpty()) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("noteId", note.noteId);
        data.put("userId", userId);
        data.put("title", note.title);
        data.put("content", note.content);
        data.put("mood", note.mood);
        data.put("createdAt", note.createdAt);
        data.put("updatedAt", note.updatedAt);

        firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.noteId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Upload note success: " + note.noteId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Upload note failed: " + note.noteId, e)
                );
    }

    public void deleteNote(String userId, String noteId) {
        if (userId == null || userId.isEmpty() || noteId == null || noteId.isEmpty()) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Delete note success: " + noteId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Delete note failed: " + noteId, e)
                );
    }

    public void fetchNotes(String userId, NoteFetchCallback callback) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<NoteEntity> notes = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        NoteEntity note = new NoteEntity();

                        String noteId = document.getString("noteId");
                        String remoteUserId = document.getString("userId");
                        Long createdAt = document.getLong("createdAt");
                        Long updatedAt = document.getLong("updatedAt");

                        note.noteId = noteId == null ? "" : noteId;
                        note.userId = remoteUserId;
                        note.title = document.getString("title");
                        note.content = document.getString("content");
                        note.mood = document.getString("mood");
                        note.createdAt = createdAt == null ? 0L : createdAt;
                        note.updatedAt = updatedAt == null ? 0L : updatedAt;

                        notes.add(note);
                    }

                    Log.d(TAG, "Fetch notes success: " + notes.size());
                    callback.onSuccess(notes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fetch notes failed", e);
                    callback.onFailure(e);
                });
    }
}
