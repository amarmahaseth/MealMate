package com.example.mealmate;

import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private List<String> ingredients;
    private String instructions;

    // Required empty constructor for Firebase
    public Recipe() {
    }

    public Recipe(String id, String name, String category, String imageUrl, List<String> ingredients, String instructions) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getIngredientsCount() {
        return ingredients != null ? ingredients.size() : 0;
    }
} 