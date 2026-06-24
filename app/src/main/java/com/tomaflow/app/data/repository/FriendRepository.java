package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tomaflow.app.data.model.FriendConnection;
import com.tomaflow.app.data.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendRepository {
    private final FirebaseFirestore db;
    private final String currentUserId;

    public FriendRepository(String currentUserId) {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = currentUserId;
    }

    // 1. Search by username
    public Task<List<UserProfile>> searchByUsername(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        String q = query.trim().toLowerCase();
        
        return db.collection("users")
                .whereGreaterThanOrEqualTo("username", q)
                .whereLessThanOrEqualTo("username", q + "\uf8ff")
                .limit(10)
                .get()
                .continueWith(task -> {
                    List<UserProfile> results = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            if (!doc.getId().equals(currentUserId)) {
                                results.add(doc.toObject(UserProfile.class));
                            }
                        }
                    }
                    return results;
                });
    }

    // 2. Suggest friends via Phone Contacts
    public Task<List<UserProfile>> findFriendsByPhones(List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        
        // Firestore 'in' query supports max 10 elements.
        // For larger lists, we chunk them.
        List<Task<List<UserProfile>>> tasks = new ArrayList<>();
        
        for (int i = 0; i < phoneNumbers.size(); i += 10) {
            int end = Math.min(i + 10, phoneNumbers.size());
            List<String> chunk = phoneNumbers.subList(i, end);
            
            Task<List<UserProfile>> chunkTask = db.collection("users")
                    .whereIn("phone", chunk)
                    .get()
                    .continueWith(task -> {
                        List<UserProfile> list = new ArrayList<>();
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                if (!doc.getId().equals(currentUserId)) {
                                    list.add(doc.toObject(UserProfile.class));
                                }
                            }
                        }
                        return list;
                    });
            tasks.add(chunkTask);
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<UserProfile> finalResults = new ArrayList<>();
            for (Object result : task.getResult()) {
                finalResults.addAll((List<UserProfile>) result);
            }
            return finalResults;
        });
    }

    // 3. Send Friend Request
    public Task<Void> sendFriendRequest(String targetUserId) {
        String connectionId = currentUserId.compareTo(targetUserId) < 0 
                ? currentUserId + "_" + targetUserId 
                : targetUserId + "_" + currentUserId;
                
        FriendConnection conn = new FriendConnection(connectionId, currentUserId, targetUserId, "PENDING");
        return db.collection("friend_connections").document(connectionId).set(conn);
    }

    // 4. Accept Friend Request
    public Task<Void> acceptFriendRequest(String connectionId) {
        return db.collection("friend_connections").document(connectionId)
                 .update("status", "ACCEPTED", "timestamp", System.currentTimeMillis());
    }

    // 5. Decline/Cancel Friend Request / Unfriend
    public Task<Void> removeConnection(String connectionId) {
        return db.collection("friend_connections").document(connectionId).delete();
    }

    // 6. Get Pending Requests (where receiver is me)
    public LiveData<List<FriendConnection>> getPendingRequests() {
        MutableLiveData<List<FriendConnection>> liveData = new MutableLiveData<>();
        db.collection("friend_connections")
          .whereEqualTo("receiverId", currentUserId)
          .whereEqualTo("status", "PENDING")
          .addSnapshotListener((snapshot, e) -> {
              if (e != null) return;
              List<FriendConnection> list = new ArrayList<>();
              if (snapshot != null) {
                  for (DocumentSnapshot doc : snapshot.getDocuments()) {
                      list.add(doc.toObject(FriendConnection.class));
                  }
              }
              liveData.setValue(list);
          });
        return liveData;
    }

    // 7. Get Friends (where I am sender or receiver and status is ACCEPTED)
    // Firestore OR queries are tricky. We do two queries and merge them.
    public LiveData<List<FriendConnection>> getFriends() {
        MutableLiveData<List<FriendConnection>> liveData = new MutableLiveData<>();
        
        // Use addSnapshotListener to update in real time. We fetch both sets separately.
        db.collection("friend_connections")
          .whereEqualTo("status", "ACCEPTED")
          .whereIn("senderId", Arrays.asList(currentUserId))
          .addSnapshotListener((snap1, e1) -> {
              db.collection("friend_connections")
                .whereEqualTo("status", "ACCEPTED")
                .whereIn("receiverId", Arrays.asList(currentUserId))
                .get()
                .addOnSuccessListener(snap2 -> {
                    List<FriendConnection> list = new ArrayList<>();
                    if (snap1 != null) {
                        for (DocumentSnapshot doc : snap1.getDocuments()) {
                            list.add(doc.toObject(FriendConnection.class));
                        }
                    }
                    if (snap2 != null) {
                        for (DocumentSnapshot doc : snap2.getDocuments()) {
                            list.add(doc.toObject(FriendConnection.class));
                        }
                    }
                    liveData.setValue(list);
                });
          });
          
        return liveData;
    }
    
    // Get single user profile to map from connection to UserProfile
    public Task<UserProfile> getUserProfile(String uid) {
        return db.collection("users").document(uid).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                return task.getResult().toObject(UserProfile.class);
            }
            return null;
        });
    }
}
