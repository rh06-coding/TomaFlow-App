package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BuiltInTrackTest {

    @Test
    public void constructor_setsAllFields() {
        BuiltInTrack t = new BuiltInTrack("lofi_chill", "Lo-Fi Chill", "Lofi", "L", 1234);
        assertEquals("lofi_chill", t.id);
        assertEquals("Lo-Fi Chill", t.name);
        assertEquals("Lofi", t.category);
        assertEquals("L", t.emoji);
        assertEquals(1234, t.rawResId);
    }

    @Test
    public void rawResId_zeroAllowedForDeviceTrackWrapper() {
        BuiltInTrack t = new BuiltInTrack("local", "Local", "Custom", "", 0);
        assertEquals(0, t.rawResId);
    }
}
