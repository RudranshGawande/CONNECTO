package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Model.Locality;
import com.megaproject.connecto.R;

import java.util.List;

public class LocalityAdapter extends RecyclerView.Adapter<LocalityAdapter.ViewHolder> {

    private Context context;
    private List<Locality> localityList;
    private OnLocalityClickListener listener;

    public interface OnLocalityClickListener {
        void onLocalityClick(Locality locality);
    }

    public LocalityAdapter(Context context, List<Locality> localityList, OnLocalityClickListener listener) {
        this.context = context;
        this.localityList = localityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_locality, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Locality locality = localityList.get(position);
        
        holder.locationNameText.setText(locality.getName());
        holder.locationDetailsText.setText(locality.getDetails());
        
        // Set different icon for recent vs all localities
        if (locality.isRecent()) {
            holder.locationIcon.setImageResource(R.drawable.ic_history);
        } else {
            holder.locationIcon.setImageResource(R.drawable.ic_location_city);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocalityClick(locality);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView locationIcon;
        TextView locationNameText;
        TextView locationDetailsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationIcon = itemView.findViewById(R.id.locationIcon);
            locationNameText = itemView.findViewById(R.id.locationNameText);
            locationDetailsText = itemView.findViewById(R.id.locationDetailsText);
        }
    }
}


