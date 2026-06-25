package com.tomaflow.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.R;
import com.tomaflow.app.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvInitials = view.findViewById(R.id.tv_avatar_initials);

        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }

        View btnSettings = view.findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_focus, false)
                        .setLaunchSingleTop(true)
                        .build();
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_settings, null, navOptions);
            });
        }

        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            tvName.setText(name);

            if (name != null && !name.isEmpty()) {
                String initials = "";
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

        if (user != null) {
            com.tomaflow.app.data.repository.ProfileRepository repo = new com.tomaflow.app.data.repository.ProfileRepository(user.getUid());
            repo.getProfile().observe(getViewLifecycleOwner(), profile -> {
                if (profile != null) {
                    if (profile.name != null && !profile.name.isEmpty()) {
                        tvName.setText(profile.name);
                    }
                    if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                        android.widget.ImageView ivAvatar = view.findViewById(R.id.iv_profile_avatar);
                        if (ivAvatar != null) {
                            ivAvatar.setVisibility(View.VISIBLE);
                            tvInitials.setVisibility(View.GONE);
                            com.tomaflow.app.utils.AvatarHelper.loadAvatar(requireContext(), profile.avatarUrl, ivAvatar);
                        }
                    }
                    
                    // Update VIP status from Firebase
                    com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
                    if (profile.isVip && !sm.isVip()) {
                        sm.setVip(true);
                    }
                    
                    TextView tvRole = view.findViewById(R.id.tv_profile_role);
                    View btnUpgrade = view.findViewById(R.id.btn_upgrade_vip);
                    
                    if (profile.isVip || sm.isVip()) {
                        tvRole.setText(R.string.premium_badge);
                        tvRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.toma_warning));
                        if (btnUpgrade != null) btnUpgrade.setVisibility(View.GONE);
                    } else {
                        tvRole.setText(getString(R.string.profile_role));
                        tvRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.toma_primary));
                        
                        if (btnUpgrade != null) {
                            btnUpgrade.setVisibility(View.VISIBLE);
                            btnUpgrade.setOnClickListener(v -> startActivity(new Intent(requireContext(), com.tomaflow.app.ui.premium.PremiumActivity.class)));
                        }
                    }
                }
            });
        }

        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
            });
        }

        View cardLeaderboard = view.findViewById(R.id.card_leaderboard);
        if (cardLeaderboard != null) {
            cardLeaderboard.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.tomaflow.app.ui.leaderboard.LeaderboardActivity.class));
            });
        }
        
        View cardFriends = view.findViewById(R.id.card_friends);
        if (cardFriends != null) {
            cardFriends.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.tomaflow.app.ui.friends.FriendsActivity.class));
            });
            
            View badgeUnread = view.findViewById(R.id.badge_friends_unread);
            if (badgeUnread != null) {
                com.tomaflow.app.utils.UnreadBadgeManager.getInstance().getTotalUnreadCount().observe(getViewLifecycleOwner(), count -> {
                    badgeUnread.setVisibility((count != null && count > 0) ? View.VISIBLE : View.GONE);
                });
            }
        }

        TextView tvHours = view.findViewById(R.id.tv_hours_value);
        TextView tvStreak = view.findViewById(R.id.tv_streak_value);
        TextView tvLevel = view.findViewById(R.id.tv_level_value);
        com.tomaflow.app.data.repository.SessionRepository sessionRepo = new com.tomaflow.app.data.repository.SessionRepository(requireActivity().getApplication());
        sessionRepo.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions == null) return;
            
            long totalSeconds = 0;
            java.util.Set<String> activeDays = new java.util.HashSet<>();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getDefault());
            
            for (com.tomaflow.app.data.db.entity.SessionEntity s : sessions) {
                if ("Completed".equals(s.status)) {
                    totalSeconds += s.duration;
                    activeDays.add(sdf.format(new java.util.Date(s.startTime)));
                }
            }
            
            float hours = totalSeconds / 3600f;
            if (tvHours != null) tvHours.setText(String.format(java.util.Locale.getDefault(), "%.1f", hours));
            
            int streak = 0;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String todayStr = sdf.format(cal.getTime());
            
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = sdf.format(cal.getTime());
            
            if (activeDays.contains(todayStr)) {
                streak = 1;
                cal.setTime(new java.util.Date());
            } else if (activeDays.contains(yesterdayStr)) {
                streak = 1;
            }
            
            if (streak > 0) {
                while (true) {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
                    if (activeDays.contains(sdf.format(cal.getTime()))) {
                        streak++;
                    } else {
                        break;
                    }
                }
            }
            
            int totalMinutes = (int) (totalSeconds / 60);
            int level = (totalMinutes / 600) + 1;
            
            if (tvStreak != null) tvStreak.setText(String.valueOf(streak));
            if (tvLevel != null) tvLevel.setText(String.valueOf(level));
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new Thread(() -> {
                // Stop services if running
                requireContext().stopService(new Intent(requireContext(), com.tomaflow.app.timer.TimerEngineService.class));
                requireContext().stopService(new Intent(requireContext(), com.tomaflow.app.ui.music.MusicService.class));

                // Clear local database
                com.tomaflow.app.data.db.TomaFlowDatabase.getInstance(requireContext()).clearAllTables();
                
                // Clear SharedPreferences
                requireContext().getSharedPreferences("tomaflow_subscription", android.content.Context.MODE_PRIVATE).edit().clear().apply();
                requireContext().getSharedPreferences("rewards_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply();
                requireContext().getSharedPreferences(com.tomaflow.app.constants.AppConstants.PREFERENCES_FILE_NAME, android.content.Context.MODE_PRIVATE).edit().clear().apply();
                
                // Clear Unread Badge Manager
                com.tomaflow.app.utils.UnreadBadgeManager.getInstance().clear();
                
                auth.signOut();
                
                requireActivity().runOnUiThread(() -> {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }).start();
        });

        return view;
    }
}
