package com.megaproject.urbanspace.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.urbanspace.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotoThumbnailAdapter extends RecyclerView.Adapter<PhotoThumbnailAdapter.ViewHolder> {

    public interface OnPhotosChangedListener {
        void onPhotosChanged();
    }

    private final List<Uri> items;
    private final OnPhotosChangedListener listener;

    public PhotoThumbnailAdapter(@NonNull List<Uri> items,
                                 @NonNull OnPhotosChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = items.get(position);

        Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(holder.thumbnailImage);

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            items.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            if (listener != null) {
                listener.onPhotosChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnailImage;
        final ImageView deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            deleteButton = itemView.findViewById(R.id.thumbnailRemove);
        }
    }
}
