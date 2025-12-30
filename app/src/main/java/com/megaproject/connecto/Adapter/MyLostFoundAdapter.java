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
import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;
import java.util.List;

public class MyLostFoundAdapter extends RecyclerView.Adapter<MyLostFoundAdapter.ViewHolder> {

    private Context context;
    private List<LostFoundItem> items;

    public MyLostFoundAdapter(Context context, List<LostFoundItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_lost_found, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getLocation() + " • " + item.getDateTime());

        // Type Badge (LOST/FOUND)
        if ("lost".equalsIgnoreCase(item.getType())) {
            holder.tvTypeBadge.setText("LOST");
            holder.tvTypeBadge.setTextColor(Color.parseColor("#B91C1C")); // Red 700
            holder.tvTypeBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF2F2"))); // Red 50
        } else {
            holder.tvTypeBadge.setText("FOUND");
            holder.tvTypeBadge.setTextColor(Color.parseColor("#047857")); // Green 700
            holder.tvTypeBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#ECFDF5"))); // Green 50
        }

        // Status Logic for Demo
        // Ideally LostFoundItem should have a status field. Assuming random for demo or passed in.
        // For this demo, let's derive it or hardcode based on position for variety.
        String status = "Active";
        if (position == 1) status = "Recovered";
        if (position == 2) status = "Closed";

        holder.tvStatus.setText(status);
        
        // Status Colors and Icon
        if ("Active".equals(status)) {
            holder.viewStatusDot.setVisibility(View.VISIBLE);
            holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2b8cee"))); // Primary
            holder.tvStatus.setTextColor(Color.parseColor("#2b8cee"));
            ((View)holder.tvStatus.getParent()).setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))); // Blue 50
        } else if ("Recovered".equals(status)) {
            holder.viewStatusDot.setVisibility(View.GONE); // Use icon? Check mark ideally but dot for now or reuse view as icon not easy without changing view type.
            // Let's keep dot but make it green/check
             holder.viewStatusDot.setVisibility(View.VISIBLE);
             holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))); // Green
            holder.tvStatus.setText("Recovered");
             holder.tvStatus.setTextColor(Color.parseColor("#059669"));
             ((View)holder.tvStatus.getParent()).setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#ECFDF5"))); // Green 50
        } else { // Closed
             holder.viewStatusDot.setVisibility(View.VISIBLE);
             holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6B7280"))); // Gray
            holder.tvStatus.setText("Closed");
             holder.tvStatus.setTextColor(Color.parseColor("#4B5563"));
             ((View)holder.tvStatus.getParent()).setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F3F4F6"))); // Gray 100
        }
        
        // In a real app, load different images
       if (item.getImageResourceId() != 0) {
            holder.ivItemImage.setImageResource(item.getImageResourceId());
            holder.ivItemImage.setPadding(0,0,0,0);
        } else {
           if("lost".equalsIgnoreCase(item.getType())) {
                 holder.ivItemImage.setImageResource(R.drawable.ic_search);
           } else {
               holder.ivItemImage.setImageResource(R.drawable.ic_person_filled); // Or shield
           }
            holder.ivItemImage.setPadding(20,20,20,20);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvTypeBadge, tvStatus;
        ImageView ivItemImage, btnMenu;
        View viewStatusDot, btnMarkRecovered;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvTypeBadge = itemView.findViewById(R.id.tvTypeBadge);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnMarkRecovered = itemView.findViewById(R.id.btnMarkRecovered);
        }
    }
}
