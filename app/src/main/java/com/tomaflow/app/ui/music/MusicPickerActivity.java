package com.tomaflow.app.ui.music;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomaflow.app.R;

public class MusicPickerActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    public static final String EXTRA_TRACK_URI = "extra_track_uri";
    public static final String EXTRA_TRACK_NAME = "extra_track_name";

    private TextView tvTrackName;
    private View cardSelectedTrack;
    private String selectedUri = null;

    private final ActivityResultLauncher<String> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handlePickedAudio(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTrackName = findViewById(R.id.tv_track_name);
        cardSelectedTrack = findViewById(R.id.card_selected_track);

        // Restore previously selected track if any
        String prevUri = getIntent().getStringExtra(EXTRA_TRACK_URI);
        String prevName = getIntent().getStringExtra(EXTRA_TRACK_NAME);
        if (prevUri != null && prevName != null) {
            tvTrackName.setText(prevName);
            selectedUri = prevUri;
        }

        findViewById(R.id.btn_pick_from_device).setOnClickListener(v ->
                pickAudioLauncher.launch("audio/*")
        );

        findViewById(R.id.btn_clear_track).setOnClickListener(v -> {
            selectedUri = null;
            tvTrackName.setText(R.string.focus_music_empty);
            setResult(RESULT_CANCELED);
        });
    }

    private void handlePickedAudio(Uri uri) {
        // Resolve display name
        String name = resolveFileName(uri);
        tvTrackName.setText(name);
        selectedUri = uri.toString();

        // Return result to caller
        Intent result = new Intent();
        result.putExtra(EXTRA_TRACK_URI, selectedUri);
        result.putExtra(EXTRA_TRACK_NAME, name);
        setResult(RESULT_OK, result);
    }

    private String resolveFileName(Uri uri) {
        String name = null;
        try (android.database.Cursor cursor = getContentResolver().query(
                uri, new String[]{MediaStore.Audio.Media.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                name = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        if (name == null) {
            name = uri.getLastPathSegment();
        }
        return name != null ? name : "Unknown track";
    }
}
