package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tomaflow.app.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public String getChatId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }

    public void sendMessage(String chatId, ChatMessage message) {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
        String messageId = messagesRef.document().getId();
        message.id = messageId;
        messagesRef.document(messageId).set(message);
    }

    public LiveData<List<ChatMessage>> getMessages(String chatId) {
        MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>();
        
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        List<ChatMessage> messages = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) {
                                messages.add(msg);
                            }
                        }
                        messagesLiveData.postValue(messages);
                    }
                });

        return messagesLiveData;
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
