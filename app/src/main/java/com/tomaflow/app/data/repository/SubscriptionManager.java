package com.tomaflow.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionManager {

    private static final String PREF_NAME = "tomaflow_subscription";
    private static final String KEY_IS_VIP = "is_vip";

    private final SharedPreferences mPrefs;

    public SubscriptionManager(Context context) {
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isVip() {
        return mPrefs.getBoolean(KEY_IS_VIP, false);
    }

    public void setVip(boolean isVip) {
        mPrefs.edit().putBoolean(KEY_IS_VIP, isVip).apply();
    }
}
