package com.example.mealmate.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.R;
import com.example.mealmate.models.Meal;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealItemAdapter extends RecyclerView.Adapter<MealItemAdapter.MealViewHolder> {
    
    private List<Meal> meals;
    private final OnMealItemClickListener listener;

    public interface OnMealItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public MealItemAdapter(OnMealItemClickListener listener) {
        this.listener = listener;
        this.meals = new ArrayList<>();
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        holder.bind(meals.get(position), position);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mealImage;
        private final TextView mealName;
        private final TextView mealTime;
        private final ImageButton deleteButton;

        MealViewHolder(View itemView) {
            super(itemView);
            mealImage = itemView.findViewById(R.id.mealImage);
            mealName = itemView.findViewById(R.id.mealName);
            mealTime = itemView.findViewById(R.id.mealTime);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Meal meal, int position) {
            mealName.setText(meal.getName());
            
            // Format and display prep + cook time
            int totalTime = meal.getTotalTime();
            String timeText = String.format(Locale.getDefault(), "Total Time: %d min", totalTime);
            mealTime.setText(timeText);
            
            // Load recipe image with better error handling
            if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(meal.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .into(mealImage);
            } else {
                // Try to load from base64 if available
                String imageBase64 = getImageBase64ForRecipe(meal.getRecipeId());
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] decodedString = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                        Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mealImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // Set a placeholder image if decoding fails
                        mealImage.setImageResource(R.drawable.ic_food_placeholder);
                    }
                } else {
                    // Set a placeholder image
                    mealImage.setImageResource(R.drawable.ic_food_placeholder);
                }
            }
            
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(position));
        }
        
        private String getImageBase64ForRecipe(String recipeId) {
            if (recipeId == null || recipeId.isEmpty()) {
                return null;
            }
            
            // Get the recipe from Firebase
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                return null;
            }
            
            // This is a synchronous method, so we can't actually fetch from Firebase here
            // In a real implementation, you would pre-fetch this data or use a callback
            // For now, we'll return null and rely on the placeholder
            return null;
        }
    }
} 