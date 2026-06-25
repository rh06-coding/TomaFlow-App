package com.tomaflow.app.ui.stats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.NoteEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter extends ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onEdit(NoteEntity note);
        void onDelete(NoteEntity note);
    }

    private OnNoteClickListener listener;

    public NoteAdapter(OnNoteClickListener listener) {
        super(new DiffUtil.ItemCallback<NoteEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
                return oldItem.noteId.equals(newItem.noteId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
                return oldItem.title.equals(newItem.title) &&
                       oldItem.content.equals(newItem.content) &&
                       oldItem.mood.equals(newItem.mood);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = getItem(position);
        holder.bind(note, listener);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent, tvMood, btnEdit, btnDelete;
        View vMoodStrip;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_note_title);
            tvDate = itemView.findViewById(R.id.tv_note_date);
            tvContent = itemView.findViewById(R.id.tv_note_content);
            tvMood = itemView.findViewById(R.id.tv_note_mood);
            vMoodStrip = itemView.findViewById(R.id.v_mood_strip);
            btnEdit = itemView.findViewById(R.id.btn_note_edit);
            btnDelete = itemView.findViewById(R.id.btn_note_delete);
        }

        public void bind(NoteEntity note, OnNoteClickListener listener) {
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(note);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(note);
            });

            android.content.Context context = itemView.getContext();

            tvTitle.setText(note.title == null || note.title.isEmpty() ? context.getString(R.string.journal_entry_default) : note.title);
            tvContent.setText(note.content);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(note.createdAt)));

            int colorRes = R.color.toma_primary;
            String moodText = "";

            if (note.mood != null) {
                switch (note.mood) {
                    case "happy":
                        colorRes = R.color.toma_success;
                        moodText = context.getString(R.string.mood_happy);
                        break;
                    case "focused":
                        colorRes = R.color.toma_info;
                        moodText = context.getString(R.string.mood_focused);
                        break;
                    case "tired":
                        colorRes = R.color.toma_warning;
                        moodText = context.getString(R.string.mood_tired);
                        break;
                    case "stressed":
                        colorRes = R.color.toma_error;
                        moodText = context.getString(R.string.mood_stressed);
                        break;
                    default:
                        moodText = note.mood;
                }
            }
            
            tvMood.setText(moodText);
            // Plain rectangular color — the parent LinearLayout's clipToOutline
            // (see item_note.xml) rounds it to the card's 16dp corners.
            vMoodStrip.setBackgroundResource(colorRes);
            tvMood.setTextColor(context.getResources().getColor(colorRes, null));
        }
    }
}
