package com.tomaflow.app.ui.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.tomaflow.app.R;
import com.tomaflow.app.databinding.FragmentRewardsBinding;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RewardsFragment extends Fragment {

    private FragmentRewardsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRewardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.tomaflow.app.utils.HeaderUIHelper.setupHeader(view, getString(R.string.rewards_title), getViewLifecycleOwner());

        binding.rvFarm.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        FarmAdapter adapter = new FarmAdapter();
        binding.rvFarm.setAdapter(adapter);

        RewardsViewModel viewModel = new ViewModelProvider(this).get(RewardsViewModel.class);
        viewModel.getDailyTomatoes().observe(getViewLifecycleOwner(), adapter::submitList);

        com.tomaflow.app.data.repository.SessionRepository sessionRepository = new com.tomaflow.app.data.repository.SessionRepository(requireActivity().getApplication());
        sessionRepository.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
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

            int totalMinutes = (int) (totalSeconds / 60);
            int hours = totalMinutes / 60;

            // 1 Level = 10 hours (600 minutes)
            int level = (totalMinutes / 600) + 1;
            int currentLevelXp = totalMinutes % 600;
            int nextLevelXp = 600;

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

            binding.tvRewardsLevel.setText(getString(R.string.rewards_level, level));
            binding.tvRewardsHours.setText(String.valueOf(hours));
            binding.tvRewardsStreak.setText(String.valueOf(streak));

            binding.tvRewardsXp.setText(getString(R.string.rewards_xp_progress, currentLevelXp, nextLevelXp, level + 1));
            binding.progressLevel.setMax(nextLevelXp);
            binding.progressLevel.setProgressCompat(currentLevelXp, true);
        });

        // Month Navigation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            if (yearMonth != null) {
                binding.tvMonthYear.setText(yearMonth.format(formatter));
            }
        });

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        // Removed toolbar logic
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set defaults from auth
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            if (name != null) {
                binding.tvRewardsProfileName.setText(name);
            }
            if (name != null && !name.isEmpty()) {
                String initials = "";
                String[] parts = name.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
                binding.tvRewardsAvatarInitials.setText(initials.toUpperCase());
            }

            // Sync from Firestore profile
            new com.tomaflow.app.data.repository.ProfileRepository(user.getUid())
                .getProfile()
                .observe(getViewLifecycleOwner(), profile -> {
                    if (profile != null) {
                        if (profile.name != null && !profile.name.isEmpty()) {
                            binding.tvRewardsProfileName.setText(profile.name);
                        }
                        if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                            binding.ivRewardsAvatar.setVisibility(View.VISIBLE);
                            binding.tvRewardsAvatarInitials.setVisibility(View.GONE);
                            com.bumptech.glide.Glide.with(this).load(profile.avatarUrl).circleCrop().into(binding.ivRewardsAvatar);
                        }
                    }
                });
        }

        binding.cardLeaderboard.setOnClickListener(v -> {
            startActivity(new android.content.Intent(requireContext(), com.tomaflow.app.ui.leaderboard.LeaderboardActivity.class));
        });

        binding.cardFriends.setOnClickListener(v -> {
            startActivity(new android.content.Intent(requireContext(), com.tomaflow.app.ui.friends.FriendsActivity.class));
        });

        com.tomaflow.app.utils.UnreadBadgeManager.getInstance().getTotalUnreadCount().observe(getViewLifecycleOwner(), count -> {
            binding.badgeFriendsUnread.setVisibility((count != null && count > 0) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
