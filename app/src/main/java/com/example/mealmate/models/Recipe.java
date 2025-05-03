package com.example.mealmate.models;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.database.PropertyName;

public class Recipe {
    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private String imageBase64;
    private List<String> ingredients;
    private String instructions;
    private String userId;
    private int calories;
    private long timestamp;

    // Required empty constructor for Firebase
    public Recipe() {
        ingredients = new ArrayList<>();
    }

    public Recipe(String id, String name, String category, String imageUrl, String imageBase64, List<String> ingredients, String instructions, String userId, int calories) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.imageBase64 = imageBase64;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions;
        this.userId = userId;
        this.calories = calories;
        this.timestamp = System.currentTimeMillis();
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @PropertyName("ingredients")
    public List<String> getIngredients() {
        return ingredients != null ? ingredients : new ArrayList<>();
    }

    @PropertyName("ingredients")
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    @PropertyName("instructions")
    public String getInstructions() {
        return instructions;
    }

    @PropertyName("instructions")
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getIngredientsCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("calories")
    public int getCalories() {
        return calories;
    }

    @PropertyName("calories")
    public void setCalories(int calories) {
        this.calories = calories;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Helper method to convert ingredients string to list (not a setter)
    public static List<String> parseIngredients(String ingredientsString) {
        if (ingredientsString == null || ingredientsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] ingredients = ingredientsString.split("\\n");
        return new ArrayList<>(Arrays.asList(ingredients));
    }
} 