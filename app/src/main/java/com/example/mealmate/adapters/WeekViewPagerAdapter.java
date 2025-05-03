package com.example.mealmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmate.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekViewPagerAdapter extends RecyclerView.Adapter<WeekViewPagerAdapter.WeekDayViewHolder> {
    private final Context context;
    private final OnDayClickListener listener;
    private final List<Date> dates;
    private int selectedPosition = 0;

    public interface OnDayClickListener {
        void onDayClick(Date date, int position);
    }

    public WeekViewPagerAdapter(Context context) {
        this.context = context;
        this.listener = (OnDayClickListener) context;
        this.dates = generateWeekDates();
        
        // Find today's position in the dates list
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        for (int i = 0; i < dates.size(); i++) {
            Calendar date = Calendar.getInstance();
            date.setTime(dates.get(i));
            if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                selectedPosition = i;
                break;
            }
        }
    }

    private List<Date> generateWeekDates() {
        List<Date> weekDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Set to start of week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        // Add 7 days
        for (int i = 0; i < 7; i++) {
            weekDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return weekDates;
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_week_day, parent, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(params);
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        Date date = dates.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());
        
        holder.dayName.setText(dayNameFormat.format(date));
        holder.dayNumber.setText(dayNumberFormat.format(date));

        // Set selected state only based on selection, not today's date
        boolean isSelected = position == selectedPosition;
        holder.itemView.setSelected(isSelected);

        // Set text colors based on selection
        int textColor = isSelected ? 
            context.getResources().getColor(android.R.color.white) : 
            context.getResources().getColor(R.color.text_primary);
        holder.dayNumber.setTextColor(textColor);
        holder.dayName.setTextColor(isSelected ? 
            context.getResources().getColor(android.R.color.white) : 
            context.getResources().getColor(R.color.text_secondary));

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != holder.getAdapterPosition()) {
                int previousPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onDayClick(date, selectedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void setSelectedPosition(int position) {
        if (position != selectedPosition) {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void updateDates() {
        dates.clear();
        dates.addAll(generateWeekDates());
        notifyDataSetChanged();
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