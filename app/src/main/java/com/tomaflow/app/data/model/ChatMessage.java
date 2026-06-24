package com.tomaflow.app.data.model;

public class ChatMessage {
    public String id;
    public String senderId;
    public String receiverId;
    public String content;
    public String type; // "text" or "achievement"
    public long timestamp;
    public boolean isRead;

    public ChatMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(ChatMessage.class)
    }

    public ChatMessage(String id, String senderId, String receiverId, String content, String type, long timestamp, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
}
