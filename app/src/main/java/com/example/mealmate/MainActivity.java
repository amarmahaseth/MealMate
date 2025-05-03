package com.example.mealmate;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mealmate.databinding.ActivityMainBinding;
import com.example.mealmate.models.Meal;
import com.example.mealmate.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, SensorEventListener {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ValueEventListener todaysMealsListener;
    private DatabaseReference mealPlansRef;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set up sensor manager for shake detection
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Set up click listeners
        setupClickListeners();

        // Update user info and today's meals
        updateUserInfo();
        setupTodaysMealsListener();

        // Apply animation to welcome section
        binding.welcomeSection.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(this);
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);

        // Geotag Stores Card Click Handler
        MaterialCardView storesCard = findViewById(R.id.storesCard);
        storesCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GeotagStoresActivity.class);
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        binding.fabAddRecipe.setOnClickListener(v -> showAddRecipeDialog());

        // Set up card click listeners
        binding.recipesCard.setOnClickListener(v -> navigateToRecipes());
        binding.plannerCard.setOnClickListener(v -> navigateToMealPlanner());
        binding.cardGroceryList.setOnClickListener(v -> navigateToGrocery());
        binding.storesCard.setOnClickListener(v -> navigateToStores());
        binding.shoppingTasksCard.setOnClickListener(v -> navigateToShoppingTasks());

        // Set up meal card click listeners
        binding.breakfastCard.setOnClickListener(v -> navigateToMealDetails("breakfast"));
        binding.lunchCard.setOnClickListener(v -> navigateToMealDetails("lunch"));
        binding.dinnerCard.setOnClickListener(v -> navigateToMealDetails("dinner"));
        binding.btnTrackFood.setOnClickListener(v -> navigateToMealPlanner());

        // Set up profile menu click listener
        binding.profileMenuButton.setOnClickListener(v -> showProfileMenu());
    }

    private void showProfileMenu() {
        PopupMenu popup = new PopupMenu(this, binding.profileMenuButton);
        popup.getMenuInflater().inflate(R.menu.menu_profile, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                navigateToProfile();
                return true;
            } else if (itemId == R.id.action_settings) {
                navigateToSettings();
                return true;
            } else if (itemId == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void logout() {
        // Remove all Firebase listeners
        removeFirebaseListeners();
        
        // Clear any cached data
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.keepSynced(false);
        }
        
        // Sign out from Firebase
        mAuth.signOut();
        
        // Clear all activities and start login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void removeFirebaseListeners() {
        // Remove today's meals listener
        if (todaysMealsListener != null && mealPlansRef != null) {
            mealPlansRef.removeEventListener(todaysMealsListener);
            todaysMealsListener = null;
        }
        
        // Disable offline persistence for references
        if (mealPlansRef != null) {
            mealPlansRef.keepSynced(false);
        }
    }

    private void updateUserInfo() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            
            // Get user data from Firebase Database
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Get first name only
                            String firstName = user.getName().split(" ")[0];
                            binding.userNameText.setText("Welcome back, " + firstName);
                        } else {
                            binding.userNameText.setText("Welcome back");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this,
                            "Failed to load user data",
                            Toast.LENGTH_SHORT).show();
                        binding.userNameText.setText("Welcome back");
                    }
                });
        } else {
            binding.userNameText.setText("Welcome back");
        }
    }

    private void setupTodaysMealsListener() {
        if (mAuth.getCurrentUser() == null) return;

        // Show shimmer effect
        binding.mealsShimmerLayout.startShimmer();
        binding.mealCardsViewSwitcher.setDisplayedChild(0); // Show shimmer view

        String userId = mAuth.getCurrentUser().getUid();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        
        mealPlansRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("meal_plans")
                .child(today);

        // Remove any existing listener
        if (todaysMealsListener != null) {
            mealPlansRef.removeEventListener(todaysMealsListener);
        }

        // Create and attach new listener
        todaysMealsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasMeals = false;
                
                // Check if any meals exist
                for (String mealType : new String[]{"breakfast", "lunch", "dinner", "snacks"}) {
                    if (dataSnapshot.child(mealType).exists() && dataSnapshot.child(mealType).hasChildren()) {
                        hasMeals = true;
                        break;
                    }
                }

                // Show/hide the meals container based on whether meals exist
                binding.todaysMealsContainer.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

                // Update meal cards
                updateMealCard("breakfast", dataSnapshot.child("breakfast"));
                updateMealCard("lunch", dataSnapshot.child("lunch"));
                updateMealCard("dinner", dataSnapshot.child("dinner"));
                updateMealCard("snacks", dataSnapshot.child("snacks"));

                // Hide shimmer effect
                binding.mealsShimmerLayout.stopShimmer();
                binding.mealCardsViewSwitcher.setDisplayedChild(1); // Show actual content
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error fetching today's meals: " + databaseError.getMessage());
                // Hide shimmer effect on error
                binding.mealsShimmerLayout.stopShimmer();
                binding.mealCardsViewSwitcher.setDisplayedChild(1); // Show actual content
            }
        };
        
        mealPlansRef.keepSynced(true);
        mealPlansRef.addValueEventListener(todaysMealsListener);
    }

    private void updateMealCard(String mealType, DataSnapshot mealSnapshot) {
        if (!mealSnapshot.exists() || !mealSnapshot.hasChildren()) {
            // Set placeholder image if no meal is planned
            ImageView mealImage = getMealImageView(mealType);
            if (mealImage != null) {
                Glide.with(MainActivity.this)
                    .load(R.drawable.placeholder_meal)
                    .into(mealImage);
            }
            return;
        }

        // Get the first meal for this type
        DataSnapshot firstMeal = mealSnapshot.getChildren().iterator().next();
        Meal meal = firstMeal.getValue(Meal.class);
        if (meal == null) return;

        // Get the meal image view
        ImageView mealImage = getMealImageView(mealType);
        if (mealImage == null) return;

        // If meal has an image URL, load it directly
        if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
            Glide.with(MainActivity.this)
                .load(meal.getImageUrl())
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.placeholder_meal)
                .into(mealImage);
            return;
        }

        // If no image URL but has recipeId, try to fetch from recipe
        if (meal.getRecipeId() != null && !meal.getRecipeId().isEmpty()) {
            String userId = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId)
                .child("recipes")
                .child(meal.getRecipeId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_meal)
                                .error(R.drawable.placeholder_meal)
                                .into(mealImage);
                            return;
                        }

                        // Try to get base64 image if URL is not available
                        String imageBase64 = snapshot.child("imageBase64").getValue(String.class);
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            String dataUrl = "data:image/jpeg;base64," + imageBase64;
                            Glide.with(MainActivity.this)
                                .load(dataUrl)
                                .placeholder(R.drawable.placeholder_meal)
                                .error(R.drawable.placeholder_meal)
                                .into(mealImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity", "Error fetching recipe details: " + error.getMessage());
                    }
                });
        }
    }

    private ImageView getMealImageView(String mealType) {
        switch (mealType) {
            case "breakfast":
                return binding.breakfastImage;
            case "lunch":
                return binding.lunchImage;
            case "dinner":
                return binding.dinnerImage;
            case "snacks":
                return binding.snacksImage;
            default:
                return null;
        }
    }

    private void showAddRecipeDialog() {
        // Navigate directly to RecipeActivity
        Intent intent = new Intent(this, RecipeActivity.class);
        startActivity(intent);
    }

    private void navigateToRecipes() {
        startActivity(new Intent(this, RecipeActivity.class));
    }

    private void navigateToMealPlanner() {
        startActivity(new Intent(this, MealPlannerActivity.class));
    }

    private void navigateToGrocery() {
        startActivity(new Intent(this, GroceryListActivity.class));
    }

    private void navigateToStores() {
        startActivity(new Intent(this, GeotagStoresActivity.class));
    }

    private void navigateToShoppingTasks() {
        Intent intent = new Intent(this, ShoppingTasksActivity.class);
        startActivity(intent);
    }

    private void navigateToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void navigateToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void navigateToMealDetails(String mealType) {
        Intent intent = new Intent(this, MealPlannerActivity.class);
        intent.putExtra("meal_type", mealType);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            // Already on home, no need to do anything
            return true;
        } else if (itemId == R.id.navigation_recipes) {
            navigateToRecipes();
            return true;
        } else if (itemId == R.id.navigation_grocery) {
            navigateToGrocery();
            return true;
        } else if (itemId == R.id.navigation_planner) {
            navigateToMealPlanner();
            return true;
        } else if (itemId == R.id.navigation_settings) {
            navigateToSettings();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info when activity resumes
        updateUserInfo();
        setupTodaysMealsListener();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Set home as selected without triggering navigation
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null && binding.mealsShimmerLayout != null) {
            binding.mealsShimmerLayout.stopShimmer();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeFirebaseListeners();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        binding = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 1000) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
                if (acceleration > SHAKE_THRESHOLD) {
                    lastShakeTime = currentTime;
                    // TODO: Implement refresh grocery list
                    Toast.makeText(this, "Refreshing grocery list...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}