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

        // Skip Focus -> Short Break (auto-start off by default -> PAUSED_BREAK)
        mTimer.skip();
        assertEquals(PomodoroTimer.State.PAUSED_BREAK, mTimer.getStateValue());
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

    @Test
    public void testResetFromRunningFocus_resetsToIdle() {
        mTimer.startFocus(10000);
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());

        mTimer.reset();
        assertEquals(PomodoroTimer.State.IDLE, mTimer.getStateValue());
        assertEquals(PomodoroTimer.Phase.FOCUS, mTimer.getPhaseValue());
        assertEquals(10000, mTimer.getRemainingMs());
        assertFalse(mTimer.isRunning());
    }

    @Test
    public void testResetFromPausedBreak_resetsToIdle() {
        mTimer.startFocus(10000);
        mTimer.skip();  // -> RUNNING_BREAK (Short Break)
        mTimer.pause(); // -> PAUSED_BREAK
        assertEquals(PomodoroTimer.State.PAUSED_BREAK, mTimer.getStateValue());

        mTimer.reset();
        assertEquals(PomodoroTimer.State.IDLE, mTimer.getStateValue());
        assertEquals(0, mTimer.getSessionCount());
    }

    @Test
    public void testResetClearsSessionCount() {
        mTimer.startFocus(10000);
        mTimer.skip(); // Focus -> Break, sessionCount = 1
        mTimer.skip(); // Break -> Focus
        mTimer.skip(); // Focus -> Break, sessionCount = 2
        assertEquals(2, mTimer.getSessionCount());

        mTimer.reset();
        assertEquals(0, mTimer.getSessionCount());
    }

    @Test
    public void testPauseWhenAlreadyPaused_isNoOp() {
        mTimer.startFocus(10000);
        mTimer.pause();
        assertEquals(PomodoroTimer.State.PAUSED_FOCUS, mTimer.getStateValue());

        // Calling pause again — should be a no-op, state remains the same
        mTimer.pause();
        assertEquals(PomodoroTimer.State.PAUSED_FOCUS, mTimer.getStateValue());
    }

    @Test
    public void testResumeWhenAlreadyRunning_isNoOp() {
        mTimer.startFocus(10000);
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());

        // Calling resume while already running — ignore
        mTimer.resume();
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());
    }

    @Test
    public void testStartFocusWhenAlreadyRunning_isNoOp() {
        mTimer.startFocus(10000);

        // Calling startFocus again — ignore, doesn't reset timer
        mTimer.startFocus(20000);
        assertEquals(PomodoroTimer.State.RUNNING_FOCUS, mTimer.getStateValue());
        // remainingMs is still 10000, not changed to 20000
        assertEquals(10000, mTimer.getRemainingMs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartFocusWithZeroDuration_throwsIllegalArgument() {
        mTimer.startFocus(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartFocusWithNegativeDuration_throwsIllegalArgument() {
        mTimer.startFocus(-5000);
    }

    @Test
    public void testOnStateChangedCalledOnStartFocus() {
        final int[] callCount = {0};

        mTimer.addTimerEventListener(new PomodoroTimer.OnTimerEventListener() {
            @Override public void onTick(PomodoroTimer.TimerState state) {}
            @Override public void onFocusComplete(int sessionCount) {}
            @Override public void onBreakComplete(int sessionCount) {}
            @Override public void onStateChanged(PomodoroTimer.TimerState state) {
                callCount[0]++;
            }
        });

        int countBefore = callCount[0];
        mTimer.startFocus(10000);
        assertTrue("onStateChanged should be called on startFocus",
                callCount[0] > countBefore);
    }

    @Test
    public void testListenerRemovedStopsReceivingCallbacks() {
        final int[] callCount = {0};

        PomodoroTimer.OnTimerEventListener listener = new PomodoroTimer.OnTimerEventListener() {
            @Override public void onTick(PomodoroTimer.TimerState state) {}
            @Override public void onFocusComplete(int sessionCount) {}
            @Override public void onBreakComplete(int sessionCount) {}
            @Override public void onStateChanged(PomodoroTimer.TimerState state) {
                callCount[0]++;
            }
        };

        mTimer.addTimerEventListener(listener);
        mTimer.startFocus(10000);
        int countAfterStart = callCount[0];

        mTimer.removeTimerEventListener(listener);
        mTimer.pause();  // Listener removed -> no callback

        assertEquals("Should not receive more callbacks after removal",
                countAfterStart, callCount[0]);
    }

    @Test
    public void testOnFocusCompleteNotCalledOnSkip() {
        // skip() calls transitionToBreak() — should NOT call onFocusComplete
        // onFocusComplete only fires when remainingMs == 0 (natural completion)
        final int[] focusCompleteCount = {0};

        mTimer.addTimerEventListener(new PomodoroTimer.OnTimerEventListener() {
            @Override public void onTick(PomodoroTimer.TimerState state) {}
            @Override public void onStateChanged(PomodoroTimer.TimerState state) {}
            @Override public void onBreakComplete(int sessionCount) {}
            @Override public void onFocusComplete(int sessionCount) {
                focusCompleteCount[0]++;
            }
        });

        mTimer.startFocus(10000);
        mTimer.skip(); // Skip — should NOT trigger onFocusComplete

        assertEquals("onFocusComplete should NOT be called on skip",
                0, focusCompleteCount[0]);
    }

    @Test
    public void testSessionCountReachesTarget_entersCompleted() {
        mTimer.startFocus(10000);

        // Target = 8 (AppConstants.TIMER_DEFAULT_TARGET_SESSIONS)
        // skip from Focus -> Break increases sessionCount
        // skip from Break -> Focus or COMPLETED
        for (int i = 0; i < 8; i++) {
            if (mTimer.getStateValue() == PomodoroTimer.State.COMPLETED) break;
            mTimer.skip(); // Focus -> Break (sessionCount++)
            if (mTimer.getStateValue() == PomodoroTimer.State.COMPLETED) break;
            mTimer.skip(); // Break -> Focus (or COMPLETED)
        }

        assertEquals(PomodoroTimer.State.COMPLETED, mTimer.getStateValue());
    }
}
