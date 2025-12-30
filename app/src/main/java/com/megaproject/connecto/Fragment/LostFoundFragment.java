package com.megaproject.connecto.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Adapter.LostFoundAdapter;
import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;

import java.util.ArrayList;
import java.util.List;

public class LostFoundFragment extends Fragment {

    private RecyclerView rvLostFound;
    private LostFoundAdapter adapter;
    private List<LostFoundItem> lostItems;

    public LostFoundFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lost_found, container, false);

        initViews(view);
        setupRecyclerView();
        loadDummyData();

        return view;
    }

    private boolean isLostTabSelected = true;

    private void initViews(View view) {
        rvLostFound = view.findViewById(R.id.rvLostFound);
        ImageView btnBack = view.findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        ImageView btnNotifications = view.findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.LostFoundNotificationsActivity.class);
                startActivity(intent);
            });
        }
        
        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.MyLostFoundActivity.class);
                startActivity(intent);
            });
        }
        
        View btnFilter = view.findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterBottomSheet());
        }

        // Tab click listeners (Visual only for now)
        TextView tabLost = view.findViewById(R.id.tabLost);
        TextView tabFound = view.findViewById(R.id.tabFound);

        android.widget.EditText etSearch = view.findViewById(R.id.etSearch);

        tabLost.setOnClickListener(v -> {
            // Set selected state
            tabLost.setBackgroundResource(R.drawable.bg_tab_item_selected);
            tabLost.setTextColor(getResources().getColor(R.color.home_text_main));
            tabLost.setElevation(2f);
            
            tabFound.setBackgroundResource(0);
            tabFound.setTextColor(getResources().getColor(R.color.home_text_secondary));
            tabFound.setElevation(0f);
            
            if (etSearch != null) etSearch.setHint("Search lost items...");
            isLostTabSelected = true;
            showLostItems();
        });

        tabFound.setOnClickListener(v -> {
             // Set selected state
            tabFound.setBackgroundResource(R.drawable.bg_tab_item_selected);
            tabFound.setTextColor(getResources().getColor(R.color.home_text_main));
            tabFound.setElevation(2f);

            tabLost.setBackgroundResource(0);
            tabLost.setTextColor(getResources().getColor(R.color.home_text_secondary));
            tabLost.setElevation(0f);

            if (etSearch != null) etSearch.setHint("Search found items...");
            isLostTabSelected = false;
            showFoundItems();
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showReportTypeBottomSheet());
        }
    }

    private List<LostFoundItem> allLostItems;
    private List<LostFoundItem> allFoundItems;
    private List<LostFoundItem> displayItems;

    private void setupRecyclerView() {
        rvLostFound.setLayoutManager(new LinearLayoutManager(getContext()));
        displayItems = new ArrayList<>();
        adapter = new LostFoundAdapter(getContext(), displayItems);
        rvLostFound.setAdapter(adapter);
    }

    private void loadDummyData() {
        // Load from Manager instead of creating locally
        List<LostFoundItem> allItems = com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().getAllItems();
        
        allLostItems = new ArrayList<>();
        allFoundItems = new ArrayList<>();

        for (LostFoundItem item : allItems) {
            if ("lost".equalsIgnoreCase(item.getType())) {
                allLostItems.add(item);
            } else {
                allFoundItems.add(item);
            }
        }

        // Initial Load (Lost Items)
        if (isLostTabSelected) {
            showLostItems();
        } else {
            showFoundItems();
        }
    }
    
    private void showLostItems() {
        displayItems.clear();
        displayItems.addAll(allLostItems);
        adapter.notifyDataSetChanged();
    }

    private void showFoundItems() {
        displayItems.clear();
        displayItems.addAll(allFoundItems);
        adapter.notifyDataSetChanged();
    }

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> reportItemLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                // New Item
                android.content.Intent data = result.getData();
                LostFoundItem newItem = new LostFoundItem();
                newItem.setTitle(data.getStringExtra("title"));
                newItem.setCategory(data.getStringExtra("category"));
                newItem.setLocation(data.getStringExtra("location"));
                newItem.setDateTime(data.getStringExtra("date"));
                newItem.setType("lost"); // Defaulting to lost for this flow
                newItem.setMine(true); // Is Mine
                
                // Add to Manager
                com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().addItem(newItem);
                
                // Reload lists
                loadDummyData();
                
                // Show Success
                android.widget.Toast.makeText(getContext(), "Report added successfully", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

    private void showReportTypeBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_report_type, null);
        bottomSheetDialog.setContentView(sheetView);

        // Actions
        sheetView.findViewById(R.id.btnReportLost).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.ReportLostItemActivity.class);
            reportItemLauncher.launch(intent);
        });

        sheetView.findViewById(R.id.btnReportFound).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // TODO: Navigate to Report Found Activity
        });

        sheetView.findViewById(R.id.btnCloseReportType).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
    
    // --- Filter Logic ---
    private List<String> selectedCategories = new ArrayList<>();
    private String selectedStatus = "Open"; // Open, Resolved, All
    private String selectedDateFilter = "Last week"; // 24h, Week, Month
    private int proximityValue = 10;

    private void showFilterBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_filter, null);
        bottomSheetDialog.setContentView(sheetView);

        // UI refs
        // Categories
        setupCategoryToggle(sheetView, R.id.filterCatElectronics, R.id.ivCheckElectronics, "Electronics");
        setupCategoryToggle(sheetView, R.id.filterCatPersonal, R.id.ivCheckPersonal, "Personal");
        setupCategoryToggle(sheetView, R.id.filterCatPets, R.id.ivCheckPets, "Pets");
        setupCategoryToggle(sheetView, R.id.filterCatKeys, R.id.ivCheckKeys, "Keys"); 

        // Status
        TextView btnStatusOpen = sheetView.findViewById(R.id.btnStatusOpen);
        TextView btnStatusResolved = sheetView.findViewById(R.id.btnStatusResolved);
        TextView btnStatusAll = sheetView.findViewById(R.id.btnStatusAll);
        
        updateStatusUI(btnStatusOpen, btnStatusResolved, btnStatusAll, selectedStatus);

        btnStatusOpen.setOnClickListener(v -> updateStatusUI(btnStatusOpen, btnStatusResolved, btnStatusAll, "Open"));
        btnStatusResolved.setOnClickListener(v -> updateStatusUI(btnStatusOpen, btnStatusResolved, btnStatusAll, "Resolved"));
        btnStatusAll.setOnClickListener(v -> updateStatusUI(btnStatusOpen, btnStatusResolved, btnStatusAll, "All"));

        // Date
        android.widget.RadioGroup rgDatePosted = sheetView.findViewById(R.id.rgDatePosted);
        if (selectedDateFilter.equals("Last 24 hours")) rgDatePosted.check(R.id.rbLast24);
        else if (selectedDateFilter.equals("Last week")) rgDatePosted.check(R.id.rbLastWeek);
        else if (selectedDateFilter.equals("Last month")) rgDatePosted.check(R.id.rbLastMonth);

        rgDatePosted.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLast24) selectedDateFilter = "Last 24 hours";
            else if (checkedId == R.id.rbLastWeek) selectedDateFilter = "Last week";
            else if (checkedId == R.id.rbLastMonth) selectedDateFilter = "Last month";
        });

        // Proximity
        android.widget.SeekBar seekBarProximity = sheetView.findViewById(R.id.seekBarProximity);
        TextView tvProximityValue = sheetView.findViewById(R.id.tvProximityValue);
        seekBarProximity.setProgress(proximityValue);
        tvProximityValue.setText(proximityValue + " km");

        seekBarProximity.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                proximityValue = progress;
                tvProximityValue.setText(proximityValue + " km");
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        // Actions
        sheetView.findViewById(R.id.btnCloseFilter).setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetView.findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            // Reset to widest permissive state
            selectedCategories.clear();
            selectedStatus = "All";
            selectedDateFilter = "Last month"; // Show all available history
            proximityValue = 50; // Max range
            bottomSheetDialog.dismiss();
            applyFilters();
        });
        
        sheetView.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            applyFilters();
        });

        bottomSheetDialog.setOnShowListener(dialog -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;
            android.widget.FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        bottomSheetDialog.show();
    }

    private void setupCategoryToggle(View parent, int layoutId, int iconId, String category) {
        View layout = parent.findViewById(layoutId);
        ImageView icon = parent.findViewById(iconId);
        boolean isSelected = selectedCategories.contains(category);
        
        updateCategoryVisuals(layout, icon, isSelected);

        layout.setOnClickListener(v -> {
            if (selectedCategories.contains(category)) {
                selectedCategories.remove(category);
                updateCategoryVisuals(layout, icon, false);
            } else {
                selectedCategories.add(category);
                updateCategoryVisuals(layout, icon, true);
            }
        });
    }

    private void updateCategoryVisuals(View layout, ImageView icon, boolean isSelected) {
        if (isSelected) {
            layout.setBackgroundResource(R.drawable.bg_filter_option_selected);
            icon.setImageResource(R.drawable.ic_check_circle_filled);
            icon.setColorFilter(getResources().getColor(R.color.primary));
        } else {
            layout.setBackgroundResource(R.drawable.bg_filter_option_unselected);
            icon.setImageResource(R.drawable.ic_circle_outline_gray);
            icon.setColorFilter(getResources().getColor(R.color.gray_300));
        }
    }

    private void updateStatusUI(TextView btnOpen, TextView btnResolved, TextView btnAll, String status) {
        selectedStatus = status;
        // Reset all
        btnOpen.setBackground(null); btnOpen.setTextColor(getResources().getColor(R.color.home_text_secondary)); btnOpen.setElevation(0f);
        btnResolved.setBackground(null); btnResolved.setTextColor(getResources().getColor(R.color.home_text_secondary)); btnResolved.setElevation(0f);
        btnAll.setBackground(null); btnAll.setTextColor(getResources().getColor(R.color.home_text_secondary)); btnAll.setElevation(0f);

        TextView target = null;
        if (status.equals("Open")) target = btnOpen;
        else if (status.equals("Resolved")) target = btnResolved;
        else target = btnAll;

        target.setBackgroundResource(R.drawable.bg_status_selected);
        target.setTextColor(getResources().getColor(R.color.home_text_main));
        target.setElevation(1f);
    }

    private void applyFilters() {
        List<LostFoundItem> source = isLostTabSelected ? allLostItems : allFoundItems;
        displayItems.clear();

        for (LostFoundItem item : source) {
            boolean matchesCategory = true;
            if (!selectedCategories.isEmpty()) {
                boolean catMatch = false;
                for (String cat : selectedCategories) {
                    if (item.getCategory() != null && item.getCategory().contains(cat)) catMatch = true;
                    // Special case: "Keys" -> Title contains Keys
                    if (cat.equals("Keys") && item.getTitle().toLowerCase().contains("key")) catMatch = true;
                }
                matchesCategory = catMatch;
            }

            boolean matchesStatus = true;
            // Dummy logic: All open.
            if (selectedStatus.equals("Resolved")) matchesStatus = false; 

            boolean matchesDate = true;
            String time = item.getDateTime().toLowerCase();
            if (selectedDateFilter.equals("Last 24 hours")) {
                if (time.contains("yesterday") || time.contains("oct")) matchesDate = false;
            } else if (selectedDateFilter.equals("Last week")) {
                if (time.contains("oct")) matchesDate = false;
            }
            // Last month includes Oct.

            if (matchesCategory && matchesStatus && matchesDate) {
                displayItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
