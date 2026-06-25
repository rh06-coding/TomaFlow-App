package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalTrackTest {

    @Test
    public void constructor_setsAllFields() {
        LocalTrack t = new LocalTrack("t1", "Rain Sounds", "/data/app/files/rain.mp3");
        assertEquals("t1", t.id);
        assertEquals("Rain Sounds", t.title);
        assertEquals("/data/app/files/rain.mp3", t.path);
    }

    @Test
    public void id_isFinalAndAccessible() {
        LocalTrack t = new LocalTrack("abc", "Title", "p");
        assertEquals("abc", t.id);
    }

    @Test
    public void emptyPath_allowed() {
        LocalTrack t = new LocalTrack("x", "Empty", "");
        assertEquals("", t.path);
    }
}
