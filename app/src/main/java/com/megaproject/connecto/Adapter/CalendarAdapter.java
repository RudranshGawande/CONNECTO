package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(Date date);
    }

    private Context context;
    private List<Date> days; // Dates for the current grid. Null for empty slots.
    private Date startDate;
    private Date endDate;
    private OnDayClickListener listener;

    // Helper to check same day
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public CalendarAdapter(Context context, List<Date> days, OnDayClickListener listener) {
        this.context = context;
        this.days = days;
        this.listener = listener;
    }

    public void updateRange(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;
        notifyDataSetChanged();
    }
    
    public void updateDays(List<Date> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Date date = days.get(position);

        // Reset
        holder.tvDay.setBackgroundResource(0);
        holder.tvDay.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray_400));
        holder.viewRangeHighlight.setVisibility(View.INVISIBLE);
        holder.viewRangeStart.setVisibility(View.INVISIBLE);
        holder.viewRangeEnd.setVisibility(View.INVISIBLE);
        holder.tvDay.setText("");
        holder.itemView.setOnClickListener(null);

        if (date == null) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        holder.tvDay.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        
        // Basic Text Color
        holder.tvDay.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.home_text_main));

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(date);
        });

        // Selection Logic
        boolean isStart = isSameDay(date, startDate);
        boolean isEnd = isSameDay(date, endDate);
        boolean isInRange = false;

        if (startDate != null && endDate != null) {
            if (date.after(startDate) && date.before(endDate)) {
                isInRange = true;
            }
        }

        if (isStart || isEnd) {
             holder.tvDay.setBackgroundResource(R.drawable.circle_primary);
             holder.tvDay.setTextColor(Color.WHITE);
        }

        if (isInRange) {
             holder.viewRangeHighlight.setVisibility(View.VISIBLE);
        }
        
        // Visual polish for start/end connectors
        if (isStart && endDate != null) {
            holder.viewRangeStart.setVisibility(View.VISIBLE);
        }
        if (isEnd && startDate != null) {
            holder.viewRangeEnd.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewRangeHighlight, viewRangeStart, viewRangeEnd;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            viewRangeHighlight = itemView.findViewById(R.id.viewRangeHighlight);
            viewRangeStart = itemView.findViewById(R.id.viewRangeStart);
            viewRangeEnd = itemView.findViewById(R.id.viewRangeEnd);
        }
    }
}
