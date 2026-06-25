package com.tomaflow.app;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView mBottomNav;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNav = findViewById(R.id.bottom_nav);
        setupBottomNavigation();

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Cold start that skipped LoginActivity still needs local data synced from
            // Firestore; LoginActivity handles this on the post-login path.
            new com.tomaflow.app.data.SyncManager(getApplication()).syncDataOnly();
            // (Re)start unread-badge tracking — idempotent, covers cold start and re-login.
            com.tomaflow.app.utils.UnreadBadgeManager.getInstance().start();
            new com.tomaflow.app.data.repository.ProfileRepository(currentUser.getUid()).getProfile().observe(this, profile -> {
                if (profile != null) {
                    boolean dark = profile.isDarkMode;
                    getSharedPreferences("user_theme_prefs", MODE_PRIVATE).edit()
                            .putBoolean("dark_" + currentUser.getUid(), dark)
                            .putBoolean("last_dark", dark).apply();
                    new com.tomaflow.app.timer.SettingsManager(this).setDarkMode(dark);
                    int expectedMode = dark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                    if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != expectedMode) {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(expectedMode);
                    }
                }
            });
        }
    }

    private void setupBottomNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(mBottomNav, navController);

            // Double tap to exit callback
            OnBackPressedCallback exitCallback = new OnBackPressedCallback(true) {
                private long lastPressedTime;
                @Override
                public void handleOnBackPressed() {
                    if (System.currentTimeMillis() - lastPressedTime < 2000) {
                        finish();
                    } else {
                        com.tomaflow.app.utils.TomaToast.show(MainActivity.this, getString(R.string.main_exit_toast));
                        lastPressedTime = System.currentTimeMillis();
                    }
                }
            };
            getOnBackPressedDispatcher().addCallback(this, exitCallback);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Only enable double-tap to exit on the home tab (Focus)
                exitCallback.setEnabled(destination.getId() == R.id.nav_focus);

                // Hide bottom nav on Profile screen
                if (destination.getId() == R.id.nav_profile) {
                    mBottomNav.setVisibility(android.view.View.GONE);
                } else {
                    mBottomNav.setVisibility(android.view.View.VISIBLE);
                }
            });

            // Setup Unread Badge
            com.tomaflow.app.utils.UnreadBadgeManager.getInstance().getTotalUnreadCount().observe(this, count -> {
                com.google.android.material.badge.BadgeDrawable badge = mBottomNav.getOrCreateBadge(R.id.nav_profile);
                if (count != null && count > 0) {
                    badge.setVisible(true);
                    badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.toma_error));
                } else {
                    badge.setVisible(false);
                }
            });
        }
    }
}
