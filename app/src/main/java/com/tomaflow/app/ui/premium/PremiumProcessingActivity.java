package com.tomaflow.app.ui.premium;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tomaflow.app.R;
import com.tomaflow.app.data.repository.ProfileRepository;
import com.tomaflow.app.data.repository.SubscriptionManager;

public class PremiumProcessingActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    private TextView tvStatus;
    private TextView tvSubStatus;
    private ProgressBar progressBar;
    private ImageView ivIcon;
    private ImageView ivSuccess;

    private int step = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_processing);

        tvStatus = findViewById(R.id.tv_status);
        tvSubStatus = findViewById(R.id.tv_sub_status);
        progressBar = findViewById(R.id.progress_bar);
        ivIcon = findViewById(R.id.iv_icon);
        ivSuccess = findViewById(R.id.iv_success);

        startFakePaymentFlow();
    }

    private void startFakePaymentFlow() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                step++;
                switch (step) {
                    case 1:
                        tvStatus.setText("Processing Payment...");
                        handler.postDelayed(this, 1500);
                        break;
                    case 2:
                        tvStatus.setText("Verifying Purchase...");
                        handler.postDelayed(this, 1500);
                        break;
                    case 3:
                        completePurchase();
                        break;
                }
            }
        }, 1500);
    }

    private void completePurchase() {
        tvStatus.setText("Success!");
        tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.toma_success));
        tvSubStatus.setText("You are now a Premium member");
        progressBar.setVisibility(View.GONE);
        ivIcon.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.VISIBLE);

        // Update Local
        SubscriptionManager sm = new SubscriptionManager(this);
        sm.setVip(true);

        // Update Remote
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ProfileRepository repo = new ProfileRepository(user.getUid());
            repo.updateVipStatus(true);
        }

        com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.premium_success), true);

        handler.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 1500);
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back press while processing
    }
}
