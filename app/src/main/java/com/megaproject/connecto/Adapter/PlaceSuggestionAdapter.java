package com.megaproject.connecto.Adapter;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.megaproject.connecto.R;
import java.util.List;
import java.util.ArrayList;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    private List<Address> suggestions = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Address address);
    }

    public PlaceSuggestionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<Address> newSuggestions) {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    public void clear() {
        this.suggestions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = suggestions.get(position);
        
        // Improve Title logic to show street/feature clearly
        String title = address.getFeatureName();
        String thoroughfare = address.getThoroughfare();
        
        // Sometimes feature name is just the house number, we prefer Thoroughfare
        if (thoroughfare != null && !thoroughfare.equals(title)) {
             // e.g. Feature: "123", Thoroughfare: "Main St" -> "123 Main St"
             // or Feature: "Starbucks", Thoroughfare: "Market St" -> "Starbucks"
             // Heuristic: If feature is numeric, prepend to thoroughfare
             if (isNumeric(title)) {
                 title = title + " " + thoroughfare;
             } else if (title == null) {
                 title = thoroughfare;
             }
        }
        if (title == null) title = address.getLocality();
        
        StringBuilder subtitle = new StringBuilder();
        if (address.getLocality() != null) subtitle.append(address.getLocality());
        if (address.getAdminArea() != null) {
            if (subtitle.length() > 0) subtitle.append(", ");
            subtitle.append(address.getAdminArea());
        }
        if (address.getCountryName() != null) {
            if (subtitle.length() > 0) subtitle.append(", ");
            subtitle.append(address.getCountryName());
        }
        
        holder.tvTitle.setText(title);
        if (subtitle.length() > 0) {
            holder.tvSubtitle.setText(subtitle.toString());
            holder.tvSubtitle.setVisibility(View.VISIBLE);
        } else {
             holder.tvSubtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(address));
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSuggestionTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSuggestionSubtitle);
        }
    }
}
