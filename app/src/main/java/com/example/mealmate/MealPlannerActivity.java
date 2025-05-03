package com.example.mealmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mealmate.adapters.MealCategoryAdapter;
import com.example.mealmate.adapters.WeekViewPagerAdapter;
import com.example.mealmate.databinding.ActivityMealPlannerBinding;
import com.example.mealmate.models.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealPlannerActivity extends AppCompatActivity 
        implements WeekViewPagerAdapter.OnDayClickListener, MealCategoryAdapter.OnMealClickListener {
    private ActivityMealPlannerBinding binding;
    private WeekViewPagerAdapter weekViewAdapter;
    private MealCategoryAdapter mealCategoryAdapter;
    private List<Meal> meals;
    private DatabaseReference mealPlansRef;
    private Calendar selectedDate;
    private ValueEventListener mealsListener;
    private ProgressDialog progressDialog;
    private static final int REQUEST_ADD_MEAL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealPlannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            Drawable navigationIcon = binding.toolbar.getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }

        initializeFirebase();
        initializeViews();
        setupWeekNavigation();
        setupMealCategories();
        setupWeekView();
        loadMealsForSelectedDate();
    }

    private void initializeFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view meal plans", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void initializeViews() {
        selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Update week text
        updateWeekText();
    }

    private void updateWeekText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        Calendar weekStart = (Calendar) selectedDate.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.getFirstDayOfWeek());
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
        
        String weekRange = sdf.format(weekStart.getTime()) + " - " + sdf.format(weekEnd.getTime());
        binding.currentWeekText.setText(weekRange);
    }

    private void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }
    
    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setupWeekNavigation() {
        binding.prevWeekButton.setOnClickListener(v -> navigateWeek(-1));
        binding.nextWeekButton.setOnClickListener(v -> navigateWeek(1));
        binding.currentWeekFab.setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            
            // Create new adapter with updated dates
            weekViewAdapter = new WeekViewPagerAdapter(this);
            binding.weekViewPager.setAdapter(weekViewAdapter);
            
            // Set the ViewPager2 to show today's date
            binding.weekViewPager.setCurrentItem(weekViewAdapter.getSelectedPosition(), false);
            
            updateWeekText();
            loadMealsForSelectedDate();
        });
    }

    private void setupMealCategories() {
        mealCategoryAdapter = new MealCategoryAdapter(this);
        binding.mealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        meals = new ArrayList<>();
        // Initialize with empty lists for each category
        for (int i = 0; i < 4; i++) {
            mealCategoryAdapter.setMeals(i, new ArrayList<>());
        }
        binding.mealsRecyclerView.setAdapter(mealCategoryAdapter);
    }

    private void setupWeekView() {
        weekViewAdapter = new WeekViewPagerAdapter(this);
        binding.weekViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.weekViewPager.setAdapter(weekViewAdapter);
        
        // Set the ViewPager2 to show today's date
        binding.weekViewPager.setCurrentItem(weekViewAdapter.getSelectedPosition(), false);
        
        // Set the ViewPager2 to not clip children to padding
        binding.weekViewPager.setClipToPadding(false);
        
        // Set the ViewPager2 to not clip children to outline
        binding.weekViewPager.setClipChildren(false);
        
        // Set the ViewPager2 to use a fixed width for each page
        binding.weekViewPager.setOffscreenPageLimit(1);
        
        // Set the ViewPager2 to use a fixed width for each page
        binding.weekViewPager.setUserInputEnabled(true);
        
        // Set the ViewPager2 to use a fixed width for each page
        binding.weekViewPager.setPageTransformer((page, position) -> {
            // No transformation needed
        });

        // Add page change listener to update selected date without changing selection
        binding.weekViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Date date = weekViewAdapter.getDates().get(position);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                selectedDate = calendar;
                updateWeekText();
                loadMealsForSelectedDate();
            }
        });

        // Set initial selection to today
        weekViewAdapter.setSelectedPosition(weekViewAdapter.getSelectedPosition());
    }

    private void navigateWeek(int offset) {
        selectedDate.add(Calendar.WEEK_OF_YEAR, offset);
        updateWeekText();
        loadMealsForSelectedDate();
    }

    private void loadMealsForSelectedDate() {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDate.getTime());

        // Show loading indicator
        showLoading();

        // Check if user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view meal plans", Toast.LENGTH_LONG).show();
            hideLoading();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mealPlansRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("meal_plans")
                .child(dateStr);

        // Remove any existing listener
        if (mealsListener != null) {
            mealPlansRef.removeEventListener(mealsListener);
        }

        // Create and attach new listener
        mealsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear all categories first
                for (int i = 0; i < 4; i++) {
                    mealCategoryAdapter.setMeals(i, new ArrayList<>());
                }

                // Load meals for each category
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String category = categorySnapshot.getKey();
                    int categoryIndex = getCategoryIndex(category);
                    if (categoryIndex >= 0) {
                        List<Meal> categoryMeals = new ArrayList<>();
                        for (DataSnapshot mealSnapshot : categorySnapshot.getChildren()) {
                            Meal meal = mealSnapshot.getValue(Meal.class);
                            if (meal != null) {
                                meal.setId(mealSnapshot.getKey());
                                categoryMeals.add(meal);
                            }
                        }
                        mealCategoryAdapter.setMeals(categoryIndex, categoryMeals);
                    }
                }
                mealCategoryAdapter.notifyDataSetChanged();
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MealPlannerActivity", "Error loading meals: " + databaseError.getMessage());
                Toast.makeText(MealPlannerActivity.this, "Failed to load meals", Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        };

        mealPlansRef.addValueEventListener(mealsListener);
    }

    private int getCategoryIndex(String category) {
        switch (category.toLowerCase()) {
            case "breakfast": return 0;
            case "lunch": return 1;
            case "dinner": return 2;
            case "snacks": return 3;
            default: return -1;
        }
    }

    @Override
    public void onDayClick(Date date, int position) {
        selectedDate.setTime(date);
        updateWeekText();
        loadMealsForSelectedDate();
    }

    @Override
    public void onAddMealClick(int categoryPosition) {
        // Create intent to RecipeActivity with selection mode
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("isSelectingForMealPlan", true);
        intent.putExtra("category", categoryPosition);
        intent.putExtra("selectedDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDate.getTime()));
        startActivityForResult(intent, REQUEST_ADD_MEAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEAL && resultCode == RESULT_OK) {
            // Refresh the meals for the selected date
            loadMealsForSelectedDate();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (mealPlansRef != null && mealsListener != null) {
            mealPlansRef.removeEventListener(mealsListener);
        }
    }

    public String getSelectedDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDate.getTime());
    }
    
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 