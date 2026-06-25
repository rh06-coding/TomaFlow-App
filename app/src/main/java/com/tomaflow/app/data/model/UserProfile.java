package com.tomaflow.app.data.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {
    public String uid;
    public String email;
    public String phone;
    public String username;
    public String name;
    public String dob;
    public String avatarUrl;
    public boolean isVip;

    public UserProfile() {}

    public UserProfile(String uid, String email, String phone, String username, String name, String dob, String avatarUrl) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.name = name;
        this.dob = dob;
        this.avatarUrl = avatarUrl;
        this.isVip = false;
    }
}
