package com.tomaflow.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.tomaflow.app.data.repository.TaskRepository;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tomaflow.app.MainActivity;
import com.tomaflow.app.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText mEdtEmail, mEdtPassword;
    private MaterialButton mBtnSignIn, mBtnGoogle;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        // Auth gate — skip login if already authenticated
        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        mEdtEmail    = findViewById(R.id.et_email);
        mEdtPassword = findViewById(R.id.et_password);
        mBtnSignIn   = findViewById(R.id.btn_signin);
        mBtnGoogle   = findViewById(R.id.btn_google);
        TextView btnCreate = findViewById(R.id.btn_create);
        TextView btnForgot = findViewById(R.id.btn_forgot);

        mBtnSignIn.setOnClickListener(v -> signIn());
        mBtnGoogle.setOnClickListener(v -> signInWithGoogle());

        btnCreate.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));

        btnForgot.setOnClickListener(v -> forgotPassword());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
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
        // Kéo task từ Firestore về Room sau khi user đã đăng nhập.
        TaskRepository taskRepository = new TaskRepository(getApplication());
        taskRepository.syncTasksFromFirestore();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
