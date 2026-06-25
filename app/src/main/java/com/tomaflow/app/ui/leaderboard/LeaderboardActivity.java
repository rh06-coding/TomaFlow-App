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
                        LeaderboardEntry entry = new LeaderboardEntry("0", uid, finalUsername, 0, 0, 1, 0);
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
                        LeaderboardEntry entry = new LeaderboardEntry("0", uid, "Unknown", 0, 0, 1, 0);
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
        return new LeaderboardEntry(String.valueOf(0), uid, username, pomodoros, streak, level, totalMinutes);
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
                rvLeaderboard.setAdapter(adapter);
            });
        }
    }

    private void bindPodium(List<LeaderboardEntry> entries) {
        View layoutPodium = findViewById(R.id.layout_podium);
        if (entries.isEmpty()) {
            layoutPodium.setVisibility(View.GONE);
            return;
        }
        layoutPodium.setVisibility(View.VISIBLE);

        View podium1 = findViewById(R.id.podium_rank_1);
        View podium2 = findViewById(R.id.podium_rank_2);
        View podium3 = findViewById(R.id.podium_rank_3);

        if (entries.size() > 0) {
            podium1.setVisibility(View.VISIBLE);
            ((android.widget.TextView) findViewById(R.id.tv_podium_name_1)).setText(entries.get(0).username);
            ((android.widget.TextView) findViewById(R.id.tv_podium_level_1)).setText(getString(R.string.rewards_level, entries.get(0).level));
        } else {
            podium1.setVisibility(View.INVISIBLE);
        }

        if (entries.size() > 1) {
            podium2.setVisibility(View.VISIBLE);
            ((android.widget.TextView) findViewById(R.id.tv_podium_name_2)).setText(entries.get(1).username);
            ((android.widget.TextView) findViewById(R.id.tv_podium_level_2)).setText(getString(R.string.rewards_level, entries.get(1).level));
        } else {
            podium2.setVisibility(View.INVISIBLE);
        }

        if (entries.size() > 2) {
            podium3.setVisibility(View.VISIBLE);
            ((android.widget.TextView) findViewById(R.id.tv_podium_name_3)).setText(entries.get(2).username);
            ((android.widget.TextView) findViewById(R.id.tv_podium_level_3)).setText(getString(R.string.rewards_level, entries.get(2).level));
        } else {
            podium3.setVisibility(View.INVISIBLE);
        }
    }

    public static class LeaderboardEntry {
        public String rank;
        public String userId;
        public String username;
        public int pomodoros;
        public int streak;
        public int level;
        public long totalMinutes;

        public LeaderboardEntry(String rank, String userId, String username, int pomodoros, int streak, int level, long totalMinutes) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.pomodoros = pomodoros;
            this.streak = streak;
            this.level = level;
            this.totalMinutes = totalMinutes;
        }
    }
}

