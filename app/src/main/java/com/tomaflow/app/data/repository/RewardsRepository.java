package com.tomaflow.app.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.tomaflow.app.R;
import com.tomaflow.app.data.db.TomaFlowDatabase;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.model.BadgeItem;

import java.util.ArrayList;
import java.util.List;

public class RewardsRepository {

    private final SessionDao sessionDao;
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "rewards_prefs";

    public RewardsRepository(Application application) {
        TomaFlowDatabase db = TomaFlowDatabase.getInstance(application);
        this.sessionDao = db.sessionDao();
        this.prefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get the live list of badges.
     * Evaluates dynamically based on total completed sessions and SharedPreferences.
     */
    public LiveData<List<BadgeItem>> getBadges() {
        MediatorLiveData<List<BadgeItem>> badgesLiveData = new MediatorLiveData<>();

        LiveData<Integer> totalSessionsLive = sessionDao.getTotalCompletedSessions();

        badgesLiveData.addSource(totalSessionsLive, totalSessions -> {
            List<BadgeItem> badges = generateBadges(totalSessions != null ? totalSessions : 0);
            badgesLiveData.setValue(badges);
        });

        return badgesLiveData;
    }

    private List<BadgeItem> generateBadges(int totalSessions) {
        List<BadgeItem> list = new ArrayList<>();

        BadgeItem b1 = new BadgeItem("newbie", R.string.badge_newbie_title, R.string.badge_newbie_desc, R.drawable.ic_toma_sprout);
        b1.setUnlocked(totalSessions >= 1);
        list.add(b1);

        BadgeItem b2 = new BadgeItem("hardworker", R.string.badge_hardworker_title, R.string.badge_hardworker_desc, R.drawable.ic_sparkle);
        b2.setUnlocked(totalSessions >= 10);
        list.add(b2);

        BadgeItem b3 = new BadgeItem("superman", R.string.badge_superman_title, R.string.badge_superman_desc, R.drawable.ic_star);
        b3.setUnlocked(totalSessions >= 50);
        list.add(b3);

        BadgeItem b4 = new BadgeItem("consistent", R.string.badge_consistent_title, R.string.badge_consistent_desc, R.drawable.ic_stats);
        b4.setUnlocked(prefs.getBoolean("badge_consistent", false));
        list.add(b4);

        BadgeItem b5 = new BadgeItem("marathon", R.string.badge_marathon_title, R.string.badge_marathon_desc, R.drawable.ic_focus);
        b5.setUnlocked(prefs.getBoolean("badge_marathon", false));
        list.add(b5);

        BadgeItem b6 = new BadgeItem("perfect", R.string.badge_perfect_title, R.string.badge_perfect_desc, R.drawable.ic_check);
        b6.setUnlocked(prefs.getBoolean("badge_perfect", false));
        list.add(b6);

        BadgeItem b7 = new BadgeItem("nightowl", R.string.badge_nightowl_title, R.string.badge_nightowl_desc, R.drawable.ic_bed);
        b7.setUnlocked(prefs.getBoolean("badge_nightowl", false));
        list.add(b7);

        BadgeItem b8 = new BadgeItem("earlybird", R.string.badge_earlybird_title, R.string.badge_earlybird_desc, R.drawable.ic_sun);
        b8.setUnlocked(prefs.getBoolean("badge_earlybird", false));
        list.add(b8);

        return list;
    }

    /**
     * Hàm tiện ích để mở khoá một badge được lưu trong SharedPreferences (VD: gọi khi timer kết thúc)
     * Đồng thời đẩy dữ liệu lên Firestore.
     */
    public void unlockBadge(String badgeKey) {
        prefs.edit().putBoolean("badge_" + badgeKey, true).apply();

        // Đồng bộ lên Firestore
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            new com.tomaflow.app.data.remote.FirestoreRewardsRemoteDataSource().updateBadge(user.getUid(), badgeKey, true);
        }
    }

    /**
     * Kéo trạng thái huy hiệu từ Firestore về SharedPreferences (gọi khi đăng nhập)
     */
    public void syncRewardsFromFirestore() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new com.tomaflow.app.data.remote.FirestoreRewardsRemoteDataSource().fetchBadges(user.getUid(), new com.tomaflow.app.data.remote.FirestoreRewardsRemoteDataSource.RewardsFetchCallback() {
            @Override
            public void onSuccess(java.util.Map<String, Boolean> unlockedBadges) {
                SharedPreferences.Editor editor = prefs.edit();
                for (java.util.Map.Entry<String, Boolean> entry : unlockedBadges.entrySet()) {
                    editor.putBoolean("badge_" + entry.getKey(), entry.getValue());
                }
                editor.apply();
                android.util.Log.d("FirestoreSync", "Synced badges from Firestore to Local: " + unlockedBadges.size());
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("FirestoreSync", "Sync badges from Firestore failed", e);
            }
        });
    }
}
