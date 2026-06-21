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
    private OnTaskCheckedChangeListener listener;
    private OnSendToFocusListener sendToFocusListener;

    public interface OnTaskCheckedChangeListener {
        void onCheckedChange(TaskEntity task, boolean isChecked);
    }

    public interface OnSendToFocusListener {
        void onSendToFocus(TaskEntity task);
    }

    public TaskAdapter(List<TaskEntity> taskList, OnTaskCheckedChangeListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setSendToFocusListener(OnSendToFocusListener sendToFocusListener) {
        this.sendToFocusListener = sendToFocusListener;
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
            holder.tvPomos.setText(task.estimatedMinutes + "m");
        } else {
            holder.tvPomos.setText(String.valueOf(task.estPomodoros));
        }

        holder.cbDone.setOnCheckedChangeListener(null); // Prevent unwanted triggers during recycling
        holder.cbDone.setChecked("Completed".equals(task.status));
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCheckedChange(task, isChecked);
            }
        });

        // "Send to Focus" button
        if (holder.btnSendToFocus != null) {
            if ("Completed".equals(task.status)) {
                holder.btnSendToFocus.setVisibility(View.GONE);
            } else {
                holder.btnSendToFocus.setVisibility(View.VISIBLE);
                holder.btnSendToFocus.setOnClickListener(v -> {
                    if (sendToFocusListener != null) {
                        sendToFocusListener.onSendToFocus(task);
                    } else {
                        // Fallback toast
                        String msg = v.getContext().getString(R.string.task_sent_to_focus, task.title);
                        Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTitle;
        TextView tvNote;
        TextView tvPomos;
        ImageView btnSendToFocus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_done);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvPomos = itemView.findViewById(R.id.tv_pomos);
            btnSendToFocus = itemView.findViewById(R.id.btn_send_to_focus);
        }
    }
}
