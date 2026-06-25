package com.tomaflow.app.ui.friends;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.data.repository.ProfileRepository;
import com.tomaflow.app.utils.TomaToast;

public class FriendProfileActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    public static final String EXTRA_USER_ID = "extra_user_id";

    private String targetUserId;
    private ProfileRepository profileRepository;

    private TextView tvName, tvUsername, tvInitials;
    private ImageView ivAvatar;
    private TextView tvLevel, tvHours, tvStreak;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        targetUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (targetUserId == null) {
            finish();
            return;
        }

        profileRepository = new ProfileRepository(targetUserId);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        tvName = findViewById(R.id.tv_profile_name);
        tvUsername = findViewById(R.id.tv_profile_role);
        tvInitials = findViewById(R.id.tv_avatar_initials);
        ivAvatar = findViewById(R.id.iv_profile_avatar);
        tvLevel = findViewById(R.id.tv_level_value);
        tvHours = findViewById(R.id.tv_hours_value);
        tvStreak = findViewById(R.id.tv_streak_value);

        MaterialButton btnMessage = findViewById(R.id.btn_message);
        btnMessage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.tomaflow.app.ui.chat.ChatActivity.class);
            intent.putExtra(com.tomaflow.app.ui.chat.ChatActivity.EXTRA_FRIEND_ID, targetUserId);
            intent.putExtra(com.tomaflow.app.ui.chat.ChatActivity.EXTRA_FRIEND_NAME, tvName.getText().toString());
            
            // To pass avatar, we can use the Profile object loaded
            profileRepository.getProfile().observe(this, profile -> {
                if (profile != null && profile.avatarUrl != null) {
                    intent.putExtra(com.tomaflow.app.ui.chat.ChatActivity.EXTRA_FRIEND_AVATAR, profile.avatarUrl);
                }
                startActivity(intent);
                profileRepository.getProfile().removeObservers(this); // only run once
            });
        });

        loadProfileData();
        loadStatsData();
    }

    private void loadProfileData() {
        profileRepository.getProfile().observe(this, profile -> {
            if (profile != null) {
                String nameToDisplay = profile.name != null ? profile.name : profile.email;
                tvName.setText(profile.name != null ? profile.name : "Unknown");
                tvUsername.setText(profile.username != null ? "@" + profile.username : "");
                
                ImageView ivVipCrown = findViewById(R.id.iv_vip_crown);
                if (ivVipCrown != null) {
                    ivVipCrown.setVisibility(profile.isVip ? View.VISIBLE : View.GONE);
                }

                if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                    ivAvatar.setVisibility(View.VISIBLE);
                    tvInitials.setVisibility(View.GONE);
                    com.tomaflow.app.utils.AvatarHelper.loadAvatar(this, profile.avatarUrl, ivAvatar);
                } else {
                    ivAvatar.setVisibility(View.GONE);
                    tvInitials.setVisibility(View.VISIBLE);
                    String initials = "";
                    String name = tvName.getText().toString();
                    String[] parts = name.split(" ");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        initials += parts[0].charAt(0);
                        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                            initials += parts[parts.length - 1].charAt(0);
                        }
                    }
                    tvInitials.setText(initials.toUpperCase());
                }
            }
        });
    }

    private void loadStatsData() {
        com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource sessionSource = new com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource();
        sessionSource.fetchSessions(targetUserId, new com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource.SessionFetchCallback() {
            @Override
            public void onSuccess(java.util.List<com.tomaflow.app.data.db.entity.SessionEntity> sessions) {
                int totalMinutes = 0;
                int completedCycles = 0;
                for (com.tomaflow.app.data.db.entity.SessionEntity session : sessions) {
                    if ("Completed".equals(session.status)) {
                        totalMinutes += session.duration / 60;
                        completedCycles++;
                    }
                }
                int hours = totalMinutes / 60;
                int level = (totalMinutes / 120) + 1; // 2 hours = 1 level
                final int finalHours = hours;
                final int finalLevel = level;
                final int finalCompletedCycles = completedCycles;

                runOnUiThread(() -> {
                    tvLevel.setText(getString(R.string.rewards_level, finalLevel));
                    tvHours.setText(String.valueOf(finalHours));
                    tvStreak.setText(getString(R.string.friend_profile_cycles, finalCompletedCycles));
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    tvLevel.setText(getString(R.string.rewards_level, 1));
                    tvHours.setText("0");
                    tvStreak.setText(getString(R.string.friend_profile_cycles, 0));
                });
            }
        });
    }
}

