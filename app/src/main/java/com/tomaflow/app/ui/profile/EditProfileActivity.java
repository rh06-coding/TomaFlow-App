package com.tomaflow.app.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.data.repository.ProfileRepository;
import com.tomaflow.app.utils.TomaToast;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvInitials;
    private EditText etName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etDob;

    private ProfileRepository repository;
    private FirebaseUser user;
    
    private Uri selectedImageUri = null;
    private String currentAvatarUrl = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAvatar.setVisibility(View.VISIBLE);
                    tvInitials.setVisibility(View.GONE);
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        repository = new ProfileRepository(user.getUid());

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.iv_avatar);
        tvInitials = findViewById(R.id.tv_avatar_initials);
        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);

        findViewById(R.id.btn_change_avatar).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        etDob.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(aVoid -> TomaToast.show(this, "Password reset email sent!", true))
                .addOnFailureListener(e -> TomaToast.show(this, "Failed to send reset email: " + e.getMessage(), false));
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        // Fallback from auth
        etEmail.setText(user.getEmail());
        String name = user.getDisplayName();
        if (name != null) etName.setText(name);

        repository.getProfile().observe(this, profile -> {
            if (profile != null) {
                if (profile.name != null) etName.setText(profile.name);
                if (profile.username != null) etUsername.setText(profile.username);
                if (profile.phone != null) etPhone.setText(profile.phone);
                if (profile.dob != null) etDob.setText(profile.dob);
                currentAvatarUrl = profile.avatarUrl;
                
                if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty() && selectedImageUri == null) {
                    ivAvatar.setVisibility(View.VISIBLE);
                    tvInitials.setVisibility(View.GONE);
                    Glide.with(this).load(currentAvatarUrl).circleCrop().into(ivAvatar);
                }
            } else {
                // Generate stable username if null
                if (etUsername.getText().toString().isEmpty()) {
                    String generated = "user_" + user.getUid().substring(0, 6).toLowerCase();
                    etUsername.setText(generated);
                }
            }
            
            // Set initials if no avatar
            if ((currentAvatarUrl == null || currentAvatarUrl.isEmpty()) && selectedImageUri == null) {
                String displayName = etName.getText().toString();
                if (displayName.isEmpty()) displayName = etEmail.getText().toString();
                String initials = "";
                String[] parts = displayName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
                tvInitials.setText(initials.toUpperCase());
                tvInitials.setVisibility(View.VISIBLE);
                ivAvatar.setVisibility(View.GONE);
            }
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            TomaToast.show(this, "Name, Username and Email are required", false);
            return;
        }

        findViewById(R.id.btn_save).setEnabled(false);

        // Update Email Auth if changed
        if (!email.equals(user.getEmail())) {
            user.verifyBeforeUpdateEmail(email)
                .addOnFailureListener(e -> {
                    TomaToast.show(this, "Failed to update email: " + e.getMessage(), false);
                });
        }
        
        // Update Display Name Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates);

        // Check uniqueness then save
        repository.isUsernameUnique(username).addOnSuccessListener(isUniqueUsername -> {
            if (!isUniqueUsername) {
                TomaToast.show(this, "Username already taken", false);
                findViewById(R.id.btn_save).setEnabled(true);
                return;
            }
            
            repository.isPhoneUnique(phone).addOnSuccessListener(isUniquePhone -> {
                if (!isUniquePhone) {
                    TomaToast.show(this, "Phone number already used", false);
                    findViewById(R.id.btn_save).setEnabled(true);
                    return;
                }
                
                // Upload avatar if selected
                if (selectedImageUri != null) {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("avatars/" + user.getUid() + ".jpg");
                    storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveToFirestore(name, username, email, phone, dob, uri.toString());
                        });
                    }).addOnFailureListener(e -> {
                        TomaToast.show(this, "Failed to upload avatar", false);
                        saveToFirestore(name, username, email, phone, dob, currentAvatarUrl);
                    });
                } else {
                    saveToFirestore(name, username, email, phone, dob, currentAvatarUrl);
                }
            });
        });
    }

    private void saveToFirestore(String name, String username, String email, String phone, String dob, String avatarUrl) {
        UserProfile profile = new UserProfile(user.getUid(), email, phone, username, name, dob, avatarUrl);
        repository.saveProfile(profile).addOnSuccessListener(aVoid -> {
            TomaToast.show(this, "Profile updated successfully!", true);
            finish();
        }).addOnFailureListener(e -> {
            TomaToast.show(this, "Error saving profile", false);
            findViewById(R.id.btn_save).setEnabled(true);
        });
    }
}
