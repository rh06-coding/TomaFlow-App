package com.tomaflow.app.data.db.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests cho {@link TaskEntity} — logic thuần Java (UUID + giá trị mặc định),
 * không cần Robolectric hay Room.
 */
public class TaskEntityTest {

    @Test
    public void defaultConstructor_assignsUuidAndDefaults() {
        TaskEntity t = new TaskEntity();
        assertNotNull(t.taskId);
        assertTrue(t.taskId.length() > 0);
        assertEquals(1, t.estPomodoros);
        assertEquals(0, t.estimatedMinutes);
    }

    @Test
    public void threeArgConstructor_setsFieldsWithPendingStatus() {
        TaskEntity t = new TaskEntity("Read book", "Chapter 1-3", 4);
        assertEquals("", t.userId);
        assertEquals("Read book", t.title);
        assertEquals("Chapter 1-3", t.description);
        assertEquals(4, t.estPomodoros);
        assertEquals(0, t.estimatedMinutes);
        assertEquals("Pending", t.status);
        assertEquals("", t.tags);
        assertTrue(t.createdAt > 0);
        assertTrue(t.updatedAt >= t.createdAt);
    }

    @Test
    public void fourArgConstructor_setsEstimatedMinutes() {
        TaskEntity t = new TaskEntity("Read book", "Chapter 1-3", 4, 120);
        assertEquals(4, t.estPomodoros);
        assertEquals(120, t.estimatedMinutes);
    }

    @Test
    public void twoDefaultConstructors_produceDistinctUuids() {
        TaskEntity a = new TaskEntity();
        TaskEntity b = new TaskEntity();
        assertNotEquals(a.taskId, b.taskId);
    }
}
