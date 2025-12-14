package com.megaproject.connecto.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.megaproject.connecto.R;

import java.util.List;

public class UnifiedDropdownAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> items;
    private String selectedItem = "";

    public UnifiedDropdownAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, R.layout.item_dropdown_unified, items);
        this.context = context;
        this.items = items;
    }

    public void setSelectedItem(String item) {
        this.selectedItem = item;
        notifyDataSetChanged();
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dropdown_unified, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String currentItem = items.get(position);
        holder.text.setText(currentItem);

        boolean isSelected = currentItem != null && currentItem.equals(selectedItem);

        if (isSelected) {
            convertView.setBackgroundResource(R.drawable.bg_dropdown_item_selected);
            holder.text.setTextColor(0xFFFFFFFF); // White
            holder.text.setTypeface(null, Typeface.BOLD);
            holder.check.setVisibility(View.VISIBLE);
            holder.check.setColorFilter(0xFFFFFFFF); // White Tint
        } else {
            convertView.setBackgroundResource(android.R.color.white);
            holder.text.setTextColor(0xFF222222); // Dark Grey/Black
            holder.text.setTypeface(null, Typeface.NORMAL);
            holder.check.setVisibility(View.INVISIBLE); // Keep layout consistent
        }

        return convertView;
    }

    private static class ViewHolder {
        final ImageView check;
        final TextView text;

        ViewHolder(View view) {
            check = view.findViewById(R.id.itemCheckIcon);
            text = view.findViewById(R.id.itemText);
        }
    }
}


