package com.example.mealmate.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanDay {
    private String dateStr;
    private Map<String, List<Meal>> meals;
    private Map<String, LocalTime> mealTimes;
    private boolean isExpanded;

    // Required empty constructor for Firestore
    public MealPlanDay() {
        meals = new HashMap<>();
        mealTimes = new HashMap<>();
        initializeMealLists();
        initializeDefaultMealTimes();
    }

    public MealPlanDay(LocalDate date) {
        this();
        this.dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void initializeMealLists() {
        meals.put("breakfast", new ArrayList<>());
        meals.put("lunch", new ArrayList<>());
        meals.put("dinner", new ArrayList<>());
        meals.put("snacks", new ArrayList<>());
    }

    private void initializeDefaultMealTimes() {
        mealTimes.put("breakfast", LocalTime.of(8, 0));
        mealTimes.put("lunch", LocalTime.of(12, 30));
        mealTimes.put("dinner", LocalTime.of(19, 0));
        mealTimes.put("snacks", LocalTime.of(15, 30));
    }

    public LocalDate getDate() {
        return LocalDate.parse(dateStr);
    }

    public void setDate(LocalDate date) {
        this.dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public List<Meal> getMeals(String mealType) {
        return meals.getOrDefault(mealType, new ArrayList<>());
    }

    public void setMeals(String mealType, List<Meal> mealList) {
        meals.put(mealType, mealList);
    }

    public void addMeal(String mealType, Meal meal) {
        List<Meal> mealList = meals.getOrDefault(mealType, new ArrayList<>());
        mealList.add(meal);
        meals.put(mealType, mealList);
    }

    public void removeMeal(String mealType, int position) {
        List<Meal> mealList = meals.get(mealType);
        if (mealList != null && position >= 0 && position < mealList.size()) {
            mealList.remove(position);
        }
    }

    public LocalTime getMealTime(String mealType) {
        return mealTimes.getOrDefault(mealType, LocalTime.of(12, 0));
    }

    public void setMealTime(String mealType, LocalTime time) {
        mealTimes.put(mealType, time);
    }

    public Map<String, LocalTime> getMealTimes() {
        return mealTimes;
    }

    public void setMealTimes(Map<String, LocalTime> mealTimes) {
        this.mealTimes = mealTimes;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }
} 