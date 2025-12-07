package com.megaproject.urbanspace.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.urbanspace.Model.WasteSchedule;
import com.megaproject.urbanspace.R;

import java.util.List;

public class WasteScheduleAdapter extends RecyclerView.Adapter<WasteScheduleAdapter.ViewHolder> {

    private List<WasteSchedule> scheduleList;

    public WasteScheduleAdapter(List<WasteSchedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public WasteScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waste_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WasteScheduleAdapter.ViewHolder holder, int position) {
        WasteSchedule schedule = scheduleList.get(position);

        holder.nameText.setText(schedule.getType());
        holder.statusText.setText(schedule.getStatus());
        holder.dateText.setText("📅 " + schedule.getDate());
        holder.timeText.setText("🕒 " + schedule.getTime());
        holder.areaText.setText("📍 " + schedule.getArea());
        holder.frequencyText.setText(schedule.getFrequency());
        holder.instructionsText.setText("ℹ️ " + schedule.getInstructions());

        // Example: change icon based on type
        switch (schedule.getType()) {
            case "General Waste":
                holder.wasteIcon.setImageResource(R.drawable.ic_trash);
                break;
            case "Recycling Waste":
                holder.wasteIcon.setImageResource(R.drawable.ic_recycle);
                break;
            case "Organic Waste":
                holder.wasteIcon.setImageResource(R.drawable.ic_organic);
                break;
            case "Hazardous Waste":
                holder.wasteIcon.setImageResource(R.drawable.ic_hazardous);
                break;
            default:
                holder.wasteIcon.setImageResource(R.drawable.ic_trash);
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public void updateList(List<WasteSchedule> newList) {
        scheduleList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wasteIcon;
        TextView nameText, statusText, dateText, timeText, areaText, frequencyText, instructionsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wasteIcon = itemView.findViewById(R.id.wasteIcon);
            nameText = itemView.findViewById(R.id.nameText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            areaText = itemView.findViewById(R.id.areaText);
            frequencyText = itemView.findViewById(R.id.frequencyText);
            instructionsText = itemView.findViewById(R.id.instructionsText);
        }
    }
}
