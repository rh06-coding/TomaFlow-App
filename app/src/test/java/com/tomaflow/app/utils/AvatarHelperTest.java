package com.tomaflow.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Base64;

public class AvatarHelperTest {

    private static final String SAMPLE_DATA_URI =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU3+QAAAABJRU5ErkJggg==";

    // ── isBase64Avatar ────────────────────────────────────────────────────────

    @Test
    public void isBase64Avatar_dataImagePrefix_true() {
        assertTrue(AvatarHelper.isBase64Avatar(SAMPLE_DATA_URI));
    }

    @Test
    public void isBase64Avatar_httpUrl_false() {
        assertFalse(AvatarHelper.isBase64Avatar("https://example.com/avatar.png"));
    }

    @Test
    public void isBase64Avatar_emptyString_false() {
        assertFalse(AvatarHelper.isBase64Avatar(""));
    }

    @Test
    public void isBase64Avatar_null_false() {
        assertFalse(AvatarHelper.isBase64Avatar(null));
    }

    // ── extractBase64 ─────────────────────────────────────────────────────────

    @Test
    public void extractBase64_returnsPayloadAfterComma() {
        String expected = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU3+QAAAABJRU5ErkJggg==";
        assertEquals(expected, AvatarHelper.extractBase64(SAMPLE_DATA_URI));
    }

    @Test
    public void extractBase64_noComma_returnsNull() {
        assertNull(AvatarHelper.extractBase64("data:image/png;base64"));
    }

    @Test
    public void extractBase64_emptyPayloadAfterComma_returnsNull() {
        assertNull(AvatarHelper.extractBase64("data:image/png;base64,"));
    }

    @Test
    public void extractBase64_null_returnsNull() {
        assertNull(AvatarHelper.extractBase64(null));
    }

    @Test
    public void extractBase64_plainUrlWithoutComma_returnsNull() {
        assertNull(AvatarHelper.extractBase64("https://example.com/avatar.png"));
    }

    @Test
    public void extractBase64_dataUriWithJpeg_returnsPayload() {
        assertEquals("abc123", AvatarHelper.extractBase64("data:image/jpeg;base64,abc123"));
    }

    // ── Round-trip: encode → data-URI → extractBase64 → decode ────────────────

    @Test
    public void extractBase64_decodesBackToOriginalBytes() {
        byte[] original = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        String encoded = Base64.getEncoder().encodeToString(original);
        String dataUri = "data:image/png;base64," + encoded;

        String payload = AvatarHelper.extractBase64(dataUri);
        byte[] decoded = Base64.getDecoder().decode(payload);

        assertEquals(original.length, decoded.length);
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], decoded[i]);
        }
    }

    // ── isBase64Avatar phân biệt hoa thường ─────────────────────────────────────

    @Test
    public void isBase64Avatar_caseSensitivePrefix_falseForUppercaseData() {
        assertFalse(AvatarHelper.isBase64Avatar("DATA:image/png;base64,abc"));
    }
}
