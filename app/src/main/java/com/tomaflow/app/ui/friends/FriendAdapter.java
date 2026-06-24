package com.tomaflow.app.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.UserProfile;

public class FriendAdapter extends ListAdapter<UserProfile, FriendAdapter.FriendViewHolder> {

    public interface OnFriendActionListener {
        void onActionClick(UserProfile user);
    }

    private final OnFriendActionListener listener;
    private final String actionText; // "Add", "Sent", "Friend"

    public FriendAdapter(String actionText, OnFriendActionListener listener) {
        super(new DiffUtil.ItemCallback<UserProfile>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
                return oldItem.uid != null && oldItem.uid.equals(newItem.uid);
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
                return oldItem.username != null && oldItem.username.equals(newItem.username)
                        && oldItem.avatarUrl != null && oldItem.avatarUrl.equals(newItem.avatarUrl);
            }
        });
        this.listener = listener;
        this.actionText = actionText;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        UserProfile user = getItem(position);
        holder.bind(user, listener, actionText);
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvUsername;
        private final TextView tvInitials;
        private final ImageView ivAvatar;
        private final com.google.android.material.button.MaterialButton btnAction;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            btnAction = itemView.findViewById(R.id.btn_action);
        }

        public void bind(UserProfile user, OnFriendActionListener listener, String actionText) {
            tvName.setText(user.name != null ? user.name : user.email);
            tvUsername.setText(user.username != null ? "@" + user.username : "");
            
            btnAction.setText(actionText);
            btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onActionClick(user);
            });
            
            if (actionText.equals("Sent") || actionText.equals("Friend")) {
                btnAction.setEnabled(false);
            } else {
                btnAction.setEnabled(true);
            }

            if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                ivAvatar.setVisibility(View.VISIBLE);
                tvInitials.setVisibility(View.GONE);
                Glide.with(itemView.getContext()).load(user.avatarUrl).circleCrop().into(ivAvatar);
            } else {
                ivAvatar.setVisibility(View.GONE);
                tvInitials.setVisibility(View.VISIBLE);
                String initials = "";
                String name = tvName.getText().toString();
                String[] parts = name.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
                tvInitials.setText(initials.toUpperCase());
            }
        }
    }
}
