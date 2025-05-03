package com.example.mealmate;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmate.databinding.ActivityRecipeEditBinding;

public class RecipeEditActivity extends AppCompatActivity {
    private ActivityRecipeEditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        // TODO: Implement recipe editing functionality
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Edit Recipe");
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