package com.megaproject.urbanspace.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.megaproject.urbanspace.Model.LostFoundItem;
import com.megaproject.urbanspace.R;
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

        // Set title
        holder.titleTextView.setText(item.getTitle());

        // Set category
        holder.categoryChip.setText(item.getCategory());

        // Set status
        holder.statusChip.setText(item.getStatus().toLowerCase());
        setStatusChipColor(holder.statusChip, item.getStatus());

        // Set date and location
        String dateTimeLocation = item.getDateTime() + " - " + item.getLocation();
        holder.dateTimeLocationTextView.setText(dateTimeLocation);

        // Set description
        holder.descriptionTextView.setText(item.getDescription());

        // Load image or show placeholder
        if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
            Uri firstImageUri = Uri.parse(item.getImageUris().get(0));
            Picasso.get()
                    .load(firstImageUri)
                    .placeholder(R.drawable.ic_package)
                    .error(R.drawable.ic_package)
                    .into(holder.itemImageView);
            
            holder.itemImageView.setVisibility(View.VISIBLE);
            holder.placeholderIcon.setVisibility(View.GONE);
        } else {
            holder.itemImageView.setVisibility(View.GONE);
            holder.placeholderIcon.setVisibility(View.VISIBLE);
        }

        // Show contact section if contact is allowed
        if (item.isAllowContact() && item.getContactName() != null) {
            holder.contactSection.setVisibility(View.VISIBLE);
            holder.contactNameTextView.setText(item.getContactName());
            holder.contactEmailTextView.setText(item.getContactEmail());
            holder.contactPhoneTextView.setText(item.getContactPhone());
        } else {
            holder.contactSection.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });

        holder.statusButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusChangeClick(item);
            }
        });

        holder.contactNameTextView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick("name", item.getContactName());
            }
        });

        holder.contactEmailTextView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick("email", item.getContactEmail());
            }
        });

        holder.contactPhoneTextView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick("phone", item.getContactPhone());
            }
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

    public void addItem(LostFoundItem item) {
        this.items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(LostFoundItem item) {
        int position = items.indexOf(item);
        if (position != -1) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void setStatusChipColor(Chip chip, String status) {
        switch (status.toLowerCase()) {
            case "open":
                chip.setChipBackgroundColorResource(R.color.status_open);
                break;
            case "found":
                chip.setChipBackgroundColorResource(R.color.blue_500);
                break;
            case "closed":
                chip.setChipBackgroundColorResource(R.color.gray_500);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.chip_background);
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        ImageView placeholderIcon;
        TextView titleTextView;
        Chip categoryChip;
        Chip statusChip;
        TextView dateTimeLocationTextView;
        TextView descriptionTextView;
        LinearLayout contactSection;
        TextView contactNameTextView;
        TextView contactEmailTextView;
        TextView contactPhoneTextView;
        MaterialButton statusButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            itemImageView = itemView.findViewById(R.id.itemImage);
            placeholderIcon = itemView.findViewById(R.id.placeholderIcon);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            statusChip = itemView.findViewById(R.id.statusChip);
            dateTimeLocationTextView = itemView.findViewById(R.id.itemDateTime);
            descriptionTextView = itemView.findViewById(R.id.itemDescription);
            contactSection = itemView.findViewById(R.id.contactSection);
            contactNameTextView = itemView.findViewById(R.id.contactName);
            contactEmailTextView = itemView.findViewById(R.id.contactEmail);
            contactPhoneTextView = itemView.findViewById(R.id.contactPhone);
            statusButton = itemView.findViewById(R.id.statusButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
