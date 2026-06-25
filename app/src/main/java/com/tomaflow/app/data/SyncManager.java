package com.tomaflow.app.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tomaflow.app.data.repository.NoteRepository;
import com.tomaflow.app.data.repository.RewardsRepository;
import com.tomaflow.app.data.repository.SessionRepository;
import com.tomaflow.app.data.repository.TaskRepository;
import com.tomaflow.app.timer.SettingsManager;

/**
 * Pulls the user's Firestore data into local stores (Room / SharedPreferences) after
 * login, and applies the persisted dark-mode theme. Moves this orchestration out of
 * the UI layer (previously {@code LoginActivity.goToMain} constructed every repository
 * directly) so it can also run on a cold start that skips LoginActivity.
 */
public class SyncManager {

    public static final String THEME_PREFS = "user_theme_prefs";

    private final Application app;

    public SyncManager(Application app) {
        this.app = app;
    }

    /** Run all post-login syncs, then {@code onComplete} on the UI thread. */
    public void syncAllOnLogin(Runnable onComplete) {
        syncData();
        applyTheme(onComplete);
    }

    /** Sync data only (no theme step) — for callers that manage theme themselves. */
    public void syncDataOnly() {
        syncData();
    }

    private void syncData() {
        new TaskRepository(app).syncTasksFromFirestore();
        new SessionRepository(app).syncSessionsFromFirestore();
        new RewardsRepository(app).syncRewardsFromFirestore();
        new NoteRepository(app).syncNotesFromFirestore();
    }

    private void applyTheme(Runnable onComplete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        SharedPreferences themePrefs = app.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);

        if (uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    // Only trust/refresh the cache when the Firestore fetch actually
                    // succeeded. A failed/offline fetch must not overwrite the cached
                    // pref and snap the user to light mode.
                    boolean fetched = task.isSuccessful() && task.getResult() != null && task.getResult().exists();
                    boolean isDark;
                    if (fetched) {
                        Boolean dark = task.getResult().getBoolean("isDarkMode");
                        isDark = dark != null && dark;
                        themePrefs.edit()
                                .putBoolean("dark_" + uid, isDark)
                                .putBoolean("last_dark", isDark).apply();
                    } else {
                        // Offline/error: keep the last known theme instead of forcing light.
                        isDark = themePrefs.getBoolean("dark_" + uid, themePrefs.getBoolean("last_dark", false));
                    }
                    new SettingsManager(app).setDarkMode(isDark);
                    AppCompatDelegate.setDefaultNightMode(
                            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                    if (onComplete != null) onComplete.run();
                });
    }
}
