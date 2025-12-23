package com.megaproject.connecto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> {

    private List<Country> countryList;
    private OnCountrySelectedListener listener;

    public interface OnCountrySelectedListener {
        void onCountrySelected(Country country);
    }

    public CountryAdapter(List<Country> countryList, OnCountrySelectedListener listener) {
        this.countryList = countryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        Country country = countryList.get(position);
        holder.bind(country, listener);
    }

    @Override
    public int getItemCount() {
        return countryList != null ? countryList.size() : 0;
    }

    static class CountryViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlag, tvName, tvDialCode;

        public CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlag = itemView.findViewById(R.id.tvCountryFlag);
            tvName = itemView.findViewById(R.id.tvCountryName);
            tvDialCode = itemView.findViewById(R.id.tvCountryDialCode);
        }

        public void bind(final Country country, final OnCountrySelectedListener listener) {
            tvFlag.setText(country.getFlag());
            tvName.setText(country.getName());
            tvDialCode.setText(country.getDialCode());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCountrySelected(country);
                }
            });
        }
    }
}
