package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mealmate.adapters.RecipeAdapter;
import com.example.mealmate.databinding.ActivityRecipesBinding;
import com.example.mealmate.models.Recipe;
import com.example.mealmate.models.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ProgressDialog;

public class RecipeActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private ActivityRecipesBinding binding;
    private RecipeAdapter adapter;
    private List<Recipe> recipes;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        // Update the database reference to use user-specific path
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("recipes");
        } else {
            databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        }

        // Initialize RecyclerView
        recipes = new ArrayList<>();
        adapter = new RecipeAdapter(recipes, this);
        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(adapter);

        // Set up FAB click listener
        binding.fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            startActivity(intent);
        });

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Load recipes
        loadRecipes();
    }

    private void loadRecipes() {
        // Show shimmer effect, hide both recycler and empty state
        if (binding.shimmerFrameLayout != null) {
            binding.shimmerFrameLayout.setVisibility(View.VISIBLE);
            binding.shimmerFrameLayout.startShimmer();
        }
        binding.recipesRecyclerView.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        
        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            Log.e("RecipeActivity", "User is not authenticated!");
            stopShimmerAndShowEmptyState();
            Toast.makeText(this, "Please sign in to view recipes", Toast.LENGTH_LONG).show();
            return;
        }

        // Update database reference to ensure we're using the correct path
        String userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("recipes");

        Log.d("RecipeActivity", "Starting to load recipes from path: " + databaseReference.getPath().toString());
        
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("RecipeActivity", "onDataChange triggered");
                Log.d("RecipeActivity", "Number of recipes: " + dataSnapshot.getChildrenCount());
                
                if (!dataSnapshot.exists()) {
                    Log.d("RecipeActivity", "No data exists at this location");
                    stopShimmerAndShowEmptyState();
                    return;
                }

                recipes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d("RecipeActivity", "Processing recipe with key: " + snapshot.getKey());
                        // Log the raw data for debugging
                        Log.d("RecipeActivity", "Raw data: " + snapshot.getValue().toString());
                        
                        Recipe recipe = new Recipe();
                        recipe.setId(snapshot.getKey());
                        
                        // Manually get each field to handle potential type mismatches
                        if (snapshot.child("name").exists()) {
                            recipe.setName(snapshot.child("name").getValue(String.class));
                        }
                        if (snapshot.child("category").exists()) {
                            recipe.setCategory(snapshot.child("category").getValue(String.class));
                        }
                        if (snapshot.child("imageUrl").exists()) {
                            recipe.setImageUrl(snapshot.child("imageUrl").getValue(String.class));
                        }
                        if (snapshot.child("imageBase64").exists()) {
                            recipe.setImageBase64(snapshot.child("imageBase64").getValue(String.class));
                        }
                        if (snapshot.child("instructions").exists()) {
                            recipe.setInstructions(snapshot.child("instructions").getValue(String.class));
                        }
                        
                        // Handle ingredients specially
                        Object ingredientsObj = snapshot.child("ingredients").getValue();
                        if (ingredientsObj instanceof List) {
                            recipe.setIngredients((List<String>) ingredientsObj);
                        } else if (ingredientsObj instanceof String) {
                            recipe.setIngredients(Recipe.parseIngredients((String) ingredientsObj));
                        } else {
                            recipe.setIngredients(new ArrayList<>());
                        }

                        recipes.add(recipe);
                        Log.d("RecipeActivity", "Added recipe: " + recipe.getName() + 
                                              ", Ingredients: " + recipe.getIngredients());
                    } catch (Exception e) {
                        Log.e("RecipeActivity", "Error processing recipe: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                Log.d("RecipeActivity", "Total recipes loaded: " + recipes.size());
                
                // Update UI based on whether we have recipes or not
                if (recipes.isEmpty()) {
                    stopShimmerAndShowEmptyState();
                } else {
                    stopShimmerAndShowRecipes();
                }
                adapter.updateRecipes(recipes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RecipeActivity", "Database error: " + databaseError.getMessage());
                Log.e("RecipeActivity", "Error details: " + databaseError.getDetails());
                stopShimmerAndShowEmptyState();
                Toast.makeText(RecipeActivity.this, 
                    "Error loading recipes: " + databaseError.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void stopShimmerAndShowEmptyState() {
        if (binding.shimmerFrameLayout != null) {
            binding.shimmerFrameLayout.stopShimmer();
            binding.shimmerFrameLayout.setVisibility(View.GONE);
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.recipesRecyclerView.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void stopShimmerAndShowRecipes() {
        if (binding.shimmerFrameLayout != null) {
            binding.shimmerFrameLayout.stopShimmer();
            binding.shimmerFrameLayout.setVisibility(View.GONE);
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.recipesRecyclerView.setVisibility(View.VISIBLE);
        binding.emptyStateContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        if (getIntent().getBooleanExtra("isSelectingForMealPlan", false)) {
            // Get meal plan details from intent
            int categoryPosition = getIntent().getIntExtra("category", 0);
            String selectedDate = getIntent().getStringExtra("selectedDate");
            
            // Create a new Meal object from the recipe
            Meal meal = new Meal(
                recipe.getId(),
                recipe.getName(),
                recipe.getImageUrl(),  // Use the recipe's image URL
                recipe.getInstructions(),
                4, // Default servings
                30, // Default prep time
                30  // Default cook time
            );
            
            // If the recipe has a base64 image but no URL, convert it to a data URI
            if ((meal.getImageUrl() == null || meal.getImageUrl().isEmpty()) && 
                recipe.getImageBase64() != null && !recipe.getImageBase64().isEmpty()) {
                meal.setImageUrl("data:image/jpeg;base64," + recipe.getImageBase64());
            }

            // Save to Realtime Database
            saveMealPlan(meal, categoryPosition, selectedDate);
        } else {
            // Normal recipe detail view
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            startActivity(intent);
        }
    }

    private void saveMealPlan(Meal meal, int categoryPosition, String selectedDate) {
        String userId = auth.getCurrentUser().getUid();
        String category = getCategoryName(categoryPosition);

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding recipe to " + category + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create reference to meal plans in Realtime Database
        DatabaseReference mealPlansRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId)
            .child("meal_plans")
            .child(selectedDate)
            .child(category);

        // Generate a new unique key for this meal
        String mealId = mealPlansRef.push().getKey();
        if (mealId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error generating meal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the meal data
        mealPlansRef.child(mealId)
            .setValue(meal)
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Recipe added to " + category, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error adding meal: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                Log.e("RecipeActivity", "Error adding meal", e);
            });
    }

    private String getCategoryName(int categoryPosition) {
        switch (categoryPosition) {
            case 0: return "breakfast";
            case 1: return "lunch";
            case 2: return "dinner";
            case 3: return "snacks";
            default: return "other";
        }
    }

    // Add button click listener for empty state
    @Override
    protected void onResume() {
        super.onResume();
        binding.btnAddFirstRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // If we're selecting for meal plan, set result to indicate we're going back
        if (getIntent().getBooleanExtra("isSelectingForMealPlan", false)) {
            setResult(RESULT_CANCELED);
        }
        onBackPressed();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        // If we're selecting for meal plan, set result to indicate we're going back
        if (getIntent().getBooleanExtra("isSelectingForMealPlan", false)) {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }
} 