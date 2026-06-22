package com.tomaflow.app.ui.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.tomaflow.app.data.model.LocalTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalMusicManager {

    private static final String TAG = "LocalMusicManager";
    private static final String FOLDER_NAME = "local_music";

    public static File getMusicDirectory(Context context) {
        File dir = new File(context.getFilesDir(), FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void copyUrisToLocal(Context context, List<Uri> uris) {
        File dir = getMusicDirectory(context);
        for (Uri uri : uris) {
            String fileName = getFileName(context, uri);
            if (fileName == null) {
                fileName = "Track_" + System.currentTimeMillis() + ".mp3";
            }
            File destFile = new File(dir, fileName);
            // Nếu file đã tồn tại thì bỏ qua hoặc ghi đè, ở đây thêm hậu tố nếu trùng tên
            if (destFile.exists()) {
                fileName = System.currentTimeMillis() + "_" + fileName;
                destFile = new File(dir, fileName);
            }

            try (InputStream is = context.getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                
                if (is == null) continue;
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to copy uri " + uri, e);
            }
        }
    }

    public static List<LocalTrack> getLocalTracks(Context context) {
        List<LocalTrack> tracks = new ArrayList<>();
        File dir = getMusicDirectory(context);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isAudioFile(file.getName())) {
                    String title = file.getName();
                    int lastDot = title.lastIndexOf('.');
                    if (lastDot > 0) {
                        title = title.substring(0, lastDot);
                    }
                    String id = "local_" + UUID.nameUUIDFromBytes(file.getAbsolutePath().getBytes()).toString();
                    tracks.add(new LocalTrack(id, title, file.getAbsolutePath()));
                }
            }
        }
        return tracks;
    }

    private static boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".ogg") || lower.endsWith(".flac");
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        result = cursor.getString(idx);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }
}
