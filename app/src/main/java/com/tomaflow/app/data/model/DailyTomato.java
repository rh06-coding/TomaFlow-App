package com.tomaflow.app.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.tomaflow.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyTomato {

    public enum Stage {
        DIRT(0, 0, R.string.toma_stage_dirt, 0), // 0 is no icon or placeholder
        SEED(1, 24, R.string.toma_stage_seed, R.drawable.ic_toma_seed),
        SPROUT(25, 49, R.string.toma_stage_sprout, R.drawable.ic_toma_sprout),
        PLANT(50, 99, R.string.toma_stage_grow, R.drawable.ic_toma_plant),
        FLOWER(100, 149, R.string.toma_stage_flower, R.drawable.ic_toma_flower),
        RIPE(150, Integer.MAX_VALUE, R.string.toma_stage_ripe, R.drawable.ic_toma_ripe);

        public final int minMinutes;
        public final int maxMinutes;
        @StringRes public final int nameRes;
        @DrawableRes public final int iconRes;

        Stage(int min, int max, @StringRes int nameRes, @DrawableRes int iconRes) {
            this.minMinutes = min;
            this.maxMinutes = max;
            this.nameRes = nameRes;
            this.iconRes = iconRes;
        }

        public static Stage fromMinutes(int minutes) {
            for (Stage stage : values()) {
                if (minutes >= stage.minMinutes && minutes <= stage.maxMinutes) {
                    return stage;
                }
            }
            return DIRT;
        }
    }

    private String dateStr; // "YYYY-MM-DD" or just day number if padding
    private int totalMinutes;
    private Stage stage;
    private boolean isPadding;

    public DailyTomato(String dateStr, int totalMinutes) {
        this.dateStr = dateStr;
        this.totalMinutes = totalMinutes;
        this.stage = Stage.fromMinutes(totalMinutes);
        this.isPadding = false;
    }

    public DailyTomato(String dateStr, int totalMinutes, boolean isPadding) {
        this.dateStr = dateStr;
        this.totalMinutes = totalMinutes;
        this.stage = Stage.fromMinutes(totalMinutes);
        this.isPadding = isPadding;
    }

    public boolean isPadding() {
        return isPadding;
    }

    public String getDateStr() {
        return dateStr;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Helper to format YYYY-MM-DD into a more readable format for UI.
     * e.g. "Oct 24" or "Today"
     */
    public String getFormattedDate() {
        if (isPadding) return "";
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdfIn.parse(dateStr);
            if (date != null) {
                SimpleDateFormat sdfOut = new SimpleDateFormat("d", Locale.getDefault());
                return sdfOut.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }
}
