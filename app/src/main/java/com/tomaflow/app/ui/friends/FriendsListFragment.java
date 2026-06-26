package com.tomaflow.app.ui.friends;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.data.repository.FriendRepository;
import com.tomaflow.app.utils.ContactsHelper;
import com.tomaflow.app.utils.TomaToast;

import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends Fragment {

    private FriendRepository friendRepository;
    private FriendAdapter adapter;
    private RecyclerView rvFriends;
    private View emptyState;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendRepository = new FriendRepository(uid);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                findFriendsFromContacts();
            } else {
                TomaToast.show(requireContext(), R.string.friend_permission_contacts_denied, false);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        emptyState = view.findViewById(R.id.empty_state);
        rvFriends = view.findViewById(R.id.rv_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new FriendAdapter(getString(R.string.friend_add), (user, action) -> {
            if (getString(R.string.friend_status_friend).equals(action)) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.friend_action_unfriend)
                    .setMessage(getString(R.string.friend_unfriend_confirm, user.name))
                    .setPositiveButton(R.string.friend_action_unfriend, (dialog, which) -> {
                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                        friendRepository.removeConnection(connectionId)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_unfriended, user.name), true));
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
            } else if (getString(R.string.friend_add).equals(action)) {
                friendRepository.sendFriendRequest(user.uid)
                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_request_sent, user.name), true))
                    .addOnFailureListener(e -> TomaToast.show(requireContext(), R.string.friend_request_failed, false));
            }
        });
        rvFriends.setAdapter(adapter);

        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btn_find_contacts).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                findFriendsFromContacts();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });
        
        // Load actual friends by default? For now, we will just show empty until searched.
        // Or we could load friends list here.
        loadMyFriends();
    }
    
    private void loadMyFriends() {
        friendRepository.getFriends().observe(getViewLifecycleOwner(), connections -> {
            updateUserStatusMap();
            if (connections == null || connections.isEmpty()) {
                if (adapter != null) adapter.submitList(new ArrayList<>());
                emptyState.setVisibility(View.VISIBLE);
                return;
            }
            emptyState.setVisibility(View.GONE);
            List<UserProfile> friendsList = new ArrayList<>();
            int[] pendingCount = {connections.size()};
            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            for (int i = 0; i < connections.size(); i++) {
                String targetUid = connections.get(i).senderId.equals(myUid) ? connections.get(i).receiverId : connections.get(i).senderId;
                
                // Initialize unknown placeholder to maintain count
                UserProfile placeholder = new UserProfile(targetUid, "", "", "unknown", getString(R.string.friend_unknown_user), "", "");
                friendsList.add(placeholder);
                final int index = i;
                
                friendRepository.getUserProfileLiveData(targetUid).observe(getViewLifecycleOwner(), profile -> {
                    if (profile != null) {
                        friendsList.set(index, profile);
                    } else {
                        friendsList.set(index, placeholder);
                    }
                    
                    if (adapter == null) {
                        adapter = new FriendAdapter(getString(R.string.friend_status_friend), (user, action) -> {
                            if (getString(R.string.friend_status_friend).equals(action)) {
                                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.friend_action_unfriend)
                                    .setMessage(getString(R.string.friend_unfriend_confirm, user.name))
                                    .setPositiveButton(R.string.friend_action_unfriend, (dialog, which) -> {
                                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                        friendRepository.removeConnection(connectionId)
                                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_unfriended, user.name), true));
                                    })
                                    .setNegativeButton(R.string.action_cancel, null)
                                    .show();
                            }
                        });
                        rvFriends.setAdapter(adapter);
                        adapter.setUserStatusMap(statusMap);
                        adapter.submitList(new ArrayList<>(friendsList));
                    } else {
                        adapter.submitList(new ArrayList<>(friendsList));
                        adapter.notifyItemChanged(index);
                    }
                    
                    // Observe unread badges
                    com.tomaflow.app.utils.UnreadBadgeManager.getInstance().getUnreadPerFriend().observe(getViewLifecycleOwner(), unreadMap -> {
                        if (adapter != null) {
                            adapter.setUnreadCountsMap(unreadMap);
                        }
                    });
                });
            }
        });

        // Also observe requests to keep the map updated
        friendRepository.getPendingRequests().observe(getViewLifecycleOwner(), connections -> updateUserStatusMap());
        friendRepository.getSentRequests().observe(getViewLifecycleOwner(), connections -> updateUserStatusMap());
    }

    private java.util.Map<String, String> statusMap = new java.util.HashMap<>();

    private void updateUserStatusMap() {
        statusMap.clear();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        List<com.tomaflow.app.data.model.FriendConnection> friends = friendRepository.getFriends().getValue();
        if (friends != null) {
            for (com.tomaflow.app.data.model.FriendConnection c : friends) {
                String target = c.senderId.equals(myUid) ? c.receiverId : c.senderId;
                statusMap.put(target, "ACCEPTED");
            }
        }
        
        List<com.tomaflow.app.data.model.FriendConnection> received = friendRepository.getPendingRequests().getValue();
        if (received != null) {
            for (com.tomaflow.app.data.model.FriendConnection c : received) {
                statusMap.put(c.senderId, "RECEIVED");
            }
        }
        
        List<com.tomaflow.app.data.model.FriendConnection> sent = friendRepository.getSentRequests().getValue();
        if (sent != null) {
            for (com.tomaflow.app.data.model.FriendConnection c : sent) {
                statusMap.put(c.receiverId, "SENT");
            }
        }
        
        if (adapter != null) {
            adapter.setUserStatusMap(statusMap);
        }
    }

    private void searchUsers(String query) {
        if (query.length() < 2) {
            loadMyFriends();
            return;
        }
        friendRepository.searchByUsername(query).addOnSuccessListener(users -> {
            if (users.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                if (adapter != null) adapter.submitList(new ArrayList<>());
            } else {
                emptyState.setVisibility(View.GONE);
                adapter = new FriendAdapter(getString(R.string.friend_add), (user, action) -> {
                    if (getString(R.string.friend_status_friend).equals(action)) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle(R.string.friend_action_unfriend)
                            .setMessage(getString(R.string.friend_unfriend_confirm, user.name))
                            .setPositiveButton(R.string.friend_action_unfriend, (dialog, which) -> {
                                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                friendRepository.removeConnection(connectionId)
                                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_unfriended, user.name), true));
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    } else if (getString(R.string.friend_add).equals(action)) {
                        statusMap.put(user.uid, "SENT");
                        if (adapter != null) adapter.setUserStatusMap(statusMap);
                        friendRepository.sendFriendRequest(user.uid)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_request_sent, user.name), true));
                    }
                });
                adapter.setUserStatusMap(statusMap);
                rvFriends.setAdapter(adapter);
                adapter.submitList(users);
            }
        });
    }

    private void findFriendsFromContacts() {
        List<String> phones = ContactsHelper.getPhoneNumbers(requireContext());
        if (phones.isEmpty()) {
            TomaToast.show(requireContext(), R.string.friend_no_contacts_found, false);
            return;
        }
        TomaToast.show(requireContext(), R.string.friend_searching_contacts, true);
        friendRepository.findFriendsByPhones(phones).addOnSuccessListener(users -> {
            if (users.isEmpty()) {
                TomaToast.show(requireContext(), R.string.friend_no_friends_from_contacts, false);
            } else {
                emptyState.setVisibility(View.GONE);
                adapter = new FriendAdapter(getString(R.string.friend_add), (user, action) -> {
                    if (getString(R.string.friend_status_friend).equals(action)) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle(R.string.friend_action_unfriend)
                            .setMessage(getString(R.string.friend_unfriend_confirm, user.name))
                            .setPositiveButton(R.string.friend_action_unfriend, (dialog, which) -> {
                                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                friendRepository.removeConnection(connectionId)
                                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_unfriended, user.name), true));
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    } else if (getString(R.string.friend_add).equals(action)) {
                        statusMap.put(user.uid, "SENT");
                        if (adapter != null) adapter.setUserStatusMap(statusMap);
                        friendRepository.sendFriendRequest(user.uid)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), getString(R.string.friend_request_sent, user.name), true));
                    }
                });
                adapter.setUserStatusMap(statusMap);
                rvFriends.setAdapter(adapter);
                adapter.submitList(users);
            }
        });
    }
}
