package com.tomaflow.app.ui.rewards;

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
import com.tomaflow.app.data.model.BadgeItem;

public class BadgeAdapter extends ListAdapter<BadgeItem, BadgeAdapter.BadgeViewHolder> {

    public BadgeAdapter() {
        super(new DiffUtil.ItemCallback<BadgeItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull BadgeItem oldItem, @NonNull BadgeItem newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull BadgeItem oldItem, @NonNull BadgeItem newItem) {
                return oldItem.isUnlocked() == newItem.isUnlocked();
            }
        });
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeItem item = getItem(position);
        holder.bind(item);
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final ImageView ivIcon;
        private final ImageView ivLock;
        private final View containerIcon;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_badge_title);
            tvDesc = itemView.findViewById(R.id.tv_badge_desc);
            ivIcon = itemView.findViewById(R.id.iv_badge);
            ivLock = itemView.findViewById(R.id.iv_lock);
            containerIcon = itemView.findViewById(R.id.icon_bg);
        }

        public void bind(BadgeItem item) {
            tvTitle.setText(itemView.getContext().getString(item.getTitleResId()));
            tvDesc.setText(itemView.getContext().getString(item.getDescResId()));
            ivIcon.setImageResource(item.getIconResId());

            if (!item.isUnlocked()) {
                containerIcon.setAlpha(0.5f);
                ivLock.setVisibility(View.VISIBLE);
            } else {
                containerIcon.setAlpha(1.0f);
                ivLock.setVisibility(View.GONE);
            }
        }
    }
}
