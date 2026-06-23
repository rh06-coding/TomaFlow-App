package com.tomaflow.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import java.util.Objects;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEdtName, mEdtEmail, mEdtPassword, mEdtConfirm;
    private MaterialButton mBtnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auth gate — skip register if already authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);

        mEdtName     = findViewById(R.id.et_name);
        mEdtEmail    = findViewById(R.id.et_email);
        mEdtPassword = findViewById(R.id.et_password);
        mEdtConfirm  = findViewById(R.id.et_confirm_password);
        mBtnCreate   = findViewById(R.id.btn_create);
        TextView btnSignIn = findViewById(R.id.btn_signin);

        mBtnCreate.setOnClickListener(v -> register());

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String name    = mEdtName.getText().toString().trim();
        String email   = mEdtEmail.getText().toString().trim();
        String pass    = mEdtPassword.getText().toString().trim();
        String confirm = mEdtConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(name))  { mEdtName.setError(getString(R.string.error_required)); return; }
        if (TextUtils.isEmpty(email)) { mEdtEmail.setError(getString(R.string.error_required)); return; }
        if (pass.length() < 8)        { mEdtPassword.setError(getString(R.string.error_password_length)); return; }
        if (!Objects.equals(pass, confirm))    { mEdtConfirm.setError(getString(R.string.error_password_match)); return; }

        mBtnCreate.setEnabled(false);
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(result -> {
                if (result.getUser() != null) {
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build();
                    result.getUser().updateProfile(profile);
                }
                // Sign out so user must log in
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, getString(R.string.auth_register_success), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                mBtnCreate.setEnabled(true);
                Toast.makeText(this, getString(R.string.auth_register_failed) + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            });
    }
}
