package com.tomaflow.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tomaflow.app.data.repository.DatabaseMaintenanceRepository;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test kiểm tra toàn vẹn SQLite và xuất dump database local.
 * Chạy bằng Android Instrumented Test vì cần Context thật của app.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseMaintenanceTest {

    @Test
    public void sqliteIntegrityCheck_shouldReturnOk() throws Exception {
        Application application = ApplicationProvider.getApplicationContext();
        DatabaseMaintenanceRepository repository =
                new DatabaseMaintenanceRepository(application);

        CountDownLatch latch = new CountDownLatch(1);

        final boolean[] resultOk = new boolean[1];
        final String[] resultMessage = new String[1];

        repository.checkIntegrity((isOk, message) -> {
            resultOk[0] = isOk;
            resultMessage[0] = message;

            Log.d("SQLiteIntegrityTest", "Integrity result: " + message);

            latch.countDown();
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);

        assertTrue("Integrity check timeout", completed);
        assertTrue("SQLite integrity failed: " + resultMessage[0], resultOk[0]);
        assertTrue(
                "Expected message ok but got: " + resultMessage[0],
                "ok".equalsIgnoreCase(resultMessage[0])
        );
    }

    @Test
    public void exportSqlDump_shouldCreateSqlFile() throws Exception {
        Application application = ApplicationProvider.getApplicationContext();
        DatabaseMaintenanceRepository repository =
                new DatabaseMaintenanceRepository(application);

        CountDownLatch latch = new CountDownLatch(1);

        final String[] dumpPath = new String[1];
        final Exception[] error = new Exception[1];

        repository.exportSqlDump(new DatabaseMaintenanceRepository.DumpCallback() {
            @Override
            public void onSuccess(String path) {
                dumpPath[0] = path;

                // In đường dẫn file dump ra Logcat để dễ lấy file.
                Log.d("SQLiteDumpTest", "Dump path: " + path);

                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                error[0] = e;

                Log.e("SQLiteDumpTest", "Dump failed", e);

                latch.countDown();
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);

        assertTrue("Export dump timeout", completed);
        assertTrue(
                "Export dump failed: " + (error[0] == null ? "" : error[0].getMessage()),
                error[0] == null
        );

        assertNotNull("Dump path is null", dumpPath[0]);
        assertTrue("Dump file is not .sql", dumpPath[0].endsWith(".sql"));
    }
}