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
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.data.repository.FriendRepository;
import com.tomaflow.app.utils.TomaToast;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsFragment extends Fragment {

    private FriendRepository friendRepository;
    private FriendRequestAdapter receivedAdapter;
    private FriendRequestAdapter sentAdapter;
    private View tvReceivedEmpty, tvSentEmpty;

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
        
        tvReceivedEmpty = view.findViewById(R.id.tv_received_empty);
        tvSentEmpty = view.findViewById(R.id.tv_sent_empty);
        
        RecyclerView rvReceived = view.findViewById(R.id.rv_received_requests);
        rvReceived.setLayoutManager(new LinearLayoutManager(getContext()));
        
        RecyclerView rvSent = view.findViewById(R.id.rv_sent_requests);
        rvSent.setLayoutManager(new LinearLayoutManager(getContext()));
        
        receivedAdapter = new FriendRequestAdapter(new FriendRequestAdapter.OnRequestActionListener() {
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
        rvReceived.setAdapter(receivedAdapter);
        
        sentAdapter = new FriendRequestAdapter(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequestItem item) { }

            @Override
            public void onDecline(FriendRequestItem item) {
                friendRepository.removeConnection(item.connection.id)
                    .addOnSuccessListener(aVoid -> TomaToast.show(requireContext(), "Canceled request", false));
            }
        });
        rvSent.setAdapter(sentAdapter);

        loadPendingRequests();
        loadSentRequests();
    }
    
    private void loadPendingRequests() {
        friendRepository.getPendingRequests().observe(getViewLifecycleOwner(), connections -> {
            if (connections == null || connections.isEmpty()) {
                tvReceivedEmpty.setVisibility(View.VISIBLE);
                receivedAdapter.submitList(new ArrayList<>());
                return;
            }
            tvReceivedEmpty.setVisibility(View.GONE);
            
            List<FriendRequestItem> items = new ArrayList<>();
            int[] pendingCount = {connections.size()};
            
            for (int i = 0; i < connections.size(); i++) {
                final int index = i;
                friendRepository.getUserProfile(connections.get(i).senderId).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        items.add(new FriendRequestItem(connections.get(index), task.getResult(), false));
                    } else {
                        UserProfile dummy = new UserProfile(connections.get(index).senderId, "", "", "unknown", "Unknown User", "", "");
                        items.add(new FriendRequestItem(connections.get(index), dummy, false));
                    }
                    
                    pendingCount[0]--;
                    if (pendingCount[0] == 0) {
                        receivedAdapter.submitList(items);
                    }
                });
            }
        });
    }

    private void loadSentRequests() {
        friendRepository.getSentRequests().observe(getViewLifecycleOwner(), connections -> {
            if (connections == null || connections.isEmpty()) {
                tvSentEmpty.setVisibility(View.VISIBLE);
                sentAdapter.submitList(new ArrayList<>());
                return;
            }
            tvSentEmpty.setVisibility(View.GONE);
            
            List<FriendRequestItem> items = new ArrayList<>();
            int[] pendingCount = {connections.size()};
            
            for (int i = 0; i < connections.size(); i++) {
                final int index = i;
                friendRepository.getUserProfile(connections.get(i).receiverId).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        items.add(new FriendRequestItem(connections.get(index), task.getResult(), true));
                    } else {
                        UserProfile dummy = new UserProfile(connections.get(index).receiverId, "", "", "unknown", "Unknown User", "", "");
                        items.add(new FriendRequestItem(connections.get(index), dummy, true));
                    }
                    
                    pendingCount[0]--;
                    if (pendingCount[0] == 0) {
                        sentAdapter.submitList(items);
                    }
                });
            }
        });
    }
}
