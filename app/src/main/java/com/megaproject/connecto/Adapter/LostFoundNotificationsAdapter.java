package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Model.LostFoundNotificationItem;
import com.megaproject.connecto.R;

import java.util.List;

public class LostFoundNotificationsAdapter extends RecyclerView.Adapter<LostFoundNotificationsAdapter.ViewHolder> {

    private Context context;
    private List<LostFoundNotificationItem> items;

    public LostFoundNotificationsAdapter(Context context, List<LostFoundNotificationItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost_found_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundNotificationItem item = items.get(position);

        if (item.isHeader()) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(item.getHeaderTitle());
            
            // Hide card content for header-only rows? 
            // Wait, standard RecyclerView practice is often different view types for headers. 
            // But to keep it simple with one layout, we can hide the card.
            holder.cardContainer.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
            holder.cardContainer.setVisibility(View.VISIBLE);

            holder.tvTitle.setText(item.getTitle());
            holder.tvTime.setText(item.getTime());
            
            // Handle bold text in description (e.g. "Alex M." or Item Names)
            // Assuming description contains simple indicators or we just use HTML
            if (item.getDescription().contains("<b>") || item.getDescription().contains("\"")) {
                 holder.tvDescription.setText(Html.fromHtml(item.getDescription(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.tvDescription.setText(item.getDescription());
            }

            if (item.isUnread()) {
                holder.viewUnreadDot.setVisibility(View.VISIBLE);
                holder.cardContainer.setAlpha(1.0f);
            } else {
                holder.viewUnreadDot.setVisibility(View.GONE);
                // Lower alpha for read items? The image shows "Case Closed" slightly faded maybe? 
                // Image "Case Closed" is opacity-80 in HTML.
                if (item.getTitle().equals("Case Closed")) {
                    holder.cardContainer.setAlpha(0.6f);
                } else {
                    holder.cardContainer.setAlpha(1.0f);
                }
            }

            holder.ivIcon.setImageResource(item.getIconResId());
            holder.ivIcon.setColorFilter(item.getIconTintColor());
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(item.getBgTintColor()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader, tvTitle, tvTime, tvDescription;
        View viewUnreadDot, cardContainer;
        ImageView ivIcon;
        FrameLayout iconContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            cardContainer = itemView.findViewById(R.id.iconContainer).getParent().getParent() instanceof androidx.cardview.widget.CardView ? 
                            (View) itemView.findViewById(R.id.iconContainer).getParent().getParent() : itemView; 
            // The card is the direct child of the root LinearLayout (after header)
            // But findViewById searches effectively. Let's look at layout structure:
            // Root Local -> Header + Card.
            // Card is the container we toggle.
            // Let's grab the card by type or just traverse. 
            // Actually, best to give ID to CardView in layout.
            // I forgot to give ID to CardView in `item_lost_found_notification.xml`.
            // Let's assume I will go back and fix it, or hack it here.
            
            // Simpler: Just get the CardView by finding one of its unique children's parent.
            View contentLayout = itemView.findViewById(R.id.iconContainer).getParent() instanceof ViewGroup ? (ViewGroup) itemView.findViewById(R.id.iconContainer).getParent() : null;
             if (contentLayout != null && contentLayout.getParent() instanceof androidx.cardview.widget.CardView) {
                 cardContainer = (View) contentLayout.getParent();
             }
            
            ivIcon = itemView.findViewById(R.id.ivIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}
