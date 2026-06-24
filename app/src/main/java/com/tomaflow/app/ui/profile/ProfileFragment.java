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
            btnSettings.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_settings));
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
                            com.bumptech.glide.Glide.with(this).load(profile.avatarUrl).circleCrop().into(ivAvatar);
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
                com.tomaflow.app.utils.TomaToast.show(requireContext(), "Leaderboard coming soon!", true);
            });
        }
        
        View cardFriends = view.findViewById(R.id.card_friends);
        if (cardFriends != null) {
            cardFriends.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.tomaflow.app.ui.friends.FriendsActivity.class));
            });
        }

        com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
        TextView tvRole = view.findViewById(R.id.tv_profile_role);
        if (sm.isVip()) {
            tvRole.setText(R.string.premium_badge);
            tvRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.toma_warning));
        } else {
            tvRole.setText(getString(R.string.profile_role));
            tvRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.toma_primary));
            
            // Add upgrade button to the header
            com.google.android.material.button.MaterialButton btnUpgrade = new com.google.android.material.button.MaterialButton(requireContext());
            btnUpgrade.setText(R.string.premium_btn_upgrade);
            btnUpgrade.setOnClickListener(v -> startActivity(new Intent(requireContext(), com.tomaflow.app.ui.premium.PremiumActivity.class)));
            ((ViewGroup) view.findViewById(R.id.container_avatar).getParent()).addView(btnUpgrade, 2);
        }

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
