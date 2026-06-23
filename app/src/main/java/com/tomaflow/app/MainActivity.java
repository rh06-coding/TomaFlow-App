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
                        Toast.makeText(MainActivity.this, getString(R.string.main_exit_toast), Toast.LENGTH_SHORT).show();
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
        }
    }
}
