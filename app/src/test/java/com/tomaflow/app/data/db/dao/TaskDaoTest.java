package com.tomaflow.app.data.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.entity.TaskEntity;

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
public class TaskDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private TomaFlowDatabase db;
    private TaskDao dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, TomaFlowDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.taskDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private TaskEntity newTask(String title, String status, long createdAt) {
        TaskEntity t = new TaskEntity();
        t.userId = "uid1";
        t.title = title;
        t.description = "";
        t.estPomodoros = 2;
        t.estimatedMinutes = 50;
        t.status = status;
        t.tags = "";
        t.createdAt = createdAt;
        t.updatedAt = createdAt;
        return t;
    }

    // ── insert + getTaskByIdSync ───────────────────────────────────────────────

    @Test
    public void insert_andGetByIdSync_returnsSameTask() {
        TaskEntity t = newTask("Read book", "Pending", 1000L);
        dao.insert(t);

        TaskEntity fetched = dao.getTaskByIdSync(t.taskId);
        assertNotNull(fetched);
        assertEquals("Read book", fetched.title);
        assertEquals("Pending", fetched.status);
    }

    @Test
    public void getTaskByIdSync_unknownId_returnsNull() {
        assertNull(dao.getTaskByIdSync("does-not-exist"));
    }

    // ── getAllTasks (LiveData) ─────────────────────────────────────────────────

    @Test
    public void getAllTasks_returnsAllInsertedOrderedByCreatedAtDesc() throws Exception {
        dao.insert(newTask("A", "Pending", 1000L));
        dao.insert(newTask("B", "Pending", 3000L));
        dao.insert(newTask("C", "Pending", 2000L));

        List<TaskEntity> list = observe(dao.getAllTasks());
        assertEquals(3, list.size());
        // newest first
        assertEquals("B", list.get(0).title);
        assertEquals("C", list.get(1).title);
        assertEquals("A", list.get(2).title);
    }

    @Test
    public void getPendingTasks_excludesCompleted() throws Exception {
        dao.insert(newTask("todo", "Pending", 1000L));
        dao.insert(newTask("done", "Completed", 2000L));

        List<TaskEntity> list = observe(dao.getPendingTasks());
        assertEquals(1, list.size());
        assertEquals("todo", list.get(0).title);
    }

    @Test
    public void getPendingTaskCount_countsNonCompleted() throws Exception {
        dao.insert(newTask("t1", "Pending", 1000L));
        dao.insert(newTask("t2", "InProgress", 2000L));
        dao.insert(newTask("t3", "Completed", 3000L));

        Integer count = observe(dao.getPendingTaskCount());
        assertEquals(2, (int) count);
    }

    // ── updateTaskStatus ───────────────────────────────────────────────────────

    @Test
    public void updateTaskStatus_changesStatusAndUpdatedAt() {
        TaskEntity t = newTask("Study", "Pending", 1000L);
        dao.insert(t);

        dao.updateTaskStatus(t.taskId, "Completed", 9999L);
        TaskEntity fetched = dao.getTaskByIdSync(t.taskId);
        assertEquals("Completed", fetched.status);
        assertEquals(9999L, fetched.updatedAt);
    }

    // ── deleteById ─────────────────────────────────────────────────────────────

    @Test
    public void deleteById_removesTask() {
        TaskEntity t = newTask("Gone", "Pending", 1000L);
        dao.insert(t);
        dao.deleteById(t.taskId);
        assertNull(dao.getTaskByIdSync(t.taskId));
    }

    // ── deleteAll ──────────────────────────────────────────────────────────────

    @Test
    public void deleteAll_removesEverything() throws Exception {
        dao.insert(newTask("a", "Pending", 1000L));
        dao.insert(newTask("b", "Pending", 2000L));
        dao.deleteAll();
        assertEquals(0, observe(dao.getAllTasks()).size());
    }

    // ── getTasksByTag ──────────────────────────────────────────────────────────

    @Test
    public void getTasksByTag_matchesSubstring() throws Exception {
        TaskEntity t1 = newTask("t1", "Pending", 1000L);
        t1.tags = "study,urgent";
        TaskEntity t2 = newTask("t2", "Pending", 2000L);
        t2.tags = "study";
        TaskEntity t3 = newTask("t3", "Pending", 3000L);
        t3.tags = "work";
        dao.insert(t1);
        dao.insert(t2);
        dao.insert(t3);

        List<TaskEntity> tagged = observe(dao.getTasksByTag("study"));
        assertEquals(2, tagged.size());
    }

    @Test
    public void updateTags_changesTagsField() {
        TaskEntity t = newTask("t", "Pending", 1000L);
        dao.insert(t);
        dao.updateTags(t.taskId, "newtag", 5555L);
        TaskEntity fetched = dao.getTaskByIdSync(t.taskId);
        assertEquals("newtag", fetched.tags);
        assertEquals(5555L, fetched.updatedAt);
    }

    // ── Helper: observe LiveData synchronously ─────────────────────────────────

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
