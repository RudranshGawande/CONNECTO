package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.megaproject.connecto.Model.EmergencyContact;
import com.megaproject.connecto.R;

import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyContact> contactList;
    private OnContactToggleListener listener;

    public interface OnContactToggleListener {
        void onToggle(EmergencyContact contact, boolean isChecked);
        void onContactClick(EmergencyContact contact);
    }

    public EmergencyContactAdapter(Context context, List<EmergencyContact> contactList, OnContactToggleListener listener) {
        this.context = context;
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);

        holder.contactName.setText(contact.getName());
        holder.contactPhone.setText(contact.getPhoneNumber());
        
        // Remove listener temporarily to avoid triggering it during bind
        holder.contactSwitch.setOnCheckedChangeListener(null);
        holder.contactSwitch.setChecked(contact.isActive());
        
        holder.activeBadge.setVisibility(contact.isActive() ? View.VISIBLE : View.INVISIBLE);

        if (contact.isSystemContact()) {
            holder.contactImage.setImageResource(R.drawable.ic_person_outline); // Or distinct icon
            holder.contactImage.setBackgroundResource(R.drawable.circle_light_gray);
            holder.itemView.setAlpha(0.8f); // Slightly dimmed per design for inactive
        } else {
            // Load image if available (using placeholder logic for now)
            holder.contactImage.setImageResource(R.drawable.ic_person_outline);
            // In real app, load from imageUrl using Glide/Picasso
        }
        
        if (!contact.isActive() && !contact.isSystemContact()) {
             holder.itemView.setAlpha(0.8f);
        } else {
             holder.itemView.setAlpha(1.0f);
        }

        holder.contactSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contact.setActive(isChecked);
            holder.activeBadge.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            if (listener != null) {
                listener.onToggle(contact, isChecked);
            }
            // Update alpha visualization
             if (!isChecked && !contact.isSystemContact()) {
                 holder.itemView.setAlpha(0.8f);
            } else {
                 holder.itemView.setAlpha(1.0f);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contactImage;
        FrameLayout activeBadge;
        TextView contactName, contactPhone;
        SwitchMaterial contactSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactImage = itemView.findViewById(R.id.contactImage);
            activeBadge = itemView.findViewById(R.id.activeBadge);
            contactName = itemView.findViewById(R.id.contactName);
            contactPhone = itemView.findViewById(R.id.contactPhone);
            contactSwitch = itemView.findViewById(R.id.contactSwitch);
        }
    }
}


