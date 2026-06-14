package com.tomaflow.app.ui.rewards;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.repository.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class RewardsViewModel extends AndroidViewModel {

    private final LiveData<List<BadgeItem>> mBadges;

    public RewardsViewModel(@NonNull Application application) {
        super(application);
        SessionRepository repository = new SessionRepository(application);

        // Chuyển đổi dữ liệu từ danh sách các session thành danh sách các BadgeItem
        mBadges = Transformations.map(repository.getAllSessions(), sessions -> {
            int completedCount = 0;
            if (sessions != null) {
                for (SessionEntity session : sessions) {
                    if ("Completed".equals(session.status)) {
                        completedCount++;
                    }
                }
            }

            List<BadgeItem> list = new ArrayList<>();
            // Badge 1: First Step
            BadgeItem b1 = new BadgeItem("b1", "First Step", "Complete 1 session", R.drawable.ic_flag);
            b1.setUnlocked(completedCount >= 1);
            list.add(b1);

            // Badge 2: Night Owl / Moon
            BadgeItem b2 = new BadgeItem("b2", "Night Owl", "Complete 5 sessions", R.drawable.ic_moon);
            b2.setUnlocked(completedCount >= 5);
            list.add(b2);

            // Badge 3: Flow State
            BadgeItem b3 = new BadgeItem("b3", "Flow State", "Complete 10 sessions", R.drawable.ic_waves);
            b3.setUnlocked(completedCount >= 10);
            list.add(b3);

            // Badge 4: Zen Master
            BadgeItem b4 = new BadgeItem("b4", "Zen Master", "Complete 20 sessions", R.drawable.ic_zen);
            b4.setUnlocked(completedCount >= 20);
            list.add(b4);

            // Badge 5: Sun / Early Bird
            BadgeItem b5 = new BadgeItem("b5", "Early Bird", "Complete 50 sessions", R.drawable.ic_sun);
            b5.setUnlocked(completedCount >= 50);
            list.add(b5);

            return list;
        });
    }

    public LiveData<List<BadgeItem>> getBadges() {
        return mBadges;
    }
}
