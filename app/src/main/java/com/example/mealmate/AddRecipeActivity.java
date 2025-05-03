package com.example.mealmate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmate.databinding.ActivityAddRecipeBinding;
import com.example.mealmate.models.Recipe;
import com.example.mealmate.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddRecipeActivity extends AppCompatActivity {
    private ActivityAddRecipeBinding binding;
    private ActivityResultLauncher<Intent> capturePhotoLauncher;
    private ActivityResultLauncher<Intent> selectImageLauncher;
    private Bitmap selectedBitmap;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String recipeId;
    private boolean isUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to add recipes", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Update database reference to use user-specific path
        String userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("recipes");

        // Check if we're updating an existing recipe
        isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        if (isUpdate) {
            recipeId = getIntent().getStringExtra("recipeId");
            setupUpdateMode();
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isUpdate ? "Update Recipe" : "Add Recipe");
        }

        // Button listeners
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnSubmitRecipe.setText(isUpdate ? "Update Recipe" : "Save Recipe");
        binding.btnSubmitRecipe.setOnClickListener(v -> submitRecipe());
        binding.btnCapturePhoto.setOnClickListener(v -> capturePhoto());
        binding.btnSelectImage.setOnClickListener(v -> selectImageFromGallery());

        // Capture Photo Launcher
        capturePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        if (photo != null) {
                            selectedBitmap = photo;
                            binding.imageView.setImageBitmap(photo);
                        }
                    }
                }
            }
        );

        // Select Image from Gallery Launcher
        selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        binding.imageView.setImageBitmap(selectedBitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void setupUpdateMode() {
        // Fill in the existing recipe details
        binding.etRecipeName.setText(getIntent().getStringExtra("name"));
        binding.etIngredients.setText(getIntent().getStringExtra("ingredients")
            .replace("• ", "")  // Remove bullet points
            .trim());          // Remove extra whitespace
        binding.etInstructions.setText(getIntent().getStringExtra("instructions"));

        // Load existing image
        String imageBase64 = getIntent().getStringExtra("imageBase64");
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            selectedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            binding.imageView.setImageBitmap(selectedBitmap);
        }
    }

    private void submitRecipe() {
        String recipeName = binding.etRecipeName.getText().toString().trim();
        String ingredients = binding.etIngredients.getText().toString().trim();
        String instructions = binding.etInstructions.getText().toString().trim();

        // Validate inputs
        if (recipeName.isEmpty()) {
            binding.etRecipeName.setError("Recipe name is required");
            return;
        }
        if (ingredients.isEmpty()) {
            binding.etIngredients.setError("Ingredients are required");
            return;
        }
        if (instructions.isEmpty()) {
            binding.etInstructions.setError("Instructions are required");
            return;
        }
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Bitmap to Base64 in background
        new Thread(() -> {
            String imageBase64 = encodeImageToBase64(selectedBitmap);
            
            // Switch back to main thread to save to Firebase
            runOnUiThread(() -> saveRecipeToFirebase(recipeName, ingredients, instructions, imageBase64));
        }).start();
    }

    private void saveRecipeToFirebase(String name, String ingredients, String instructions, String imageBase64) {
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        List<String> ingredientsList = Recipe.parseIngredients(ingredients);
        recipe.put("ingredients", ingredientsList);
        recipe.put("instructions", instructions);
        recipe.put("imageBase64", imageBase64);

        DatabaseReference recipeRef;
        if (isUpdate && recipeId != null) {
            recipeRef = databaseReference.child(recipeId);
        } else {
            recipeRef = databaseReference.push();
        }

        recipeRef.setValue(recipe)
            .addOnSuccessListener(aVoid -> {
                // After saving the recipe, add ingredients to grocery list
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference groceryListRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("grocery_list");

                // Get current grocery list to avoid duplicates
                groceryListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot grocerySnapshot) {
                        // Create a set of existing item names (case-insensitive)
                        Set<String> existingItems = new HashSet<>();
                        for (DataSnapshot itemSnapshot : grocerySnapshot.getChildren()) {
                            GroceryItem item = itemSnapshot.getValue(GroceryItem.class);
                            if (item != null) {
                                existingItems.add(item.getName().toLowerCase().trim());
                            }
                        }

                        // Add new ingredients to grocery list
                        int newItemsCount = 0;
                        for (String ingredient : ingredientsList) {
                            ingredient = ingredient.trim();
                            // Only add if not already in the list
                            if (!existingItems.contains(ingredient.toLowerCase())) {
                                String category = GroceryItem.categorizeIngredient(ingredient);
                                GroceryItem newItem = new GroceryItem(null, ingredient, "", category);
                                groceryListRef.push().setValue(newItem);
                                newItemsCount++;
                            }
                        }

                        // Show success message
                        String message = isUpdate ? "Recipe updated successfully" : "Recipe saved successfully";
                        if (newItemsCount > 0) {
                            message += "\nAdded " + newItemsCount + " ingredients to grocery list";
                        }
                        Toast.makeText(AddRecipeActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        String message = isUpdate ? "Recipe updated successfully" : "Recipe saved successfully";
                        message += "\nFailed to add ingredients to grocery list";
                        Toast.makeText(AddRecipeActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            })
            .addOnFailureListener(e -> {
                String message = isUpdate ? "Failed to update recipe" : "Failed to save recipe";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.e("AddRecipeActivity", "Error: " + e.getMessage());
            });
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        // First resize the bitmap if it's too large
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 800); // 800px max dimension
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Use JPEG format with 70% quality for better compression
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        
        if (bitmap != resizedBitmap) {
            resizedBitmap.recycle(); // Clean up the resized bitmap
        }
        
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // If the bitmap is already smaller than maxSize, return it as is
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

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

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            capturePhotoLauncher.launch(takePictureIntent);
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }
}
