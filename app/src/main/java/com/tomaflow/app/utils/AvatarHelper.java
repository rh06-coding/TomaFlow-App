package com.tomaflow.app.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AvatarHelper {
    public static void loadAvatar(Context context, String avatarUrl, ImageView imageView) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        try {
            if (avatarUrl.startsWith("data:image")) {
                String base64Image = avatarUrl.split(",")[1];
                byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                Glide.with(context)
                        .asBitmap()
                        .load(decodedBytes)
                        .circleCrop()
                        .into(imageView);
            } else {
                Glide.with(context)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Fallback attempt
                Glide.with(context)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(imageView);
            } catch (Exception ignored) {}
        }
    }
}
