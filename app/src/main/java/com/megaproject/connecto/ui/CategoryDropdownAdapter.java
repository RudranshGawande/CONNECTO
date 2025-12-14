package com.megaproject.connecto.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.megaproject.connecto.R;

import java.util.List;

public class CategoryDropdownAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> categories;
    private int selectedPosition;

    public CategoryDropdownAdapter(Context context, List<String> categories, int defaultPosition) {
        this.context = context;
        this.categories = categories;
        this.selectedPosition = defaultPosition;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_option, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String category = categories.get(position);
        holder.text.setText(category);

        boolean isSelected = position == selectedPosition;

        if (isSelected) {
            convertView.setBackgroundResource(R.drawable.bg_category_selected);
            holder.text.setTextColor(0xFFFFFFFF);
            holder.text.setTypeface(holder.text.getTypeface(), android.graphics.Typeface.BOLD);
            holder.check.setVisibility(View.VISIBLE);
        } else {
            convertView.setBackgroundResource(android.R.color.white);
            holder.text.setTextColor(0xFF000000);
            holder.text.setTypeface(holder.text.getTypeface(), android.graphics.Typeface.NORMAL);
            holder.check.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        final ImageView check;
        final TextView text;

        ViewHolder(View itemView) {
            check = itemView.findViewById(R.id.itemCheckIcon);
            text = itemView.findViewById(R.id.itemCategoryText);
        }
    }
}


