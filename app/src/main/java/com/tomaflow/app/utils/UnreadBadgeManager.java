package com.tomaflow.app.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tomaflow.app.data.model.FriendConnection;
import com.tomaflow.app.data.repository.FriendRepository;
import com.tomaflow.app.utils.ChatIds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnreadBadgeManager {

    private static UnreadBadgeManager instance;
    private final FirebaseFirestore db;
    private String currentUserId;
    private FriendRepository friendRepository;
    private Observer<List<FriendConnection>> friendsObserver;
    private boolean friendsObserverRegistered;
    
    private final MutableLiveData<Integer> totalUnreadCount = new MutableLiveData<>(0);
    private final MutableLiveData<Map<String, Integer>> unreadPerFriend = new MutableLiveData<>(new HashMap<>());
    
    private final Map<String, ListenerRegistration> listeners = new HashMap<>();

    private UnreadBadgeManager() {
        db = FirebaseFirestore.getInstance();
        init();
    }

    public static synchronized UnreadBadgeManager getInstance() {
        if (instance == null) {
            instance = new UnreadBadgeManager();
        }
        return instance;
    }

    /** Public entry point — call after login (e.g. from MainActivity.onCreate). Idempotent. */
    public void start() {
        init();
    }

    private void init() {
        // Guard against double-registration: the singleton constructor calls this,
        // and start() is re-invoked on re-login. Without this, observeForever would
        // stack a new permanent observer on every login.
        if (friendsObserverRegistered || currentUserId != null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();

        friendRepository = new FriendRepository(currentUserId);
        friendsObserver = this::onFriendsLoaded;
        friendRepository.getFriends().observeForever(friendsObserver);
        friendsObserverRegistered = true;
    }

    private void onFriendsLoaded(List<FriendConnection> friends) {
        if (friends == null || currentUserId == null) return;
        
        // Remove listeners for friends that were removed
        Map<String, ListenerRegistration> oldListeners = new HashMap<>(listeners);
        
        for (FriendConnection friend : friends) {
            String friendId = friend.senderId.equals(currentUserId) ? friend.receiverId : friend.senderId;
            String chatId = getChatId(currentUserId, friendId);
            
            if (!listeners.containsKey(chatId)) {
                ListenerRegistration registration = db.collection("chats").document(chatId).collection("messages")
                        .whereEqualTo("receiverId", currentUserId)
                        .whereEqualTo("isRead", false)
                        .addSnapshotListener((value, error) -> {
                            if (error != null) return;
                            
                            int count = value != null ? value.size() : 0;
                            Map<String, Integer> map = unreadPerFriend.getValue();
                            if (map == null) map = new HashMap<>();
                            
                            map.put(friendId, count);
                            unreadPerFriend.postValue(map);
                            
                            int total = 0;
                            for (int c : map.values()) {
                                total += c;
                            }
                            totalUnreadCount.postValue(total);
                        });
                listeners.put(chatId, registration);
            }
            oldListeners.remove(chatId);
        }
        
        // Clean up old listeners
        for (Map.Entry<String, ListenerRegistration> entry : oldListeners.entrySet()) {
            entry.getValue().remove();
            listeners.remove(entry.getKey());
        }
    }

    private String getChatId(String uid1, String uid2) {
        return ChatIds.chatIdFor(uid1, uid2);
    }

    public LiveData<Integer> getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public LiveData<Map<String, Integer>> getUnreadPerFriend() {
        return unreadPerFriend;
    }

    public void clear() {
        // Remove the friends LiveData observer FIRST. Without this the observeForever
        // observer leaks across sessions and the badge double-fires after re-login.
        if (friendRepository != null && friendsObserver != null) {
            friendRepository.getFriends().removeObserver(friendsObserver);
        }
        friendsObserver = null;
        friendsObserverRegistered = false;

        for (ListenerRegistration registration : listeners.values()) {
            registration.remove();
        }
        listeners.clear();
        totalUnreadCount.postValue(0);
        unreadPerFriend.postValue(new HashMap<>());
        currentUserId = null;
        friendRepository = null;
    }
}
