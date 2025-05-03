package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation();
    }

    protected void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(this);
            setSelectedNavigationItem();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            if (!getClass().equals(MainActivity.class)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            return true;
        } else if (itemId == R.id.navigation_recipes) {
            if (!getClass().equals(RecipeActivity.class)) {
                startActivity(new Intent(this, RecipeActivity.class));
                finish();
            }
            return true;
        } else if (itemId == R.id.navigation_planner) {
            if (!getClass().equals(MealPlannerActivity.class)) {
                startActivity(new Intent(this, MealPlannerActivity.class));
                finish();
            }
            return true;
        } else if (itemId == R.id.navigation_grocery) {
            if (!getClass().equals(GroceryListActivity.class)) {
                startActivity(new Intent(this, GroceryListActivity.class));
                finish();
            }
            return true;
        } else if (itemId == R.id.navigation_settings) {
            if (!getClass().equals(SettingsActivity.class)) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            }
            return true;
        }
        return false;
    }

    protected abstract void setSelectedNavigationItem();
} 