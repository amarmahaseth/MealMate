package com.example.mealmate;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MealMateApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
} 