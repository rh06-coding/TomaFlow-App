package com.tomaflow.app.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.TaskEntity;

public class TaskPickerAdapter extends ListAdapter<TaskEntity, TaskPickerAdapter.TaskViewHolder> {

    private final OnTaskClickListener mListener;
    private final String mSelectedTaskId;

    public interface OnTaskClickListener {
        void onTaskClick(TaskEntity task);
    }

    public TaskPickerAdapter(String selectedTaskId, OnTaskClickListener listener) {
        super(new DiffCallback());
        this.mSelectedTaskId = selectedTaskId;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_picker, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = getItem(position);
        holder.bind(task, task.taskId.equals(mSelectedTaskId));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvPomos;
        private final ImageView ivCheck;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvPomos = itemView.findViewById(R.id.tv_pomos);
            ivCheck = itemView.findViewById(R.id.iv_check);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onTaskClick(getItem(position));
                }
            });
        }

        public void bind(TaskEntity task, boolean isSelected) {
            tvTitle.setText(task.title);
            if (task.description == null || task.description.trim().isEmpty()) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setText(task.description);
                tvDesc.setVisibility(View.VISIBLE);
            }
            tvPomos.setText(String.valueOf(task.estPomodoros));
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<TaskEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return oldItem.taskId.equals(newItem.taskId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return oldItem.title.equals(newItem.title) &&
                   ((oldItem.description == null && newItem.description == null) || 
                    (oldItem.description != null && oldItem.description.equals(newItem.description))) &&
                   oldItem.estPomodoros == newItem.estPomodoros;
        }
    }
}
