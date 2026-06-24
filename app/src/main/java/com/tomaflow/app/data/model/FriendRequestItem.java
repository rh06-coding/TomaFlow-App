package com.tomaflow.app.data.model;

public class FriendRequestItem {
    public FriendConnection connection;
    public UserProfile user;

    public FriendRequestItem(FriendConnection connection, UserProfile user) {
        this.connection = connection;
        this.user = user;
    }
}
