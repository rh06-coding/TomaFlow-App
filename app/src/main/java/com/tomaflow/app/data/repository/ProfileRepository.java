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
        // Write only the editable fields. UserProfile.isVip and isDarkMode are
        // primitive booleans (default false) that Firestore always serializes, so
        // .set(profile, merge()) would still overwrite them with false — revoking
        // VIP and flipping dark mode (the dark-mode flip caused a dark->light flash
        // via MainActivity's profile observer). A field-restricted Map with merge
        // preserves isDarkMode/isVip and any other existing fields on the doc.
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("email", profile.email);
        data.put("phone", profile.phone);
        data.put("username", profile.username);
        data.put("name", profile.name);
        data.put("dob", profile.dob);
        data.put("avatarUrl", profile.avatarUrl);
        return db.collection("users").document(uid).set(data, SetOptions.merge());
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
