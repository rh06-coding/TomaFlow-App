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

        if (entry.userId.equals(currentUserId)) {
            holder.tvIsMe.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.drawable.bg_card_primary_soft);
        } else {
            holder.tvIsMe.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(R.drawable.bg_card);
        }
        
        // Highlight top 3
        if (position == 0) {
            holder.tvRank.setTextColor(Color.parseColor("#F59E0B")); // Gold
        } else if (position == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#9CA3AF")); // Silver
        } else if (position == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#B45309")); // Bronze
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#C8324A")); // toma_primary
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvIsMe, tvStreak, tvPomodoros;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvIsMe = itemView.findViewById(R.id.tv_is_me);
            tvStreak = itemView.findViewById(R.id.tv_streak);
            tvPomodoros = itemView.findViewById(R.id.tv_pomodoros);
        }
    }
}
