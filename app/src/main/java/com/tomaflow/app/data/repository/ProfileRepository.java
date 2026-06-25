package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tomaflow.app.data.model.UserProfile;

public class ProfileRepository {
    private final FirebaseFirestore db;
    private final String uid;

    public ProfileRepository(String uid) {
        this.db = FirebaseFirestore.getInstance();
        this.uid = uid;
    }

    public LiveData<UserProfile> getProfile() {
        MutableLiveData<UserProfile> liveData = new MutableLiveData<>();
        if (uid == null) return liveData;
        db.collection("users").document(uid).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                liveData.setValue(snapshot.toObject(UserProfile.class));
            } else {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public Task<Void> saveProfile(UserProfile profile) {
        if (uid == null) return null;
        return db.collection("users").document(uid).set(profile);
    }
    
    public Task<Boolean> isUsernameUnique(String username) {
        return db.collection("users").whereEqualTo("username", username).get()
            .continueWith(task -> {
                if (!task.isSuccessful() || task.getResult() == null) return false;
                return task.getResult().isEmpty() || 
                    (task.getResult().size() == 1 && task.getResult().getDocuments().get(0).getId().equals(uid));
            });
    }
    
    public Task<Boolean> isPhoneUnique(String phone) {
        if (phone == null || phone.isEmpty()) return com.google.android.gms.tasks.Tasks.forResult(true);
        return db.collection("users").whereEqualTo("phone", phone).get()
            .continueWith(task -> {
                if (!task.isSuccessful() || task.getResult() == null) return false;
                return task.getResult().isEmpty() || 
                    (task.getResult().size() == 1 && task.getResult().getDocuments().get(0).getId().equals(uid));
            });
    }

    public Task<Void> updateVipStatus(boolean isVip) {
        if (uid == null) return null;
        return db.collection("users").document(uid).update("isVip", isVip);
    }

    public Task<Void> updateDarkMode(boolean isDarkMode) {
        if (uid == null) return null;
        return db.collection("users").document(uid).update("isDarkMode", isDarkMode);
    }
}
