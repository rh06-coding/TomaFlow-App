package com.tomaflow.app.data.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FriendConnection {
    public String id;
    public String senderId;
    public String receiverId;
    public String status; // "PENDING", "ACCEPTED"
    public long timestamp;

    public FriendConnection() {}

    public FriendConnection(String id, String senderId, String receiverId, String status) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
}
