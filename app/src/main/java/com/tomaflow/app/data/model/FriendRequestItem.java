package com.tomaflow.app.data.model;

public class FriendRequestItem {
    public FriendConnection connection;
    public UserProfile user;
    public boolean isSentRequest;

    public FriendRequestItem(FriendConnection connection, UserProfile user) {
        this.connection = connection;
        this.user = user;
        this.isSentRequest = false;
    }
    
    public FriendRequestItem(FriendConnection connection, UserProfile user, boolean isSentRequest) {
        this.connection = connection;
        this.user = user;
        this.isSentRequest = isSentRequest;
    }
}
