package com.tomaflow.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEdtEmail, mEdtPassword;
    private MaterialButton mBtnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Auth gate — skip login if already authenticated
        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        mEdtEmail    = findViewById(R.id.et_email);
        mEdtPassword = findViewById(R.id.et_password);
        mBtnSignIn   = findViewById(R.id.btn_signin);
        TextView btnCreate = findViewById(R.id.btn_create);
        TextView btnForgot = findViewById(R.id.btn_forgot);

        mBtnSignIn.setOnClickListener(v -> signIn());

        btnCreate.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));

        btnForgot.setOnClickListener(v -> forgotPassword());
    }

    private void signIn() {
        String email = mEdtEmail.getText().toString().trim();
        String pass  = mEdtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) { mEdtEmail.setError("Bắt buộc"); return; }
        if (TextUtils.isEmpty(pass))  { mEdtPassword.setError("Bắt buộc"); return; }

        mBtnSignIn.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(result -> goToMain())
            .addOnFailureListener(e -> {
                mBtnSignIn.setEnabled(true);
                Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            });
    }

    private void forgotPassword() {
        String email = mEdtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEdtEmail.setError("Nhập email để reset mật khẩu");
            return;
        }
        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(v ->
                Toast.makeText(this, "Email reset đã gửi", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
