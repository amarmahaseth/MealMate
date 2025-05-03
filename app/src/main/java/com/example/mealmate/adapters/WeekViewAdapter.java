package com.example.mealmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.R;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekViewAdapter extends RecyclerView.Adapter<WeekViewAdapter.WeekDayViewHolder> {
    private List<Date> dates;
    private OnDayClickListener listener;
    private int selectedPosition = -1;

    public interface OnDayClickListener {
        void onDayClick(Date date, int position);
    }

    public WeekViewAdapter(OnDayClickListener listener) {
        this.listener = listener;
        this.dates = new ArrayList<>();
        generateWeekDates();
    }

    private void generateWeekDates() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Add 7 days starting from today
        for (int i = 0; i < 7; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        
        // Set the width to match_parent for ViewPager2
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(params);
        
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        Date date = dates.get(position);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());

        holder.dayName.setText(dayNameFormat.format(date));
        holder.dayNumber.setText(dayNumberFormat.format(date));

        // Set selected state
        holder.itemView.setSelected(position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDayClick(date, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public Date getSelectedDate() {
        return selectedPosition >= 0 ? dates.get(selectedPosition) : null;
    }

    static class WeekDayViewHolder extends RecyclerView.ViewHolder {
        TextView dayName;
        TextView dayNumber;

        WeekDayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayName = itemView.findViewById(R.id.dayName);
            dayNumber = itemView.findViewById(R.id.dayNumber);
        }
    }
} 