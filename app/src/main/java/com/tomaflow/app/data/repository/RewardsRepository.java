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
import com.tomaflow.app.data.model.DailyTomato;

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
     * Get the live list of monthly tomatoes.
     */
    public LiveData<List<DailyTomato>> getMonthlyTomatoes(String yearMonth) {
        MediatorLiveData<List<DailyTomato>> farmLiveData = new MediatorLiveData<>();

        LiveData<List<SessionDao.DailyTomatoRow>> rowsLive = sessionDao.getMonthlyTomatoes(yearMonth);

        farmLiveData.addSource(rowsLive, rows -> {
            List<DailyTomato> tomatoes = new ArrayList<>();
            if (rows != null) {
                for (SessionDao.DailyTomatoRow row : rows) {
                    tomatoes.add(new DailyTomato(row.dateStr, row.totalMinutes));
                }
            }
            farmLiveData.setValue(tomatoes);
        });

        return farmLiveData;
    }
    // Badge generation logic removed.

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
                com.tomaflow.app.utils.TomaFlowLog.d("FirestoreSync", "Synced badges from Firestore to Local: " + unlockedBadges.size());
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("FirestoreSync", "Sync badges from Firestore failed", e);
            }
        });
    }
}
