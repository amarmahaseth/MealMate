package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mealmate.adapters.GrocerySelectionAdapter;
import com.example.mealmate.databinding.ActivityGrocerySelectionBinding;
import com.example.mealmate.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrocerySelectionActivity extends AppCompatActivity {
    
    private static final String TAG = "GrocerySelectionActivity";
    private static final int REQUEST_GROCERY_SELECTION = 2;
    private ActivityGrocerySelectionBinding binding;
    private GrocerySelectionAdapter adapter;
    private DatabaseReference groceryListRef;
    private FirebaseAuth auth;
    private ValueEventListener groceryListListener;
    private List<String> selectedItems = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGrocerySelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view grocery list", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        groceryListRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("grocery_list");
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Grocery Items");
        }
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup buttons
        binding.btnDone.setOnClickListener(v -> finishWithSelectedItems());
        binding.btnCancel.setOnClickListener(v -> finish());
        
        // Load grocery items
        loadGroceryItems();
    }
    
    private void setupRecyclerView() {
        adapter = new GrocerySelectionAdapter();
        adapter.setOnItemSelectedListener((item, isSelected) -> {
            String itemText = item.getName();
            if (!item.getQuantity().isEmpty()) {
                itemText += " (" + item.getQuantity() + ")";
            }
            
            if (isSelected) {
                if (!selectedItems.contains(itemText)) {
                    selectedItems.add(itemText);
                }
            } else {
                selectedItems.remove(itemText);
            }
            
            updateButtonState();
        });
        
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void updateButtonState() {
        binding.btnDone.setEnabled(!selectedItems.isEmpty());
        binding.tvSelectedCount.setText(selectedItems.size() + " items selected");
    }
    
    private void loadGroceryItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        groceryListListener = groceryListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GroceryItem> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    GroceryItem item = itemSnapshot.getValue(GroceryItem.class);
                    if (item != null) {
                        item.setId(itemSnapshot.getKey());
                        // Only show unpurchased items
                        if (!item.isPurchased()) {
                            items.add(item);
                        }
                    }
                }
                
                // Sort items by category
                Collections.sort(items, (item1, item2) -> 
                    item1.getCategory().compareTo(item2.getCategory()));
                
                adapter.setItems(items);
                binding.progressBar.setVisibility(View.GONE);
                updateEmptyView();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GrocerySelectionActivity.this, 
                    "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateEmptyView() {
        binding.emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
    }
    
    private void finishWithSelectedItems() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selected_items", new ArrayList<>(selectedItems));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groceryListListener != null) {
            groceryListRef.removeEventListener(groceryListListener);
        }
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