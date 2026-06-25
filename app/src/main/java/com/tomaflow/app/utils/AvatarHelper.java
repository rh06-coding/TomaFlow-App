package com.tomaflow.app.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AvatarHelper {

    /** Tiền tố của ảnh avatar được mã hoá Base64 dạng data-URI. */
    public static final String DATA_IMAGE_PREFIX = "data:image";

    /**
     * Kiểm tra xem một URL avatar có phải là data-URI Base64 hay không.
     */
    public static boolean isBase64Avatar(String avatarUrl) {
        return avatarUrl != null && avatarUrl.startsWith(DATA_IMAGE_PREFIX);
    }

    /**
     * Trích xuất phần Base64 từ một data-URI dạng {@code data:image/...;base64,<dữ liệu>}.
     * Trả về {@code null} nếu không phải data-URI hoặc không có dấu phẩy ngăn cách.
     *
     * Được trích xuất thành hàm tĩnh (static) riêng để có thể kiểm thử đơn vị (unit-test)
     * mà không cần {@link Context} hay Glide.
     */
    public static String extractBase64(String avatarUrl) {
        if (avatarUrl == null) {
            return null;
        }
        int comma = avatarUrl.indexOf(',');
        if (comma < 0 || comma == avatarUrl.length() - 1) {
            return null;
        }
        return avatarUrl.substring(comma + 1);
    }

    public static void loadAvatar(Context context, String avatarUrl, ImageView imageView) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        try {
            if (isBase64Avatar(avatarUrl)) {
                String base64Image = extractBase64(avatarUrl);
                if (base64Image == null) {
                    imageView.setImageDrawable(null);
                    return;
                }
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
