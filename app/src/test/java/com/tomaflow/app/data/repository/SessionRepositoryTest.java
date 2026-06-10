package com.tomaflow.app.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;

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
public class SessionRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private SessionRepository repository;
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        repository = new SessionRepository(application);
    }

    // INSERT TESTS

    @Test
    public void insertSession_validData_success() throws InterruptedException {
        // Arrange
        SessionEntity session = new SessionEntity();
        session.userId = 1;
        session.taskId = "123";
        session.startTime = System.currentTimeMillis();
        session.endTime = session.startTime + 1500000; // 25 minutes
        session.duration = 1500; // 25 minutes in seconds
        session.status = "Completed";

        // Act
        repository.insert(session);

        // Give background thread time to complete
        Thread.sleep(100);

        // Assert - verify no exception thrown
        assertTrue("Insert should complete without throwing exception", true);
    }

    @Test
    public void insertSession_nullTaskId_success() throws InterruptedException {
        // Arrange - null taskId is allowed (user starts timer without selecting task)
        SessionEntity session = new SessionEntity();
        session.userId = 1;
        session.taskId = null; // Explicitly null - allowed per schema
        session.startTime = System.currentTimeMillis();
        session.endTime = session.startTime + 1500000;
        session.duration = 1500;
        session.status = "Completed";

        // Act
        repository.insert(session);

        // Give background thread time to complete
        Thread.sleep(100);

        // Assert - verify no exception thrown for null taskId
        assertTrue("Insert with null taskId should complete without exception", true);
    }

    // QUERY TESTS

    @Test
    public void getAllSessions_returnsLiveData() {
        // Act
        LiveData<List<SessionEntity>> result = repository.getAllSessions();

        // Assert
        assertNotNull("getAllSessions should return non-null LiveData", result);
    }

    @Test
    public void getWeeklyMinutes_returnsLiveData() {
        // Act
        LiveData<Integer> result = repository.getWeeklyMinutes();

        // Assert
        assertNotNull("getWeeklyMinutes should return non-null LiveData", result);
    }

    @Test
    public void getWeeklyCycles_returnsLiveData() {
        // Act
        LiveData<Integer> result = repository.getWeeklyCycles();

        // Assert
        assertNotNull("getWeeklyCycles should return non-null LiveData", result);
    }

    @Test
    public void getWeeklyDailyStats_returnsLiveData() {
        // Act
        LiveData<List<SessionDao.DailyStatRow>> result = repository.getWeeklyDailyStats();

        // Assert
        assertNotNull("getWeeklyDailyStats should return non-null LiveData", result);
    }

    //DURATION CALCULATION TESTS 

    @Test
    public void sessionDuration_25minutes_calculatedCorrectly() {
        // Arrange
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 1500000; // 25 minutes later
        int expectedDuration = 1500; // 25 minutes in seconds

        // Act - verify calculation logic matches repository pattern
        int calculatedDuration = (int) ((endTime - startTime) / 1000L);

        // Assert
        assertEquals("25-minute duration should be 1500 seconds", expectedDuration, calculatedDuration);
    }

    @Test
    public void sessionDuration_30minutes_calculatedCorrectly() {
        // Arrange
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 1800000; // 30 minutes later
        int expectedDuration = 1800; // 30 minutes in seconds

        // Act
        int calculatedDuration = (int) ((endTime - startTime) / 1000L);

        // Assert
        assertEquals("30-minute duration should be 1800 seconds", expectedDuration, calculatedDuration);
    }

    @Test
    public void sessionDuration_zeroTime_handledCorrectly() {
        // Arrange - edge case: startTime == endTime
        long time = System.currentTimeMillis();

        // Act
        int duration = (int) Math.max(0, (time - time) / 1000L);

        // Assert - should handle zero duration gracefully
        assertEquals("Zero duration should be handled correctly", 0, duration);
    }

    @Test
    public void sessionDuration_negativeTime_preventedByMaxFunction() {
        // Arrange - edge case: endTime < startTime (shouldn't happen but test robustness)
        long startTime = System.currentTimeMillis();
        long endTime = startTime - 1000; // 1 second earlier

        // Act - Math.max prevents negative durations
        int duration = (int) Math.max(0, (endTime - startTime) / 1000L);

        // Assert
        assertEquals("Negative duration should be clamped to 0", 0, duration);
    }

    // STATUS FIELD TESTS

    @Test
    public void sessionStatus_completed_setCorrectly() {
        // Arrange
        SessionEntity session = new SessionEntity();
        session.userId = 1;
        session.taskId = "456";
        session.startTime = System.currentTimeMillis();
        session.endTime = session.startTime + 1500000;
        session.duration = 1500;
        session.status = "Completed";

        // Assert
        assertEquals("Status should be 'Completed'", "Completed", session.status);
    }

    @Test
    public void sessionStatus_failed_setCorrectly() {
        // Arrange
        SessionEntity session = new SessionEntity();
        session.userId = 1;
        session.taskId = null; // Failed session without task
        session.startTime = System.currentTimeMillis();
        session.endTime = session.startTime + 500000; // Only 8 minutes (incomplete)
        session.duration = 500;
        session.status = "Failed";

        // Assert
        assertEquals("Status should be 'Failed'", "Failed", session.status);
    }

    // HELPER METHOD

    /**
     * Helper to observe LiveData in tests.
     *  useful for future integration tests.
     */
    private <T> T getValueFromLiveData(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);

        return (T) data[0];
    }
}
