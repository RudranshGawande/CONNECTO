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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import com.megaproject.connecto.Adapter.CalendarAdapter;
import androidx.recyclerview.widget.GridLayoutManager;

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
            btnFilter.setOnClickListener(v -> showGeneralFilterBottomSheet());
        }

        View btnFilterDate = view.findViewById(R.id.btnFilterDate);
        if (btnFilterDate != null) {
            btnFilterDate.setOnClickListener(v -> showDateFilterBottomSheet());
        }

        // Tab click listeners (Visual only for now)
        TextView tabLost = view.findViewById(R.id.tabLost);
        TextView tabFound = view.findViewById(R.id.tabFound);

        View btnFilterProximity = view.findViewById(R.id.btnFilterProximity);
        if (btnFilterProximity != null) {
            btnFilterProximity.setOnClickListener(v -> showProximityFilterBottomSheet());
        }

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

    private com.google.android.material.bottomsheet.BottomSheetDialog currentProximityDialog;
    private com.google.android.gms.maps.model.LatLng proximityCenter;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> radiusPickerLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                android.content.Intent data = result.getData();
                int radius = data.getIntExtra("selected_radius", 15);
                double lat = data.getDoubleExtra("latitude", 0);
                double lng = data.getDoubleExtra("longitude", 0);
                
                // Update Proximity bottom sheet implies we need to refresh its state if it's open, 
                // BUT the bottom sheet is likely closed or covered.
                // Actually the flow is: BottomSheet -> Click Map -> Picker -> Result -> Update BottomSheet vars.
                // Wait, if we use start activity, the BSheet might dismiss? No, it stays if we don't dismiss it.
                // But typically we want to update the view inside the open BSheet.
                // Storing values to member variables first
                proximityValue = radius;
                
                // If we need to update the LIVE bottom sheet, we would need a reference to its view.
                // However, since we are inside Fragment, it's easier to just rely on the member variable next time it opens
                // OR if it's currently open, we need to update it.
                // For simplicity, let's assume the user re-opens or we refresh the currently open dialog if we keep track of it.
                
                // Wait, the user clicks "Set Location" on the bottom sheet, opens activity, comes back. 
                // We should ideally update the map preview on the bottom sheet if possible.
                // Since `showProximityFilterBottomSheet` creates a new Dialog each time, 
                // we'll need to update `proximityCenter` if we want to persist location.
                
                // Let's store center
                proximityCenter = new com.google.android.gms.maps.model.LatLng(lat, lng);
                
                // If the bottom sheet dialog is showing, we should update it. 
                if (currentProximityDialog != null && currentProximityDialog.isShowing()) {
                    android.widget.SeekBar sb = currentProximityDialog.findViewById(R.id.seekBarRadius);
                    TextView tv = currentProximityDialog.findViewById(R.id.tvRadiusValue);
                    com.google.android.gms.maps.MapView map = currentProximityDialog.findViewById(R.id.mapLiteView);
                    
                    if (sb != null) sb.setProgress(radius);
                    if (tv != null) tv.setText(String.valueOf(radius));
                    if (map != null && proximityCenter != null) {
                         map.getMapAsync(gMap -> {
                             gMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(proximityCenter, 13f));
                         });
                    }
                }
            }
        });

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
                
                // Use type from intent if available, else default to "lost"
                String type = data.getStringExtra("type");
                newItem.setType(type != null ? type : "lost");
                
                newItem.setMine(true); // Is Mine
                
                // Get Image URI
                String imageUri = data.getStringExtra("imageUri");
                if (imageUri != null) {
                    java.util.List<String> uris = new java.util.ArrayList<>();
                    uris.add(imageUri);
                    newItem.setImageUris(uris);
                }
                
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
            android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.ReportFoundItemActivity.class);
            reportItemLauncher.launch(intent);
        });

        sheetView.findViewById(R.id.btnCloseReportType).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
    
    // --- Filter Logic ---
    // --- Filter Logic ---
    // --- Filter Logic (General) ---
    private List<String> selectedCategories = new ArrayList<>();
    private String selectedStatus = "Open"; // Open, Resolved, All
    private String selectedDateFilter = "Last week"; // 24h, Week, Month
    private int proximityValue = 10;
    
    // --- Filter Logic (Date Specific) ---
    private Date filterStartDate;
    private Date filterEndDate;
    private Calendar currentDisplayCalendar;

    private void showProximityFilterBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        currentProximityDialog = bottomSheetDialog; // Store ref
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_proximity_filter, null);
        bottomSheetDialog.setContentView(sheetView);

        // UI References
        android.widget.SeekBar seekBarRadius = sheetView.findViewById(R.id.seekBarRadius);
        TextView tvRadiusValue = sheetView.findViewById(R.id.tvRadiusValue);
        androidx.appcompat.widget.SwitchCompat switchNearMe = sheetView.findViewById(R.id.switchNearMe);
        com.google.android.gms.maps.MapView mapLiteView = sheetView.findViewById(R.id.mapLiteView);
        View btnSetLocation = sheetView.findViewById(R.id.btnSetLocation);

        // Map Initialization
        mapLiteView.onCreate(null);
        mapLiteView.onResume(); // Needed for the map to display immediately
        mapLiteView.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            // Default to some location or current location if available
            com.google.android.gms.maps.model.LatLng defaultLoc = new com.google.android.gms.maps.model.LatLng(40.7812, -73.9665); // Central Park
            if (proximityCenter != null) defaultLoc = proximityCenter;
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(defaultLoc, 13f));
        });

        // Init Values
        seekBarRadius.setProgress(proximityValue);
        tvRadiusValue.setText(String.valueOf(proximityValue));

        // Listeners
        seekBarRadius.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                tvRadiusValue.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        btnSetLocation.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), com.megaproject.connecto.LocationPickerActivity.class);
            intent.putExtra("is_radius_mode", true);
            intent.putExtra("initial_radius", seekBarRadius.getProgress());
            if (proximityCenter != null) {
                intent.putExtra("latitude", proximityCenter.latitude);
                intent.putExtra("longitude", proximityCenter.longitude);
            }
            radiusPickerLauncher.launch(intent);
        });

        // Actions
        sheetView.findViewById(R.id.btnReset).setOnClickListener(v -> {
            seekBarRadius.setProgress(15);
            tvRadiusValue.setText("15");
            switchNearMe.setChecked(true);
        });

        sheetView.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheetDialog.dismiss());

        sheetView.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            // Apply Logic
            proximityValue = seekBarRadius.getProgress();
            bottomSheetDialog.dismiss();
            applyFilters();
        });
        
        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (mapLiteView != null) {
                mapLiteView.onDestroy();
            }
        });
        
        bottomSheetDialog.show();
    }

    private void showDateFilterBottomSheet() {
        if (getContext() == null) return;
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_date_filter, null);
        bottomSheetDialog.setContentView(sheetView);

        // UI References
        TextView tvMonthYear = sheetView.findViewById(R.id.tvMonthYear);
        TextView tvDisplayDate = sheetView.findViewById(R.id.tvDisplayDate);
        RecyclerView rvCalendarDays = sheetView.findViewById(R.id.rvCalendarDays);
        
        // Init Calendar
        currentDisplayCalendar = Calendar.getInstance();
        currentDisplayCalendar.set(Calendar.DAY_OF_MONTH, 1);
        
        // Init Adapter
        List<Date> days = generateDaysForMonth(currentDisplayCalendar);
        CalendarAdapter[] calendarAdapter = new CalendarAdapter[1];
        calendarAdapter[0] = new CalendarAdapter(getContext(), days, date -> {
            // Handle Date Selection
            handleDateSelection(date, calendarAdapter[0], tvDisplayDate);
        });
        
        rvCalendarDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarDays.setAdapter(calendarAdapter[0]);
        
        updateMonthHeader(tvMonthYear);
        updateDisplayDate(tvDisplayDate);
        
        if (filterStartDate != null) {
            calendarAdapter[0].updateRange(filterStartDate, filterEndDate);
        }

        // --- Presets ---
        sheetView.findViewById(R.id.btnPresetToday).setOnClickListener(v -> setPreset(0, calendarAdapter[0], tvDisplayDate, sheetView));
        sheetView.findViewById(R.id.btnPresetYesterday).setOnClickListener(v -> setPreset(1, calendarAdapter[0], tvDisplayDate, sheetView));
        sheetView.findViewById(R.id.btnPresetLast7).setOnClickListener(v -> setPreset(7, calendarAdapter[0], tvDisplayDate, sheetView));
        sheetView.findViewById(R.id.btnPresetLast30).setOnClickListener(v -> setPreset(30, calendarAdapter[0], tvDisplayDate, sheetView));

        // --- Custom Range Navigation ---
        sheetView.findViewById(R.id.btnMonthPrev).setOnClickListener(v -> {
            currentDisplayCalendar.add(Calendar.MONTH, -1);
            updateMonthHeader(tvMonthYear);
            calendarAdapter[0].updateDays(generateDaysForMonth(currentDisplayCalendar));
        });

        sheetView.findViewById(R.id.btnMonthNext).setOnClickListener(v -> {
            currentDisplayCalendar.add(Calendar.MONTH, 1);
            updateMonthHeader(tvMonthYear);
            calendarAdapter[0].updateDays(generateDaysForMonth(currentDisplayCalendar));
        });

        // --- Actions ---
        sheetView.findViewById(R.id.btnClose).setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        sheetView.findViewById(R.id.btnReset).setOnClickListener(v -> {
            filterStartDate = null;
            filterEndDate = null;
            updateDisplayDate(tvDisplayDate);
            calendarAdapter[0].updateRange(null, null);
            // Reset preset visuals
            resetPresetVisuals(sheetView);
            // Reset logic
            filterStartDate = null;
            filterEndDate = null;
        });

        sheetView.findViewById(R.id.btnApply).setOnClickListener(v -> {
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

    private void handleDateSelection(Date date, CalendarAdapter adapter, TextView displayTv) {
        if (filterStartDate == null) {
            filterStartDate = date;
            filterEndDate = null;
        } else if (filterEndDate == null) {
            if (date.before(filterStartDate)) {
                filterStartDate = date;
            } else {
                filterEndDate = date;
            }
        } else {
            filterStartDate = date;
            filterEndDate = null;
        }
        adapter.updateRange(filterStartDate, filterEndDate);
        updateDisplayDate(displayTv);
        // Deselect presets usually if manual selection
    }

    private void setPreset(int daysAgo, CalendarAdapter adapter, TextView displayTv, View root) {
        Calendar cal = Calendar.getInstance();
        // Clear time components for clean comparison
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        
        resetPresetVisuals(root);
        
        // Visual Selection
        View btn = null;
        if (daysAgo == 0) btn = root.findViewById(R.id.btnPresetToday);
        else if (daysAgo == 1) btn = root.findViewById(R.id.btnPresetYesterday);
        else if (daysAgo == 7) btn = root.findViewById(R.id.btnPresetLast7);
        else if (daysAgo == 30) btn = root.findViewById(R.id.btnPresetLast30);
        
        if (btn != null) btn.setSelected(true);

        if (daysAgo == 0) { // Today
            filterStartDate = cal.getTime();
            filterEndDate = cal.getTime();
        } else if (daysAgo == 1) { // Yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1);
            filterStartDate = cal.getTime();
            filterEndDate = cal.getTime();
        } else {
            filterEndDate = cal.getTime(); // Today
            cal.add(Calendar.DAY_OF_YEAR, -(daysAgo - 1));
            filterStartDate = cal.getTime();
        }
        
        adapter.updateRange(filterStartDate, filterEndDate);
        updateDisplayDate(displayTv);
        
        // Align calendar view if needed? Optional.
    }
    
    private void resetPresetVisuals(View root) {
         root.findViewById(R.id.btnPresetToday).setSelected(false);
         root.findViewById(R.id.btnPresetYesterday).setSelected(false);
         root.findViewById(R.id.btnPresetLast7).setSelected(false);
         root.findViewById(R.id.btnPresetLast30).setSelected(false);
    }

    private List<Date> generateDaysForMonth(Calendar monthCalendar) {
        List<Date> days = new ArrayList<>();
        Calendar cal = (Calendar) monthCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sun=1
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Add empty for prev days
        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(null);
        }
        
        for (int i = 1; i <= maxDay; i++) {
            days.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    private void updateMonthHeader(TextView tv) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tv.setText(sdf.format(currentDisplayCalendar.getTime()));
    }
    
    private void updateDisplayDate(TextView tv) {
        if (filterStartDate == null) {
            tv.setText("Select Date");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM", Locale.getDefault());
        String text = sdf.format(filterStartDate);
        if (filterEndDate != null && !isSameDay(filterStartDate, filterEndDate)) {
            text += " - " + sdf.format(filterEndDate);
        }
        tv.setText(text);
    }
    
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void showGeneralFilterBottomSheet() {
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

        // Date (General)
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
            // Priority 1: Check Date Range (from Date Filter)
            boolean matchesRange = true;
            if (filterStartDate != null) {
                // Should parse item.getDateTime() and check if between filterStartDate and filterEndDate
                // Dummy implementation for safety:
                matchesRange = true; 
            }

            // Priority 2: Check Categories (from General Filter)
            boolean matchesCategory = true;
            if (!selectedCategories.isEmpty()) {
                boolean catMatch = false;
                for (String cat : selectedCategories) {
                    if (item.getCategory() != null && item.getCategory().contains(cat)) catMatch = true;
                    if (cat.equals("Keys") && item.getTitle().toLowerCase().contains("key")) catMatch = true;
                }
                matchesCategory = catMatch;
            }

            // Priority 3: Check Status (from General Filter)
            boolean matchesStatus = true;
            if (selectedStatus.equals("Resolved")) matchesStatus = false; 

            // Priority 4: Check General Date (from General Filter) - optional overlap
            boolean matchesGeneralDate = true;
            String time = item.getDateTime().toLowerCase();
            if (selectedDateFilter.equals("Last 24 hours")) {
                if (time.contains("yesterday") || time.contains("oct")) matchesGeneralDate = false;
            } else if (selectedDateFilter.equals("Last week")) {
                if (time.contains("oct")) matchesGeneralDate = false;
            }

            if (matchesCategory && matchesStatus && matchesGeneralDate && matchesRange) {
                displayItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
