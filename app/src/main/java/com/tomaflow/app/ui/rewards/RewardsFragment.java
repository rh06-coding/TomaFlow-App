package com.tomaflow.app.ui.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RewardsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rewards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFarm = view.findViewById(R.id.rv_farm);
        rvFarm.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        FarmAdapter adapter = new FarmAdapter();
        rvFarm.setAdapter(adapter);

        RewardsViewModel viewModel = new ViewModelProvider(this).get(RewardsViewModel.class);
        viewModel.getDailyTomatoes().observe(getViewLifecycleOwner(), adapter::submitList);

        // Month Navigation
        TextView tvMonthYear = view.findViewById(R.id.tv_month_year);
        ImageView btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        ImageView btnNextMonth = view.findViewById(R.id.btn_next_month);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            if (yearMonth != null) {
                tvMonthYear.setText(yearMonth.format(formatter));
            }
        });

        btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        // Remove back button from toolbar since this is now a top-level BottomNav fragment
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
        }
        // Cập nhật tên Profile từ Firebase
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            android.widget.TextView tvName = view.findViewById(R.id.tv_rewards_profile_name);
            android.widget.TextView tvInitials = view.findViewById(R.id.tv_rewards_avatar_initials);

            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            if (tvName != null && name != null) {
                tvName.setText(name);
            }

            if (tvInitials != null && name != null && !name.isEmpty()) {
                String initials = "";
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
