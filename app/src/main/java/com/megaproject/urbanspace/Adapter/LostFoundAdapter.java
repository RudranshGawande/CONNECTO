package com.megaproject.urbanspace.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.urbanspace.Model.LostFoundItem;
import com.megaproject.urbanspace.R;

import java.util.ArrayList;
import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

    private final List<LostFoundItem> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_found_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.category.setText(item.getCategory());
        holder.status.setText(item.getStatus());
        holder.meta.setText(item.getMeta());
        holder.description.setText(item.getDescription());
        holder.name.setText(item.getContactName());
        holder.email.setText(item.getContactEmail());
        holder.phone.setText(item.getContactPhone());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submit(List<LostFoundItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, status, meta, description, name, email, phone;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            category = itemView.findViewById(R.id.itemCategory);
            status = itemView.findViewById(R.id.itemStatus);
            meta = itemView.findViewById(R.id.itemMeta);
            description = itemView.findViewById(R.id.itemDescription);
            name = itemView.findViewById(R.id.contactName);
            email = itemView.findViewById(R.id.contactEmail);
            phone = itemView.findViewById(R.id.contactPhone);
        }
    }
}

