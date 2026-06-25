package com.tomaflow.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConnectionIdsTest {

    @Test
    public void idFor_smallerUidFirst_ordersLexicographically() {
        assertEquals("alice_bob", ConnectionIds.idFor("alice", "bob"));
    }

    @Test
    public void idFor_reversedArgs_produceSameId() {
        // A friend request from A->B and B->A must map to the same connection record.
        assertEquals(ConnectionIds.idFor("alice", "bob"),
                     ConnectionIds.idFor("bob", "alice"));
    }

    @Test
    public void idFor_equalUids_joinsWithUnderscore() {
        assertEquals("u_u", ConnectionIds.idFor("u", "u"));
    }

    @Test
    public void idFor_distinctPairs_produceDistinctIds() {
        assertNotEquals(ConnectionIds.idFor("a", "b"), ConnectionIds.idFor("a", "c"));
    }

    @Test
    public void idFor_matchesChatIdSemantics() {
        // Both use the same ordering rule, so equal uids give equal ids.
        assertEquals(ChatIds.chatIdFor("a", "b"), ConnectionIds.idFor("a", "b"));
    }

    @Test
    public void idFor_nullFirst_throws() {
        assertThrows(IllegalArgumentException.class, () -> ConnectionIds.idFor(null, "b"));
    }

    @Test
    public void idFor_nullSecond_throws() {
        assertThrows(IllegalArgumentException.class, () -> ConnectionIds.idFor("a", null));
    }

    @Test
    public void idFor_containsSeparator() {
        assertTrue(ConnectionIds.idFor("x", "y").contains("_"));
    }
}
