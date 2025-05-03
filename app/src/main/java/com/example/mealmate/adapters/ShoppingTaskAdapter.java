package com.example.mealmate.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.models.ShoppingTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShoppingTaskAdapter extends RecyclerView.Adapter<ShoppingTaskAdapter.TaskViewHolder> {

    private final List<ShoppingTask> tasks;
    private final Context context;
    private final OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskClick(ShoppingTask task, int position);
        void onStatusChange(ShoppingTask task, int position);
        void onDeleteTask(ShoppingTask task, int position);
        void onEditTask(ShoppingTask task, int position);
    }

    public ShoppingTaskAdapter(Context context, List<ShoppingTask> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ShoppingTask task = tasks.get(position);
        holder.bind(task, position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTask(ShoppingTask task, int position) {
        if (position >= 0 && position < tasks.size()) {
            tasks.set(position, task);
            notifyItemChanged(position);
        }
    }

    public void removeTask(int position) {
        if (position >= 0 && position < tasks.size()) {
            tasks.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addTask(ShoppingTask task) {
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View statusIndicator;
        private final TextView tvTaskTitle;
        private final TextView tvTaskStatus;
        private final TextView tvAssignedContact;
        private final TextView tvDueDate;
        private final View assignedContactContainer;
        private final ImageButton btnOptions;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvAssignedContact = itemView.findViewById(R.id.tvAssignedContact);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            assignedContactContainer = itemView.findViewById(R.id.assignedContactContainer);
            btnOptions = itemView.findViewById(R.id.btnOptions);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position), position);
                }
            });

            tvTaskStatus.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStatusChange(tasks.get(position), position);
                }
            });
            
            btnOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showOptionsMenu(v, position);
                }
            });
        }
        
        private void showOptionsMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.menu_task_options);
            
            popup.setOnMenuItemClickListener(item -> {
                ShoppingTask task = tasks.get(position);
                int itemId = item.getItemId();
                
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditTask(task, position);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteTask(task, position);
                    }
                    return true;
                }
                
                return false;
            });
            
            popup.show();
        }

        void bind(ShoppingTask task, int position) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskStatus.setText(task.getStatusDisplayName());
            
            // Set status color
            int statusColor;
            switch (task.getStatus()) {
                case COMPLETED:
                    statusColor = ContextCompat.getColor(context, R.color.status_completed);
                    break;
                case IN_PROGRESS:
                    statusColor = ContextCompat.getColor(context, R.color.status_in_progress);
                    break;
                case DELEGATED:
                    statusColor = ContextCompat.getColor(context, R.color.status_delegated);
                    break;
                case PENDING:
                default:
                    statusColor = task.isOverdue() ? 
                            ContextCompat.getColor(context, R.color.status_overdue) : 
                            ContextCompat.getColor(context, R.color.status_pending);
                    break;
            }
            
            statusIndicator.setBackgroundColor(statusColor);
            tvTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, 
                    getStatusColorResource(task.getStatus(), task.isOverdue())));
            
            // Set assigned contact
            if (task.getAssignedContactName() != null && !task.getAssignedContactName().isEmpty()) {
                assignedContactContainer.setVisibility(View.VISIBLE);
                tvAssignedContact.setText(task.getAssignedContactName());
            } else {
                assignedContactContainer.setVisibility(View.GONE);
            }
            
            // Set due date
            if (task.getDueDate() != null) {
                String formattedDate = formatDueDate(task.getDueDate());
                tvDueDate.setText(formattedDate);
                
                if (task.isOverdue()) {
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.status_overdue));
                } else {
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                }
            } else {
                tvDueDate.setText("No due date");
            }
        }
        
        private int getStatusColorResource(ShoppingTask.Status status, boolean isOverdue) {
            if (isOverdue && status == ShoppingTask.Status.PENDING) {
                return R.color.status_overdue;
            }
            
            switch (status) {
                case COMPLETED:
                    return R.color.status_completed;
                case IN_PROGRESS:
                    return R.color.status_in_progress;
                case DELEGATED:
                    return R.color.status_delegated;
                case PENDING:
                default:
                    return R.color.status_pending;
            }
        }
        
        private String formatDueDate(Date dueDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar today = Calendar.getInstance();
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            
            if (isSameDay(dueCal, today)) {
                return "Today, " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(dueDate);
            } else if (isSameDay(dueCal, tomorrow)) {
                return "Tomorrow, " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(dueDate);
            } else {
                return dateFormat.format(dueDate);
            }
        }
        
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
} 