package com.tomaflow.app.data.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.entity.SettingsEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class SettingsDaoTest {

    private TomaFlowDatabase db;
    private SettingsDao dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, TomaFlowDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.settingsDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private SettingsEntity newSettings(int work, int shortB, int longB, int interval) {
        SettingsEntity s = new SettingsEntity();
        s.userId = 1;
        s.workDuration = work;
        s.shortBreak = shortB;
        s.longBreak = longB;
        s.longBreakInterval = interval;
        s.focusMusic = false;
        s.createdAt = 1000L;
        return s;
    }

    @Test
    public void getSettingsSync_empty_returnsNull() {
        assertNull(dao.getSettingsSync());
    }

    @Test
    public void insert_andGetSettingsSync_returnsRow() {
        dao.insert(newSettings(25, 5, 15, 4));
        SettingsEntity s = dao.getSettingsSync();
        assertEquals(25, s.workDuration);
        assertEquals(5, s.shortBreak);
        assertEquals(15, s.longBreak);
        assertEquals(4, s.longBreakInterval);
    }

    @Test
    public void update_changesDurations() {
        dao.insert(newSettings(25, 5, 15, 4));
        SettingsEntity s = dao.getSettingsSync();
        s.workDuration = 50;
        s.shortBreak = 10;
        dao.update(s);

        SettingsEntity fetched = dao.getSettingsSync();
        assertEquals(50, fetched.workDuration);
        assertEquals(10, fetched.shortBreak);
    }

    @Test
    public void deleteAll_clearsSettings() {
        dao.insert(newSettings(25, 5, 15, 4));
        dao.deleteAll();
        assertNull(dao.getSettingsSync());
    }

    @Test
    public void insert_returnsGeneratedId() {
        long id = dao.insert(newSettings(25, 5, 15, 4));
        assertTrue("settings id should be > 0", id > 0);
    }
}
