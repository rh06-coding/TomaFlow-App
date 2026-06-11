package com.tomaflow.app.data.repository;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tomaflow.app.data.db.TomaFlowDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository dùng để kiểm tra toàn vẹn SQLite và xuất dump database local.
 * Phục vụ debug và nộp minh chứng kèm báo cáo.
 */
public class DatabaseMaintenanceRepository {

    private final Application application;
    private final TomaFlowDatabase roomDatabase;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public DatabaseMaintenanceRepository(Application application) {
        this.application = application;
        this.roomDatabase = TomaFlowDatabase.getInstance(application);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface IntegrityCallback {
        void onResult(boolean isOk, String message);
    }

    public interface DumpCallback {
        void onSuccess(String dumpPath);

        void onFailure(Exception e);
    }

    /**
     * Kiểm tra database SQLite local bằng PRAGMA integrity_check.
     * SQLite trả về "ok" nếu database không bị lỗi.
     */
    public void checkIntegrity(IntegrityCallback callback) {
        executorService.execute(() -> {
            try {
                SupportSQLiteDatabase db = roomDatabase.getOpenHelper().getReadableDatabase();
                String result = runSingleResultQuery(db, "PRAGMA integrity_check");

                boolean isOk = "ok".equalsIgnoreCase(result);

                mainHandler.post(() -> callback.onResult(isOk, result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(false, e.getMessage()));
            }
        });
    }

    /**
     * Xuất dump SQLite thành file .sql trong thư mục Downloads/TomaFlowDumps.
     * File nằm ngoài app-specific storage nên không bị mất khi test runner gỡ app.
     */
    public void exportSqlDump(DumpCallback callback) {
        executorService.execute(() -> {
            try {
                SupportSQLiteDatabase db = roomDatabase.getOpenHelper().getReadableDatabase();

                String fileName = "tomaflow_sqlite_dump_" + System.currentTimeMillis() + ".sql";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/sql");
                values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/TomaFlowDumps"
                );

                android.net.Uri uri = application.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                );

                if (uri == null) {
                    throw new IOException("Cannot create dump file in Downloads");
                }

                OutputStream outputStream = application.getContentResolver().openOutputStream(uri);

                if (outputStream == null) {
                    throw new IOException("Cannot open dump output stream");
                }

                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    writeSqlDump(db, writer);
                    writer.flush();
                }

                String dumpPath = "Download/TomaFlowDumps/" + fileName;
                mainHandler.post(() -> callback.onSuccess(dumpPath));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    private String runSingleResultQuery(SupportSQLiteDatabase db, String sql) {
        Cursor cursor = db.query(sql);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return "No result";
        } finally {
            cursor.close();
        }
    }

    private void writeSqlDump(SupportSQLiteDatabase db, Writer writer) throws IOException {
        writer.write("-- TomaFlow SQLite Dump\n");
        writer.write("-- Generated at: " + System.currentTimeMillis() + "\n\n");
        writer.write("PRAGMA foreign_keys=OFF;\n");
        writer.write("BEGIN TRANSACTION;\n\n");

        dumpSchema(db, writer);
        dumpTableData(db, writer);

        writer.write("\nCOMMIT;\n");
    }

    /**
     * Xuất câu lệnh CREATE TABLE từ sqlite_master.
     */
    private void dumpSchema(SupportSQLiteDatabase db, Writer writer) throws IOException {
        Cursor cursor = db.query(
                "select name, sql from sqlite_master " +
                        "where type = 'table' " +
                        "and name not like 'sqlite_%' " +
                        "and name != 'android_metadata' " +
                        "and name != 'room_master_table' " +
                        "order by name"
        );

        try {
            writer.write("-- Schema\n");

            while (cursor.moveToNext()) {
                String createSql = cursor.getString(1);

                if (createSql != null && !createSql.trim().isEmpty()) {
                    writer.write(createSql);
                    writer.write(";\n\n");
                }
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Xuất dữ liệu từng bảng thành các câu INSERT.
     */
    private void dumpTableData(SupportSQLiteDatabase db, Writer writer) throws IOException {
        Cursor tableCursor = db.query(
                "select name from sqlite_master " +
                        "where type = 'table' " +
                        "and name not like 'sqlite_%' " +
                        "and name != 'android_metadata' " +
                        "and name != 'room_master_table' " +
                        "order by name"
        );

        try {
            writer.write("-- Data\n");

            while (tableCursor.moveToNext()) {
                String tableName = tableCursor.getString(0);
                dumpSingleTable(db, writer, tableName);
            }
        } finally {
            tableCursor.close();
        }
    }

    private void dumpSingleTable(SupportSQLiteDatabase db, Writer writer, String tableName)
            throws IOException {
        Cursor cursor = db.query("select * from `" + tableName + "`");

        try {
            String[] columns = cursor.getColumnNames();

            writer.write("\n-- Table: " + tableName + "\n");

            while (cursor.moveToNext()) {
                writer.write("INSERT INTO `");
                writer.write(tableName);
                writer.write("` (");

                for (int i = 0; i < columns.length; i++) {
                    if (i > 0) {
                        writer.write(", ");
                    }

                    writer.write("`");
                    writer.write(columns[i]);
                    writer.write("`");
                }

                writer.write(") VALUES (");

                for (int i = 0; i < columns.length; i++) {
                    if (i > 0) {
                        writer.write(", ");
                    }

                    writer.write(toSqlValue(cursor, i));
                }

                writer.write(");\n");
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Chuyển dữ liệu Cursor thành giá trị SQL an toàn để ghi vào file dump.
     */
    private String toSqlValue(Cursor cursor, int columnIndex) {
        int type = cursor.getType(columnIndex);

        switch (type) {
            case Cursor.FIELD_TYPE_NULL:
                return "NULL";

            case Cursor.FIELD_TYPE_INTEGER:
                return String.valueOf(cursor.getLong(columnIndex));

            case Cursor.FIELD_TYPE_FLOAT:
                return String.valueOf(cursor.getDouble(columnIndex));

            case Cursor.FIELD_TYPE_BLOB:
                return "NULL";

            case Cursor.FIELD_TYPE_STRING:
            default:
                String value = cursor.getString(columnIndex);
                return "'" + value.replace("'", "''") + "'";
        }
    }
}