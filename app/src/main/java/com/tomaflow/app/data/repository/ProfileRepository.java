package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.utils.FirestoreLiveData;

public class ProfileRepository {
    private final FirebaseFirestore db;
    private final String uid;
    private FirestoreLiveData<UserProfile> profileLiveData;

    public ProfileRepository(String uid) {
        this.db = FirebaseFirestore.getInstance();
        this.uid = uid;
    }

    public LiveData<UserProfile> getProfile() {
        if (uid == null) return new FirestoreLiveData<UserProfile>() {
            @Override protected ListenerRegistration listen() { return null; }
        };
        if (profileLiveData == null) {
            DocumentReference ref = db.collection("users").document(uid);
            profileLiveData = new FirestoreLiveData<UserProfile>() {
                @Override
                protected ListenerRegistration listen() {
                    return ref.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            setValue(snapshot.toObject(UserProfile.class));
                        } else {
                            setValue(null);
                        }
                    });
                }
            };
        }
        return profileLiveData;
    }

    public Task<Void> saveProfile(UserProfile profile) {
        if (uid == null) return null;
        // merge() so we don't overwrite fields the caller didn't set. The 7-arg
        // UserProfile constructor defaults isVip/isDarkMode=false; without merge a
        // save from EditProfileActivity would clobber the user's dark-mode and VIP
        // flags (the dark-mode clobber also caused a dark->light flash on save via
        // MainActivity's profile observer).
        return db.collection("users").document(uid).set(profile, SetOptions.merge());
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
