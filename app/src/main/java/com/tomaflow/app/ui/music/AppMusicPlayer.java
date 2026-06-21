package com.tomaflow.app.ui.music;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tomaflow.app.data.model.BuiltInTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton wrapper around MediaPlayer for background music.
 *
 * KEY DESIGN:
 * - Uses AudioAttributes(USAGE_MEDIA, CONTENT_TYPE_MUSIC) — correct stream type,
 *   prevents system from misrouting audio or causing ducking/distortion.
 * - prepareAsync() so prepare never blocks the calling thread.
 * - cleanupPlayer() fully releases old player before creating a new one.
 * - MusicService is started ONCE after prepare completes, not on every state change.
 * - No interaction with AudioFocus — keep it simple.
 */
public class AppMusicPlayer {

    private static final String TAG = "AppMusicPlayer";

    private static AppMusicPlayer sInstance;

    public static synchronized AppMusicPlayer getInstance() {
        if (sInstance == null) {
            sInstance = new AppMusicPlayer();
        }
        return sInstance;
    }

    private MediaPlayer     mPlayer;
    @Nullable
    private BuiltInTrack    mCurrentTrack;
    private boolean         mIsPlaying = false;

    public interface OnPlaybackStateChanged {
        void onStateChanged(boolean isPlaying, @Nullable BuiltInTrack track);
    }

    private final List<OnPlaybackStateChanged> mListeners = new ArrayList<>();

    private AppMusicPlayer() {}

    // ── Listener management ───────────────────────────────────────────────────

    public void addListener(OnPlaybackStateChanged listener) {
        if (!mListeners.contains(listener)) mListeners.add(listener);
        listener.onStateChanged(mIsPlaying, mCurrentTrack); // deliver current state immediately
    }

    public void removeListener(OnPlaybackStateChanged listener) {
        mListeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnPlaybackStateChanged l : new ArrayList<>(mListeners)) {
            l.onStateChanged(mIsPlaying, mCurrentTrack);
        }
    }

    // ── Internal cleanup ──────────────────────────────────────────────────────

    private void cleanupPlayer() {
        if (mPlayer == null) return;
        try { if (mPlayer.isPlaying()) mPlayer.stop(); } catch (Exception ignored) {}
        try { mPlayer.reset();   } catch (Exception ignored) {}
        try { mPlayer.release(); } catch (Exception ignored) {}
        mPlayer = null;
    }

    private static AudioAttributes musicAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
    }

    // ── Play built-in (res/raw) ───────────────────────────────────────────────

    public void play(@NonNull Context context, @NonNull BuiltInTrack track) {
        // Already playing this exact track — do nothing
        if (mCurrentTrack != null && mCurrentTrack.id.equals(track.id) && mIsPlaying) return;

        cleanupPlayer();

        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(musicAttributes());

            android.content.res.AssetFileDescriptor afd =
                    context.getResources().openRawResourceFd(track.rawResId);
            if (afd == null) {
                Log.e(TAG, "AssetFileDescriptor null for: " + track.id);
                mp.release();
                return;
            }
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mp.setLooping(true);
            mp.setVolume(1.0f, 1.0f);

            mp.setOnPreparedListener(readyMp -> {
                if (mPlayer != readyMp) {
                    // User switched track before this one finished preparing
                    readyMp.release();
                    return;
                }
                readyMp.start();
                mCurrentTrack = track;
                mIsPlaying    = true;
                notifyListeners();
                startMusicService(context);
            });

            mp.setOnErrorListener((errMp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + " extra=" + extra);
                cleanupPlayer();
                mIsPlaying = false;
                notifyListeners();
                return true;
            });

            mPlayer = mp;
            mp.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "play() failed: " + track.id, e);
            cleanupPlayer();
        }
    }

    // ── Play device file (local path) ─────────────────────────────────────────

    public void playFromPath(@NonNull Context context,
                             @NonNull String trackId,
                             @NonNull String displayName,
                             @NonNull String path) {
        if (mCurrentTrack != null && mCurrentTrack.id.equals(trackId) && mIsPlaying) return;

        if (!new File(path).exists()) {
            Log.e(TAG, "File not found: " + path);
            return;
        }

        cleanupPlayer();

        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(musicAttributes());
            mp.setDataSource(path);
            mp.setLooping(true);
            mp.setVolume(1.0f, 1.0f);

            mp.setOnPreparedListener(readyMp -> {
                if (mPlayer != readyMp) {
                    readyMp.release();
                    return;
                }
                readyMp.start();
                mCurrentTrack = new BuiltInTrack(trackId, displayName, "Từ thiết bị", "🎵", 0);
                mIsPlaying    = true;
                notifyListeners();
                startMusicService(context);
            });

            mp.setOnErrorListener((errMp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error (path): what=" + what + " extra=" + extra);
                cleanupPlayer();
                mIsPlaying = false;
                notifyListeners();
                return true;
            });

            mPlayer = mp;
            mp.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "playFromPath() failed: " + path, e);
            cleanupPlayer();
        }
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    public void pause(Context context) {
        if (mPlayer == null || !mIsPlaying) return;
        try {
            mPlayer.pause();
            mIsPlaying = false;
            notifyListeners();
            startMusicService(context); // update notification button
        } catch (Exception e) {
            Log.e(TAG, "pause() error", e);
        }
    }

    public void resume(Context context) {
        if (mPlayer == null || mIsPlaying) return;
        try {
            mPlayer.start();
            mIsPlaying = true;
            notifyListeners();
            startMusicService(context); // update notification button
        } catch (Exception e) {
            Log.e(TAG, "resume() error", e);
        }
    }

    public void stop(Context context) {
        cleanupPlayer();
        mCurrentTrack = null;
        mIsPlaying    = false;
        notifyListeners();
        if (context != null) {
            try {
                context.stopService(new Intent(context, MusicService.class));
            } catch (Exception e) {
                Log.e(TAG, "stopService failed", e);
            }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean isPlaying()              { return mIsPlaying; }
    @Nullable public BuiltInTrack getCurrentTrack() { return mCurrentTrack; }

    // ── MusicService (foreground notification) ────────────────────────────────

    private void startMusicService(Context context) {
        try {
            Intent intent = new Intent(context, MusicService.class);
            intent.setAction(MusicService.ACTION_UPDATE_NOTIFICATION);
            androidx.core.content.ContextCompat.startForegroundService(context, intent);
        } catch (Exception e) {
            Log.e(TAG, "startForegroundService failed", e);
        }
    }
}
