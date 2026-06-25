package com.tomaflow.app.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class SubscriptionManagerTest {

    private SubscriptionManager manager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        // Clear any leftover VIP flag from previous runs
        context.getSharedPreferences("tomaflow_subscription", Context.MODE_PRIVATE)
                .edit().clear().commit();
        manager = new SubscriptionManager(context);
    }

    @Test
    public void isVip_default_isFalse() {
        assertFalse(manager.isVip());
    }

    @Test
    public void setVip_true_persists() {
        manager.setVip(true);
        assertTrue(manager.isVip());
    }

    @Test
    public void setVip_false_persists() {
        manager.setVip(true);
        manager.setVip(false);
        assertFalse(manager.isVip());
    }

    @Test
    public void setVip_survivesNewInstance() {
        manager.setVip(true);

        // Re-create from the same context — should read the persisted value
        Context context = ApplicationProvider.getApplicationContext();
        SubscriptionManager second = new SubscriptionManager(context);
        assertTrue(second.isVip());
    }

    @Test
    public void setVip_toggleMultipleTimes_reflectsLastValue() {
        manager.setVip(true);
        manager.setVip(false);
        manager.setVip(true);
        assertTrue(manager.isVip());
    }
}
