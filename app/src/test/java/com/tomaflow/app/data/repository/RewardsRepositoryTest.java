package com.tomaflow.app.data.repository;

import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.tomaflow.app.data.model.DailyTomato;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Unit tests cho {@link RewardsRepository} — chỉ test nhánh local (constructor dùng Room +
 * SharedPreferences, không đụng Firestore). {@code unlockBadge} và {@code syncRewardsFromFirestore}
 * gọi FirebaseAuth/Firestore nên bỏ qua theo nguyên tắc "local logic only".
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class RewardsRepositoryTest {

    private static final String PREF_NAME = "rewards_prefs";

    private RewardsRepository repository;

    private static void ensureFirebaseInitialized(Context ctx) {
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx, new FirebaseOptions.Builder()
                    .setApplicationId("tomaflow-test")
                    .setProjectId("tomaflow-test")
                    .setApiKey("fake-api-key-for-unit-tests")
                    .build());
        }
    }

    @Before
    public void setUp() {
        Application application = ApplicationProvider.getApplicationContext();
        ensureFirebaseInitialized(application);
        application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
        repository = new RewardsRepository(application);
    }

    @Test
    public void constructor_constructsWithoutFirestoreComponent() {
        // Constructor chỉ cần Room + SharedPreferences; không được ném "Firestore component not present".
        assertNotNull(repository);
    }

    @Test
    public void getMonthlyTomatoes_returnsNonNullLiveData() {
        LiveData<List<DailyTomato>> live = repository.getMonthlyTomatoes("2026-06");
        assertNotNull(live);
    }
}
