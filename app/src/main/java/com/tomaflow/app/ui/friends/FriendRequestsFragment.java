package com.tomaflow.app.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.FriendRequestItem;
import com.tomaflow.app.data.repository.FriendRepository;
import com.tomaflow.app.utils.TomaToast;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsFragment extends Fragment {

    private FriendRepository friendRepository;
    private FriendRequestAdapter adapter;
    private View emptyState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendRepository = new FriendRepository(uid);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        emptyState = view.findViewById(R.id.empty_state);
        RecyclerView rvRequests = view.findViewById(R.id.rv_requests);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new FriendRequestAdapter(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequestItem item) {
                friendRepository.acceptFriendRequest(item.connection.id)
                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Accepted request from " + item.user.name, true));
            }

            @Override
            public void onDecline(FriendRequestItem item) {
                friendRepository.removeConnection(item.connection.id)
                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Declined request", false));
            }
        });
        rvRequests.setAdapter(adapter);

        loadPendingRequests();
    }
    
    private void loadPendingRequests() {
        friendRepository.getPendingRequests().observe(getViewLifecycleOwner(), connections -> {
            if (connections == null || connections.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                adapter.submitList(new ArrayList<>());
                return;
            }
            emptyState.setVisibility(View.GONE);
            
            List<FriendRequestItem> items = new ArrayList<>();
            for (int i = 0; i < connections.size(); i++) {
                final int index = i;
                friendRepository.getUserProfile(connections.get(i).senderId).addOnSuccessListener(profile -> {
                    if (profile != null) {
                        items.add(new FriendRequestItem(connections.get(index), profile));
                        if (items.size() == connections.size()) {
                            adapter.submitList(items);
                        }
                    }
                });
            }
        });
    }
}
