package com.tomaflow.app.data.repository;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.tomaflow.app.data.model.FriendConnection;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.utils.ConnectionIds;
import com.tomaflow.app.utils.FirestoreLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                UserProfile profile = doc.toObject(UserProfile.class);
                                if (profile != null) {
                                    profile.uid = doc.getId();
                                    results.add(profile);
                                }
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
                                    UserProfile profile = doc.toObject(UserProfile.class);
                                    if (profile != null) {
                                        profile.uid = doc.getId();
                                        list.add(profile);
                                    }
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
        String connectionId = ConnectionIds.idFor(currentUserId, targetUserId);

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
    private FirestoreLiveData<List<FriendConnection>> pendingRequestsLiveData;
    public LiveData<List<FriendConnection>> getPendingRequests() {
        if (pendingRequestsLiveData == null) {
            Query query = db.collection("friend_connections")
                    .whereEqualTo("receiverId", currentUserId)
                    .whereEqualTo("status", "PENDING");
            pendingRequestsLiveData = new FirestoreLiveData<List<FriendConnection>>() {
                @Override
                protected ListenerRegistration listen() {
                    return query.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        List<FriendConnection> list = new ArrayList<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                list.add(doc.toObject(FriendConnection.class));
                            }
                        }
                        setValue(list);
                    });
                }
            };
        }
        return pendingRequestsLiveData;
    }

    // 6.5 Get Sent Requests (where sender is me)
    private FirestoreLiveData<List<FriendConnection>> sentRequestsLiveData;
    public LiveData<List<FriendConnection>> getSentRequests() {
        if (sentRequestsLiveData == null) {
            Query query = db.collection("friend_connections")
                    .whereEqualTo("senderId", currentUserId)
                    .whereEqualTo("status", "PENDING");
            sentRequestsLiveData = new FirestoreLiveData<List<FriendConnection>>() {
                @Override
                protected ListenerRegistration listen() {
                    return query.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        List<FriendConnection> list = new ArrayList<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                list.add(doc.toObject(FriendConnection.class));
                            }
                        }
                        setValue(list);
                    });
                }
            };
        }
        return sentRequestsLiveData;
    }

    // 7. Get Friends (where I am sender or receiver and status is ACCEPTED)
    private FirestoreLiveData<List<FriendConnection>> friendsLiveData;
    public LiveData<List<FriendConnection>> getFriends() {
        if (friendsLiveData == null) {
            friendsLiveData = new FriendsLiveData();
        }
        return friendsLiveData;
    }

    /**
     * Friends list is the union of two queries (I am sender OR I am receiver, both
     * ACCEPTED). Firestore cannot OR two queries in one, so we listen to the
     * sender-side docs and, on each emission, fetch the receiver-side docs once and
     * merge. Only the outer snapshot listener is registered via FirestoreLiveData;
     * the inner one-shot {@code .get()} is gated on having active observers so we
     * don't publish after teardown.
     *
     * <p>Known limitation: the inner fetch is not live, so receiver-side new friends
     * only refresh on the next sender-side doc change. Preserves prior behavior;
     * tracked as a separate follow-up.</p>
     */
    private final class FriendsLiveData extends FirestoreLiveData<List<FriendConnection>> {
        @Override
        protected ListenerRegistration listen() {
            Query senderQuery = db.collection("friend_connections")
                    .whereEqualTo("status", "ACCEPTED")
                    .whereEqualTo("senderId", currentUserId);
            Query receiverQuery = db.collection("friend_connections")
                    .whereEqualTo("status", "ACCEPTED")
                    .whereEqualTo("receiverId", currentUserId);
            return senderQuery.addSnapshotListener((snap1, e1) -> {
                if (e1 != null) return;
                receiverQuery.get().addOnSuccessListener(snap2 -> {
                    if (!hasActiveObservers()) return;
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
                    setValue(list);
                });
            });
        }
    }
    
    // Get single user profile to map from connection to UserProfile
    public Task<UserProfile> getUserProfile(String uid) {
        return db.collection("users").document(uid).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                UserProfile profile = task.getResult().toObject(UserProfile.class);
                if (profile != null) {
                    profile.uid = task.getResult().getId();
                    return profile;
                }
            }
            return null;
        });
    }

    // Get live data for a specific user profile
    private final Map<String, FirestoreLiveData<UserProfile>> userProfileCache = new HashMap<>();
    public LiveData<UserProfile> getUserProfileLiveData(String uid) {
        FirestoreLiveData<UserProfile> liveData = userProfileCache.get(uid);
        if (liveData == null) {
            com.google.firebase.firestore.DocumentReference ref = db.collection("users").document(uid);
            liveData = new FirestoreLiveData<UserProfile>() {
                @Override
                protected ListenerRegistration listen() {
                    return ref.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            UserProfile profile = snapshot.toObject(UserProfile.class);
                            if (profile != null) {
                                profile.uid = snapshot.getId();
                                setValue(profile);
                            }
                        }
                    });
                }
            };
            userProfileCache.put(uid, liveData);
        }
        return liveData;
    }
}
