package com.tomaflow.app.ui.rewards;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tomaflow.app.data.model.BadgeItem;
import com.tomaflow.app.data.repository.RewardsRepository;

import java.util.List;

public class RewardsViewModel extends AndroidViewModel {

    private final RewardsRepository repository;
    private final LiveData<List<BadgeItem>> badges;

    public RewardsViewModel(@NonNull Application application) {
        super(application);
        repository = new RewardsRepository(application);
        badges = repository.getBadges();
    }

    public LiveData<List<BadgeItem>> getBadges() {
        return badges;
    }
}
