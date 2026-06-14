package com.tomaflow.app.ui.rewards;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.tomaflow.app.R;

/**
 * RewardsActivity — Achievements / Rewards Screen (stub).
 *
 * Corresponds to the Rewards tab in the bottom navigation.
 * Will display a user profile card (level, total hours, cycle count, streak)
 * and a 3-column grid of achievement badges.
 *
 * Prototype reference: RewardsScreen.tsx
 *
 * TODO: Inflate activity_rewards.xml, wire RecyclerView (GridLayoutManager, 3 cols),
 *       connect RewardsRepository.
 */
public class RewardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        androidx.recyclerview.widget.RecyclerView rvBadges = findViewById(R.id.rv_badges);
        rvBadges.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
        BadgeAdapter adapter = new BadgeAdapter();
        rvBadges.setAdapter(adapter);

        RewardsViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(RewardsViewModel.class);
        viewModel.getBadges().observe(this, badges -> {
            adapter.submitList(badges);
        });

        ((androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
    }
}
