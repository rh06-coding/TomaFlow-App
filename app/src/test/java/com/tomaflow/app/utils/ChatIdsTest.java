package com.tomaflow.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChatIdsTest {

    @Test
    public void chatIdFor_smallerUidFirst_ordersLexicographically() {
        assertEquals("aaa_bbb", ChatIds.chatIdFor("aaa", "bbb"));
    }

    @Test
    public void chatIdFor_reversedArgs_producesSameId() {
        String ab = ChatIds.chatIdFor("aaa", "bbb");
        String ba = ChatIds.chatIdFor("bbb", "aaa");
        assertEquals("Chat id must be order-independent", ab, ba);
    }

    @Test
    public void chatIdFor_equalUids_joinsWithUnderscore() {
        assertEquals("x_x", ChatIds.chatIdFor("x", "x"));
    }

    @Test
    public void chatIdFor_distinctPairs_produceDistinctIds() {
        assertNotEquals(ChatIds.chatIdFor("a", "b"), ChatIds.chatIdFor("a", "c"));
        assertNotEquals(ChatIds.chatIdFor("a", "b"), ChatIds.chatIdFor("c", "d"));
    }

    @Test
    public void chatIdFor_containsSeparator() {
        String id = ChatIds.chatIdFor("uid1", "uid2");
        assertTrue(id.contains("_"));
    }

    @Test
    public void chatIdFor_nullFirst_throws() {
        assertThrows(IllegalArgumentException.class, () -> ChatIds.chatIdFor(null, "b"));
    }

    @Test
    public void chatIdFor_nullSecond_throws() {
        assertThrows(IllegalArgumentException.class, () -> ChatIds.chatIdFor("a", null));
    }

    @Test
    public void chatIdFor_bothNull_throws() {
        assertThrows(IllegalArgumentException.class, () -> ChatIds.chatIdFor(null, null));
    }

    @Test
    public void chatIdFor_caseSensitiveComparison() {
        // "Apple" < "apple" by ASCII; ensure deterministic ordering still applied
        String id = ChatIds.chatIdFor("apple", "Apple");
        assertEquals("Apple_apple", id);
    }

    @Test
    public void chatIdFor_numericUids_orderedCorrectly() {
        // String comparison, not numeric: "100" < "99" lexicographically
        assertEquals("100_99", ChatIds.chatIdFor("99", "100"));
    }
}
