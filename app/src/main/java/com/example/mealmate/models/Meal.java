package com.example.mealmate.models;

import com.google.firebase.database.Exclude;

public class Meal {
    private String id;
    private String recipeId;
    private String name;
    private String imageUrl;
    private String description;
    private int servings;
    private int prepTime;
    private int cookTime;
    private long timestamp;

    public Meal() {
        // Required empty constructor for Firebase
        timestamp = System.currentTimeMillis();
    }

    public Meal(String recipeId, String name, String imageUrl, String description, int servings, int prepTime, int cookTime) {
        this.recipeId = recipeId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.servings = servings;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.timestamp = System.currentTimeMillis();
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public int getTotalTime() {
        return prepTime + cookTime;
    }
} 