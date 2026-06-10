package com.tomaflow.app.ui.stats;

import static org.junit.Assert.assertEquals;

import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsAggregatorTest {

    private static final float DELTA = 0.001f;

    private SessionDao.DailyStatRow row(String dayNum, int minutes, int cycles) {
        SessionDao.DailyStatRow r = new SessionDao.DailyStatRow();
        r.dayNum = dayNum;
        r.minutes = minutes;
        r.cycles = cycles;
        return r;
    }

    private SessionEntity session(String status) {
        SessionEntity s = new SessionEntity();
        s.status = status;
        return s;
    }

    // ── minutesByDay ──────────────────────────────────────────────────────────

    @Test
    public void minutesByDay_mapsRowsToWeekdayIndex() {
        List<SessionDao.DailyStatRow> rows = Arrays.asList(
                row("0", 30, 1),   // Sunday
                row("3", 50, 2),   // Wednesday
                row("6", 10, 1));  // Saturday

        float[] result = StatsAggregator.minutesByDay(rows);

        assertEquals(7, result.length);
        assertEquals(30f, result[0], DELTA);
        assertEquals(50f, result[3], DELTA);
        assertEquals(10f, result[6], DELTA);
        assertEquals(0f, result[1], DELTA);
    }

    @Test
    public void minutesByDay_emptyAndNull_returnAllZeros() {
        float[] fromEmpty = StatsAggregator.minutesByDay(new ArrayList<>());
        float[] fromNull = StatsAggregator.minutesByDay(null);

        for (int i = 0; i < StatsAggregator.DAYS_IN_WEEK; i++) {
            assertEquals(0f, fromEmpty[i], DELTA);
            assertEquals(0f, fromNull[i], DELTA);
        }
    }

    @Test
    public void minutesByDay_ignoresUnparseableOrOutOfRangeDay() {
        List<SessionDao.DailyStatRow> rows = Arrays.asList(
                row(null, 99, 1),
                row("x", 99, 1),
                row("9", 99, 1),
                row("2", 40, 1));

        float[] result = StatsAggregator.minutesByDay(rows);

        assertEquals(40f, result[2], DELTA);
        // Bad rows contributed nothing
        float sum = 0;
        for (float v : result) sum += v;
        assertEquals(40f, sum, DELTA);
    }

    // ── totalMinutes / totalCycles ────────────────────────────────────────────

    @Test
    public void totalMinutes_sumsAllRows() {
        List<SessionDao.DailyStatRow> rows = Arrays.asList(
                row("1", 25, 1), row("2", 50, 2), row("3", 25, 1));
        assertEquals(100, StatsAggregator.totalMinutes(rows));
    }

    @Test
    public void totalCycles_sumsAllRows() {
        List<SessionDao.DailyStatRow> rows = Arrays.asList(
                row("1", 25, 1), row("2", 50, 2), row("3", 25, 1));
        assertEquals(4, StatsAggregator.totalCycles(rows));
    }

    @Test
    public void totals_emptyData_returnZero() {
        assertEquals(0, StatsAggregator.totalMinutes(new ArrayList<>()));
        assertEquals(0, StatsAggregator.totalCycles(null));
    }

    // ── bestDayIndex ──────────────────────────────────────────────────────────

    @Test
    public void bestDayIndex_returnsDayWithMostMinutes() {
        float[] minutes = {10f, 0f, 75f, 30f, 0f, 0f, 5f};
        assertEquals(2, StatsAggregator.bestDayIndex(minutes));
    }

    @Test
    public void bestDayIndex_allZero_returnsMinusOne() {
        assertEquals(-1, StatsAggregator.bestDayIndex(new float[7]));
        assertEquals(-1, StatsAggregator.bestDayIndex(null));
    }

    @Test
    public void bestDayIndex_firstMaxWinsOnTie() {
        float[] minutes = {0f, 40f, 40f, 0f, 0f, 0f, 0f};
        assertEquals(1, StatsAggregator.bestDayIndex(minutes));
    }

    // ── countByStatus ─────────────────────────────────────────────────────────

    @Test
    public void countByStatus_countsMatchingStatusOnly() {
        List<SessionEntity> sessions = Arrays.asList(
                session("Completed"),
                session("Completed"),
                session("Failed"),
                session(null));

        assertEquals(2, StatsAggregator.countByStatus(sessions, "Completed"));
        assertEquals(1, StatsAggregator.countByStatus(sessions, "Failed"));
    }

    @Test
    public void countByStatus_failedDoesNotInflateCompleted() {
        List<SessionEntity> sessions = Arrays.asList(
                session("Failed"), session("Failed"), session("Failed"));
        assertEquals(0, StatsAggregator.countByStatus(sessions, "Completed"));
    }

    @Test
    public void countByStatus_emptyOrNull_returnZero() {
        assertEquals(0, StatsAggregator.countByStatus(new ArrayList<>(), "Completed"));
        assertEquals(0, StatsAggregator.countByStatus(null, "Completed"));
    }
}
