package com.tomaflow.app.ui.premium;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tomaflow.app.R;
import com.tomaflow.app.data.repository.SubscriptionManager;

public class PremiumActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_upgrade).setOnClickListener(v -> {
            SubscriptionManager sm = new SubscriptionManager(this);
            sm.setVip(true);
            com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.premium_success));
            finish();
        });
        
        findViewById(R.id.btn_restore).setOnClickListener(v -> {
            SubscriptionManager sm = new SubscriptionManager(this);
            sm.setVip(true);
            com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.premium_success));
            finish();
        });
    }
}

