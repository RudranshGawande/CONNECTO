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

import com.megaproject.connecto.Model.IssueActivity;
import com.megaproject.connecto.R;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private Context context;
    private List<IssueActivity> activityList;

    public RecentActivityAdapter(Context context, List<IssueActivity> activityList) {
        this.context = context;
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssueActivity activity = activityList.get(position);

        holder.tvTitle.setText(activity.getTitle());
        holder.tvDetails.setText(activity.getDate());
        holder.imgThumb.setImageResource(activity.getImageResId());
        
        holder.tvStatus.setText(activity.getStatus().toUpperCase());
        
        // Simple status coloring logic
        if ("SENT".equalsIgnoreCase(activity.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#059669")); // Green
            holder.tvStatus.setBackgroundColor(Color.parseColor("#D1FAE5")); // Light Green
        } else if ("DRAFT".equalsIgnoreCase(activity.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#6B7280")); // Gray
            holder.tvStatus.setBackgroundColor(Color.TRANSPARENT);
        } else {
             holder.tvStatus.setTextColor(Color.BLACK);
             holder.tvStatus.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvStatus;
        ImageView imgThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDetails = itemView.findViewById(R.id.tv_activity_details);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            imgThumb = itemView.findViewById(R.id.img_activity_thumb);
        }
    }
}
