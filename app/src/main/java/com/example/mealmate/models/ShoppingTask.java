package com.example.mealmate.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ShoppingTask {
    
    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        DELEGATED
    }
    
    @DocumentId
    private String id;
    private String title;
    private String description;
    private Status status;
    private String assignedContactName;
    private String assignedContactPhone;
    private Date dueDate;
    private String createdBy;
    private Long createdAtMillis;
    private Long updatedAtMillis;
    
    // Empty constructor needed for Firestore and Realtime Database
    public ShoppingTask() {
    }
    
    public ShoppingTask(String title, String description, Status status, 
                        String assignedContactName, String assignedContactPhone, 
                        Date dueDate, String createdBy) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedContactName = assignedContactName;
        this.assignedContactPhone = assignedContactPhone;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
        this.updatedAtMillis = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAtMillis = System.currentTimeMillis();
    }
    
    public String getAssignedContactName() {
        return assignedContactName;
    }
    
    public void setAssignedContactName(String assignedContactName) {
        this.assignedContactName = assignedContactName;
    }
    
    public String getAssignedContactPhone() {
        return assignedContactPhone;
    }
    
    public void setAssignedContactPhone(String assignedContactPhone) {
        this.assignedContactPhone = assignedContactPhone;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getCreatedAtMillis() {
        return createdAtMillis;
    }
    
    public void setCreatedAtMillis(Long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }
    
    public Long getUpdatedAtMillis() {
        return updatedAtMillis;
    }
    
    public void setUpdatedAtMillis(Long updatedAtMillis) {
        this.updatedAtMillis = updatedAtMillis;
    }
    
    // Helper methods
    public boolean isOverdue() {
        if (dueDate == null) return false;
        return dueDate.before(new Date()) && status != Status.COMPLETED;
    }
    
    public String getStatusDisplayName() {
        switch (status) {
            case PENDING:
                return "Pending";
            case IN_PROGRESS:
                return "In Progress";
            case COMPLETED:
                return "Completed";
            case DELEGATED:
                return "Delegated";
            default:
                return "Unknown";
        }
    }
} 