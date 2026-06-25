package com.tomaflow.app.ui.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RewardsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rewards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.tomaflow.app.utils.HeaderUIHelper.setupHeader(view, getString(R.string.rewards_title), getViewLifecycleOwner());

        RecyclerView rvFarm = view.findViewById(R.id.rv_farm);
        rvFarm.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        FarmAdapter adapter = new FarmAdapter();
        rvFarm.setAdapter(adapter);

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
            
            TextView tvLevel = view.findViewById(R.id.tv_rewards_level);
            TextView tvHours = view.findViewById(R.id.tv_rewards_hours);
            TextView tvStreak = view.findViewById(R.id.tv_rewards_streak);
            TextView tvXp = view.findViewById(R.id.tv_rewards_xp);
            com.google.android.material.progressindicator.LinearProgressIndicator progressLevel = view.findViewById(R.id.progress_level);
            
            if (tvLevel != null) tvLevel.setText(getString(R.string.rewards_level, level));
            if (tvHours != null) tvHours.setText(String.valueOf(hours));
            if (tvStreak != null) tvStreak.setText(String.valueOf(streak));
            
            if (tvXp != null) tvXp.setText(getString(R.string.rewards_xp_progress, currentLevelXp, nextLevelXp, level + 1));
            if (progressLevel != null) {
                progressLevel.setMax(nextLevelXp);
                progressLevel.setProgressCompat(currentLevelXp, true);
            }
        });

        // Month Navigation
        TextView tvMonthYear = view.findViewById(R.id.tv_month_year);
        ImageView btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        ImageView btnNextMonth = view.findViewById(R.id.btn_next_month);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            if (yearMonth != null) {
                tvMonthYear.setText(yearMonth.format(formatter));
            }
        });

        btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        // Removed toolbar logic
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            android.widget.TextView tvName = view.findViewById(R.id.tv_rewards_profile_name);
            android.widget.TextView tvInitials = view.findViewById(R.id.tv_rewards_avatar_initials);
            
            // Set defaults from auth
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            if (tvName != null && name != null) {
                tvName.setText(name);
            }
            if (tvInitials != null && name != null && !name.isEmpty()) {
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

            // Sync from Firestore profile
            new com.tomaflow.app.data.repository.ProfileRepository(user.getUid())
                .getProfile()
                .observe(getViewLifecycleOwner(), profile -> {
                    if (profile != null) {
                        if (profile.name != null && !profile.name.isEmpty()) {
                            if (tvName != null) tvName.setText(profile.name);
                        }
                        if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                            android.widget.ImageView ivAvatar = view.findViewById(R.id.iv_rewards_avatar);
                            if (ivAvatar != null) {
                                ivAvatar.setVisibility(View.VISIBLE);
                                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
                                com.bumptech.glide.Glide.with(this).load(profile.avatarUrl).circleCrop().into(ivAvatar);
                            }
                        }
                    }
                });
        }
    }
}
