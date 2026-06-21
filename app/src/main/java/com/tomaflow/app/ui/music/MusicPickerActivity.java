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
import com.tomaflow.app.data.model.BuiltInTrack;
import android.widget.LinearLayout;
import android.view.LayoutInflater;

public class MusicPickerActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    public static final String EXTRA_TRACK_URI = "extra_track_uri";
    public static final String EXTRA_TRACK_NAME = "extra_track_name";
    public static final String EXTRA_BUILTIN_TRACK_ID = "extra_builtin_track_id";
    public static final String EXTRA_CLEAR_TRACK = "extra_clear_track";

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

        setupBuiltInTracks();

        findViewById(R.id.btn_clear_track).setOnClickListener(v -> {
            selectedUri = null;
            tvTrackName.setText(R.string.focus_music_empty);
            Intent result = new Intent();
            result.putExtra(EXTRA_CLEAR_TRACK, true);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void setupBuiltInTracks() {
        LinearLayout layoutBuiltIn = findViewById(R.id.layout_builtin_tracks);
        if (layoutBuiltIn == null) return;

        LayoutInflater inflater = getLayoutInflater();
        for (BuiltInTrack track : BuiltInTrackCatalog.TRACKS) {
            View itemView = inflater.inflate(R.layout.item_builtin_track, layoutBuiltIn, false);
            TextView tvIcon = itemView.findViewById(R.id.tv_track_icon);
            TextView tvName = itemView.findViewById(R.id.tv_track_name);
            TextView tvCategory = itemView.findViewById(R.id.tv_track_category);

            tvIcon.setText(track.emoji);
            tvName.setText(track.name);
            tvCategory.setText(track.category);

            itemView.setOnClickListener(v -> {
                handlePickedBuiltInTrack(track);
            });

            layoutBuiltIn.addView(itemView);
        }
    }

    private void handlePickedBuiltInTrack(BuiltInTrack track) {
        tvTrackName.setText(track.name);
        selectedUri = "android.resource://" + getPackageName() + "/" + track.rawResId;

        Intent result = new Intent();
        result.putExtra(EXTRA_TRACK_URI, selectedUri);
        result.putExtra(EXTRA_TRACK_NAME, track.name);
        result.putExtra(EXTRA_BUILTIN_TRACK_ID, track.id);
        setResult(RESULT_OK, result);
        finish();
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
        finish();
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
