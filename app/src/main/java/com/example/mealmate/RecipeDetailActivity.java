package com.example.mealmate;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmate.databinding.ActivityRecipeDetailBinding;
import com.example.mealmate.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import android.view.View;

public class RecipeDetailActivity extends AppCompatActivity {
    private ActivityRecipeDetailBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view recipe details", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database reference
        String userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("recipes")
            .child(recipeId);

        // Load recipe details
        loadRecipeDetails();

        // Setup toolbar
        setupToolbar();
    }

    private void loadRecipeDetails() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(RecipeDetailActivity.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                try {
                    // Manually create Recipe object from snapshot
                    String name = snapshot.child("name").getValue(String.class);
                    String instructions = snapshot.child("instructions").getValue(String.class);
                    String imageBase64 = snapshot.child("imageBase64").getValue(String.class);
                    
                    // Handle ingredients with type checking
                    List<String> ingredientsList = new ArrayList<>();
                    DataSnapshot ingredientsSnapshot = snapshot.child("ingredients");
                    if (ingredientsSnapshot.exists()) {
                        if (ingredientsSnapshot.getValue() instanceof String) {
                            // If ingredients is stored as a single string, parse it
                            String ingredientsStr = ingredientsSnapshot.getValue(String.class);
                            ingredientsList = Recipe.parseIngredients(ingredientsStr);
                        } else {
                            // If ingredients is stored as a list
                            for (DataSnapshot ingredientSnapshot : ingredientsSnapshot.getChildren()) {
                                String ingredient = ingredientSnapshot.getValue(String.class);
                                if (ingredient != null) {
                                    ingredientsList.add(ingredient);
                                }
                            }
                        }
                    }

                    // Update UI
                    binding.recipeName.setText(name);
                    
                    // Display ingredients as a bulleted list
                    StringBuilder ingredientsText = new StringBuilder();
                    for (String ingredient : ingredientsList) {
                        ingredientsText.append("• ").append(ingredient).append("\n");
                    }
                    binding.ingredients.setText(ingredientsText.toString());
                    
                    binding.instructions.setText(instructions);

                    // Load image if available
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        binding.recipeImage.setImageBitmap(bitmap);
                    }

                    // Setup edit button with compressed image
                    binding.btnEditRecipe.setOnClickListener(v -> {
                        Intent intent = new Intent(RecipeDetailActivity.this, AddRecipeActivity.class);
                        intent.putExtra("isUpdate", true);
                        intent.putExtra("recipeId", recipeId);
                        intent.putExtra("name", name);
                        intent.putExtra("ingredients", ingredientsText.toString());
                        intent.putExtra("instructions", instructions);
                        
                        // Compress the image before passing it
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                
                                // Resize the bitmap to a smaller size
                                Bitmap resizedBitmap = getResizedBitmap(bitmap, 800); // 800px max dimension
                                
                                // Convert to Base64 with compression
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                byte[] compressedBytes = baos.toByteArray();
                                String compressedBase64 = Base64.encodeToString(compressedBytes, Base64.DEFAULT);
                                
                                intent.putExtra("imageBase64", compressedBase64);
                            } catch (Exception e) {
                                Log.e("RecipeDetailActivity", "Error compressing image: " + e.getMessage());
                                // If compression fails, don't pass the image
                                Toast.makeText(RecipeDetailActivity.this, 
                                    "Image might need to be re-selected", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        startActivity(intent);
                    });

                    // Setup delete button
                    binding.btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmation());

                } catch (Exception e) {
                    Log.e("RecipeDetailActivity", "Error loading recipe: " + e.getMessage());
                    Toast.makeText(RecipeDetailActivity.this, "Error loading recipe details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailActivity", "Database error: " + error.getMessage());
                Toast.makeText(RecipeDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete", (dialog, which) -> deleteRecipe())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteRecipe() {
        databaseReference.removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                Log.e("RecipeDetailActivity", "Error deleting recipe", e);
            });
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Recipe Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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