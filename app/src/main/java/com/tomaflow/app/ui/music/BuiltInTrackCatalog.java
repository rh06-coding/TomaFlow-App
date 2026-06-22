package com.tomaflow.app.ui.music;

import com.tomaflow.app.R;
import com.tomaflow.app.data.model.BuiltInTrack;

import java.util.Arrays;
import java.util.List;

/**
 * Danh mục các bài nhạc nền tích hợp sẵn trong ứng dụng.
 *
 * CÁCH THÊM NHẠC MẶC ĐỊNH:
 * ========================================================================================
 * 1. Copy file .mp3 hoặc .ogg của bạn vào thư mục:
 *       app/src/main/res/raw/
 *    Ví dụ: app/src/main/res/raw/lofi_chill.mp3
 *    (Tên file phải viết thường, không có dấu cách, chỉ gồm chữ cái/chữ số/dấu gạch dưới)
 *
 * 2. Thêm một mục BuiltInTrack mới ở dưới, tham chiếu đến R.raw.tên_file_của_bạn:
 *       new BuiltInTrack("lofi_chill", "Lo-Fi Chill", "Lofi", "🎵", R.raw.lofi_chill),
 *
 * 3. Xong! Bài nhạc sẽ tự động xuất hiện trong MusicPickerActivity.
 * ========================================================================================
 *
 * LƯU Ý: Mặc định không có bài nhạc nào được thêm vào vì file âm thanh thường khá nặng.
 * Bạn có thể tự thêm file nhạc vào res/raw/ và đăng ký tại đây.
 * Ứng dụng xử lý danh sách rỗng rất mượt mà (hiện trạng thái "Chưa có nhạc").
 */
public final class BuiltInTrackCatalog {

    private BuiltInTrackCatalog() {}

    /**
     * All built-in tracks. Add entries here after placing audio files in res/raw/.
     *
     * Example (uncomment and add your file):
     *   new BuiltInTrack("lofi_chill",   "Lo-Fi Chill",      "Lofi",   "🎵", R.raw.lofi_chill),
     *   new BuiltInTrack("rain_sounds",  "Rain Sounds",       "Nature", "🌧", R.raw.rain_sounds),
     *   new BuiltInTrack("white_noise",  "White Noise",       "Focus",  "🌊", R.raw.white_noise),
     *   new BuiltInTrack("forest_walk",  "Forest Walk",       "Nature", "🌿", R.raw.forest_walk),
     *   new BuiltInTrack("deep_focus",   "Deep Focus",        "Focus",  "🧘", R.raw.deep_focus),
     */
    public static final List<BuiltInTrack> TRACKS = Arrays.asList(
        new BuiltInTrack("lofi_chill", "Lo-Fi Chill", "Lofi", "🎵", R.raw.lofi_chill),
        new BuiltInTrack("piano_focus", "Piano Focus", "Piano", "🎹", R.raw.piano_focus),
        new BuiltInTrack("rain_ambience", "Tiếng Mưa", "Nature", "🌧", R.raw.rain_ambience)
    );
}
