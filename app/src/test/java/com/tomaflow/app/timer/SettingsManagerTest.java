package com.tomaflow.app.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.constants.AppConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests cho {@link SettingsManager} — logic thuần SharedPreferences, không đụng Firebase.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class SettingsManagerTest {

    private SettingsManager manager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Xoá mọi preference còn sót từ test trước để mỗi test chạy độc lập.
        context.getSharedPreferences(AppConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
        manager = new SettingsManager(context);
    }

    // ── Giá trị mặc định khi chưa thiết lập ────────────────────────────────────

    @Test
    public void getFocusDurationMs_default_is25Minutes() {
        assertEquals(AppConstants.TIMER_WORK_DURATION_MS, manager.getFocusDurationMs());
    }

    @Test
    public void getShortBreakDurationMs_default_is5Minutes() {
        assertEquals(AppConstants.TIMER_SHORT_BREAK_MS, manager.getShortBreakDurationMs());
    }

    @Test
    public void getLongBreakDurationMs_default_is15Minutes() {
        assertEquals(AppConstants.TIMER_LONG_BREAK_MS, manager.getLongBreakDurationMs());
    }

    // ── Round-trip thời lượng ──────────────────────────────────────────────────

    @Test
    public void setFocusDurationMinutes_roundTrips() {
        manager.setFocusDurationMinutes(30);
        assertEquals(30 * 60_000L, manager.getFocusDurationMs());
    }

    @Test
    public void setShortBreakDurationMinutes_roundTrips() {
        manager.setShortBreakDurationMinutes(10);
        assertEquals(10 * 60_000L, manager.getShortBreakDurationMs());
    }

    @Test
    public void setLongBreakDurationMinutes_roundTrips() {
        manager.setLongBreakDurationMinutes(20);
        assertEquals(20 * 60_000L, manager.getLongBreakDurationMs());
    }

    @Test
    public void setFocusDurationMinutes_zero_fallsBackToDefault() {
        // resolveMs trả default khi minutes <= 0
        manager.setFocusDurationMinutes(0);
        assertEquals(AppConstants.TIMER_WORK_DURATION_MS, manager.getFocusDurationMs());
    }

    @Test
    public void setFocusDurationMinutes_persistsAcrossInstances() {
        manager.setFocusDurationMinutes(45);
        // Tạo đối tượng mới đọc cùng SharedPreferences
        SettingsManager reloaded = new SettingsManager(context);
        assertEquals(45 * 60_000L, reloaded.getFocusDurationMs());
    }

    // ── Các toggle cài đặt ─────────────────────────────────────────────────────

    @Test
    public void strictMode_defaultFalse_thenRoundTrips() {
        assertFalse(manager.isStrictMode());
        manager.setStrictMode(true);
        assertTrue(manager.isStrictMode());
        manager.setStrictMode(false);
        assertFalse(manager.isStrictMode());
    }

    @Test
    public void dndMode_defaultFalse_thenRoundTrips() {
        assertFalse(manager.isDndMode());
        manager.setDndMode(true);
        assertTrue(manager.isDndMode());
    }

    @Test
    public void autoStartBreak_defaultFalse_thenRoundTrips() {
        assertFalse(manager.isAutoStartBreak());
        manager.setAutoStartBreak(true);
        assertTrue(manager.isAutoStartBreak());
    }

    @Test
    public void darkMode_defaultFalse_thenRoundTrips() {
        assertFalse(manager.isDarkMode());
        manager.setDarkMode(true);
        assertTrue(manager.isDarkMode());
        manager.setDarkMode(false);
        assertFalse(manager.isDarkMode());
    }
}
