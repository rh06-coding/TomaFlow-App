package com.tomaflow.app.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PomodoroTimerTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private PomodoroTimer mTimer;

    @Before
    public void setUp() {
        mTimer = new PomodoroTimer();
        // Use short durations for testing
        mTimer.setDurations(10000, 5000, 15000);
        mTimer.setCyclesBeforeLongBreak(4);
    }

    @Test
    public void testInitialState() {
        assertEquals(PomodoroTimer.State.IDLE, mTimer.getStateValue());
        assertEquals(PomodoroTimer.Phase.FOCUS, mTimer.getPhaseValue());
        assertEquals(10000, mTimer.getRemainingMs());
        assertFalse(mTimer.isRunning());
    }

    @Test
    public void testStartFocus() {
        mTimer.startFocus(10000);
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());
        assertEquals(PomodoroTimer.Phase.FOCUS, mTimer.getPhaseValue());
        assertTrue(mTimer.isRunning());
    }

    @Test
    public void testPhaseTransitions() {
        mTimer.startFocus(10000);

        // Skip Focus -> Short Break
        mTimer.skip();
        assertEquals(PomodoroTimer.State.RUNNING_BREAK, mTimer.getStateValue());
        assertEquals(PomodoroTimer.Phase.SHORT_BREAK, mTimer.getPhaseValue());
        assertEquals(1, mTimer.getSessionCount());

        // Skip Short Break -> Focus
        mTimer.skip();
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());
        assertEquals(PomodoroTimer.Phase.FOCUS, mTimer.getPhaseValue());

        // Skip through to Long Break
        mTimer.skip(); // Break 2 (Short)
        mTimer.skip(); // Focus 3
        mTimer.skip(); // Break 3 (Short)
        mTimer.skip(); // Focus 4
        mTimer.skip(); // Break 4 (Long)

        assertEquals(PomodoroTimer.Phase.LONG_BREAK, mTimer.getPhaseValue());
        assertEquals(15000, mTimer.getRemainingMs());
        assertEquals(4, mTimer.getSessionCount());
    }

    @Test
    public void testPauseResume() {
        mTimer.startFocus(10000);
        mTimer.pause();
        assertEquals(PomodoroTimer.State.PAUSED_FOCUS, mTimer.getStateValue());
        assertFalse(mTimer.isRunning());

        mTimer.resume();
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());
        assertTrue(mTimer.isRunning());
    }
}
