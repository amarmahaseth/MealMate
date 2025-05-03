package com.example.mealmate;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.adapters.ShoppingTaskAdapter;
import com.example.mealmate.databinding.ActivityShoppingTasksBinding;
import com.example.mealmate.models.ShoppingTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class ShoppingTasksActivity extends AppCompatActivity implements ShoppingTaskAdapter.OnTaskClickListener {

    private ActivityShoppingTasksBinding binding;
    private RecyclerView recyclerView;
    private ShoppingTaskAdapter adapter;
    private List<ShoppingTask> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private Button btnAddFirstTask;
    private FloatingActionButton fabAddTask;

    private static final int REQUEST_CREATE_TASK = 1001;
    private static final int REQUEST_EDIT_TASK = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShoppingTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            binding.toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            Drawable navigationIcon = binding.toolbar.getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }

        // Initialize views
        recyclerView = binding.recyclerView;
        progressBar = binding.progressBar;
        emptyView = binding.emptyView;
        btnAddFirstTask = binding.btnAddFirstTask;
        fabAddTask = binding.fabAddTask;

        // Set up RecyclerView
        taskList = new ArrayList<>();
        adapter = new ShoppingTaskAdapter(this, taskList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up click listeners
        setupClickListeners();

        // Load tasks
        loadTasks();
    }

    private void loadTasks() {
        showLoading(true);
        
        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference tasksRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shopping_tasks");
            
        tasksRef.orderByChild("dueDate").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                taskList.clear();
                
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showLoading(false);
                    updateEmptyState();
                    return;
                }
                
                for (com.google.firebase.database.DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    ShoppingTask task = taskSnapshot.getValue(ShoppingTask.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                
                adapter.notifyDataSetChanged();
                showLoading(false);
                updateEmptyState();
            }
            
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                showLoading(false);
                updateEmptyState();
                Toast.makeText(ShoppingTasksActivity.this, "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openTaskDetail(String taskId) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        if (taskId != null) {
            intent.putExtra("task_id", taskId);
            startActivityForResult(intent, REQUEST_EDIT_TASK);
        } else {
            startActivityForResult(intent, REQUEST_CREATE_TASK);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTaskClick(ShoppingTask task, int position) {
        openTaskDetail(task.getId());
    }

    @Override
    public void onStatusChange(ShoppingTask task, int position) {
        updateTaskStatus(task, position);
    }

    @Override
    public void onEditTask(ShoppingTask task, int position) {
        openTaskDetail(task.getId());
    }

    @Override
    public void onDeleteTask(ShoppingTask task, int position) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete", (dialog, which) -> deleteTask(task, position))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Refresh tasks when returning to this screen
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            String taskTitle = data != null ? data.getStringExtra("task_title") : "task";
            
            if (requestCode == REQUEST_CREATE_TASK) {
                Snackbar.make(recyclerView, "Task \"" + taskTitle + "\" created successfully", Snackbar.LENGTH_LONG).show();
                loadTasks();
            } else if (requestCode == REQUEST_EDIT_TASK) {
                Snackbar.make(recyclerView, "Task \"" + taskTitle + "\" updated successfully", Snackbar.LENGTH_LONG).show();
                loadTasks();
            }
        }
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> openTaskDetail(null));
        btnAddFirstTask.setOnClickListener(v -> openTaskDetail(null));
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void deleteTask(ShoppingTask task, int position) {
        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference taskRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shopping_tasks")
            .child(task.getId());
            
        taskRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                // Task will be removed from the list by the ValueEventListener
                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> {
                        // Restore the task
                        taskRef.setValue(task)
                            .addOnSuccessListener(aVoid1 -> {
                                // Task will be added back to the list by the ValueEventListener
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ShoppingTasksActivity.this, "Error restoring task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    }).show();
            })
            .addOnFailureListener(e -> {
                adapter.notifyItemChanged(position); // Restore the swiped item
                Toast.makeText(this, "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateTaskStatus(ShoppingTask task, int position) {
        ShoppingTask.Status newStatus;
        
        // Cycle through statuses: PENDING -> IN_PROGRESS -> COMPLETED -> PENDING
        switch (task.getStatus()) {
            case PENDING:
                newStatus = ShoppingTask.Status.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                newStatus = ShoppingTask.Status.COMPLETED;
                break;
            default:
                newStatus = ShoppingTask.Status.PENDING;
                break;
        }
        
        task.setStatus(newStatus);
        
        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference taskRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shopping_tasks")
            .child(task.getId());
            
        taskRef.child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> {
                // The ValueEventListener will update the UI
                String statusText = task.getStatusDisplayName();
                Snackbar.make(recyclerView, "Task marked as " + statusText, Snackbar.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                adapter.notifyItemChanged(position); // Restore the swiped item
                Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 