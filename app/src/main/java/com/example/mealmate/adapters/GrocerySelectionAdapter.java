package com.example.mealmate.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.R;
import com.example.mealmate.models.GroceryItem;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class GrocerySelectionAdapter extends RecyclerView.Adapter<GrocerySelectionAdapter.SelectionViewHolder> {
    
    private List<GroceryItem> items = new ArrayList<>();
    private List<GroceryItem> selectedItems = new ArrayList<>();
    private OnItemSelectedListener itemSelectedListener;
    
    public interface OnItemSelectedListener {
        void onItemSelected(GroceryItem item, boolean isSelected);
    }
    
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }
    
    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery_selection, parent, false);
        return new SelectionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void setItems(List<GroceryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    public List<GroceryItem> getSelectedItems() {
        return selectedItems;
    }
    
    class SelectionViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView tvName;
        private final TextView tvQuantity;
        private final Chip tvCategory;
        
        SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    GroceryItem item = items.get(position);
                    if (isChecked) {
                        if (!selectedItems.contains(item)) {
                            selectedItems.add(item);
                        }
                    } else {
                        selectedItems.remove(item);
                    }
                    
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onItemSelected(item, isChecked);
                    }
                }
            });
        }
        
        void bind(GroceryItem item) {
            tvName.setText(item.getName());
            tvQuantity.setText(item.getQuantity());
            tvCategory.setText(item.getCategory());
            checkBox.setChecked(selectedItems.contains(item));
            setCategoryChipStyle(item.getCategory());
        }
        
        private void setCategoryChipStyle(String category) {
            int colorResId;
            switch (category) {
                case GroceryItem.CATEGORY_PRODUCE:
                    colorResId = android.R.color.holo_green_light;
                    break;
                case GroceryItem.CATEGORY_MEAT:
                    colorResId = android.R.color.holo_red_light;
                    break;
                case GroceryItem.CATEGORY_DAIRY:
                    colorResId = android.R.color.holo_blue_light;
                    break;
                case GroceryItem.CATEGORY_PANTRY:
                    colorResId = android.R.color.holo_orange_light;
                    break;
                case GroceryItem.CATEGORY_BAKERY:
                    colorResId = android.R.color.holo_purple;
                    break;
                case GroceryItem.CATEGORY_FROZEN:
                    colorResId = android.R.color.holo_blue_bright;
                    break;
                case GroceryItem.CATEGORY_BEVERAGES:
                    colorResId = android.R.color.holo_blue_dark;
                    break;
                case GroceryItem.CATEGORY_SNACKS:
                    colorResId = android.R.color.holo_orange_dark;
                    break;
                case GroceryItem.CATEGORY_CONDIMENTS:
                    colorResId = android.R.color.holo_red_dark;
                    break;
                default:
                    colorResId = android.R.color.darker_gray;
                    break;
            }
            
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            tvCategory.setChipBackgroundColor(ColorStateList.valueOf(color));
            tvCategory.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        }
    }
} 