package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChatMessageTest {

    @Test
    public void fullConstructor_setsAllFields() {
        ChatMessage m = new ChatMessage("id1", "u1", "u2", "hello", "text", 1000L, false);
        assertEquals("id1", m.id);
        assertEquals("u1", m.senderId);
        assertEquals("u2", m.receiverId);
        assertEquals("hello", m.content);
        assertEquals("text", m.type);
        assertEquals(1000L, m.timestamp);
        assertFalse(m.isRead);
    }

    @Test
    public void defaultConstructor_leavesFieldsDefault() {
        ChatMessage m = new ChatMessage();
        assertNull(m.id);
        assertEquals(0L, m.timestamp);
        assertFalse(m.isRead);
    }

    @Test
    public void achievementType_setCorrectly() {
        ChatMessage m = new ChatMessage("id2", "u1", "u2", "Completed 4 pomodoros!", "achievement", 2000L, true);
        assertEquals("achievement", m.type);
        assertTrue(m.isRead);
    }

    @Test
    public void readFlag_canBeToggled() {
        ChatMessage m = new ChatMessage("id3", "u1", "u2", "hi", "text", 3000L, false);
        m.isRead = true;
        assertTrue(m.isRead);
    }
}
