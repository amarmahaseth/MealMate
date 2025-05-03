package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmate.databinding.ActivityEmailVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private ActivityEmailVerificationBinding binding;
    private FirebaseAuth mAuth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email != null) {
            binding.tvEmail.setText(email);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set click listeners
        binding.btnResend.setOnClickListener(v -> resendVerificationEmail());
        binding.btnContinue.setOnClickListener(v -> checkEmailVerification());
        binding.tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmailVerificationActivity.this, "Failed to send verification email", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Email is verified, navigate to MainActivity
                        startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EmailVerificationActivity.this, "Please verify your email first", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check email verification status when returning to this activity
        checkEmailVerification();
    }
} 