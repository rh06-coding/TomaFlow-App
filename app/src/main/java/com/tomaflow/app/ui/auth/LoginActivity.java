package com.tomaflow.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton btnSignIn = findViewById(R.id.btn_signin);
        TextView btnCreate = findViewById(R.id.btn_create);
        TextView btnForgot = findViewById(R.id.btn_forgot);

        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnForgot.setOnClickListener(v -> {
            // Placeholder for forgot password
        });
    }
}
