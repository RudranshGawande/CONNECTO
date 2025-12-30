package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;

import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

    private Context context;
    private List<LostFoundItem> items;

    public LostFoundAdapter(Context context, List<LostFoundItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost_found, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getCategory() + " • " + item.getDateTime());
        holder.tvLocation.setText(item.getLocation());
        
        // Tag Logic
        if ("lost".equalsIgnoreCase(item.getType())) {
            holder.tvTag.setText("LOST");
            holder.tvTag.setTextColor(Color.parseColor("#dc2626")); // Red-600
            holder.tvTag.setBackgroundResource(R.drawable.bg_tag_lost);
        } else {
            holder.tvTag.setText("FOUND");
            holder.tvTag.setTextColor(Color.parseColor("#059669")); // Green-600
            // Reuse lost tag bg for shape
            holder.tvTag.setBackgroundResource(R.drawable.bg_tag_found); 
        }

        // Icon & Color Logic based on Category
        int iconRes = R.drawable.ic_category;
        int bgTint = Color.parseColor("#F3F4F6"); // Gray default
        int iconTint = Color.parseColor("#6B7280"); // Gray default

        String cat = (item.getCategory() != null) ? item.getCategory().toLowerCase() : "";
        String fullText = (item.getTitle() + " " + cat).toLowerCase();
        
        if (cat.contains("pet") || cat.contains("animal")) {
            iconRes = R.drawable.ic_pets;
            bgTint = Color.parseColor("#FFF7ED"); // Orange-50
            iconTint = Color.parseColor("#ea580c"); // Orange-500
        } else if (cat.contains("electronic") || cat.contains("phone")  || cat.contains("iphone")) {
            iconRes = R.drawable.ic_smartphone;
            bgTint = Color.parseColor("#EFF6FF"); // Blue-50
            iconTint = Color.parseColor("#3B82F6"); // Blue-500
        } else if (cat.contains("personal")) {
             // Sub-categories within Personal
             if (fullText.contains("key")) {
                 iconRes = R.drawable.ic_key;
                 bgTint = Color.parseColor("#F8FAFC"); // Slate-50 (using light gray)
                 iconTint = Color.parseColor("#64748B"); // Slate-500
             } else if (fullText.contains("card") || fullText.contains("visa")) {
                 iconRes = R.drawable.ic_credit_card;
                 bgTint = Color.parseColor("#EFF6FF"); // Blue-50
                 iconTint = Color.parseColor("#3B82F6"); // Blue-500 (Matches image)
             } else {
                 iconRes = R.drawable.ic_wallet;
                 bgTint = Color.parseColor("#F3F4F6"); // Gray-100
                 iconTint = Color.parseColor("#9CA3AF"); // Gray-400
             }
        } else if (cat.contains("accessories") || cat.contains("bag")) {
             if (fullText.contains("glass") || fullText.contains("ray-ban")) {
                 iconRes = R.drawable.ic_eyeglasses;
                 bgTint = Color.parseColor("#FFF7ED"); // Orange-50
                 iconTint = Color.parseColor("#F97316"); // Orange-500 (using a variant)
             } else if (fullText.contains("umbrella")) {
                 iconRes = R.drawable.ic_umbrella;
                 bgTint = Color.parseColor("#F0FDFA"); // Teal-50
                 iconTint = Color.parseColor("#14B8A6"); // Teal-500
             } else {
                 iconRes = R.drawable.ic_backpack;
                 bgTint = Color.parseColor("#F3E8FF"); // Purple-50/100
                 iconTint = Color.parseColor("#A855F7"); // Purple-500
             }
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setColorFilter(iconTint);
        holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(bgTint));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTag, tvSubtitle, tvLocation;
        ImageView ivIcon;
        FrameLayout iconContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}
