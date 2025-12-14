package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LostFoundResultAdapter extends RecyclerView.Adapter<LostFoundResultAdapter.ViewHolder> {

    private Context context;
    private List<LostFoundItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
        void onDeleteClick(LostFoundItem item);
        void onStatusChangeClick(LostFoundItem item);
        void onContactClick(String contactType, String contactValue);
    }

    public LostFoundResultAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost_found_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);

        // Title and Category
        holder.itemTitle.setText(item.getTitle());
        holder.categoryText.setText(item.getCategory());
        setCategoryIcon(holder.categoryIcon, item.getCategory());

        // Status
        holder.statusChip.setText(item.getStatus().toLowerCase());
        setStatusChipColor(holder.statusChip, item.getStatus());

        // Image
        if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
            Uri firstImageUri = Uri.parse(item.getImageUris().get(0));
            Picasso.get()
                    .load(firstImageUri)
                    .placeholder(R.drawable.bg_rounded_gray)
                    .error(R.drawable.bg_rounded_gray)
                    .fit()
                    .centerCrop()
                    .into(holder.itemImage);
            
            holder.itemImage.setVisibility(View.VISIBLE);
            holder.placeholderIcon.setVisibility(View.GONE);
        } else {
            holder.itemImage.setVisibility(View.GONE);
            holder.placeholderIcon.setVisibility(View.VISIBLE);
        }

        // Details
        holder.itemDescription.setText(item.getDescription());
        holder.locationText.setText(item.getLocation());
        holder.dateTimeText.setText(item.getDateTime());

        // Contact Section
        if (item.isAllowContact() && item.getContactName() != null && !item.getContactName().isEmpty()) {
            holder.contactName.setText(item.getContactName());
            
            String role = "Finder";
            if ("lost".equalsIgnoreCase(item.getType())) {
                role = "Owner";
            }
            holder.contactRole.setText(role);

            // Handle Email Button
            if (item.getContactEmail() != null && !item.getContactEmail().isEmpty()) {
                holder.btnEmail.setVisibility(View.VISIBLE);
                holder.btnEmail.setOnClickListener(v -> {
                    if (listener != null) listener.onContactClick("email", item.getContactEmail());
                });
            } else {
                holder.btnEmail.setVisibility(View.GONE);
            }

            // Handle Call Button
            if (item.getContactPhone() != null && !item.getContactPhone().isEmpty()) {
                holder.btnCall.setVisibility(View.VISIBLE);
                holder.btnCall.setOnClickListener(v -> {
                    if (listener != null) listener.onContactClick("phone", item.getContactPhone());
                });
            } else {
                holder.btnCall.setVisibility(View.GONE);
            }
            
            // Show divider and footer only if contact info exists
            holder.divider.setVisibility(View.VISIBLE);
            ((View) holder.contactName.getParent().getParent()).setVisibility(View.VISIBLE);

        } else {
            // Hide contact footer
             holder.divider.setVisibility(View.GONE);
             ((View) holder.contactName.getParent().getParent()).setVisibility(View.GONE);
        }

        // Item Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        // Status Click (on Chip)
        holder.statusChip.setOnClickListener(v -> {
            if (listener != null) listener.onStatusChangeClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<LostFoundItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    private void setCategoryIcon(ImageView icon, String category) {
        if (category == null) return;
        
        switch (category.toLowerCase()) {
            case "electronics":
            case "accessories":
            case "jewelry":
                icon.setImageResource(R.drawable.ic_watch);
                break;
            case "documents":
                icon.setImageResource(R.drawable.ic_mail);
                break;
            case "clothing":
                icon.setImageResource(R.drawable.ic_backpack);
                break;
            case "waste":
            case "bin":
                icon.setImageResource(R.drawable.ic_delete); // or trash
                break;
            case "personal items":
            case "bag":
                icon.setImageResource(R.drawable.ic_backpack);
                break;
            default:
                icon.setImageResource(R.drawable.ic_package); // info or package
                break;
        }
    }

    private void setStatusChipColor(Chip chip, String status) {
        if (status == null) return;
        
        switch (status.toLowerCase()) {
            case "open":
                chip.setChipBackgroundColorResource(R.color.browse_primary_light);
                chip.setTextColor(context.getResources().getColor(R.color.browse_primary));
                chip.setText("Open");
                break;
            case "resolved":
            case "found":
            case "closed":
                chip.setChipBackgroundColorResource(R.color.green_100);
                chip.setTextColor(context.getResources().getColor(R.color.green_600));
                chip.setText("Resolved");
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.gray_100);
                chip.setTextColor(context.getResources().getColor(R.color.gray_600));
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        ImageView categoryIcon;
        TextView categoryText;
        Chip statusChip;
        ImageView itemImage;
        ImageView placeholderIcon;
        TextView itemDescription;
        TextView locationText;
        TextView dateTimeText;
        View divider;
        TextView contactName;
        TextView contactRole;
        FrameLayout btnEmail;
        FrameLayout btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryText = itemView.findViewById(R.id.categoryText);
            statusChip = itemView.findViewById(R.id.statusChip);
            itemImage = itemView.findViewById(R.id.itemImage);
            placeholderIcon = itemView.findViewById(R.id.placeholderIcon);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            locationText = itemView.findViewById(R.id.locationText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            divider = itemView.findViewById(R.id.divider);
            contactName = itemView.findViewById(R.id.contactName);
            contactRole = itemView.findViewById(R.id.contactRole);
            btnEmail = itemView.findViewById(R.id.btnEmail);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}


