package com.example.mealmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mealmate.models.ShoppingTask;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TaskDetailActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailActivity";
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_GROCERY_SELECTION = 2;
    private static final int REQUEST_SMS = 3;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 101;
    
    private TextInputEditText etTaskTitle, etTaskDescription, etDueDate, etDueTime;
    private TextInputEditText etContactName, etContactPhone;
    private ChipGroup statusChipGroup;
    private Chip chipPending, chipInProgress, chipCompleted, chipDelegated;
    private Button btnSelectContact, btnSave, btnSendSMS;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String taskId;
    private ShoppingTask currentTask;
    private Calendar dueDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private boolean isFromGroceryList = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize views
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        statusChipGroup = findViewById(R.id.statusChipGroup);
        chipPending = findViewById(R.id.chipPending);
        chipInProgress = findViewById(R.id.chipInProgress);
        chipCompleted = findViewById(R.id.chipCompleted);
        chipDelegated = findViewById(R.id.chipDelegated);
        btnSelectContact = findViewById(R.id.btnSelectContact);
        btnSave = findViewById(R.id.btnSave);
        btnSendSMS = findViewById(R.id.btnSendSMS);
        
        // Set up chip listeners for text color changes
        setupChipListeners();
        
        // Add button to select grocery items
        Button btnSelectGroceries = findViewById(R.id.btnSelectGroceries);
        if (btnSelectGroceries != null) {
            btnSelectGroceries.setOnClickListener(v -> openGrocerySelector());
        }
        
        // Set up date and time pickers
        setupDateTimePickers();
        
        // Set up contact selection
        btnSelectContact.setOnClickListener(v -> selectContact());
        
        // Set up save button
        btnSave.setOnClickListener(v -> saveTask());
        
        // Set up SMS button
        btnSendSMS.setOnClickListener(v -> sendTaskViaSMS());
        
        // Get task ID from intent
        taskId = getIntent().getStringExtra("task_id");
        
        // Check if coming from grocery list
        isFromGroceryList = getIntent().getBooleanExtra("from_grocery", false);
        String mode = getIntent().getStringExtra("mode");
        String type = getIntent().getStringExtra("type");
        
        if (taskId != null) {
            // Load existing task
            loadTask(taskId);
            setTitle("Edit Task");
        } else {
            // New task
            setTitle("New Task");
            currentTask = new ShoppingTask();
            currentTask.setStatus(ShoppingTask.Status.PENDING);
            chipPending.setChecked(true);
            
            // Handle pre-filled data from grocery list
            if (isFromGroceryList || "create".equals(mode) && "shopping".equals(type)) {
                String title = getIntent().getStringExtra("title");
                String description = getIntent().getStringExtra("description");
                
                Log.d(TAG, "Creating shopping task - Title: " + title);
                Log.d(TAG, "Creating shopping task - Description: " + description);
                
                if (title != null && !title.isEmpty()) {
                    etTaskTitle.setText(title);
                } else {
                    etTaskTitle.setText("Buy groceries");
                }
                
                if (description != null && !description.isEmpty()) {
                    etTaskDescription.setText(description);
                    Log.d(TAG, "Set description to: " + description);
                }
                
                // Set due date to tomorrow
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                dueDateTime = tomorrow;
                updateDateTimeDisplay();
            }
        }
    }
    
    private void setupDateTimePickers() {
        etDueDate.setOnClickListener(v -> showDatePicker());
        etDueTime.setOnClickListener(v -> showTimePicker());
        
        // Set current date and time
        updateDateTimeDisplay();
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dueDateTime.set(Calendar.YEAR, year);
                    dueDateTime.set(Calendar.MONTH, month);
                    dueDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                dueDateTime.get(Calendar.YEAR),
                dueDateTime.get(Calendar.MONTH),
                dueDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    dueDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    dueDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                dueDateTime.get(Calendar.HOUR_OF_DAY),
                dueDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }
    
    private void updateDateTimeDisplay() {
        etDueDate.setText(dateFormat.format(dueDateTime.getTime()));
        etDueTime.setText(timeFormat.format(dueDateTime.getTime()));
    }
    
    private void selectContact() {
        // Check if we have the READ_CONTACTS permission
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            // Permission is not granted, request it
            showContactsPermissionExplanationDialog();
        } else {
            // Permission is already granted, proceed with selecting contact
            launchContactPicker();
        }
    }
    
    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // If request is cancelled, the result arrays are empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with selecting contact
                launchContactPicker();
            } else {
                // Permission denied, show a message to the user
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    // User denied permission but didn't check "Don't ask again"
                    Toast.makeText(this, "Contact selection requires permission to access contacts",
                            Toast.LENGTH_LONG).show();
                } else {
                    // User denied permission and checked "Don't ask again"
                    showSettingsDialog("Contacts");
                }
            }
        } else if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the SMS
                sendSMS(etContactPhone.getText().toString().trim());
            } else {
                // Permission denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    // User denied permission but didn't check "Don't ask again"
                    Toast.makeText(this, "SMS permission denied. Cannot send task via SMS.", Toast.LENGTH_LONG).show();
                } else {
                    // User denied permission and checked "Don't ask again"
                    showSettingsDialog("SMS");
                }
            }
        }
    }
    
    private void openGrocerySelector() {
        Intent intent = new Intent(this, GrocerySelectionActivity.class);
        startActivityForResult(intent, REQUEST_GROCERY_SELECTION);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            
            try {
                // Query for contact ID
                Cursor cursor = getContentResolver().query(contactUri, new String[]{ContactsContract.Contacts._ID}, 
                        null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    Log.d(TAG, "Selected contact ID: " + contactId);
                    
                    // Get contact name
                    Cursor nameCursor = getContentResolver().query(
                            ContactsContract.Contacts.CONTENT_URI,
                            new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                            ContactsContract.Contacts._ID + " = ?",
                            new String[]{contactId},
                            null
                    );
                    
                    if (nameCursor != null && nameCursor.moveToFirst()) {
                        String contactName = nameCursor.getString(nameCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        etContactName.setText(contactName);
                        Log.d(TAG, "Contact name: " + contactName);
                        nameCursor.close();
                    }
                    
                    // Check if the contact has a phone number
                    String hasPhone = "0";
                    Cursor hasPhoneCursor = getContentResolver().query(
                            ContactsContract.Contacts.CONTENT_URI,
                            new String[]{ContactsContract.Contacts.HAS_PHONE_NUMBER},
                            ContactsContract.Contacts._ID + " = ?",
                            new String[]{contactId},
                            null
                    );
                    
                    if (hasPhoneCursor != null && hasPhoneCursor.moveToFirst()) {
                        hasPhone = hasPhoneCursor.getString(hasPhoneCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        hasPhoneCursor.close();
                    }
                    
                    // Get contact phone if available
                    if ("1".equals(hasPhone)) {
                        Cursor phoneCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId},
                                null
                        );
                        
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            etContactPhone.setText(phoneNumber);
                            Log.d(TAG, "Contact phone: " + phoneNumber);
                            phoneCursor.close();
                        } else {
                            Log.d(TAG, "No phone cursor or empty cursor");
                        }
                    } else {
                        Log.d(TAG, "Contact has no phone number");
                        Toast.makeText(this, "Selected contact has no phone number", Toast.LENGTH_SHORT).show();
                    }
                    
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when accessing contacts: " + e.getMessage());
                Toast.makeText(this, "Unable to access contacts. Please check app permissions.", 
                        Toast.LENGTH_LONG).show();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error accessing contact data: " + e.getMessage());
                Toast.makeText(this, "Error retrieving contact information", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_GROCERY_SELECTION && resultCode == RESULT_OK && data != null) {
            // Handle grocery selection result
            ArrayList<String> selectedItems = data.getStringArrayListExtra("selected_items");
            if (selectedItems != null && !selectedItems.isEmpty()) {
                // Build the task description from selected items
                StringBuilder description = new StringBuilder(etTaskDescription.getText().toString());
                if (description.length() > 0 && !description.toString().endsWith("\n")) {
                    description.append("\n\n");
                }
                
                description.append("Shopping list:\n");
                for (String item : selectedItems) {
                    description.append("- ").append(item).append("\n");
                }
                
                etTaskDescription.setText(description.toString());
                
                // Set a default title if empty
                if (TextUtils.isEmpty(etTaskTitle.getText())) {
                    etTaskTitle.setText("Buy groceries");
                }
            }
        }
    }
    
    private void loadTask(String taskId) {
        String userId = auth.getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference taskRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("shopping_tasks")
            .child(taskId);
            
        taskRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentTask = snapshot.getValue(ShoppingTask.class);
                    populateTaskDetails();
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(TaskDetailActivity.this, "Error loading task: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateTaskDetails() {
        if (currentTask == null) return;
        
        etTaskTitle.setText(currentTask.getTitle());
        etTaskDescription.setText(currentTask.getDescription());
        
        // Set status chip
        if (currentTask.getStatus() != null) {
            switch (currentTask.getStatus()) {
                case PENDING:
                    chipPending.setChecked(true);
                    break;
                case IN_PROGRESS:
                    chipInProgress.setChecked(true);
                    break;
                case COMPLETED:
                    chipCompleted.setChecked(true);
                    break;
                case DELEGATED:
                    chipDelegated.setChecked(true);
                    break;
            }
        }
        
        // Set due date and time
        if (currentTask.getDueDate() != null) {
            dueDateTime.setTime(currentTask.getDueDate());
            updateDateTimeDisplay();
        }
        
        // Set contact info
        etContactName.setText(currentTask.getAssignedContactName());
        etContactPhone.setText(currentTask.getAssignedContactPhone());
    }
    
    private void saveTask() {
        if (!validateForm()) return;
        
        // Show loading indicator
        final View loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
        
        try {
            // Get task details from form
            String title = etTaskTitle.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            ShoppingTask.Status status = getSelectedStatus();
            String contactName = etContactName.getText().toString().trim();
            String contactPhone = etContactPhone.getText().toString().trim();
            Date dueDate = dueDateTime.getTime();
            
            // Update task object
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setStatus(status);
            currentTask.setAssignedContactName(contactName);
            currentTask.setAssignedContactPhone(contactPhone);
            currentTask.setDueDate(dueDate);
            currentTask.setUpdatedAtMillis(System.currentTimeMillis());
            
            if (currentTask.getCreatedBy() == null && auth.getCurrentUser() != null) {
                currentTask.setCreatedBy(auth.getCurrentUser().getUid());
            }
            
            if (currentTask.getCreatedAtMillis() == null) {
                currentTask.setCreatedAtMillis(System.currentTimeMillis());
            }
            
            // Save to Firebase Realtime Database
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.database.DatabaseReference tasksRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("shopping_tasks");
                
            if (taskId == null) {
                // New task - generate a new ID
                taskId = tasksRef.push().getKey();
                currentTask.setId(taskId);
            }
            
            // Save the task
            tasksRef.child(taskId).setValue(currentTask)
                .addOnSuccessListener(aVoid -> {
                    // Hide loading indicator
                    if (loadingOverlay != null) {
                        loadingOverlay.setVisibility(View.GONE);
                    }
                    
                    // Set result to indicate success
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(taskId == null ? "task_created" : "task_updated", true);
                    resultIntent.putExtra("task_title", title);
                    setResult(RESULT_OK, resultIntent);
                    
                    // Show success message and finish
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    if (loadingOverlay != null) {
                        loadingOverlay.setVisibility(View.GONE);
                    }
                    
                    Log.e(TAG, "Error saving task: " + e.getMessage());
                    Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            
        } catch (Exception e) {
            // Hide loading indicator in case of any exception
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
            
            Log.e(TAG, "Error in saveTask: " + e.getMessage());
            Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validateForm() {
        boolean valid = true;
        
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTaskTitle.setError("Required");
            valid = false;
        } else {
            etTaskTitle.setError(null);
        }
        
        if (statusChipGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        return valid;
    }
    
    private ShoppingTask.Status getSelectedStatus() {
        int checkedChipId = statusChipGroup.getCheckedChipId();
        
        if (checkedChipId == R.id.chipPending) {
            return ShoppingTask.Status.PENDING;
        } else if (checkedChipId == R.id.chipInProgress) {
            return ShoppingTask.Status.IN_PROGRESS;
        } else if (checkedChipId == R.id.chipCompleted) {
            return ShoppingTask.Status.COMPLETED;
        } else if (checkedChipId == R.id.chipDelegated) {
            return ShoppingTask.Status.DELEGATED;
        }
        
        return ShoppingTask.Status.PENDING; // Default
    }
    
    private void sendTaskViaSMS() {
        String phoneNumber = etContactPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if we have SMS permission
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            // Request the permission
            showSmsPermissionExplanationDialog();
            return;
        }
        
        // We have permission, send the SMS
        sendSMS(phoneNumber);
    }
    
    private void sendSMS(String phoneNumber) {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String dueDateTime = etDueDate.getText().toString() + " at " + etDueTime.getText().toString();
        
        String message = "Shopping Task: " + title + "\n" +
                "Description: " + description + "\n" +
                "Due: " + dueDateTime + "\n" +
                "From: MealMate App";
        
        try {
            // Use the most basic and reliable approach
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            
            startActivity(intent);
            
            // Update task status to delegated
            chipDelegated.setChecked(true);
            
            // Don't save the task or finish the activity
            // Let the user click Save when they're ready
            Toast.makeText(this, "SMS app opened. Click Save to update task status.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "SMS failed: " + e.getMessage(), e);
            Toast.makeText(this, "Could not open SMS app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupChipListeners() {
        // Set initial text colors
        updateChipTextColors();
        
        // Add listeners to update text colors when chips are checked/unchecked
        chipPending.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipTextColors());
        chipInProgress.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipTextColors());
        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipTextColors());
        chipDelegated.setOnCheckedChangeListener((buttonView, isChecked) -> updateChipTextColors());
    }
    
    private void updateChipTextColors() {
        // Set text color based on checked state
        chipPending.setTextColor(chipPending.isChecked() ? 
                ContextCompat.getColor(this, android.R.color.white) : 
                ContextCompat.getColor(this, R.color.text_primary));
        
        chipInProgress.setTextColor(chipInProgress.isChecked() ? 
                ContextCompat.getColor(this, android.R.color.white) : 
                ContextCompat.getColor(this, R.color.text_primary));
        
        chipCompleted.setTextColor(chipCompleted.isChecked() ? 
                ContextCompat.getColor(this, android.R.color.white) : 
                ContextCompat.getColor(this, R.color.text_primary));
        
        chipDelegated.setTextColor(chipDelegated.isChecked() ? 
                ContextCompat.getColor(this, android.R.color.white) : 
                ContextCompat.getColor(this, R.color.text_primary));
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
    public void onBackPressed() {
        // Check if loading overlay is visible
        View loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
            // Hide loading overlay
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
            return;
        }
        
        super.onBackPressed();
    }
    
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }
    
    private void showSmsPermissionExplanationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("SMS Permission Required")
            .setMessage("To send tasks via SMS, the app needs permission to send text messages. Would you like to grant this permission?")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                requestPermission(Manifest.permission.SEND_SMS, PERMISSIONS_REQUEST_SEND_SMS);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(this, "SMS permission denied. Cannot send task via SMS.", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showSettingsDialog(String permissionType) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(permissionType + " Permission Required")
            .setMessage("You have denied " + permissionType + " permission permanently. To use this feature, please enable it in app settings.")
            .setPositiveButton("Go to Settings", (dialog, which) -> {
                // Open app settings
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show();
    }
    
    private void showContactsPermissionExplanationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Contacts Permission Required")
            .setMessage("To select a contact for task delegation, the app needs permission to access your contacts. Would you like to grant this permission?")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                requestPermission(Manifest.permission.READ_CONTACTS, PERMISSIONS_REQUEST_READ_CONTACTS);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(this, "Contacts permission denied. Cannot select contact.", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }
} 