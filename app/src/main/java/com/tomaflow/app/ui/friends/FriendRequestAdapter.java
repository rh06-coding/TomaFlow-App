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
import com.tomaflow.app.data.model.FriendRequestItem;
import com.tomaflow.app.data.model.UserProfile;

public class FriendRequestAdapter extends ListAdapter<FriendRequestItem, FriendRequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(FriendRequestItem item);
        void onDecline(FriendRequestItem item);
    }

    private final OnRequestActionListener listener;

    public FriendRequestAdapter(OnRequestActionListener listener) {
        super(new DiffUtil.ItemCallback<FriendRequestItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull FriendRequestItem oldItem, @NonNull FriendRequestItem newItem) {
                return oldItem.connection.id.equals(newItem.connection.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull FriendRequestItem oldItem, @NonNull FriendRequestItem newItem) {
                return oldItem.connection.status.equals(newItem.connection.status);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvUsername;
        private final TextView tvInitials;
        private final ImageView ivAvatar;
        private final ImageView btnAccept;
        private final ImageView btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }

        public void bind(FriendRequestItem item, OnRequestActionListener listener) {
            UserProfile user = item.user;
            tvName.setText(user.name != null ? user.name : user.email);
            tvUsername.setText(user.username != null ? "@" + user.username : "");
            
            btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(item);
            });
            btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(item);
            });

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
