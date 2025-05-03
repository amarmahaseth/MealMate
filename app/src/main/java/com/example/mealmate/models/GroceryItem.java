package com.example.mealmate.models;

public class GroceryItem {
    private String id;
    private String name;
    private String quantity;
    private String category;
    private boolean purchased;

    // Category constants
    public static final String CATEGORY_PRODUCE = "Produce";
    public static final String CATEGORY_MEAT = "Meat & Seafood";
    public static final String CATEGORY_DAIRY = "Dairy & Eggs";
    public static final String CATEGORY_PANTRY = "Pantry";
    public static final String CATEGORY_BAKERY = "Bakery";
    public static final String CATEGORY_FROZEN = "Frozen";
    public static final String CATEGORY_BEVERAGES = "Beverages";
    public static final String CATEGORY_SNACKS = "Snacks";
    public static final String CATEGORY_CONDIMENTS = "Condiments";
    public static final String CATEGORY_OTHER = "Other";

    // Required empty constructor for Firebase
    public GroceryItem() {}

    public GroceryItem(String id, String name, String quantity, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.purchased = false;
    }

    // Getters and setters
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    // Helper method to categorize ingredients
    public static String categorizeIngredient(String ingredient) {
        if (ingredient == null) return CATEGORY_OTHER;
        
        String lowerIngredient = ingredient.toLowerCase().trim();

        // Produce (Fruits & Vegetables)
        if (matchesAny(lowerIngredient,
            "apple", "banana", "orange", "berry", "fruit",
            "carrot", "onion", "tomato", "potato", "lettuce",
            "cucumber", "pepper", "garlic", "ginger", "vegetable",
            "celery", "mushroom", "broccoli", "spinach", "herb",
            "lemon", "lime", "avocado", "cabbage", "cauliflower",
            "zucchini", "squash", "pumpkin", "eggplant", "asparagus",
            "kale", "cilantro", "parsley", "basil", "mint",
            "produce")) {
            return CATEGORY_PRODUCE;
        }

        // Meat & Seafood
        if (matchesAny(lowerIngredient,
            "chicken", "beef", "pork", "lamb", "turkey",
            "fish", "salmon", "tuna", "shrimp", "meat",
            "steak", "ground", "bacon", "sausage", "ham",
            "seafood", "crab", "lobster", "scallop", "meat",
            "duck", "veal", "mutton", "prawn", "oyster",
            "clam", "mussel")) {
            return CATEGORY_MEAT;
        }

        // Dairy & Eggs
        if (matchesAny(lowerIngredient,
            "milk", "cheese", "yogurt", "cream", "butter",
            "egg", "dairy", "sour cream", "cottage cheese", "mozzarella",
            "cheddar", "parmesan", "ricotta", "whey", "buttermilk",
            "margarine", "ghee")) {
            return CATEGORY_DAIRY;
        }

        // Pantry
        if (matchesAny(lowerIngredient,
            "flour", "sugar", "salt", "rice", "pasta",
            "bean", "lentil", "grain", "cereal", "oil",
            "vinegar", "broth", "stock", "can", "dried",
            "spice", "herb", "baking", "powder", "soda",
            "vanilla", "cocoa", "chocolate", "syrup", "honey",
            "peanut butter", "jam", "preserves")) {
            return CATEGORY_PANTRY;
        }

        // Bakery
        if (matchesAny(lowerIngredient,
            "bread", "roll", "bun", "bagel", "muffin",
            "croissant", "pastry", "cake", "pie", "cookie",
            "dough", "tortilla", "pita", "naan", "baguette")) {
            return CATEGORY_BAKERY;
        }

        // Frozen
        if (matchesAny(lowerIngredient,
            "frozen", "ice cream", "pizza", "fries", "peas",
            "mixed vegetable", "waffle", "ice")) {
            return CATEGORY_FROZEN;
        }

        // Beverages
        if (matchesAny(lowerIngredient,
            "water", "juice", "soda", "coffee", "tea",
            "drink", "milk", "beverage", "wine", "beer",
            "alcohol", "liquor", "cocktail")) {
            return CATEGORY_BEVERAGES;
        }

        // Snacks
        if (matchesAny(lowerIngredient,
            "chip", "crisp", "popcorn", "pretzel", "nut",
            "candy", "chocolate", "snack", "cracker", "cookie")) {
            return CATEGORY_SNACKS;
        }

        // Condiments
        if (matchesAny(lowerIngredient,
            "sauce", "ketchup", "mustard", "mayonnaise", "dressing",
            "oil", "vinegar", "soy sauce", "hot sauce", "marinade",
            "seasoning", "spice", "herb", "condiment")) {
            return CATEGORY_CONDIMENTS;
        }

        return CATEGORY_OTHER;
    }

    private static boolean matchesAny(String ingredient, String... keywords) {
        for (String keyword : keywords) {
            if (ingredient.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
} 