package com.tomaflow.app.ui.leaderboard;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.model.FriendConnection;
import com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource;
import com.tomaflow.app.data.repository.FriendRepository;

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

    private RecyclerView rvLeaderboard;
    private View layoutFriendsEmpty;
    private LeaderboardAdapter adapter;
    private FriendRepository friendRepository;
    private FirestoreSessionRemoteDataSource sessionDataSource;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        layoutFriendsEmpty = findViewById(R.id.layout_friends_empty);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            friendRepository = new FriendRepository(currentUserId);
            sessionDataSource = new FirestoreSessionRemoteDataSource();
            loadLeaderboardData();
        }

        findViewById(R.id.btn_add_friend).setOnClickListener(v -> finish());
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
                layoutFriendsEmpty.setVisibility(View.VISIBLE);
                rvLeaderboard.setVisibility(View.GONE);
                return;
            }
            
            layoutFriendsEmpty.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.VISIBLE);
            
            fetchStatsForUsers(userIds);
        });
    }

    private void fetchStatsForUsers(List<String> userIds) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        AtomicInteger pendingUsers = new AtomicInteger(userIds.size());
        
        for (String uid : userIds) {
            friendRepository.getUserProfile(uid).addOnSuccessListener(profile -> {
                String username = profile != null ? profile.name : "Unknown";
                if (uid.equals(currentUserId) && profile != null) username = profile.name + " (You)";
                
                final String finalUsername = username;
                sessionDataSource.fetchSessions(uid, new FirestoreSessionRemoteDataSource.SessionFetchCallback() {
                    @Override
                    public void onSuccess(List<SessionEntity> sessions) {
                        LeaderboardEntry entry = calculateStats(uid, finalUsername, sessions);
                        synchronized (entries) {
                            entries.add(entry);
                        }
                        checkAndSort(entries, pendingUsers.decrementAndGet());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LeaderboardEntry entry = new LeaderboardEntry("", uid, finalUsername, 0, 0);
                        synchronized (entries) {
                            entries.add(entry);
                        }
                        checkAndSort(entries, pendingUsers.decrementAndGet());
                    }
                });
            }).addOnFailureListener(e -> {
                sessionDataSource.fetchSessions(uid, new FirestoreSessionRemoteDataSource.SessionFetchCallback() {
                    @Override
                    public void onSuccess(List<SessionEntity> sessions) {
                        LeaderboardEntry entry = calculateStats(uid, "Unknown", sessions);
                        synchronized (entries) {
                            entries.add(entry);
                        }
                        checkAndSort(entries, pendingUsers.decrementAndGet());
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        LeaderboardEntry entry = new LeaderboardEntry("", uid, "Unknown", 0, 0);
                        synchronized (entries) {
                            entries.add(entry);
                        }
                        checkAndSort(entries, pendingUsers.decrementAndGet());
                    }
                });
            });
        }
    }

    private LeaderboardEntry calculateStats(String uid, String username, List<SessionEntity> sessions) {
        int pomodoros = 0;
        List<Long> completedDays = new ArrayList<>();

        for (SessionEntity s : sessions) {
             if ("Completed".equals(s.status)) {
                 pomodoros++;
                 
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
        
        return new LeaderboardEntry("", uid, username, pomodoros, streak);
    }

    private void checkAndSort(List<LeaderboardEntry> entries, int pending) {
        if (pending == 0) {
            Collections.sort(entries, (a, b) -> Integer.compare(b.pomodoros, a.pomodoros)); // Descending
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).rank = String.valueOf(i + 1);
            }
            
            runOnUiThread(() -> {
                adapter = new LeaderboardAdapter(entries, currentUserId);
                rvLeaderboard.setAdapter(adapter);
            });
        }
    }

    public static class LeaderboardEntry {
        public String rank;
        public String userId;
        public String username;
        public int pomodoros;
        public int streak;

        public LeaderboardEntry(String rank, String userId, String username, int pomodoros, int streak) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.pomodoros = pomodoros;
            this.streak = streak;
        }
    }
}

