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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        noteViewModel = new ViewModelProvider(requireParentFragment()).get(NoteViewModel.class);

        EditText etTitle = view.findViewById(R.id.et_note_title);
        EditText etContent = view.findViewById(R.id.et_note_content);
        ChipGroup cgMoods = view.findViewById(R.id.cg_moods);
        
        view.findViewById(R.id.btn_save_note).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            
            if (content.isEmpty()) {
                etContent.setError("Vui lòng nhập nội dung");
                return;
            }
            
            String mood = "focused";
            int checkedId = cgMoods.getCheckedChipId();
            if (checkedId == R.id.chip_happy) mood = "happy";
            else if (checkedId == R.id.chip_focused) mood = "focused";
            else if (checkedId == R.id.chip_tired) mood = "tired";
            else if (checkedId == R.id.chip_stressed) mood = "stressed";
            
            NoteEntity note = new NoteEntity();
            note.title = title;
            note.content = content;
            note.mood = mood;
            
            noteViewModel.insert(note);
            dismiss();
        });
    }
}
