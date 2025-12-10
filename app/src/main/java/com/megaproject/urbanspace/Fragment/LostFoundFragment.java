package com.megaproject.urbanspace.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.megaproject.urbanspace.Adapter.LostFoundAdapter;
import com.megaproject.urbanspace.R;

public class LostFoundFragment extends Fragment {

    private LostFoundViewModel viewModel;
    private MaterialButton tabLost;
    private MaterialButton tabFound;
    private LinearLayout contentLost;
    private LinearLayout contentFound;
    private TextView emptyFound;
    private LostFoundAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lost_found, container, false);

        viewModel = new ViewModelProvider(this).get(LostFoundViewModel.class);

        tabLost = view.findViewById(R.id.tabLost);
        tabFound = view.findViewById(R.id.tabFound);
        contentLost = view.findViewById(R.id.contentLost);
        contentFound = view.findViewById(R.id.contentFound);
        emptyFound = view.findViewById(R.id.emptyFound);
        ImageButton menu = view.findViewById(R.id.buttonLfMenu);

        RecyclerView foundList = view.findViewById(R.id.foundList);
        foundList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LostFoundAdapter();
        foundList.setAdapter(adapter);

        tabLost.setOnClickListener(v -> viewModel.setActiveTab(LostFoundViewModel.Tab.LOST));
        tabFound.setOnClickListener(v -> viewModel.setActiveTab(LostFoundViewModel.Tab.FOUND));

        menu.setOnClickListener(v -> {
            // placeholder hook for menu, can be wired to drawer later
        });

        viewModel.getActiveTab().observe(getViewLifecycleOwner(), tab -> updateTabs(tab));
        viewModel.getFoundItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submit(items);
            emptyFound.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // initialize default tab
        viewModel.setActiveTab(LostFoundViewModel.Tab.LOST);

        return view;
    }

    private void updateTabs(LostFoundViewModel.Tab tab) {
        // Active tab: white background with dark text (always visible)
        // Inactive tab: light gray background with dark text (always visible)
        int activeBg = R.drawable.tab_active_lostfound;
        int inactiveBg = R.drawable.tab_inactive_lostfound;
        int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);

        if (tab == LostFoundViewModel.Tab.LOST) {
            tabLost.setBackgroundResource(activeBg);
            tabLost.setTextColor(textColor);
            tabFound.setBackgroundResource(inactiveBg);
            tabFound.setTextColor(textColor);
            contentLost.setVisibility(View.VISIBLE);
            contentFound.setVisibility(View.GONE);
        } else {
            tabFound.setBackgroundResource(activeBg);
            tabFound.setTextColor(textColor);
            tabLost.setBackgroundResource(inactiveBg);
            tabLost.setTextColor(textColor);
            contentLost.setVisibility(View.GONE);
            contentFound.setVisibility(View.VISIBLE);
        }
    }
}