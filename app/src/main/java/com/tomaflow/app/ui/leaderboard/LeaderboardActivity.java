package com.tomaflow.app.ui.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.tomaflow.app.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private View layoutFriendsEmpty;
    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        layoutFriendsEmpty = findViewById(R.id.layout_friends_empty);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        
        List<LeaderboardEntry> mockData = generateMockData();
        adapter = new LeaderboardAdapter(mockData, "user_me");
        rvLeaderboard.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvLeaderboard.setVisibility(View.VISIBLE);
                    layoutFriendsEmpty.setVisibility(View.GONE);
                } else {
                    rvLeaderboard.setVisibility(View.GONE);
                    layoutFriendsEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        findViewById(R.id.btn_add_friend).setOnClickListener(v -> 
            com.tomaflow.app.utils.TomaToast.show(this, R.string.friend_system_coming_soon)
        );
    }

    private List<LeaderboardEntry> generateMockData() {
        List<LeaderboardEntry> list = new ArrayList<>();
        list.add(new LeaderboardEntry("1", "user_1", "Alex Chen", 42, 12));
        list.add(new LeaderboardEntry("2", "user_2", "Sarah Smith", 38, 9));
        list.add(new LeaderboardEntry("3", "user_me", "You", 35, 5));
        list.add(new LeaderboardEntry("4", "user_4", "David Kim", 30, 4));
        list.add(new LeaderboardEntry("5", "user_5", "Emma Watson", 28, 7));
        list.add(new LeaderboardEntry("6", "user_6", "Michael T.", 25, 2));
        list.add(new LeaderboardEntry("7", "user_7", "Jessica Alba", 22, 1));
        list.add(new LeaderboardEntry("8", "user_8", "Chris Evans", 19, 0));
        list.add(new LeaderboardEntry("9", "user_9", "Tom Holland", 15, 3));
        list.add(new LeaderboardEntry("10", "user_10", "Zendaya", 10, 1));
        return list;
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
