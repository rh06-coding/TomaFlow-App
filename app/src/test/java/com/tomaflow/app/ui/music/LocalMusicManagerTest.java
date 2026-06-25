package com.tomaflow.app.ui.music;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.data.model.LocalTrack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Unit tests cho {@link LocalMusicManager} — thao tác file IO trong filesDir riêng của app
 * qua Robolectric, không cần ContentResolver/MediaStore thật.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class LocalMusicManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Đảm bảo thư mục nhạc rỗng trước mỗi test.
        File dir = LocalMusicManager.getMusicDirectory(context);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
    }

    @Test
    public void getMusicDirectory_exists_andIsNamedLocalMusic() {
        File dir = LocalMusicManager.getMusicDirectory(context);
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertEquals("local_music", dir.getName());
        assertEquals(context.getFilesDir(), dir.getParentFile());
    }

    @Test
    public void getLocalTracks_emptyDirectory_returnsEmptyList() {
        List<LocalTrack> tracks = LocalMusicManager.getLocalTracks(context);
        assertNotNull(tracks);
        assertTrue(tracks.isEmpty());
    }

    @Test
    public void getLocalTracks_includesMp3File_withTitleWithoutExtension() throws Exception {
        File dir = LocalMusicManager.getMusicDirectory(context);
        writeDummyFile(new File(dir, "song.mp3"), new byte[]{1, 2, 3});

        List<LocalTrack> tracks = LocalMusicManager.getLocalTracks(context);
        assertEquals(1, tracks.size());
        LocalTrack t = tracks.get(0);
        assertEquals("song", t.title);
        assertTrue("id should start with local_", t.id.startsWith("local_"));
        assertTrue(t.path.endsWith("song.mp3"));
    }

    @Test
    public void getLocalTracks_ignoresNonAudioFiles() throws Exception {
        File dir = LocalMusicManager.getMusicDirectory(context);
        writeDummyFile(new File(dir, "readme.txt"), new byte[]{1});
        writeDummyFile(new File(dir, "notes.md"), new byte[]{1});

        List<LocalTrack> tracks = LocalMusicManager.getLocalTracks(context);
        assertTrue(tracks.isEmpty());
    }

    @Test
    public void getLocalTracks_includesMultipleAudioExtensions() throws Exception {
        File dir = LocalMusicManager.getMusicDirectory(context);
        writeDummyFile(new File(dir, "a.mp3"), new byte[]{1});
        writeDummyFile(new File(dir, "b.wav"), new byte[]{1});
        writeDummyFile(new File(dir, "c.m4a"), new byte[]{1});
        writeDummyFile(new File(dir, "d.ogg"), new byte[]{1});
        writeDummyFile(new File(dir, "e.flac"), new byte[]{1});

        List<LocalTrack> tracks = LocalMusicManager.getLocalTracks(context);
        assertEquals(5, tracks.size());
    }

    @Test
    public void getLocalTracks_skipsDirectories() {
        File dir = LocalMusicManager.getMusicDirectory(context);
        // Tạo một thư mục con có đuôi .mp3 — phải bị bỏ qua vì không phải file.
        File subDir = new File(dir, "notatrack.mp3");
        assertFalse(subDir.exists() && subDir.isDirectory()); // sanity
        subDir.mkdirs();
        assertTrue(subDir.isDirectory());

        List<LocalTrack> tracks = LocalMusicManager.getLocalTracks(context);
        assertTrue(tracks.isEmpty());
    }

    private void writeDummyFile(File file, byte[] bytes) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
    }
}
