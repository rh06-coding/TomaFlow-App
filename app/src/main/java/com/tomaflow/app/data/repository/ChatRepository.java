package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.tomaflow.app.data.model.ChatMessage;
import com.tomaflow.app.utils.ChatIds;
import com.tomaflow.app.utils.FirestoreLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private final FirebaseFirestore db;
    private final Map<String, FirestoreLiveData<List<ChatMessage>>> messagesCache = new HashMap<>();

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public String getChatId(String uid1, String uid2) {
        return ChatIds.chatIdFor(uid1, uid2);
    }

    public void sendMessage(String chatId, ChatMessage message) {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
        String messageId = messagesRef.document().getId();
        message.id = messageId;
        messagesRef.document(messageId).set(message);
    }

    public LiveData<List<ChatMessage>> getMessages(String chatId) {
        FirestoreLiveData<List<ChatMessage>> liveData = messagesCache.get(chatId);
        if (liveData == null) {
            Query query = db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING);
            liveData = new FirestoreLiveData<List<ChatMessage>>() {
                @Override
                protected ListenerRegistration listen() {
                    return query.addSnapshotListener((value, error) -> {
                        if (error != null) return;
                        if (value != null) {
                            List<ChatMessage> messages = new ArrayList<>();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                                ChatMessage msg = doc.toObject(ChatMessage.class);
                                if (msg != null) {
                                    messages.add(msg);
                                }
                            }
                            postValue(messages);
                        }
                    });
                }
            };
            messagesCache.put(chatId, liveData);
        }
        return liveData;
    }

    public void markMessagesAsRead(String chatId, String currentUserId) {
        db.collection("chats").document(chatId).collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit();
                });
    }
}
