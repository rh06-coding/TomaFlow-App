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

    private final ActivityResultLauncher<Intent> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    java.util.List<Uri> uris = new java.util.ArrayList<>();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            uris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        uris.add(data.getData());
                    }
                    if (!uris.isEmpty()) {
                        LocalMusicManager.copyUrisToLocal(this, uris);
                        setupLocalTracks(); // refresh list
                    }
                }
            });

    private LinearLayout layoutBuiltIn;
    private LinearLayout layoutLocal;

    private final AppMusicPlayer.OnPlaybackStateChanged mMusicListener = (isPlaying, track) -> {
        refreshCheckmarks();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_music_picker);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_stop_music) {
                AppMusicPlayer.getInstance().stop(this);
                return true;
            }
            return false;
        });

        layoutBuiltIn = findViewById(R.id.layout_builtin_tracks);
        layoutLocal = findViewById(R.id.layout_local_tracks);

        findViewById(R.id.btn_pick_from_device).setOnClickListener(v -> {
            com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(this);
            if (!sm.isVip()) {
                com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getSupportFragmentManager(), "PremiumGateDialog");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pickAudioLauncher.launch(intent);
        });

        setupBuiltInTracks();
        setupLocalTracks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppMusicPlayer.getInstance().addListener(mMusicListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppMusicPlayer.getInstance().removeListener(mMusicListener);
    }

    private void setupBuiltInTracks() {
        if (layoutBuiltIn == null) return;
        layoutBuiltIn.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (BuiltInTrack track : BuiltInTrackCatalog.TRACKS) {
            View itemView = inflater.inflate(R.layout.item_builtin_track, layoutBuiltIn, false);
            TextView tvIcon = itemView.findViewById(R.id.tv_track_icon);
            TextView tvName = itemView.findViewById(R.id.tv_track_name);
            TextView tvCategory = itemView.findViewById(R.id.tv_track_category);

            tvIcon.setText(track.emoji);
            tvName.setText(track.name);
            tvCategory.setText(track.category);

            itemView.setTag(track.id);

            itemView.setOnClickListener(v -> {
                com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(this);
                // Assume first track is free, others require VIP
                boolean isFirstTrack = track.id.equals(BuiltInTrackCatalog.TRACKS.get(0).id);
                if (!isFirstTrack && !sm.isVip()) {
                    com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getSupportFragmentManager(), "PremiumGateDialog");
                    return;
                }
                AppMusicPlayer.getInstance().play(this, track);
            });

            layoutBuiltIn.addView(itemView);
        }
    }

    private void setupLocalTracks() {
        if (layoutLocal == null) return;
        layoutLocal.removeAllViews();
        java.util.List<com.tomaflow.app.data.model.LocalTrack> tracks = LocalMusicManager.getLocalTracks(this);
        LayoutInflater inflater = getLayoutInflater();
        
        for (com.tomaflow.app.data.model.LocalTrack track : tracks) {
            View itemView = inflater.inflate(R.layout.item_builtin_track, layoutLocal, false);
            TextView tvIcon = itemView.findViewById(R.id.tv_track_icon);
            TextView tvName = itemView.findViewById(R.id.tv_track_name);
            TextView tvCategory = itemView.findViewById(R.id.tv_track_category);

            tvIcon.setText("🎧");
            tvName.setText(track.title);
            tvCategory.setText(getString(R.string.music_from_device));

            itemView.setTag(track.id);

            itemView.setOnClickListener(v -> {
                AppMusicPlayer.getInstance().playFromPath(this, track.id, track.title, track.path);
            });

            layoutLocal.addView(itemView);
        }
        
        refreshCheckmarks();
    }

    private void refreshCheckmarks() {
        BuiltInTrack currentTrack = AppMusicPlayer.getInstance().getCurrentTrack();
        boolean isPlaying = AppMusicPlayer.getInstance().isPlaying();
        String currentId = (currentTrack != null) ? currentTrack.id : null;

        updateCheckmarksInLayout(layoutBuiltIn, currentId);
        updateCheckmarksInLayout(layoutLocal, currentId);
    }

    private void updateCheckmarksInLayout(LinearLayout layout, String currentId) {
        if (layout == null) return;
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            Object tag = child.getTag();
            android.widget.ImageView ivCheck = child.findViewById(R.id.iv_check);
            if (ivCheck != null) {
                if (currentId != null && currentId.equals(tag)) {
                    ivCheck.setVisibility(View.VISIBLE);
                } else {
                    ivCheck.setVisibility(View.GONE);
                }
            }
        }
    }
}
