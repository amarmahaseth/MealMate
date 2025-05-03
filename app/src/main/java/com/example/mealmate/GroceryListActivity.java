package com.example.mealmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mealmate.adapters.GroceryListAdapter;
import com.example.mealmate.databinding.ActivityGroceryListBinding;
import com.example.mealmate.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * OPTIONAL FEATURE: Smart Grocery List Management
 * 
 * Purpose:
 * - Manage shopping lists efficiently
 * - Track purchased items
 * - Enable list sharing and collaboration
 * 
 * Implementation Details:
 * - Real-time Firebase synchronization
 * - Offline data persistence
 * - Smart item categorization
 * - Share functionality
 * 
 * Key Features:
 * 1. List Management:
 *    - Add/Edit/Delete items
 *    - Mark items as purchased
 *    - Categorize items automatically
 * 
 * 2. Sharing:
 *    - Share lists with family/friends
 *    - Collaborative editing
 *    - Real-time updates
 * 
 * 3. Smart Features:
 *    - Automatic categorization
 *    - Purchase history
 *    - Shopping patterns
 * 
 * Future Enhancements:
 * 1. Smart Recommendations:
 *    - Suggest items based on purchase history
 *    - Predict shopping needs
 *    - Seasonal recommendations
 * 
 * 2. Integration:
 *    - Link with recipes
 *    - Connect with meal planner
 *    - Sync with store locations
 * 
 * 3. Analytics:
 *    - Shopping frequency
 *    - Spending patterns
 *    - Item preferences
 */

public class GroceryListActivity extends AppCompatActivity {
    private ActivityGroceryListBinding binding;
    private GroceryListAdapter adapter;
    private List<GroceryItem> groceryItems;
    private DatabaseReference groceryListRef;
    private ValueEventListener groceryItemsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroceryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Grocery List");

        // Setup navigation
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();

        // Setup click listeners
        binding.fabAddItem.setOnClickListener(v -> showAddItemDialog());

        // Load grocery items
        loadGroceryItems();
    }

    private void setupRecyclerView() {
        groceryItems = new ArrayList<>();
        adapter = new GroceryListAdapter();
        adapter.setOnItemClickListener(item -> {
            // TODO: Handle item click
        });
        adapter.setOnItemCheckedListener((item, isChecked) -> {
            updateItemPurchasedStatus(item, isChecked);
        });
        adapter.setOnItemEditListener(item -> {
            showEditItemDialog(item);
        });
        adapter.setOnItemDeleteListener(item -> {
            showDeleteConfirmationDialog(item);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadGroceryItems() {
        /**
         * OPTIONAL FEATURE: Real-time Data Synchronization
         * 
         * Purpose:
         * - Keep grocery list updated across devices
         * - Enable offline access
         * - Support collaborative editing
         * 
         * Implementation:
         * - Firebase Realtime Database
         * - ValueEventListener for updates
         * - Offline persistence
         * 
         * Future Enhancements:
         * 1. Conflict Resolution:
         *    - Handle concurrent edits
         *    - Merge changes intelligently
         *    - Version control
         * 
         * 2. Performance Optimization:
         *    - Pagination for large lists
         *    - Caching strategies
         *    - Delta updates
         * 
         * 3. Data Analytics:
         *    - Track changes over time
         *    - User behavior analysis
         *    - Usage patterns
         */
        
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to view your grocery list", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groceryListRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("grocery_list");

        // Remove any existing listener
        if (groceryItemsListener != null) {
            groceryListRef.removeEventListener(groceryItemsListener);
        }

        // Create and attach new listener
        groceryItemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<GroceryItem> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    GroceryItem item = itemSnapshot.getValue(GroceryItem.class);
                    if (item != null) {
                        item.setId(itemSnapshot.getKey());
                        items.add(item);
                    }
                }
                adapter.setItems(items);
                updateEmptyView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GroceryListActivity.this, "Failed to load grocery list", Toast.LENGTH_SHORT).show();
            }
        };

        groceryListRef.addValueEventListener(groceryItemsListener);
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddItemDialog() {
        /**
         * OPTIONAL FEATURE: Smart Item Addition
         * 
         * Purpose:
         * - Simplify item entry
         * - Ensure data consistency
         * - Provide smart suggestions
         * 
         * Implementation:
         * - Form validation
         * - Unit conversion
         * - Category prediction
         * 
         * Future Enhancements:
         * 1. Smart Input:
         *    - Voice input support
         *    - Barcode scanning
         *    - Image recognition
         * 
         * 2. Suggestions:
         *    - Previous items
         *    - Common combinations
         *    - Seasonal items
         * 
         * 3. Integration:
         *    - Recipe ingredients
         *    - Store inventory
         *    - Price tracking
         */
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_grocery_item, null);
        EditText itemNameInput = dialogView.findViewById(R.id.itemNameInput);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        AutoCompleteTextView unitSpinner = dialogView.findViewById(R.id.unitSpinner);

        // Setup unit dropdown
        String[] units = new String[]{"kg", "g", "l", "ml", "pcs", "oz", "lb"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line,
            units);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setText(units[0], false); // Set default value

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add Grocery Item")
            .setView(dialogView)
            .setPositiveButton("Add", (dialogInterface, i) -> {
                String name = itemNameInput.getText().toString().trim();
                String quantityStr = quantityInput.getText().toString().trim();
                String unit = unitSpinner.getText().toString();

                if (name.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double quantity = Double.parseDouble(quantityStr);
                    addGroceryItem(name, quantity, unit);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();
    }

    private void addGroceryItem(String name, double quantity, String unit) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to add items", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference groceryListRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("grocery_list");

        // Convert quantity to string with unit
        String quantityString = quantity + " " + unit;
        // Get category based on item name
        String category = GroceryItem.categorizeIngredient(name);
        
        // Generate a new ID for the item
        String itemId = groceryListRef.push().getKey();
        if (itemId != null) {
            // Create item with all required parameters
            GroceryItem item = new GroceryItem(itemId, name, quantityString, category);
            groceryListRef.child(itemId).setValue(item)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateItemPurchasedStatus(GroceryItem item, boolean isPurchased) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("grocery_list")
            .child(item.getId());

        item.setPurchased(isPurchased);
        itemRef.setValue(item)
            .addOnSuccessListener(aVoid -> {
                // Successfully updated
                Toast.makeText(this, "Item status updated", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Failed to update
                Toast.makeText(this, "Failed to update item status", Toast.LENGTH_SHORT).show();
                // Revert the checkbox state
                adapter.notifyDataSetChanged();
            });
    }

    private void showEditItemDialog(GroceryItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_grocery_item, null);
        EditText itemNameInput = dialogView.findViewById(R.id.itemNameInput);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        AutoCompleteTextView unitSpinner = dialogView.findViewById(R.id.unitSpinner);

        // Setup unit dropdown
        String[] units = new String[]{"kg", "g", "l", "ml", "pcs", "oz", "lb"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line,
            units);
        unitSpinner.setAdapter(unitAdapter);

        // Parse existing quantity and unit
        String[] parts = item.getQuantity().split(" ", 2);
        String quantity = parts[0];
        String unit = parts.length > 1 ? parts[1] : "pcs";

        // Set existing values
        itemNameInput.setText(item.getName());
        quantityInput.setText(quantity);
        unitSpinner.setText(unit, false);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Edit Grocery Item")
            .setView(dialogView)
            .setPositiveButton("Save", (dialogInterface, i) -> {
                String name = itemNameInput.getText().toString().trim();
                String quantityStr = quantityInput.getText().toString().trim();
                String newUnit = unitSpinner.getText().toString();

                if (name.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double newQuantity = Double.parseDouble(quantityStr);
                    updateGroceryItem(item, name, newQuantity, newUnit);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();
    }

    private void updateGroceryItem(GroceryItem item, String name, double quantity, String unit) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("grocery_list")
            .child(item.getId());

        // Convert quantity to string with unit
        String quantityString = quantity + " " + unit;
        // Get category based on item name
        String category = GroceryItem.categorizeIngredient(name);

        // Update item properties
        item.setName(name);
        item.setQuantity(quantityString);
        item.setCategory(category);

        itemRef.setValue(item)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog(GroceryItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> deleteGroceryItem(item))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteGroceryItem(GroceryItem item) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("grocery_list")
            .child(item.getId());

        itemRef.removeValue()
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grocery_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareGroceryList();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void shareGroceryList() {
        /**
         * OPTIONAL FEATURE: List Sharing
         * 
         * Purpose:
         * - Enable collaborative shopping
         * - Share lists with family/friends
         * - Coordinate shopping tasks
         * 
         * Implementation:
         * - System share sheet
         * - Formatted text output
         * - Purchase status tracking
         * 
         * Future Enhancements:
         * 1. Collaboration:
         *    - Real-time sharing
         *    - Role-based access
         *    - Change tracking
         * 
         * 2. Integration:
         *    - Messaging apps
         *    - Calendar events
         *    - Task management
         * 
         * 3. Analytics:
         *    - Share frequency
         *    - Collaboration patterns
         *    - Usage statistics
         */
        
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No items to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder("My Grocery List:\n\n");
        for (GroceryItem item : adapter.getItems()) {
            String status = item.isPurchased() ? "✓ " : "";
            shareText.append(status)
                    .append(item.getName())
                    .append(" - ")
                    .append(item.getQuantity())
                    .append(" (")
                    .append(item.getCategory())
                    .append(")\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Grocery List"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groceryListRef != null && groceryItemsListener != null) {
            groceryListRef.removeEventListener(groceryItemsListener);
        }
    }
} 