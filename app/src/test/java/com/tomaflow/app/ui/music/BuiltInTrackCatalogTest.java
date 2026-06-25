package com.tomaflow.app.ui.music;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.tomaflow.app.data.model.BuiltInTrack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class BuiltInTrackCatalogTest {

    @Test
    public void tracks_isNotNullAndNonEmpty() {
        assertNotNull(BuiltInTrackCatalog.TRACKS);
        assertTrue("Catalog should ship at least one built-in track",
                BuiltInTrackCatalog.TRACKS.size() >= 1);
    }

    @Test
    public void tracks_shipsThreeTracks() {
        assertEquals(3, BuiltInTrackCatalog.TRACKS.size());
    }

    @Test
    public void tracks_haveUniqueIds() {
        long distinct = BuiltInTrackCatalog.TRACKS.stream()
                .map(t -> t.id)
                .distinct()
                .count();
        assertEquals(BuiltInTrackCatalog.TRACKS.size(), distinct);
    }

    @Test
    public void tracks_haveNonBlankNames() {
        for (BuiltInTrack t : BuiltInTrackCatalog.TRACKS) {
            assertNotNull(t.name);
            assertFalse("name must not be blank", t.name.trim().isEmpty());
        }
    }

    @Test
    public void tracks_haveNonBlankCategory() {
        for (BuiltInTrack t : BuiltInTrackCatalog.TRACKS) {
            assertNotNull(t.category);
            assertFalse("category must not be blank", t.category.trim().isEmpty());
        }
    }

    @Test
    public void tracks_lofiChillPresent() {
        boolean hasLofi = BuiltInTrackCatalog.TRACKS.stream()
                .anyMatch(t -> "lofi_chill".equals(t.id));
        assertTrue("lofi_chill track should be present", hasLofi);
    }

    @Test
    public void tracks_rawResId_nonNegative() {
        for (BuiltInTrack t : BuiltInTrackCatalog.TRACKS) {
            assertTrue("rawResId should be >= 0", t.rawResId >= 0);
        }
    }
}
