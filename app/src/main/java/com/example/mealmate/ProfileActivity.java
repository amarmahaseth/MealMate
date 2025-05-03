package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmate.databinding.ActivityProfileBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Setup navigation
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Load user data
        loadUserData();

        // Setup save button
        binding.btnSave.setOnClickListener(v -> saveUserData());

        // Setup change password button
        binding.btnChangePassword.setOnClickListener(v -> {
            // TODO: Implement change password functionality
            Toast.makeText(this, "Change password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view your profile", Toast.LENGTH_LONG).show();
            return;
        }

        // Show progress indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        // Get current user email
        String email = mAuth.getCurrentUser().getEmail();
        if (email != null) {
            binding.emailText.setText(email);
        }

        // Load user data from Firebase
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Hide progress indicator
                binding.progressBar.setVisibility(View.GONE);

                // Get user data
                String name = dataSnapshot.child("name").getValue(String.class);
                if (name != null) {
                    binding.nameText.setText(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Hide progress indicator
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to save your profile", Toast.LENGTH_LONG).show();
            return;
        }

        String name = binding.nameText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        // Save user data to Firebase
        userRef.child("name").setValue(name)
            .addOnSuccessListener(aVoid -> {
                // Hide progress indicator
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Hide progress indicator
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
