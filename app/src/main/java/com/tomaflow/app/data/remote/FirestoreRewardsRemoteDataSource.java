package com.tomaflow.app.data.remote;

import android.util.Log;
import com.tomaflow.app.utils.TomaFlowLog;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Mục đích: Đồng bộ trạng thái Huy hiệu (Rewards) với Firebase Firestore.
 * 
 * Các method quan trọng:
 * - updateBadge: Mở khoá 1 huy hiệu trên cloud (Lưu dưới dạng Map key-value).
 * - fetchBadges: Tải trạng thái các huy hiệu từ cloud về máy.
 */
public class FirestoreRewardsRemoteDataSource {

    private static final String TAG = "FirestoreRewards";
    private final FirebaseFirestore firestore;

    public FirestoreRewardsRemoteDataSource() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface RewardsFetchCallback {
        void onSuccess(Map<String, Boolean> unlockedBadges);
        void onFailure(Exception e);
    }

    public void updateBadge(String userId, String badgeKey, boolean isUnlocked) {
        if (userId == null || userId.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put(badgeKey, isUnlocked);

        firestore.collection("users")
                .document(userId)
                .collection("rewards")
                .document("badges")
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> TomaFlowLog.d(TAG, "Update badge success: " + badgeKey))
                .addOnFailureListener(e -> Log.e(TAG, "Update badge failed: " + badgeKey, e));
    }

    public void fetchBadges(String userId, RewardsFetchCallback callback) {
        if (userId == null || userId.isEmpty()) return;

        firestore.collection("users")
                .document(userId)
                .collection("rewards")
                .document("badges")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Boolean> badges = new HashMap<>();
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        for (Map.Entry<String, Object> entry : documentSnapshot.getData().entrySet()) {
                            if (entry.getValue() instanceof Boolean) {
                                badges.put(entry.getKey(), (Boolean) entry.getValue());
                            }
                        }
                    }
                    TomaFlowLog.d(TAG, "Fetch badges success: " + badges.size());
                    callback.onSuccess(badges);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fetch badges failed", e);
                    callback.onFailure(e);
                });
    }
}
