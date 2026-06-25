package com.tomaflow.app.utils;

import android.view.View;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.R;

public class HeaderUIHelper {

    public static void setupHeader(View rootView, String screenTitle, androidx.lifecycle.LifecycleOwner lifecycleOwner) {
        TextView tvTitle = rootView.findViewById(R.id.tv_header_screen_title);
        if (tvTitle != null) {
            tvTitle.setText(screenTitle);
        }

        View btnAvatar = rootView.findViewById(R.id.btn_header_avatar);
        if (btnAvatar != null) {
            btnAvatar.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.nav_profile);
                } catch (Exception e) {
                    // ignore if not within nav host
                }
            });
        }

        TextView tvInitials = rootView.findViewById(R.id.tv_header_avatar_initials);
        android.widget.ImageView ivAvatar = rootView.findViewById(R.id.iv_header_avatar_img);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvInitials != null) {
            // Default Initials
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
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
                tvInitials.setText(initials.toUpperCase());
            }
            
            if (lifecycleOwner != null && ivAvatar != null) {
                new com.tomaflow.app.data.repository.ProfileRepository(user.getUid())
                    .getProfile()
                    .observe(lifecycleOwner, profile -> {
                        if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                            ivAvatar.setVisibility(View.VISIBLE);
                            tvInitials.setVisibility(View.GONE);
                            com.bumptech.glide.Glide.with(rootView).load(profile.avatarUrl).circleCrop().into(ivAvatar);
                        } else {
                            ivAvatar.setVisibility(View.GONE);
                            tvInitials.setVisibility(View.VISIBLE);
                        }
                    });
            }
        }
    }
}
