package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmate.databinding.ActivitySignupBinding;
import com.example.mealmate.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setup click listeners
        binding.btnSignUp.setOnClickListener(v -> handleSignUp());
        binding.btnLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void handleSignUp() {
        // Clear previous errors
        binding.nameInputLayout.setError(null);
        binding.emailInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);
        binding.confirmPasswordInputLayout.setError(null);

        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError("Please enter a valid email address");
            return;
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            binding.passwordInputLayout.setError("Password must be at least 6 characters");
            return;
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.setError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordInputLayout.setError("Passwords do not match");
            return;
        }

        // Show progress
        showProgress(true);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        // Create User object
                        User user = new User(firebaseUser.getUid(), name, email);
                        
                        // Save user details to Realtime Database
                        mDatabase.child("users")
                            .child(firebaseUser.getUid())
                            .setValue(user)
                            .addOnCompleteListener(dbTask -> {
                                showProgress(false);
                                if (dbTask.isSuccessful()) {
                                    // Send email verification
                                    firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Navigate to email verification screen
                                                navigateToEmailVerification(email);
                                            } else {
                                                Toast.makeText(SignUpActivity.this,
                                                    "Failed to send verification email",
                                                    Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                } else {
                                    Toast.makeText(SignUpActivity.this,
                                        "Failed to save user data",
                                        Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    showProgress(false);
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : 
                        "Failed to create account";
                    
                    if (errorMessage != null && errorMessage.contains("email address is already in use")) {
                        binding.emailInputLayout.setError("Email is already registered");
                    } else {
                        Toast.makeText(SignUpActivity.this,
                            errorMessage,
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void navigateToEmailVerification(String email) {
        Intent intent = new Intent(this, EmailVerificationActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSignUp.setEnabled(!show);
        binding.btnLogin.setEnabled(!show);
        binding.nameInput.setEnabled(!show);
        binding.emailInput.setEnabled(!show);
        binding.passwordInput.setEnabled(!show);
        binding.confirmPasswordInput.setEnabled(!show);
    }
} 