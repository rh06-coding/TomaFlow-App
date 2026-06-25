package com.tomaflow.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContactsHelperTest {

    // ── normalizePhone ────────────────────────────────────────────────────────

    @Test
    public void normalizePhone_stripsSpacesAndDashes() {
        assertEquals("+84901234567", ContactsHelper.normalizePhone("+84 90-123-4567"));
    }

    @Test
    public void normalizePhone_stripsParentheses() {
        assertEquals("84901234567", ContactsHelper.normalizePhone("(84) 901234567"));
    }

    @Test
    public void normalizePhone_keepsPlusAndDigitsOnly() {
        assertEquals("+1234567890", ContactsHelper.normalizePhone("+1 (234) 567-890"));
    }

    @Test
    public void normalizePhone_removesLetters() {
        assertEquals("123", ContactsHelper.normalizePhone("1a2b3c"));
    }

    @Test
    public void normalizePhone_emptyString_returnsEmpty() {
        assertEquals("", ContactsHelper.normalizePhone(""));
    }

    @Test
    public void normalizePhone_onlyLetters_returnsEmpty() {
        assertEquals("", ContactsHelper.normalizePhone("abcdef"));
    }

    @Test
    public void normalizePhone_null_returnsEmpty() {
        assertEquals("", ContactsHelper.normalizePhone(null));
    }

    @Test
    public void normalizePhone_pureDigits_unchanged() {
        assertEquals("0901234567", ContactsHelper.normalizePhone("0901234567"));
    }

    @Test
    public void normalizePhone_multiplePlus_keepsAll() {
        // spec keeps every '+' and digit; multiple '+' unusual but allowed by the regex
        assertEquals("++123", ContactsHelper.normalizePhone("++1-2-3"));
        assertTrue(ContactsHelper.normalizePhone("+84 90 123 45 67").startsWith("+84"));
    }

    @Test
    public void normalizePhone_whitespaceOnly_returnsEmpty() {
        assertEquals("", ContactsHelper.normalizePhone("   "));
    }

    @Test
    public void normalizePhone_tabsAndNewlines_stripped() {
        assertEquals("+849012", ContactsHelper.normalizePhone("+84\n90\t12"));
    }

    @Test
    public void normalizePhone_plusWithSpaces_preservesPlus() {
        assertEquals("+123", ContactsHelper.normalizePhone("+ 1 2 3"));
    }
}
