package com.tomaflow.app.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class UserRepository {

    private static volatile UserRepository INSTANCE;
    private final FirebaseAuth firebaseAuth;

    private UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static UserRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository();
                }
            }
        }
        return INSTANCE;
    }

    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}