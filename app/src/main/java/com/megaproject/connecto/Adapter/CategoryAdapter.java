package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.megaproject.connecto.Model.ReportCategory;
import com.megaproject.connecto.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<ReportCategory> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(ReportCategory category);
    }

    public CategoryAdapter(Context context, List<ReportCategory> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    public List<ReportCategory> getCategories() {
        return categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportCategory category = categories.get(position);

        holder.tvName.setText(category.getName());
        holder.imgIcon.setImageResource(category.getIconResId());

        if (category.isSelected()) {
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.blue_500));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blue_100));
            holder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setStrokeColor(Color.TRANSPARENT);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.imgCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            for (ReportCategory item : categories) {
                item.setSelected(false);
            }
            category.setSelected(true);
            notifyDataSetChanged();
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgIcon;
        ImageView imgCheck;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            imgIcon = itemView.findViewById(R.id.img_category_icon);
            imgCheck = itemView.findViewById(R.id.img_check);
            cardView = itemView.findViewById(R.id.card_category);
        }
    }
}
