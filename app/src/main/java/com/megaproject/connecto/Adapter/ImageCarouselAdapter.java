package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.megaproject.connecto.R;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {

    private final List<Object> images;
    private final Context context;

    public ImageCarouselAdapter(Context context, List<Object> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (images == null || images.isEmpty()) return;

        // Loop logic
        int realPosition = position % images.size();
        Object imageSource = images.get(realPosition);

        if (imageSource instanceof Integer) {
            // Resource ID
            Glide.with(context)
                    .load((Integer) imageSource)
                    .centerCrop()
                    .into(holder.imageView);
        } else if (imageSource instanceof String) {
            // URL or URI string
            Glide.with(context)
                    .load((String) imageSource)
                    .centerCrop()
                    .into(holder.imageView);
        } else if (imageSource instanceof Uri) {
            Glide.with(context)
                    .load((Uri) imageSource)
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        if (images == null || images.isEmpty()) return 0;
        // If more than 1 image, enable infinite looping
        return images.size() > 1 ? Integer.MAX_VALUE : 1;
    }

    public int getRealCount() {
        return images == null ? 0 : images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivCarouselImage);
        }
    }
}
