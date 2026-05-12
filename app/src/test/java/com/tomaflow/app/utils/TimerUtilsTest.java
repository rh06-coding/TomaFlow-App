package com.tomaflow.app.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tomaflow.app.timer.PomodoroTimer.Phase;

import org.junit.Test;

public class TimerUtilsTest {

    // ── formatMillisToMmSs ────────────────────────────────────────────────────

    @Test
    public void formatMillisToMmSs_exactMinutes() {
        assertEquals("25:00", TimerUtils.formatMillisToMmSs(1500000));
    }

    @Test
    public void formatMillisToMmSs_minutesAndSeconds() {
        assertEquals("01:05", TimerUtils.formatMillisToMmSs(65000));
    }

    @Test
    public void formatMillisToMmSs_zero() {
        assertEquals("00:00", TimerUtils.formatMillisToMmSs(0));
    }

    @Test
    public void formatMillisToMmSs_negativeClampsToZero() {
        assertEquals("00:00", TimerUtils.formatMillisToMmSs(-5000));
    }

    @Test
    public void formatMillisToMmSs_oneSecond() {
        assertEquals("00:01", TimerUtils.formatMillisToMmSs(1000));
    }

    @Test
    public void formatMillisToMmSs_subSecondTruncates() {
        assertEquals("00:00", TimerUtils.formatMillisToMmSs(999));
    }

    // ── formatMillisToHhMmSs ──────────────────────────────────────────────────

    @Test
    public void formatMillisToHhMmSs_oneHourOneMinuteOneSecond() {
        assertEquals("01:01:01", TimerUtils.formatMillisToHhMmSs(3661000));
    }

    @Test
    public void formatMillisToHhMmSs_zero() {
        assertEquals("00:00:00", TimerUtils.formatMillisToHhMmSs(0));
    }

    @Test
    public void formatMillisToHhMmSs_underOneHour() {
        assertEquals("00:25:00", TimerUtils.formatMillisToHhMmSs(1500000));
    }

    @Test
    public void formatMillisToHhMmSs_negativeClampsToZero() {
        assertEquals("00:00:00", TimerUtils.formatMillisToHhMmSs(-1));
    }

    // ── getPhaseLabel ─────────────────────────────────────────────────────────

    @Test
    public void getPhaseLabel_focus() {
        assertEquals("Focus", TimerUtils.getPhaseLabel(Phase.FOCUS));
    }

    @Test
    public void getPhaseLabel_shortBreak() {
        assertEquals("Short Break", TimerUtils.getPhaseLabel(Phase.SHORT_BREAK));
    }

    @Test
    public void getPhaseLabel_longBreak() {
        assertEquals("Long Break", TimerUtils.getPhaseLabel(Phase.LONG_BREAK));
    }

    @Test
    public void getPhaseLabel_null_returnsUnknown() {
        assertEquals("Unknown", TimerUtils.getPhaseLabel(null));
    }

    // ── calculateProgressPercent ──────────────────────────────────────────────

    @Test
    public void calculateProgressPercent_halfway() {
        assertEquals(50, TimerUtils.calculateProgressPercent(5000, 10000));
    }

    @Test
    public void calculateProgressPercent_complete() {
        assertEquals(100, TimerUtils.calculateProgressPercent(10000, 10000));
    }

    @Test
    public void calculateProgressPercent_notStarted() {
        assertEquals(0, TimerUtils.calculateProgressPercent(0, 10000));
    }

    @Test
    public void calculateProgressPercent_overMax_clampsTo100() {
        assertEquals(100, TimerUtils.calculateProgressPercent(15000, 10000));
    }

    @Test
    public void calculateProgressPercent_zeroTotal_returnsZero() {
        assertEquals(0, TimerUtils.calculateProgressPercent(5000, 0));
    }

    @Test
    public void calculateProgressPercent_negativeTotal_returnsZero() {
        assertEquals(0, TimerUtils.calculateProgressPercent(5000, -1));
    }

    // ── calculateProgressFloat ────────────────────────────────────────────────

    @Test
    public void calculateProgressFloat_halfway() {
        assertEquals(0.5f, TimerUtils.calculateProgressFloat(5000, 10000), 0.001f);
    }

    @Test
    public void calculateProgressFloat_complete() {
        assertEquals(1.0f, TimerUtils.calculateProgressFloat(10000, 10000), 0.001f);
    }

    @Test
    public void calculateProgressFloat_notStarted() {
        assertEquals(0.0f, TimerUtils.calculateProgressFloat(0, 10000), 0.001f);
    }

    @Test
    public void calculateProgressFloat_overMax_clampsTo1() {
        assertEquals(1.0f, TimerUtils.calculateProgressFloat(15000, 10000), 0.001f);
    }

    @Test
    public void calculateProgressFloat_zeroTotal_returnsZero() {
        assertEquals(0.0f, TimerUtils.calculateProgressFloat(5000, 0), 0.001f);
    }

    // ── isBreakPhase / isWorkPhase ────────────────────────────────────────────

    @Test
    public void isBreakPhase_shortBreak() {
        assertTrue(TimerUtils.isBreakPhase(Phase.SHORT_BREAK));
    }

    @Test
    public void isBreakPhase_longBreak() {
        assertTrue(TimerUtils.isBreakPhase(Phase.LONG_BREAK));
    }

    @Test
    public void isBreakPhase_focus_isFalse() {
        assertFalse(TimerUtils.isBreakPhase(Phase.FOCUS));
    }

    @Test
    public void isWorkPhase_focus() {
        assertTrue(TimerUtils.isWorkPhase(Phase.FOCUS));
    }

    @Test
    public void isWorkPhase_shortBreak_isFalse() {
        assertFalse(TimerUtils.isWorkPhase(Phase.SHORT_BREAK));
    }

    @Test
    public void isWorkPhase_longBreak_isFalse() {
        assertFalse(TimerUtils.isWorkPhase(Phase.LONG_BREAK));
    }

    // ── Unit conversions ──────────────────────────────────────────────────────

    @Test
    public void secondsToMillis() {
        assertEquals(5000L, TimerUtils.secondsToMillis(5));
    }

    @Test
    public void secondsToMillis_zero() {
        assertEquals(0L, TimerUtils.secondsToMillis(0));
    }

    @Test
    public void minutesToMillis() {
        assertEquals(1500000L, TimerUtils.minutesToMillis(25));
    }

    @Test
    public void minutesToMillis_zero() {
        assertEquals(0L, TimerUtils.minutesToMillis(0));
    }

    @Test
    public void minutesToSeconds() {
        assertEquals(1500L, TimerUtils.minutesToSeconds(25));
    }

    @Test
    public void millisToMinutes() {
        assertEquals(25, TimerUtils.millisToMinutes(1500000));
    }

    @Test
    public void millisToMinutes_truncates() {
        assertEquals(1, TimerUtils.millisToMinutes(90000)); // 1.5 min -> 1
    }

    @Test
    public void millisToSeconds() {
        assertEquals(65, TimerUtils.millisToSeconds(65000));
    }

    @Test
    public void millisToSeconds_truncates() {
        assertEquals(0, TimerUtils.millisToSeconds(999));
    }

    // ── getMinutesAndSeconds ──────────────────────────────────────────────────

    @Test
    public void getMinutesAndSeconds_exactMinutes() {
        assertArrayEquals(new int[]{25, 0}, TimerUtils.getMinutesAndSeconds(1500000));
    }

    @Test
    public void getMinutesAndSeconds_minutesAndSeconds() {
        assertArrayEquals(new int[]{1, 5}, TimerUtils.getMinutesAndSeconds(65000));
    }

    @Test
    public void getMinutesAndSeconds_zero() {
        assertArrayEquals(new int[]{0, 0}, TimerUtils.getMinutesAndSeconds(0));
    }

    @Test
    public void getMinutesAndSeconds_underOneMinute() {
        assertArrayEquals(new int[]{0, 30}, TimerUtils.getMinutesAndSeconds(30000));
    }
}
