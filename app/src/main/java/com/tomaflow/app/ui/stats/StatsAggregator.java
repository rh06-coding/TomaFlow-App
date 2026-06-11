package com.tomaflow.app.ui.stats;

import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

import java.util.List;

/**
 * Pure aggregation helpers that turn raw session data into chart-ready values.
 * Kept free of Android dependencies so it can be unit-tested directly.
 */
public final class StatsAggregator {

    public static final int DAYS_IN_WEEK = 7;

    /** Day-of-week labels, indexed 0=Sunday..6=Saturday to match SQLite strftime('%w'). */
    public static final String[] DAY_LABELS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private StatsAggregator() {}

    /**
     * Focus minutes per weekday as a 7-element array. Index matches strftime('%w')
     * (0=Sunday). Rows with an out-of-range or unparseable day are ignored.
     */
    public static float[] minutesByDay(List<SessionDao.DailyStatRow> rows) {
        float[] result = new float[DAYS_IN_WEEK];
        if (rows == null) {
            return result;
        }
        for (SessionDao.DailyStatRow row : rows) {
            int day = parseDay(row.dayNum);
            if (day >= 0 && day < DAYS_IN_WEEK) {
                result[day] += row.minutes;
            }
        }
        return result;
    }

    /**
     * Break minutes per weekday (estimated: cycles × shortBreakMinutes).
     */
    public static float[] breakMinutesByDay(List<SessionDao.DailyStatRow> rows, int shortBreakMinutes) {
        float[] result = new float[DAYS_IN_WEEK];
        if (rows == null) {
            return result;
        }
        for (SessionDao.DailyStatRow row : rows) {
            int day = parseDay(row.dayNum);
            if (day >= 0 && day < DAYS_IN_WEEK) {
                result[day] += row.cycles * shortBreakMinutes;
            }
        }
        return result;
    }


    public static int totalMinutes(List<SessionDao.DailyStatRow> rows) {
        if (rows == null) {
            return 0;
        }
        int total = 0;
        for (SessionDao.DailyStatRow row : rows) {
            total += row.minutes;
        }
        return total;
    }

    public static int totalCycles(List<SessionDao.DailyStatRow> rows) {
        if (rows == null) {
            return 0;
        }
        int total = 0;
        for (SessionDao.DailyStatRow row : rows) {
            total += row.cycles;
        }
        return total;
    }

    /** Index of the weekday with the most focus minutes, or -1 if there is no focus time. */
    public static int bestDayIndex(float[] minutesByDay) {
        if (minutesByDay == null) {
            return -1;
        }
        int best = -1;
        float max = 0f;
        for (int i = 0; i < minutesByDay.length; i++) {
            if (minutesByDay[i] > max) {
                max = minutesByDay[i];
                best = i;
            }
        }
        return best;
    }

    /** Count sessions matching the given status (case-sensitive). Null status never matches. */
    public static int countByStatus(List<SessionEntity> sessions, String status) {
        if (sessions == null || status == null) {
            return 0;
        }
        int count = 0;
        for (SessionEntity session : sessions) {
            if (status.equals(session.status)) {
                count++;
            }
        }
        return count;
    }

    private static int parseDay(String dayNum) {
        if (dayNum == null) {
            return -1;
        }
        try {
            return Integer.parseInt(dayNum.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
