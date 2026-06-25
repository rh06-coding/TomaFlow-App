package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tomaflow.app.data.model.DailyTomato.Stage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class DailyTomatoTest {

    // ── Stage.fromMinutes ─────────────────────────────────────────────────────

    @Test
    public void fromMinutes_zero_returnsDirt() {
        assertEquals(Stage.DIRT, Stage.fromMinutes(0));
    }

    @Test
    public void fromMinutes_seedRange() {
        assertEquals(Stage.SEED, Stage.fromMinutes(1));
        assertEquals(Stage.SEED, Stage.fromMinutes(24));
    }

    @Test
    public void fromMinutes_sproutRange() {
        assertEquals(Stage.SPROUT, Stage.fromMinutes(25));
        assertEquals(Stage.SPROUT, Stage.fromMinutes(49));
    }

    @Test
    public void fromMinutes_plantRange() {
        assertEquals(Stage.PLANT, Stage.fromMinutes(50));
        assertEquals(Stage.PLANT, Stage.fromMinutes(74));
    }

    @Test
    public void fromMinutes_flowerRange() {
        assertEquals(Stage.FLOWER, Stage.fromMinutes(75));
        assertEquals(Stage.FLOWER, Stage.fromMinutes(99));
    }

    @Test
    public void fromMinutes_ripeRange() {
        assertEquals(Stage.RIPE, Stage.fromMinutes(100));
        assertEquals(Stage.RIPE, Stage.fromMinutes(500));
        assertEquals(Stage.RIPE, Stage.fromMinutes(Integer.MAX_VALUE));
    }

    @Test
    public void fromMinutes_negative_fallsBackToDirt() {
        assertEquals(Stage.DIRT, Stage.fromMinutes(-1));
        assertEquals(Stage.DIRT, Stage.fromMinutes(-100));
    }

    // ── Stage boundaries ──────────────────────────────────────────────────────

    @Test
    public void stageBoundaries_areContiguous() {
        // No gap between stages: max of one + 1 == min of next
        assertEquals(Stage.SEED.minMinutes, Stage.DIRT.maxMinutes + 1);
        assertEquals(Stage.SPROUT.minMinutes, Stage.SEED.maxMinutes + 1);
        assertEquals(Stage.PLANT.minMinutes, Stage.SPROUT.maxMinutes + 1);
        assertEquals(Stage.FLOWER.minMinutes, Stage.PLANT.maxMinutes + 1);
        assertEquals(Stage.RIPE.minMinutes, Stage.FLOWER.maxMinutes + 1);
    }

    // ── Constructor + getters ─────────────────────────────────────────────────

    @Test
    public void constructor_computesStageFromMinutes() {
        DailyTomato t = new DailyTomato("2026-06-25", 30);
        assertEquals("2026-06-25", t.getDateStr());
        assertEquals(30, t.getTotalMinutes());
        assertEquals(Stage.SPROUT, t.getStage());
        assertFalse(t.isPadding());
    }

    @Test
    public void constructor_paddingFlag_preserved() {
        DailyTomato t = new DailyTomato("2026-06-01", 0, true);
        assertTrue(t.isPadding());
        assertEquals(Stage.DIRT, t.getStage());
    }

    @Test
    public void constructor_nonPadding_isPaddingFalse() {
        DailyTomato t = new DailyTomato("2026-06-01", 0, false);
        assertFalse(t.isPadding());
    }

    // ── getFormattedDate ──────────────────────────────────────────────────────

    @Test
    public void getFormattedDate_padding_returnsEmpty() {
        DailyTomato t = new DailyTomato("2026-06-01", 0, true);
        assertEquals("", t.getFormattedDate());
    }

    @Test
    public void getFormattedDate_validDate_returnsDayOfMonth() {
        DailyTomato t = new DailyTomato("2026-06-25", 60);
        assertEquals("25", t.getFormattedDate());
    }

    @Test
    public void getFormattedDate_invalidDate_returnsRawString() {
        DailyTomato t = new DailyTomato("not-a-date", 60);
        assertEquals("not-a-date", t.getFormattedDate());
    }

    @Test
    public void getFormattedDate_singleDigitDay() {
        DailyTomato t = new DailyTomato("2026-06-03", 10);
        assertEquals("3", t.getFormattedDate());
    }
}
