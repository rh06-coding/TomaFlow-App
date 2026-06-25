package com.tomaflow.app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class UserProfileTest {

    @Test
    public void fullConstructor_setsAllFieldsExceptVip() {
        UserProfile p = new UserProfile("uid1", "u@e.com", "+8490", "alice", "Alice", "2000-01-01", "url");
        assertEquals("uid1", p.uid);
        assertEquals("u@e.com", p.email);
        assertEquals("+8490", p.phone);
        assertEquals("alice", p.username);
        assertEquals("Alice", p.name);
        assertEquals("2000-01-01", p.dob);
        assertEquals("url", p.avatarUrl);
        assertFalse("new user is not VIP by default", p.isVip);
    }

    @Test
    public void defaultConstructor_leavesFieldsNull() {
        UserProfile p = new UserProfile();
        org.junit.Assert.assertNull(p.uid);
        org.junit.Assert.assertNull(p.email);
        org.junit.Assert.assertNull(p.username);
        assertFalse(p.isVip);
    }

    @Test
    public void vipFlag_canBeSet() {
        UserProfile p = new UserProfile("uid2", null, null, "bob", null, null, null);
        p.isVip = true;
        assertEquals(true, p.isVip);
    }

    @Test
    public void uid_canBeAssignedAfterConstruction() {
        UserProfile p = new UserProfile();
        p.uid = "assigned-uid";
        assertEquals("assigned-uid", p.uid);
    }
}
