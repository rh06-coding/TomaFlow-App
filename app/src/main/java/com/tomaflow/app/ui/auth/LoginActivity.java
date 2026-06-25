package com.tomaflow.app.ui.auth;

import android.content.Context;
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
import com.tomaflow.app.utils.LanguageManager;

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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

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
                        com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.auth_google_failed), false);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();


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

        setupLanguageToggle();
    }

    private void setupLanguageToggle() {
        TextView tvLangToggle = findViewById(R.id.tv_lang_toggle);
        if (tvLangToggle == null) return;
        
        String currentLang = LanguageManager.getSavedLanguage(this);
        tvLangToggle.setText(LanguageManager.LANG_VI.equals(currentLang) ? "EN" : "VI");

        tvLangToggle.setOnClickListener(v -> {
            String newLang = LanguageManager.LANG_VI.equals(currentLang) 
                    ? LanguageManager.LANG_EN : LanguageManager.LANG_VI;
            LanguageManager.setLanguage(this, newLang);
            recreate();
        });
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
                        String uid = mAuth.getCurrentUser().getUid();
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(snapshotTask -> {
                            if (snapshotTask.isSuccessful() && snapshotTask.getResult() != null && !snapshotTask.getResult().exists()) {
                                String email = mAuth.getCurrentUser().getEmail();
                                String name = mAuth.getCurrentUser().getDisplayName();
                                String generatedUsername = "user_" + uid.substring(0, 6).toLowerCase();
                                com.tomaflow.app.data.model.UserProfile userProfile = new com.tomaflow.app.data.model.UserProfile(
                                        uid, email, "", generatedUsername, name, "", "");
                                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).set(userProfile);
                            }
                            goToMain();
                        });
                    } else {
                        com.tomaflow.app.utils.TomaToast.show(LoginActivity.this, getString(R.string.auth_login_failed), false);
                    }
                });
    }

    private void signIn() {
        String email = mEdtEmail.getText().toString().trim();
        String pass  = mEdtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) { mEdtEmail.setError(getString(R.string.error_required)); return; }
        if (TextUtils.isEmpty(pass))  { mEdtPassword.setError(getString(R.string.error_required)); return; }

        mBtnSignIn.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(result -> goToMain())
            .addOnFailureListener(e -> {
                mBtnSignIn.setEnabled(true);
                com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.auth_login_failed_msg), false);
            });
    }

    private void forgotPassword() {
        String email = mEdtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEdtEmail.setError(getString(R.string.auth_email_required_reset));
            return;
        }
        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(v ->
                com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.auth_reset_email_sent)))
            .addOnFailureListener(e ->
                com.tomaflow.app.utils.TomaToast.show(this, getString(R.string.auth_reset_email_failed), false));
    }

    private void goToMain() {
        // Kéo task từ Firestore về Room sau khi user đã đăng nhập.
        com.tomaflow.app.data.repository.TaskRepository taskRepository = new com.tomaflow.app.data.repository.TaskRepository(getApplication());
        taskRepository.syncTasksFromFirestore();

        // Kéo lịch sử Pomodoro từ Firestore về Room
        com.tomaflow.app.data.repository.SessionRepository sessionRepository = new com.tomaflow.app.data.repository.SessionRepository(getApplication());
        sessionRepository.syncSessionsFromFirestore();

        // Kéo huy hiệu từ Firestore về SharedPreferences
        com.tomaflow.app.data.repository.RewardsRepository rewardsRepository = new com.tomaflow.app.data.repository.RewardsRepository(getApplication());
        rewardsRepository.syncRewardsFromFirestore();

        // Kéo nhật ký từ Firestore về Room
        com.tomaflow.app.data.repository.NoteRepository noteRepository = new com.tomaflow.app.data.repository.NoteRepository(getApplication());
        noteRepository.syncNotesFromFirestore();

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            android.content.SharedPreferences themePrefs = getSharedPreferences("user_theme_prefs", MODE_PRIVATE);
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    // Only trust/refresh the cache when the Firestore fetch actually
                    // succeeded. Previously a failed/offline fetch fell through to
                    // isDark=false and overwrote the cached pref, snapping the user
                    // to light mode and clobbering their last known theme.
                    boolean fetched = task.isSuccessful() && task.getResult() != null && task.getResult().exists();
                    boolean isDark;
                    if (fetched) {
                        Boolean dark = task.getResult().getBoolean("isDarkMode");
                        isDark = dark != null && dark;
                        themePrefs.edit()
                                .putBoolean("dark_" + uid, isDark)
                                .putBoolean("last_dark", isDark).apply();
                    } else {
                        // Offline/error: keep the last known theme instead of forcing light.
                        isDark = themePrefs.getBoolean("dark_" + uid, themePrefs.getBoolean("last_dark", false));
                    }
                    new com.tomaflow.app.timer.SettingsManager(this).setDarkMode(isDark);
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    navigateToMain();
                });
        } else {
            navigateToMain();
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(this, com.tomaflow.app.MainActivity.class));
        finish();
    }
}
