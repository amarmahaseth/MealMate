package com.example.mealmate.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.models.Store;

import java.util.ArrayList;
import java.util.List;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.StoreViewHolder> {
    private List<Store> stores;
    private final Context context;
    private final OnStoreClickListener listener;

    public interface OnStoreClickListener {
        void onStoreClick(Store store);
        void onNavigateClick(Store store);
    }

    public StoresAdapter(Context context, OnStoreClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.stores = new ArrayList<>();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.bind(store);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
        notifyDataSetChanged();
    }

    public void addStore(Store store) {
        stores.add(0, store);
        notifyItemInserted(0);
    }

    public void removeStore(Store store) {
        int position = stores.indexOf(store);
        if (position != -1) {
            stores.remove(position);
            notifyItemRemoved(position);
        }
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView storeName;
        private final TextView storeAddress;
        private final ImageButton navigateButton;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeName);
            storeAddress = itemView.findViewById(R.id.storeAddress);
            navigateButton = itemView.findViewById(R.id.navigateButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStoreClick(stores.get(position));
                }
            });

            navigateButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNavigateClick(stores.get(position));
                }
            });
        }

        void bind(Store store) {
            storeName.setText(store.getName());
            storeAddress.setText(store.getAddress());
        }
    }
} 