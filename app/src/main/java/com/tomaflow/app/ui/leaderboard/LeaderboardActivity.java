package com.tomaflow.app.ui.leaderboard;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.model.FriendConnection;
import com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource;
import com.tomaflow.app.data.repository.FriendRepository;
import com.tomaflow.app.databinding.ActivityLeaderboardBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    private ActivityLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private FriendRepository friendRepository;
    private FirestoreSessionRemoteDataSource sessionDataSource;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            friendRepository = new FriendRepository(currentUserId);
            sessionDataSource = new FirestoreSessionRemoteDataSource();
            loadLeaderboardData();
        }

        binding.btnAddFriend.setOnClickListener(v -> finish());
    }

    private void loadLeaderboardData() {
        friendRepository.getFriends().observe(this, connections -> {
            if (connections == null) return;

            List<String> userIds = new ArrayList<>();
            userIds.add(currentUserId); // Add self

            for (FriendConnection c : connections) {
                String friendId = c.senderId.equals(currentUserId) ? c.receiverId : c.senderId;
                userIds.add(friendId);
            }

            if (userIds.size() == 1) {
                binding.layoutFriendsEmpty.setVisibility(View.VISIBLE);
                binding.rvLeaderboard.setVisibility(View.GONE);
                return;
            }

            binding.layoutFriendsEmpty.setVisibility(View.GONE);
            binding.rvLeaderboard.setVisibility(View.VISIBLE);

            fetchStatsForUsers(userIds);
        });
    }

    private void fetchStatsForUsers(List<String> userIds) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        AtomicInteger pendingUsers = new AtomicInteger(userIds.size());
        
        for (String uid : userIds) {
            friendRepository.getUserProfileLiveData(uid).observe(this, profile -> {
                String username = profile != null ? profile.name : "Unknown";
                String avatarUrl = profile != null ? profile.avatarUrl : null;
                boolean isVip = profile != null && profile.isVip;
                
                // Update if already exists
                boolean found = false;
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).userId.equals(uid)) {
                        entries.get(i).username = username;
                        entries.get(i).avatarUrl = avatarUrl;
                        entries.get(i).isVip = isVip;
                        if (adapter != null) {
                            adapter.notifyItemChanged(i);
                        }
                        bindPodium(entries);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    sessionDataSource.fetchSessions(uid, new FirestoreSessionRemoteDataSource.SessionFetchCallback() {
                        @Override
                        public void onSuccess(List<SessionEntity> sessions) {
                            LeaderboardEntry entry = calculateStats(uid, username, isVip, sessions);
                            entry.avatarUrl = avatarUrl;
                            synchronized (entries) {
                                boolean added = false;
                                for (LeaderboardEntry e : entries) {
                                    if (e.userId.equals(uid)) { added = true; break; }
                                }
                                if (!added) entries.add(entry);
                            }
                            checkAndSort(entries, pendingUsers.decrementAndGet());
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LeaderboardEntry entry = new LeaderboardEntry("0", uid, username, 0, 0, 1, 0, isVip);
                            entry.avatarUrl = avatarUrl;
                            synchronized (entries) {
                                boolean added = false;
                                for (LeaderboardEntry e : entries) {
                                    if (e.userId.equals(uid)) { added = true; break; }
                                }
                                if (!added) entries.add(entry);
                            }
                            checkAndSort(entries, pendingUsers.decrementAndGet());
                        }
                    });
                }
            });
        }
    }

    private LeaderboardEntry calculateStats(String uid, String username, boolean isVip, List<SessionEntity> sessions) {
        int pomodoros = 0;
        long totalMinutes = 0;
        List<Long> completedDays = new ArrayList<>();

        for (SessionEntity s : sessions) {
             if ("Completed".equals(s.status)) {
                 pomodoros++;
                 totalMinutes += s.duration / 60;
                 
                 Calendar c = Calendar.getInstance();
                 c.setTimeInMillis(s.startTime);
                 c.set(Calendar.HOUR_OF_DAY, 0);
                 c.set(Calendar.MINUTE, 0);
                 c.set(Calendar.SECOND, 0);
                 c.set(Calendar.MILLISECOND, 0);
                 long day = c.getTimeInMillis();
                 if (!completedDays.contains(day)) {
                     completedDays.add(day);
                 }
             }
        }
        Collections.sort(completedDays, Collections.reverseOrder());
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        long startOfYesterday = startOfToday - 24 * 60 * 60 * 1000L;

        int streak = 0;
        long currentDayToCheck = startOfToday;
        if (!completedDays.contains(currentDayToCheck)) {
            currentDayToCheck = startOfYesterday;
        }
        
        for (Long day : completedDays) {
            if (day == currentDayToCheck) {
                streak++;
                currentDayToCheck -= 24 * 60 * 60 * 1000L;
            } else if (day < currentDayToCheck) {
                break;
            }
        }
        
        int level = (int) (totalMinutes / 120) + 1;
        return new LeaderboardEntry(String.valueOf(0), uid, username, pomodoros, streak, level, totalMinutes, isVip);
    }

    private void checkAndSort(List<LeaderboardEntry> entries, int pending) {
        if (pending == 0) {
            Collections.sort(entries, (a, b) -> Long.compare(b.totalMinutes, a.totalMinutes)); // Descending
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).rank = String.valueOf(i + 1);
            }
            
            runOnUiThread(() -> {
                bindPodium(entries);

                List<LeaderboardEntry> remaining = new ArrayList<>();
                if (entries.size() > 3) {
                    remaining.addAll(entries.subList(3, entries.size()));
                }
                adapter = new LeaderboardAdapter(remaining, currentUserId);
                binding.rvLeaderboard.setAdapter(adapter);
            });
        }
    }

    private void bindPodium(List<LeaderboardEntry> entries) {
        if (entries.isEmpty()) {
            binding.layoutPodium.setVisibility(View.GONE);
            return;
        }
        binding.layoutPodium.setVisibility(View.VISIBLE);

        if (entries.size() > 0) {
            binding.podiumRank1.setVisibility(View.VISIBLE);
            binding.tvPodiumName1.setText(entries.get(0).username);
            binding.tvPodiumLevel1.setText(getString(R.string.rewards_level, entries.get(0).level));
            binding.ivPodiumVip1.setVisibility(entries.get(0).isVip ? View.VISIBLE : View.GONE);
            setPodiumAvatar(1, entries.get(0));
        } else {
            binding.podiumRank1.setVisibility(View.INVISIBLE);
        }

        if (entries.size() > 1) {
            binding.podiumRank2.setVisibility(View.VISIBLE);
            binding.tvPodiumName2.setText(entries.get(1).username);
            binding.tvPodiumLevel2.setText(getString(R.string.rewards_level, entries.get(1).level));
            binding.ivPodiumVip2.setVisibility(entries.get(1).isVip ? View.VISIBLE : View.GONE);
            setPodiumAvatar(2, entries.get(1));
        } else {
            binding.podiumRank2.setVisibility(View.INVISIBLE);
        }

        if (entries.size() > 2) {
            binding.podiumRank3.setVisibility(View.VISIBLE);
            binding.tvPodiumName3.setText(entries.get(2).username);
            binding.tvPodiumLevel3.setText(getString(R.string.rewards_level, entries.get(2).level));
            binding.ivPodiumVip3.setVisibility(entries.get(2).isVip ? View.VISIBLE : View.GONE);
            setPodiumAvatar(3, entries.get(2));
        } else {
            binding.podiumRank3.setVisibility(View.INVISIBLE);
        }
    }

    private void setPodiumAvatar(int rank, LeaderboardEntry entry) {
        android.widget.ImageView ivAvatar;
        android.widget.TextView tvInitials;
        android.widget.ImageView ivDefault;
        switch (rank) {
            case 1: ivAvatar = binding.ivPodiumAvatar1; tvInitials = binding.tvPodiumInitials1; ivDefault = binding.ivPodiumDefault1; break;
            case 2: ivAvatar = binding.ivPodiumAvatar2; tvInitials = binding.tvPodiumInitials2; ivDefault = binding.ivPodiumDefault2; break;
            default: ivAvatar = binding.ivPodiumAvatar3; tvInitials = binding.tvPodiumInitials3; ivDefault = binding.ivPodiumDefault3; break;
        }

        if (entry.avatarUrl != null && !entry.avatarUrl.isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            tvInitials.setVisibility(View.GONE);
            ivDefault.setVisibility(View.GONE);
            com.tomaflow.app.utils.AvatarHelper.loadAvatar(this, entry.avatarUrl, ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);
            String initials = "";
            String name = entry.username;
            if (name != null) {
                String[] parts = name.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
            }
            if (!initials.isEmpty()) {
                tvInitials.setText(initials.toUpperCase());
                tvInitials.setVisibility(View.VISIBLE);
                ivDefault.setVisibility(View.GONE);
            } else {
                tvInitials.setVisibility(View.GONE);
                ivDefault.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class LeaderboardEntry {
        public String rank;
        public String userId;
        public String username;
        public String avatarUrl;
        public int pomodoros;
        public int streak;
        public int level;
        public long totalMinutes;
        public boolean isVip;

        public LeaderboardEntry(String rank, String userId, String username, int pomodoros, int streak, int level, long totalMinutes, boolean isVip) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.pomodoros = pomodoros;
            this.streak = streak;
            this.level = level;
            this.totalMinutes = totalMinutes;
            this.avatarUrl = null;
            this.isVip = isVip;
        }
    }
}

