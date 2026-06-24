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
        
        adapter = new FriendAdapter("Add", user -> {
            friendRepository.sendFriendRequest(user.uid)
                .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true))
                .addOnFailureListener(e -> TomaToast.show(requireContext(), "Failed to send request", false));
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
            if (connections == null || connections.isEmpty()) {
                adapter.submitList(new ArrayList<>());
                emptyState.setVisibility(View.VISIBLE);
                return;
            }
            emptyState.setVisibility(View.GONE);
            // We need to fetch UserProfiles for these connections.
            // For simplicity, we just show "Friend" in the UI.
            // Let's implement a quick fetch for UserProfiles.
            List<UserProfile> friendsList = new ArrayList<>();
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            for (int i = 0; i < connections.size(); i++) {
                String targetUid = connections.get(i).senderId.equals(myUid) ? connections.get(i).receiverId : connections.get(i).senderId;
                friendRepository.getUserProfile(targetUid).addOnSuccessListener(profile -> {
                    if (profile != null) {
                        friendsList.add(profile);
                        if (friendsList.size() == connections.size()) {
                            adapter = new FriendAdapter("Friend", null);
                            rvFriends.setAdapter(adapter);
                            adapter.submitList(friendsList);
                        }
                    }
                });
            }
        });
    }

    private void searchUsers(String query) {
        if (query.length() < 2) {
            loadMyFriends();
            return;
        }
        friendRepository.searchByUsername(query).addOnSuccessListener(users -> {
            if (users.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                adapter.submitList(new ArrayList<>());
            } else {
                emptyState.setVisibility(View.GONE);
                adapter = new FriendAdapter("Add", user -> {
                    friendRepository.sendFriendRequest(user.uid)
                        .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true));
                });
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
                adapter = new FriendAdapter("Add", user -> {
                    friendRepository.sendFriendRequest(user.uid)
                        .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Friend request sent to " + user.name, true));
                });
                rvFriends.setAdapter(adapter);
                adapter.submitList(users);
            }
        });
    }
}
