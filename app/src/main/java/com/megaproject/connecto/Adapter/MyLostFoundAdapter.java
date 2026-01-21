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
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onMenuClick(LostFoundItem item);
    }

    public MyLostFoundAdapter(Context context, List<LostFoundItem> items, OnItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public MyLostFoundAdapter(Context context, List<LostFoundItem> items) {
        this(context, items, null);
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
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost);
        } else {
            holder.tvTypeBadge.setText("FOUND");
            holder.tvTypeBadge.setTextColor(Color.parseColor("#047857")); // Green 700
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost);
            holder.tvTypeBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#ECFDF5"))); // Green 50
        }

        // Status Logic
        String status = item.getStatus();
        if (status == null) status = "Active";
        
        // Capitalize first letter for display if needed, but assuming data is clean or we just display as is
        // For display consistency let's title case it if it's lower case
        if (status.length() > 0) {
            status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        }

        holder.tvStatus.setText(status);
        holder.btnMarkRecovered.setVisibility(View.GONE); // Default hidden

        switch (status) { // Case sensitive now that we title-cased it
            case "Active":
                holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))); // Blue
                holder.tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                holder.chipStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))); // Blue 50
                holder.btnMarkRecovered.setVisibility(View.VISIBLE);
                break;
            case "Matched":
                holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F97316"))); // Orange
                holder.tvStatus.setTextColor(Color.parseColor("#F97316"));
                holder.chipStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF7ED"))); // Orange 50
                holder.btnMarkRecovered.setVisibility(View.VISIBLE);
                break;
            case "Recovered":
                holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#16A34A"))); // Green
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A"));
                holder.chipStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F0FDF4"))); // Green 50
                break;
            case "Closed":
                holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6B7280"))); // Gray
                holder.tvStatus.setTextColor(Color.parseColor("#6B7280"));
                holder.chipStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F9FAFB"))); // Gray 50
                break;
            default:
                // Default to Active style
                holder.viewStatusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6")));
                holder.tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                holder.chipStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                holder.btnMarkRecovered.setVisibility(View.VISIBLE);
                break;
        }

        // Image Loading
       if (item.getImageResourceId() != 0) {
            holder.ivItemImage.setImageResource(item.getImageResourceId());
            holder.ivItemImage.setPadding(0,0,0,0);
        } else {
           if("lost".equalsIgnoreCase(item.getType())) {
                 holder.ivItemImage.setImageResource(R.drawable.ic_search);
           } else {
               holder.ivItemImage.setImageResource(R.drawable.ic_person_filled); 
           }
            holder.ivItemImage.setPadding(20,20,20,20);
        }
        
        // Menu Click Listener
        holder.btnMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(item);
            }
        });

        // Mark Recovered Click Listener
        holder.btnMarkRecovered.setOnClickListener(v -> {
            // Action to mark as recovered
        });

        // Item Click Listener to Details
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.megaproject.connecto.LostFoundItemDetailsActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("status", item.getType()); // Using type as status badge (LOST/FOUND)
            intent.putExtra("date", item.getDateTime());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("description", item.getDescription() != null ? item.getDescription() : "No description provided.");
            intent.putExtra("image_res_id", item.getImageResourceId());
            
            if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
                intent.putStringArrayListExtra("image_urls", new java.util.ArrayList<>(item.getImageUris()));
            }
            
            intent.putExtra("reporter_name", item.getContactName() != null ? item.getContactName() : "You");
            
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvTypeBadge, tvStatus;
        ImageView ivItemImage, btnMenu;
        View viewStatusDot, btnMarkRecovered, chipStatus;

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
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
