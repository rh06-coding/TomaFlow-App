package com.tomaflow.app.ui.leaderboard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardActivity.LeaderboardEntry> entries;
    private final String currentUserId;

    public LeaderboardAdapter(List<LeaderboardActivity.LeaderboardEntry> entries, String currentUserId) {
        this.entries = entries;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardActivity.LeaderboardEntry entry = entries.get(position);
        
        holder.tvRank.setText(entry.rank);
        holder.tvUsername.setText(entry.username);
        holder.tvPomodoros.setText(String.valueOf(entry.pomodoros));
        
        if (entry.streak > 0) {
            holder.tvStreak.setText(holder.itemView.getContext().getString(R.string.leaderboard_streak, entry.streak));
            holder.tvStreak.setVisibility(View.VISIBLE);
        } else {
            holder.tvStreak.setVisibility(View.GONE);
        }
        
        holder.ivVipCrown.setVisibility(entry.isVip ? View.VISIBLE : View.GONE);

        if (entry.userId.equals(currentUserId)) {
            holder.tvIsMe.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.drawable.bg_card_primary_soft);
        } else {
            holder.tvIsMe.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(R.drawable.bg_card);
        }
        
        holder.tvRank.setTextColor(Color.parseColor("#C8324A")); // toma_primary

        // Load avatar
        if (entry.avatarUrl != null && !entry.avatarUrl.isEmpty()) {
            holder.ivAvatar.setVisibility(View.VISIBLE);
            holder.tvInitials.setVisibility(View.GONE);
            holder.ivDefault.setVisibility(View.GONE);
            com.tomaflow.app.utils.AvatarHelper.loadAvatar(holder.itemView.getContext(), entry.avatarUrl, holder.ivAvatar);
        } else {
            holder.ivAvatar.setVisibility(View.GONE);
            String initials = "";
            String name = entry.username;
            if (name != null) {
                String[] parts = name.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
            }
            if (!initials.isEmpty()) {
                holder.tvInitials.setText(initials.toUpperCase());
                holder.tvInitials.setVisibility(View.VISIBLE);
                holder.ivDefault.setVisibility(View.GONE);
            } else {
                holder.tvInitials.setVisibility(View.GONE);
                holder.ivDefault.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvIsMe, tvStreak, tvPomodoros;
        android.widget.ImageView ivAvatar, ivDefault, ivVipCrown;
        TextView tvInitials;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvIsMe = itemView.findViewById(R.id.tv_is_me);
            tvStreak = itemView.findViewById(R.id.tv_streak);
            tvPomodoros = itemView.findViewById(R.id.tv_pomodoros);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvInitials = itemView.findViewById(R.id.tv_avatar_initials);
            ivDefault = itemView.findViewById(R.id.iv_default_avatar);
            ivVipCrown = itemView.findViewById(R.id.iv_vip_crown);
        }
    }
}
