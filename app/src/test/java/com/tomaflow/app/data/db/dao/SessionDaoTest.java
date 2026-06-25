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
import com.tomaflow.app.data.db.entity.SessionEntity;

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
public class SessionDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private TomaFlowDatabase db;
    private SessionDao dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, TomaFlowDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.sessionDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private SessionEntity newSession(String taskId, long start, int durationSec, String status) {
        SessionEntity s = new SessionEntity();
        s.userId = 1;
        s.taskId = taskId;
        s.startTime = start;
        s.endTime = start + durationSec * 1000L;
        s.duration = durationSec;
        s.status = status;
        return s;
    }

    @Test
    public void insert_returnsGeneratedId() {
        long id = dao.insert(newSession("t1", 1000L, 1500, "Completed"));
        assertTrue("inserted id should be > 0", id > 0);
    }

    @Test
    public void getAllSessions_orderedByStartTimeDesc() throws Exception {
        dao.insert(newSession("a", 1000L, 1500, "Completed"));
        dao.insert(newSession("b", 3000L, 1500, "Completed"));
        dao.insert(newSession("c", 2000L, 1500, "Completed"));

        List<SessionEntity> list = observe(dao.getAllSessions());
        assertEquals(3, list.size());
        assertEquals("b", list.get(0).taskId);
        assertEquals("c", list.get(1).taskId);
        assertEquals("a", list.get(2).taskId);
    }

    @Test
    public void getTotalCompletedSessions_countsOnlyCompleted() throws Exception {
        dao.insert(newSession("a", 1000L, 1500, "Completed"));
        dao.insert(newSession("b", 2000L, 1500, "Completed"));
        dao.insert(newSession("c", 3000L, 600, "Failed"));

        Integer total = observe(dao.getTotalCompletedSessions());
        assertEquals(2, (int) total);
    }

    @Test
    public void getSessionsSince_filtersByStartTime() throws Exception {
        dao.insert(newSession("old", 1000L, 1500, "Completed"));
        dao.insert(newSession("new", 5000L, 1500, "Completed"));

        List<SessionEntity> since = observe(dao.getSessionsSince(4000L));
        assertEquals(1, since.size());
        assertEquals("new", since.get(0).taskId);
    }

    @Test
    public void deleteAll_removesEverything() throws Exception {
        dao.insert(newSession("a", 1000L, 1500, "Completed"));
        dao.insert(newSession("b", 2000L, 1500, "Completed"));
        dao.deleteAll();
        assertEquals(0, observe(dao.getAllSessions()).size());
    }

    @Test
    public void nullTaskId_persistsAndRetrieves() throws Exception {
        SessionEntity s = newSession(null, 1000L, 1500, "Completed");
        dao.insert(s);
        List<SessionEntity> list = observe(dao.getAllSessions());
        assertEquals(1, list.size());
        assertTrue(list.get(0).taskId == null);
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
