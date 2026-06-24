package com.tomaflow.app.ui.stats;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.tomaflow.app.R;
import com.tomaflow.app.utils.LanguageManager;

public class JournalActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageManager.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        NoteViewModel noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        NoteAdapter noteAdapter = new NoteAdapter();
        RecyclerView recyclerNotes = findViewById(R.id.recycler_notes);
        recyclerNotes.setAdapter(noteAdapter);

        TextView tvJournalEmpty = findViewById(R.id.tv_journal_empty);

        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.submitList(notes);
            if (notes == null || notes.isEmpty()) {
                tvJournalEmpty.setVisibility(View.VISIBLE);
                recyclerNotes.setVisibility(View.GONE);
            } else {
                tvJournalEmpty.setVisibility(View.GONE);
                recyclerNotes.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.fab_add_note).setOnClickListener(v -> {
            AddNoteBottomSheet.newInstance().show(getSupportFragmentManager(), "AddNoteBottomSheet");
        });
    }
}
