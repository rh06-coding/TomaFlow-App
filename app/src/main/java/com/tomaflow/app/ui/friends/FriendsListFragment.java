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
                TomaToast.show(requireContext(), "Permission denied. Cannot read contacts.", false);
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
        
        adapter = new FriendAdapter("Add", (user, action) -> {
            if ("Friend".equals(action)) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Unfriend")
                    .setMessage("Are you sure you want to unfriend " + user.name + "?")
                    .setPositiveButton("Unfriend", (dialog, which) -> {
                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                        friendRepository.removeConnection(connectionId)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Unfriended " + user.name, true));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else if ("Add".equals(action)) {
                friendRepository.sendFriendRequest(user.uid)
                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true))
                    .addOnFailureListener(e -> TomaToast.show(requireContext(), "Failed to send request", false));
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
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            for (int i = 0; i < connections.size(); i++) {
                String targetUid = connections.get(i).senderId.equals(myUid) ? connections.get(i).receiverId : connections.get(i).senderId;
                friendRepository.getUserProfile(targetUid).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        friendsList.add(task.getResult());
                    } else {
                        friendsList.add(new UserProfile(targetUid, "", "", "unknown", "Unknown User", "", ""));
                    }
                    
                    pendingCount[0]--;
                    if (pendingCount[0] == 0) {
                        if (adapter == null) {
                            adapter = new FriendAdapter("Friend", (user, action) -> {
                                if ("Friend".equals(action)) {
                                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Unfriend")
                                        .setMessage("Are you sure you want to unfriend " + user.name + "?")
                                        .setPositiveButton("Unfriend", (dialog, which) -> {
                                            // Find connection ID
                                            String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                            friendRepository.removeConnection(connectionId)
                                                .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Unfriended " + user.name, true));
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                                }
                            });
                            rvFriends.setAdapter(adapter);
                        }
                        adapter.setUserStatusMap(statusMap);
                        adapter.submitList(friendsList);
                    }
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
                adapter = new FriendAdapter("Add", (user, action) -> {
                    if ("Friend".equals(action)) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Unfriend")
                            .setMessage("Are you sure you want to unfriend " + user.name + "?")
                            .setPositiveButton("Unfriend", (dialog, which) -> {
                                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                friendRepository.removeConnection(connectionId)
                                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Unfriended " + user.name, true));
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    } else if ("Add".equals(action)) {
                        statusMap.put(user.uid, "SENT");
                        if (adapter != null) adapter.setUserStatusMap(statusMap);
                        friendRepository.sendFriendRequest(user.uid)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true));
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
            TomaToast.show(requireContext(), "No contacts found.", false);
            return;
        }
        TomaToast.show(requireContext(), "Searching contacts...", true);
        friendRepository.findFriendsByPhones(phones).addOnSuccessListener(users -> {
            if (users.isEmpty()) {
                TomaToast.show(requireContext(), "No friends found from contacts.", false);
            } else {
                emptyState.setVisibility(View.GONE);
                adapter = new FriendAdapter("Add", (user, action) -> {
                    if ("Friend".equals(action)) {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Unfriend")
                            .setMessage("Are you sure you want to unfriend " + user.name + "?")
                            .setPositiveButton("Unfriend", (dialog, which) -> {
                                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String connectionId = myId.compareTo(user.uid) < 0 ? myId + "_" + user.uid : user.uid + "_" + myId;
                                friendRepository.removeConnection(connectionId)
                                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Unfriended " + user.name, true));
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    } else if ("Add".equals(action)) {
                        statusMap.put(user.uid, "SENT");
                        if (adapter != null) adapter.setUserStatusMap(statusMap);
                        friendRepository.sendFriendRequest(user.uid)
                            .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true));
                    }
                });
                adapter.setUserStatusMap(statusMap);
                rvFriends.setAdapter(adapter);
                adapter.submitList(users);
            }
        });
    }
}
