package com.tomaflow.app.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.UserProfile;
import com.tomaflow.app.data.repository.ProfileRepository;
import com.tomaflow.app.databinding.ActivityEditProfileBinding;
import com.tomaflow.app.utils.TomaToast;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    private ActivityEditProfileBinding binding;
    private ProfileRepository repository;
    private FirebaseUser user;
    
    private Uri selectedImageUri = null;
    private String currentAvatarUrl = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivAvatar.setVisibility(View.VISIBLE);
                    binding.tvAvatarInitials.setVisibility(View.GONE);
                    Glide.with(this).load(uri).circleCrop().into(binding.ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        repository = new ProfileRepository(user.getUid());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.etDob.setOnClickListener(v -> showDatePicker());

        binding.btnChangePassword.setOnClickListener(v -> {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(aVoid -> TomaToast.show(this, "Password reset email sent!", true))
                .addOnFailureListener(e -> TomaToast.show(this, "Failed to send reset email: " + e.getMessage(), false));
        });

        binding.btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        // Fallback from auth
        binding.etEmail.setText(user.getEmail());
        String name = user.getDisplayName();
        if (name != null) binding.etName.setText(name);

        repository.getProfile().observe(this, profile -> {
            if (profile != null) {
                if (profile.name != null) binding.etName.setText(profile.name);
                if (profile.username != null) binding.etUsername.setText(profile.username);
                if (profile.phone != null) binding.etPhone.setText(profile.phone);
                if (profile.dob != null) binding.etDob.setText(profile.dob);
                currentAvatarUrl = profile.avatarUrl;

                if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty() && selectedImageUri == null) {
                    binding.ivAvatar.setVisibility(View.VISIBLE);
                    binding.tvAvatarInitials.setVisibility(View.GONE);
                    com.tomaflow.app.utils.AvatarHelper.loadAvatar(this, currentAvatarUrl, binding.ivAvatar);
                }
            } else {
                // Generate stable username if null
                if (binding.etUsername.getText().toString().isEmpty()) {
                    String generated = "user_" + user.getUid().substring(0, 6).toLowerCase();
                    binding.etUsername.setText(generated);
                }
            }

            // Set initials if no avatar
            if ((currentAvatarUrl == null || currentAvatarUrl.isEmpty()) && selectedImageUri == null) {
                String displayName = binding.etName.getText().toString();
                if (displayName.isEmpty()) displayName = binding.etEmail.getText().toString();
                String initials = "";
                String[] parts = displayName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
                binding.tvAvatarInitials.setText(initials.toUpperCase());
                binding.tvAvatarInitials.setVisibility(View.VISIBLE);
                binding.ivAvatar.setVisibility(View.GONE);
            }

            // Handle VIP status
            com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(this);
            boolean isVip = (profile != null && profile.isVip) || sm.isVip();
            View btnCancelVip = binding.btnCancelVip;
            if (isVip) {
                btnCancelVip.setVisibility(View.VISIBLE);
                btnCancelVip.setOnClickListener(v -> {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.premium_cancel_title)
                        .setMessage(R.string.premium_cancel_msg)
                        .setPositiveButton(R.string.premium_cancel_confirm, (dialog, which) -> {
                            repository.updateVipStatus(false);
                            sm.setVip(false);
                            TomaToast.show(this, "VIP cancelled", false);
                            btnCancelVip.setVisibility(View.GONE);
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
                });
            } else {
                btnCancelVip.setVisibility(View.GONE);
            }
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            binding.etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            TomaToast.show(this, "Name, Username and Email are required", false);
            return;
        }

        binding.btnSave.setEnabled(false);

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
                binding.btnSave.setEnabled(true);
                return;
            }

            repository.isPhoneUnique(phone).addOnSuccessListener(isUniquePhone -> {
                if (!isUniquePhone) {
                    TomaToast.show(this, "Phone number already used", false);
                    binding.btnSave.setEnabled(true);
                    return;
                }

                // Upload avatar if selected
                if (selectedImageUri != null) {
                    String base64Avatar = encodeImageToBase64(selectedImageUri);
                    if (base64Avatar != null) {
                        saveToFirestore(name, username, email, phone, dob, base64Avatar);
                    } else {
                        TomaToast.show(this, "Failed to process image", false);
                        binding.btnSave.setEnabled(true);
                    }
                } else {
                    saveToFirestore(name, username, email, phone, dob, currentAvatarUrl);
                }
            });
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            java.io.InputStream imageStream = getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap selectedImage = android.graphics.BitmapFactory.decodeStream(imageStream);
            
            // Resize to max 250x250
            int MAX_SIZE = 250;
            int width = selectedImage.getWidth();
            int height = selectedImage.getHeight();
            float ratio = Math.min((float) MAX_SIZE / width, (float) MAX_SIZE / height);
            int newWidth = Math.round(ratio * width);
            int newHeight = Math.round(ratio * height);
            
            android.graphics.Bitmap resizedBitmap = android.graphics.Bitmap.createScaledBitmap(selectedImage, newWidth, newHeight, false);
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] b = baos.toByteArray();
            return "data:image/jpeg;base64," + android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveToFirestore(String name, String username, String email, String phone, String dob, String avatarUrl) {
        UserProfile profile = new UserProfile(user.getUid(), email, phone, username, name, dob, avatarUrl);
        repository.saveProfile(profile).addOnSuccessListener(aVoid -> {
            TomaToast.show(this, "Profile updated successfully!", true);
            finish();
        }).addOnFailureListener(e -> {
            TomaToast.show(this, "Error saving profile", false);
            binding.btnSave.setEnabled(true);
        });
    }
}

