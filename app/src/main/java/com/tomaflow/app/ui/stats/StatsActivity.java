package com.tomaflow.app.ui.stats;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * StatsActivity — Weekly Progress / Statistics Screen (stub).
 *
 * Corresponds to the Stats tab in the bottom navigation.
 * Will render bar charts for Focus Hours and Pomodoro Cycles
 * sourced from the Room database session log.
 *
 * Prototype reference: StatsScreen.tsx
 *
 * TODO: Inflate activity_stats.xml, add MPAndroidChart or Canvas-drawn bars,
 *       connect SessionRepository with aggregated weekly query.
 *
 * SQL NOTE (per project rules):
 *   All future SQL strings must use lowercase commands and UPPERCASE identifiers.
 *   Example:
 *     "select sum(DURATION_MINUTES) from SESSION where strftime('%W', CREATED_AT) = strftime('%W', 'now')"
 */
public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_stats);
    }
}
