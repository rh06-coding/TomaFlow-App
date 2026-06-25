package com.tomaflow.app.ui.music;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.tomaflow.app.data.model.BuiltInTrack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests cho {@link AppMusicPlayer} — chỉ kiểm tra logic listener/singleton/state khi
 * không có MediaPlayer thật (không gọi play()/playFromPath() để tránh I/O và async prepare).
 */
public class AppMusicPlayerTest {

    private AppMusicPlayer player;

    @Before
    public void setUp() throws Exception {
        resetSingleton();
        player = AppMusicPlayer.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        Field f = AppMusicPlayer.class.getDeclaredField("sInstance");
        f.setAccessible(true);
        f.set(null, null);
    }

    // ── Singleton ──────────────────────────────────────────────────────────────

    @Test
    public void getInstance_returnsSameInstance() {
        assertSame(player, AppMusicPlayer.getInstance());
    }

    // ── Trạng thái mặc định ─────────────────────────────────────────────────────

    @Test
    public void freshInstance_isNotPlaying_andNoTrack() {
        assertFalse(player.isPlaying());
        assertNull(player.getCurrentTrack());
    }

    // ── Listener management ─────────────────────────────────────────────────────

    @Test
    public void addListener_deliversCurrentStateImmediately() {
        AtomicInteger callCount = new AtomicInteger(0);
        AppMusicPlayer.OnPlaybackStateChanged listener = (isPlaying, track) -> callCount.incrementAndGet();

        player.addListener(listener);
        assertEquals(1, callCount.get());
    }

    @Test
    public void removeListener_stopsReceivingNotifications() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        AppMusicPlayer.OnPlaybackStateChanged listener = (isPlaying, track) -> callCount.incrementAndGet();

        player.addListener(listener);          // immediate delivery → count=1
        player.addListener(listener);          // dedup trong list, nhưng vẫn deliver ngay → count=2
        player.removeListener(listener);

        // stop(null) gọi notifyListeners(); listener đã bị remove nên không được báo tiếp.
        player.stop(null);
        assertEquals(2, callCount.get());
    }

    @Test
    public void stop_clearsState_andNotifiesListeners() {
        AtomicInteger callCount = new AtomicInteger(0);
        AppMusicPlayer.OnPlaybackStateChanged listener = (isPlaying, track) -> callCount.incrementAndGet();
        player.addListener(listener);   // count=1

        player.stop(null);              // notify → count=2, state reset
        assertEquals(2, callCount.get());
        assertFalse(player.isPlaying());
        assertNull(player.getCurrentTrack());
    }

    // ── Pause/Resume no-op khi không có player ──────────────────────────────────

    @Test
    public void pause_whenIdle_isNoOp() {
        player.pause(null);
        assertFalse(player.isPlaying());
    }

    @Test
    public void resume_whenIdle_isNoOp() {
        player.resume(null);
        assertFalse(player.isPlaying());
    }
}
