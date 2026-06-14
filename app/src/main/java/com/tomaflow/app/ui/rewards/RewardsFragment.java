package com.tomaflow.app.ui.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tomaflow.app.R;

public class RewardsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rewards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvBadges = view.findViewById(R.id.rv_badges);
        rvBadges.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        BadgeAdapter adapter = new BadgeAdapter();
        rvBadges.setAdapter(adapter);

        RewardsViewModel viewModel = new ViewModelProvider(this).get(RewardsViewModel.class);
        viewModel.getBadges().observe(getViewLifecycleOwner(), adapter::submitList);

        // Remove back button from toolbar since this is now a top-level BottomNav fragment
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
        }
    }
}
