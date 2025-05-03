package com.example.mealmate.adapters;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.example.mealmate.models.GroceryItem;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryViewHolder> {
    private static final String TAG = "GroceryListAdapter";
    private List<GroceryItem> items = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnItemCheckedListener itemCheckedListener;
    private OnItemEditListener itemEditListener;
    private OnItemDeleteListener itemDeleteListener;

    // Default constructor
    public GroceryListAdapter() {
        // Empty constructor
    }

    public interface OnItemClickListener {
        void onItemClick(GroceryItem item);
    }

    public interface OnItemCheckedListener {
        void onItemChecked(GroceryItem item, boolean isChecked);
    }

    public interface OnItemEditListener {
        void onItemEdit(GroceryItem item);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(GroceryItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        this.itemCheckedListener = listener;
    }

    public void setOnItemEditListener(OnItemEditListener listener) {
        this.itemEditListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.itemDeleteListener = listener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<GroceryItem> getItems() {
        return items;
    }

    public void setItems(List<GroceryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void addItem(GroceryItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public int findItemPosition(String itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    class GroceryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox checkbox;
        private final TextView tvName;
        private final TextView tvQuantity;
        private final Chip tvCategory;
        private final View contentLayout;
        private final ImageButton btnItemMenu;

        GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            btnItemMenu = itemView.findViewById(R.id.btnItemMenu);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(items.get(position));
                }
            });

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemCheckedListener != null && buttonView.isPressed()) {
                    itemCheckedListener.onItemChecked(items.get(position), isChecked);
                }
            });

            btnItemMenu.setOnClickListener(v -> showItemMenu(v, getAdapterPosition()));
        }

        private void showItemMenu(View view, int position) {
            if (position == RecyclerView.NO_POSITION) return;

            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.menu_grocery_item);

            popup.setOnMenuItemClickListener(item -> {
                GroceryItem groceryItem = items.get(position);
                int itemId = item.getItemId();
                
                if (itemId == R.id.action_edit) {
                    if (itemEditListener != null) {
                        itemEditListener.onItemEdit(groceryItem);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (itemDeleteListener != null) {
                        itemDeleteListener.onItemDelete(groceryItem);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }

        void bind(GroceryItem item, int position) {
            tvName.setText(item.getName());
            tvQuantity.setText(item.getQuantity());
            tvCategory.setText(item.getCategory());
            checkbox.setChecked(item.isPurchased());
            updateStrikeThrough(tvName, item.isPurchased());
            setCategoryChipStyle(item.getCategory());
            
            // Update constraints for the content layout
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentLayout.getLayoutParams();
            params.setMarginStart(8);
            contentLayout.setLayoutParams(params);
        }

        private void updateStrikeThrough(TextView textView, boolean isPurchased) {
            if (isPurchased) {
                textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
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