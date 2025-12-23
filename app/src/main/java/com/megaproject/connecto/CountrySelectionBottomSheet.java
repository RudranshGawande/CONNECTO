package com.megaproject.connecto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CountrySelectionBottomSheet extends BottomSheetDialogFragment {

    private CountryAdapter.OnCountrySelectedListener listener;

    public void setOnCountrySelectedListener(CountryAdapter.OnCountrySelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_country_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvCountries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Country> countries = getCountries();
        CountryAdapter adapter = new CountryAdapter(countries, country -> {
            if (listener != null) {
                listener.onCountrySelected(country);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);
    }

    private List<Country> getCountries() {
        List<Country> list = new ArrayList<>();
        list.add(new Country("US", "United States", "+1", "🇺🇸"));
        list.add(new Country("IN", "India", "+91", "🇮🇳"));
        list.add(new Country("GB", "United Kingdom", "+44", "🇬🇧"));
        list.add(new Country("CA", "Canada", "+1", "🇨🇦"));
        list.add(new Country("AU", "Australia", "+61", "🇦🇺"));
        list.add(new Country("DE", "Germany", "+49", "🇩🇪"));
        list.add(new Country("FR", "France", "+33", "🇫🇷"));
        list.add(new Country("JP", "Japan", "+81", "🇯🇵"));
        list.add(new Country("KR", "South Korea", "+82", "🇰🇷"));
        list.add(new Country("CN", "China", "+86", "🇨🇳"));
        list.add(new Country("BR", "Brazil", "+55", "🇧🇷"));
        list.add(new Country("RU", "Russia", "+7", "🇷🇺"));
        list.add(new Country("IT", "Italy", "+39", "🇮🇹"));
        list.add(new Country("ES", "Spain", "+34", "🇪🇸"));
        return list;
    }
}
