package com.tomaflow.app.data.db.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoteEntityTest {

    @Test
    public void defaultConstructor_generatesIdAndTimestamps() {
        NoteEntity n = new NoteEntity();
        assertNotNull(n.noteId);
        assertFalse(n.noteId.isEmpty());
        assertTrue("createdAt should be set", n.createdAt > 0);
        assertEquals(n.createdAt, n.updatedAt);
    }

    @Test
    public void defaultConstructor_generatesUniqueIds() {
        NoteEntity a = new NoteEntity();
        NoteEntity b = new NoteEntity();
        assertFalse("UUIDs should be unique", a.noteId.equals(b.noteId));
    }

    @Test
    public void fields_canBeSetAndRead() {
        NoteEntity n = new NoteEntity();
        n.title = "Today";
        n.content = "Finished 4 pomodoros";
        n.mood = "happy";
        n.userId = "uid1";
        n.updatedAt = n.createdAt + 5000L;

        assertEquals("Today", n.title);
        assertEquals("Finished 4 pomodoros", n.content);
        assertEquals("happy", n.mood);
        assertEquals("uid1", n.userId);
        assertTrue(n.updatedAt > n.createdAt);
    }

    @Test
    public void defaultConstructor_noteId_isUuidFormat() {
        NoteEntity n = new NoteEntity();
        // UUID toString format: 8-4-4-4-12 hex digits
        assertTrue(n.noteId.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"));
    }
}
