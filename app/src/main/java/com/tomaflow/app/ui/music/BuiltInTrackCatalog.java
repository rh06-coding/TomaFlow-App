package com.tomaflow.app.ui.music;

import com.tomaflow.app.R;
import com.tomaflow.app.data.model.BuiltInTrack;

import java.util.Arrays;
import java.util.List;

/**
 * Catalog of built-in background music tracks.
 *
 * HOW TO ADD DEFAULT MUSIC:
 * ────────────────────────────────────────────────────────────────────
 * 1. Copy your .mp3 or .ogg file into:
 *       app/src/main/res/raw/
 *    Example: app/src/main/res/raw/lofi_chill.mp3
 *    (filename must be lowercase, no spaces, only letters/digits/underscores)
 *
 * 2. Add a new BuiltInTrack entry below, referencing R.raw.your_filename:
 *       new BuiltInTrack("lofi_chill", "Lo-Fi Chill", "Lofi", "🎵", R.raw.lofi_chill),
 *
 * 3. That's it — the track will appear automatically in MusicPickerActivity.
 * ────────────────────────────────────────────────────────────────────
 *
 * NOTE: No tracks are included by default because audio files are large.
 * Add your own tracks to res/raw/ and register them here.
 * The app handles an empty catalog gracefully (shows "no tracks" state).
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
