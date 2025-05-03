package com.example.mealmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmate.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set click listeners
        binding.btnResetPassword.setOnClickListener(v -> sendResetLink());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendResetLink() {
        String email = binding.etEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            return;
        }

        // Show loading state
        binding.btnResetPassword.setEnabled(false);
        binding.btnResetPassword.setText("Sending...");

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                    // Reset button state
                    binding.btnResetPassword.setEnabled(true);
                    binding.btnResetPassword.setText("Send Reset Link");
                });
    }
} 