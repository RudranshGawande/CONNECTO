package com.megaproject.connecto;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationSelectorBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rvLocationList;
    private TextView tvSelectorTitle;
    private TextView tvBreadcrumbs;
    private LocationAdapter adapter;

    // State
    private List<String> currentPath = new ArrayList<>();
    private OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(String fullPath);
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    public LocationSelectorBottomSheet() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_location_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLocationList = view.findViewById(R.id.rvLocationList);
        tvSelectorTitle = view.findViewById(R.id.tvSelectorTitle);
        tvBreadcrumbs = view.findViewById(R.id.tvBreadcrumbs);

        rvLocationList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocationAdapter();
        rvLocationList.setAdapter(adapter);

        // Start at root
        loadLevel(null);
    }

    private void loadLevel(String parent) {
        List<String> items = LocationDataProvider.getItems(parent);
        
        // Update UI Text
        updateHeaderText();

        if (items.isEmpty()) {
            // Reached leaf node (City/Village selected)
            finishSelection();
        } else {
            // Show list
            adapter.setData(items);
        }
    }

    private void updateHeaderText() {
        if (currentPath.isEmpty()) {
            tvSelectorTitle.setText("Select Country");
            tvBreadcrumbs.setVisibility(View.GONE);
        } else {
            // Determine level name
            String levelName = "Region";
            int depth = currentPath.size();
            switch(depth) {
                case 1: levelName = "State"; break;
                case 2: levelName = "District / Taluka"; break;
                case 3: levelName = "City / Village"; break;
            }
            tvSelectorTitle.setText("Select " + levelName);
            
            // Build breadcrumbs: India > Maharashtra
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                sb.append(currentPath.get(i));
                if (i < currentPath.size() - 1) {
                    sb.append(" > ");
                }
            }
            tvBreadcrumbs.setText(sb.toString());
            tvBreadcrumbs.setVisibility(View.VISIBLE);
        }
    }

    private void onItemSelected(String item) {
        currentPath.add(item);
        
        // Check if this item has children strictly.
        // If not, it is a leaf.
        // We use the data provider to check connectivity.
        if (LocationDataProvider.getItems(item).isEmpty()) {
            // It might be the end.
            finishSelection();
        } else {
            // Go deeper
            loadLevel(item);
        }
    }

    private void finishSelection() {
        if (listener != null) {
            StringBuilder sb = new StringBuilder();
            // Format: City, District, State (Reverse order excluding Country if desired, 
            // or just standard: "Paithan, Aurangabad, Maharashtra")
            // Let's do reverse order of the last 3 items if available, to make it look like an address.
            
            // Current path e.g.: [India, Maharashtra, Aurangabad, Paithan]
            // Desired: "Paithan, Aurangabad, Maharashtra"
            
            for (int i = currentPath.size() - 1; i >= 0; i--) {
                String item = currentPath.get(i);
                // Optional: Skip "India" if you want shorter text, but let's keep it if user didn't explicitly ban it, 
                // just banned the "->" format. 
                // Actually user said: "try another way" than "India -> Maharashtra..."
                // Address style is best.
                
                sb.append(item);
                if (i > 0) {
                    sb.append(", ");
                }
            }
            listener.onLocationSelected(sb.toString());
        }
        dismiss();
    }

    // Inner Adapter Class
    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
        private List<String> data = new ArrayList<>();

        public void setData(List<String> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_text, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String item = data.get(position);
            holder.text.setText(item);
            holder.itemView.setOnClickListener(v -> onItemSelected(item));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.tvLocationName);
            }
        }
    }
}
