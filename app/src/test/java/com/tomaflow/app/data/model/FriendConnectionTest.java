package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FriendConnectionTest {

    @Test
    public void fullConstructor_setsFieldsAndTimestamp() {
        FriendConnection c = new FriendConnection("a_b", "a", "b", "PENDING");
        assertEquals("a_b", c.id);
        assertEquals("a", c.senderId);
        assertEquals("b", c.receiverId);
        assertEquals("PENDING", c.status);
        assertTrue("timestamp should be set", c.timestamp > 0);
    }

    @Test
    public void defaultConstructor_leavesFieldsDefault() {
        FriendConnection c = new FriendConnection();
        org.junit.Assert.assertNull(c.id);
        org.junit.Assert.assertNull(c.status);
        assertEquals(0L, c.timestamp);
    }

    @Test
    public void acceptedStatus_setCorrectly() {
        FriendConnection c = new FriendConnection("a_b", "a", "b", "ACCEPTED");
        assertEquals("ACCEPTED", c.status);
    }

    @Test
    public void status_canTransition() {
        FriendConnection c = new FriendConnection("a_b", "a", "b", "PENDING");
        c.status = "ACCEPTED";
        assertEquals("ACCEPTED", c.status);
    }
}
