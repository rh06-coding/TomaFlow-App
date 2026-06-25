package com.tomaflow.app.data.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.entity.NoteEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class NoteDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private TomaFlowDatabase db;
    private NoteDao dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, TomaFlowDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.noteDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private NoteEntity newNote(String title, String content, long createdAt) {
        NoteEntity n = new NoteEntity();
        n.title = title;
        n.content = content;
        n.mood = "happy";
        n.userId = "uid1";
        n.createdAt = createdAt;
        n.updatedAt = createdAt;
        return n;
    }

    @Test
    public void insert_andGetAll_returnsOrderedByCreatedAtDesc() throws Exception {
        dao.insertNote(newNote("first", "a", 1000L));
        dao.insertNote(newNote("second", "b", 3000L));
        dao.insertNote(newNote("third", "c", 2000L));

        List<NoteEntity> list = observe(dao.getAllNotes());
        assertEquals(3, list.size());
        assertEquals("second", list.get(0).title);
        assertEquals("third", list.get(1).title);
        assertEquals("first", list.get(2).title);
    }

    @Test
    public void deleteNote_removesSpecificNote() throws Exception {
        NoteEntity n = newNote("delete me", "x", 1000L);
        dao.insertNote(n);
        assertEquals(1, observe(dao.getAllNotes()).size());

        dao.deleteNote(n);
        assertEquals(0, observe(dao.getAllNotes()).size());
    }

    @Test
    public void deleteAll_removesEverything() throws Exception {
        dao.insertNote(newNote("a", "x", 1000L));
        dao.insertNote(newNote("b", "y", 2000L));
        dao.deleteAll();
        assertEquals(0, observe(dao.getAllNotes()).size());
    }

    @Test
    public void insert_replaceOnConflict_overwritesSameId() throws Exception {
        NoteEntity n = newNote("original", "x", 1000L);
        dao.insertNote(n);

        n.title = "updated";
        dao.insertNote(n); // same noteId -> REPLACE

        List<NoteEntity> list = observe(dao.getAllNotes());
        assertEquals(1, list.size());
        assertEquals("updated", list.get(0).title);
    }

    private <T> T observe(LiveData<T> liveData) throws Exception {
        final Object[] holder = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override public void onChanged(T t) {
                holder[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        assertTrue("LiveData did not emit within timeout", latch.await(3, TimeUnit.SECONDS));
        return (T) holder[0];
    }
}
