package com.example.mealmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectedItemsAdapter extends RecyclerView.Adapter<SelectedItemsAdapter.ItemViewHolder> {
    private List<Map<String, String>> items = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_grocery, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Map<String, String> item = items.get(position);
        String name = item.get("name");
        String quantity = item.get("quantity");
        String text = name + (quantity != null && !quantity.isEmpty() ? " (" + quantity + ")" : "");
        holder.tvName.setText(text);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Map<String, String>> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<Map<String, String>> getItems() {
        return items;
    }

    public void addItem(Map<String, String> item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnRemove;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
} 