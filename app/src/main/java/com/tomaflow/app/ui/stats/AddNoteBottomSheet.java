package com.tomaflow.app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.NoteEntity;

public class AddNoteBottomSheet extends BottomSheetDialogFragment {

    private NoteViewModel noteViewModel;

    public static AddNoteBottomSheet newInstance() {
        return new AddNoteBottomSheet();
    }

    public static AddNoteBottomSheet newInstance(String noteId, String title, String content, String mood) {
        AddNoteBottomSheet fragment = new AddNoteBottomSheet();
        Bundle args = new Bundle();
        args.putString("noteId", noteId);
        args.putString("title", title);
        args.putString("content", content);
        args.putString("mood", mood);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        EditText etTitle = view.findViewById(R.id.et_note_title);
        EditText etContent = view.findViewById(R.id.et_note_content);
        ChipGroup cgMoods = view.findViewById(R.id.cg_moods);

        String editNoteId = null;
        if (getArguments() != null) {
            editNoteId = getArguments().getString("noteId");
            etTitle.setText(getArguments().getString("title"));
            etContent.setText(getArguments().getString("content"));
            String mood = getArguments().getString("mood");
            if (mood != null) {
                switch (mood) {
                    case "happy": cgMoods.check(R.id.chip_happy); break;
                    case "focused": cgMoods.check(R.id.chip_focused); break;
                    case "tired": cgMoods.check(R.id.chip_tired); break;
                    case "stressed": cgMoods.check(R.id.chip_stressed); break;
                }
            }
        }
        
        final String finalEditNoteId = editNoteId;

        view.findViewById(R.id.btn_save_note).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            
            if (content.isEmpty()) {
                etContent.setError(getString(R.string.error_empty_content));
                return;
            }
            
            String mood = "focused";
            int checkedId = cgMoods.getCheckedChipId();
            if (checkedId == R.id.chip_happy) mood = "happy";
            else if (checkedId == R.id.chip_focused) mood = "focused";
            else if (checkedId == R.id.chip_tired) mood = "tired";
            else if (checkedId == R.id.chip_stressed) mood = "stressed";
            
            NoteEntity note = new NoteEntity();
            if (finalEditNoteId != null) {
                note.noteId = finalEditNoteId;
            }
            note.title = title;
            note.content = content;
            note.mood = mood;
            
            noteViewModel.insert(note);
            dismiss();
        });
    }
}
