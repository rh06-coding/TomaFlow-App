package com.tomaflow.app.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.TaskEntity;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskEntity> taskList;
    private OnTaskDeleteListener listener;

    public interface OnTaskDeleteListener {
        void onDeleteClick(TaskEntity task);
    }


    public TaskAdapter(List<TaskEntity> taskList, OnTaskDeleteListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }



    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = taskList.get(position);
        holder.tvTitle.setText(task.title);
        holder.tvNote.setText(task.description);

        // Show estimated minutes if set, else show estPomodoros (tomato count)
        if (task.estimatedMinutes > 0) {
            holder.tvPomos.setText(task.estimatedMinutes + " " + holder.itemView.getContext().getString(R.string.task_duration_unit));
        } else {
            holder.tvPomos.setText(String.valueOf(task.estPomodoros));
        }

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDelete;
        TextView tvTitle;
        TextView tvNote;
        TextView tvPomos;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvPomos = itemView.findViewById(R.id.tv_pomos);
        }
    }
}
