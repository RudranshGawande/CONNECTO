package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.megaproject.connecto.Model.Report;
import com.megaproject.connecto.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
    private Context context;
    private List<Report> reports;

    public ReportsAdapter(Context context, List<Report> reports) {
        this.context = context;
        this.reports = reports;
    }

    public void setReports(List<Report> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.tvCategory.setText(report.getCategory());
        holder.tvAddress.setText(report.getAddress());
        holder.tvStatus.setText(report.getStatus());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText("Reported " + sdf.format(new Date(report.getTimestamp())));

        // Set status color
        switch (report.getStatus()) {
            case "Pending":
                holder.tvStatus.setBackgroundTintList(context.getColorStateList(R.color.gray_200)); // Make sure these colors exist or use hex
                holder.tvStatus.setTextColor(Color.parseColor("#757575"));
                break;
            case "In Progress":
                holder.tvStatus.setBackgroundTintList(context.getColorStateList(R.color.orange_100)); // Assuming existence or fallback
                // Quick fix for potentially missing colors, using safe approach with Color.parseColor if resources not sure
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0")); 
                holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"));
                break;
            case "Resolved":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
        }

        // Set icon based on category (Simple mapping)
        int iconRes = R.drawable.ic_layers; // Default
        if (report.getCategory().contains("Pothole")) iconRes = R.drawable.ic_warning;
        else if (report.getCategory().contains("Light")) iconRes = R.drawable.ic_light_mode;
        else if (report.getCategory().contains("Waste")) iconRes = R.drawable.ic_recycling;
        else if (report.getCategory().contains("Graffiti")) iconRes = R.drawable.ic_edit;
        
        holder.imgCategory.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAddress, tvStatus;
        ImageView imgCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvStatus = itemView.findViewById(R.id.chip_status);
            imgCategory = itemView.findViewById(R.id.img_category_icon);
        }
    }
}
