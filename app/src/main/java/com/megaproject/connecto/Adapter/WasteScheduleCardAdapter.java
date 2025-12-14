package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Model.WasteSchedule;
import com.megaproject.connecto.R;

import java.util.List;

public class WasteScheduleCardAdapter extends RecyclerView.Adapter<WasteScheduleCardAdapter.ViewHolder> {

    private Context context;
    private List<WasteSchedule> scheduleList;

    public WasteScheduleCardAdapter(Context context, List<WasteSchedule> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_waste_schedule_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WasteSchedule schedule = scheduleList.get(position);
        
        holder.wasteTypeText.setText(schedule.getType());
        holder.wasteDateText.setText(schedule.getDate());
        holder.wasteTimeText.setText(schedule.getTime());
        
        // Set icon and colors
        if (schedule.getIconResource() != 0) {
            holder.wasteIcon.setImageResource(schedule.getIconResource());
            
            if (schedule.getBackgroundColor() != 0) {
                holder.wasteIcon.setBackgroundResource(schedule.getBackgroundColor());
            }
            
            if (schedule.getIconColor() != 0) {
                holder.wasteIcon.setColorFilter(
                    ContextCompat.getColor(context, schedule.getIconColor())
                );
            }
        }
        
        // Apply opacity for future items
        if (schedule.getStatus().equals("Next Week")) {
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wasteIcon;
        TextView wasteTypeText;
        TextView wasteDateText;
        TextView wasteTimeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wasteIcon = itemView.findViewById(R.id.wasteIcon);
            wasteTypeText = itemView.findViewById(R.id.wasteTypeText);
            wasteDateText = itemView.findViewById(R.id.wasteDateText);
            wasteTimeText = itemView.findViewById(R.id.wasteTimeText);
        }
    }
}


