package com.megaproject.connecto.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context context;
    private List<Uri> photoUris;
    private OnPhotoDeleteListener deleteListener;

    public interface OnPhotoDeleteListener {
        void onPhotoDelete(int position);
    }

    public PhotoAdapter(Context context, List<Uri> photoUris, OnPhotoDeleteListener deleteListener) {
        this.context = context;
        this.photoUris = photoUris;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);
        holder.imgPreview.setImageURI(photoUri);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPhotoDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.img_photo_preview);
            btnDelete = itemView.findViewById(R.id.btn_delete_photo);
        }
    }
}
