package com.tomaflow.app.ui.chat;

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
import com.tomaflow.app.data.model.ChatMessage;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final String currentUserId;
    private final String friendAvatarUrl;
    private final String friendName;

    public ChatAdapter(String currentUserId, String friendAvatarUrl, String friendName) {
        super(new DiffUtil.ItemCallback<ChatMessage>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.id != null && oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.content != null && oldItem.content.equals(newItem.content)
                        && oldItem.timestamp == newItem.timestamp;
            }
        });
        this.currentUserId = currentUserId;
        this.friendAvatarUrl = friendAvatarUrl;
        this.friendName = friendName;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        if (currentUserId.equals(message.senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, friendAvatarUrl, friendName);
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_sent);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.content);
            if ("achievement".equals(message.type)) {
                tvMessage.setBackgroundResource(R.drawable.bg_card_primary_soft);
                tvMessage.setTextColor(tvMessage.getContext().getResources().getColor(R.color.toma_primary));
                tvMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_sent);
                tvMessage.setTextColor(android.graphics.Color.WHITE);
                tvMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ImageView ivAvatar;
        TextView tvInitials;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_received);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvInitials = itemView.findViewById(R.id.tv_avatar_initials);
        }

        void bind(ChatMessage message, String avatarUrl, String name) {
            tvMessage.setText(message.content);
            if ("achievement".equals(message.type)) {
                tvMessage.setBackgroundResource(R.drawable.bg_card_primary_soft);
                tvMessage.setTextColor(tvMessage.getContext().getResources().getColor(R.color.toma_primary));
                tvMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_received);
                tvMessage.setTextColor(android.graphics.Color.BLACK);
                tvMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                ivAvatar.setVisibility(View.VISIBLE);
                tvInitials.setVisibility(View.GONE);
                Glide.with(itemView.getContext()).load(avatarUrl).circleCrop().into(ivAvatar);
            } else {
                ivAvatar.setVisibility(View.GONE);
                tvInitials.setVisibility(View.VISIBLE);
                String initials = "";
                if (name != null) {
                    String[] parts = name.split(" ");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        initials += parts[0].charAt(0);
                        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                            initials += parts[parts.length - 1].charAt(0);
                        }
                    }
                }
                tvInitials.setText(initials.toUpperCase());
            }
        }
    }
}
