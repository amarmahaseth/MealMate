package com.example.mealmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.MealPlannerActivity;
import com.example.mealmate.R;
import com.example.mealmate.models.Meal;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealCategoryAdapter extends RecyclerView.Adapter<MealCategoryAdapter.CategoryViewHolder> {
    
    private static final String[] CATEGORIES = {
        "Breakfast 🍳", "Lunch 🥗", "Dinner 🍛", "Snacks 🍪"
    };

    private final OnMealClickListener listener;
    private final Map<Integer, List<Meal>> categoryMeals;
    private final Map<Integer, MealItemAdapter> mealAdapters;
    private Context context;

    public interface OnMealClickListener {
        void onAddMealClick(int categoryPosition);
    }

    public MealCategoryAdapter(OnMealClickListener listener) {
        this.listener = listener;
        this.categoryMeals = new HashMap<>();
        this.mealAdapters = new HashMap<>();
        
        // Initialize empty lists for each category
        for (int i = 0; i < CATEGORIES.length; i++) {
            categoryMeals.put(i, new ArrayList<>());
            // Initialize adapters for each category
            final int position = i;
            MealItemAdapter adapter = new MealItemAdapter(new MealItemAdapter.OnMealItemClickListener() {
                @Override
                public void onEditClick(int mealPosition) {
                    // Edit functionality removed
                }

                @Override
                public void onDeleteClick(int mealPosition) {
                    List<Meal> meals = categoryMeals.get(position);
                    if (meals != null && mealPosition < meals.size()) {
                        Meal mealToDelete = meals.get(mealPosition);
                        // Remove from UI first for responsiveness
                        meals.remove(mealPosition);
                        MealItemAdapter categoryAdapter = mealAdapters.get(position);
                        if (categoryAdapter != null) {
                            categoryAdapter.setMeals(meals);
                        }
                        
                        // Then delete from database
                        if (mealToDelete.getId() != null) {
                            String category = getCategoryName(position);
                            deleteMealFromDatabase(mealToDelete.getId(), category);
                        }
                    }
                }
            });
            mealAdapters.put(i, adapter);
        }
    }
    
    private String getCategoryName(int position) {
        switch (position) {
            case 0: return "breakfast";
            case 1: return "lunch";
            case 2: return "dinner";
            case 3: return "snacks";
            default: return "other";
        }
    }
    
    private void deleteMealFromDatabase(String mealId, String category) {
        // Get the current date from MealPlannerActivity
        String dateStr = null;
        if (context instanceof MealPlannerActivity) {
            MealPlannerActivity activity = (MealPlannerActivity) context;
            dateStr = activity.getSelectedDateString();
        }
        
        if (dateStr == null) {
            return;
        }
        
        // Get user ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        
        // Delete from database
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId)
            .child("meal_plans")
            .child(dateStr)
            .child(category)
            .child(mealId)
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                // Successfully deleted
            })
            .addOnFailureListener(e -> {
                // Failed to delete, add it back to the UI
                if (context instanceof MealPlannerActivity) {
                    Toast.makeText(context, "Failed to delete meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void setMeals(int categoryPosition, List<Meal> meals) {
        categoryMeals.put(categoryPosition, meals);
        MealItemAdapter adapter = mealAdapters.get(categoryPosition);
        if (adapter != null) {
            adapter.setMeals(meals);
        }
        notifyItemChanged(categoryPosition);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_meal_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(CATEGORIES[position], position);
    }

    @Override
    public int getItemCount() {
        return CATEGORIES.length;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryTitle;
        private final ImageButton addButton;
        private final RecyclerView mealsRecyclerView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            addButton = itemView.findViewById(R.id.addMealButton);
            mealsRecyclerView = itemView.findViewById(R.id.mealsRecyclerView);
        }

        void bind(String category, final int position) {
            categoryTitle.setText(category);
            addButton.setOnClickListener(v -> listener.onAddMealClick(position));

            // Get the adapter for this category
            MealItemAdapter adapter = mealAdapters.get(position);
            if (adapter != null) {
                mealsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                mealsRecyclerView.setAdapter(adapter);
                adapter.setMeals(categoryMeals.get(position));
            }
        }
    }
} 