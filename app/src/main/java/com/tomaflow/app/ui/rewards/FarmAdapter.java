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
import com.tomaflow.app.data.model.DailyTomato;

public class FarmAdapter extends ListAdapter<DailyTomato, FarmAdapter.FarmViewHolder> {

    protected FarmAdapter() {
        super(new DiffUtil.ItemCallback<DailyTomato>() {
            @Override
            public boolean areItemsTheSame(@NonNull DailyTomato oldItem, @NonNull DailyTomato newItem) {
                return oldItem.getDateStr().equals(newItem.getDateStr());
            }

            @Override
            public boolean areContentsTheSame(@NonNull DailyTomato oldItem, @NonNull DailyTomato newItem) {
                return oldItem.getTotalMinutes() == newItem.getTotalMinutes()
                        && oldItem.getStage() == newItem.getStage();
            }
        });
    }

    @NonNull
    @Override
    public FarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farm_tomato, parent, false);
        return new FarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class FarmViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivTomatoStage;
        private final TextView tvFarmDate;
        private final View flTomatoBg;

        public FarmViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTomatoStage = itemView.findViewById(R.id.iv_tomato_stage);
            tvFarmDate = itemView.findViewById(R.id.tv_farm_date);
            flTomatoBg = itemView.findViewById(R.id.fl_tomato_bg);
        }

        public void bind(DailyTomato tomato) {
            if (tomato.isPadding()) {
                tvFarmDate.setText("");
                flTomatoBg.setVisibility(View.INVISIBLE);
            } else {
                tvFarmDate.setText(tomato.getFormattedDate());
                flTomatoBg.setVisibility(View.VISIBLE);
                if (tomato.getStage() == DailyTomato.Stage.DIRT) {
                    ivTomatoStage.setVisibility(View.INVISIBLE);
                } else {
                    ivTomatoStage.setVisibility(View.VISIBLE);
                    ivTomatoStage.setImageResource(tomato.getStage().iconRes);
                }
            }
        }
    }
}
