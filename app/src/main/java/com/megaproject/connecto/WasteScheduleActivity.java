package com.megaproject.connecto;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.megaproject.connecto.Adapter.WasteScheduleCardAdapter;
import com.megaproject.connecto.Model.WasteSchedule;

import java.util.ArrayList;
import java.util.List;

public class WasteScheduleActivity extends AppCompatActivity {

    private FrameLayout notificationButton, locationButton;
    private View notificationDot;
    private Chip chipAll, chipGeneral, chipRecycling, chipOrganic, chipGlass;
    private ImageView heroImage, heroIcon;
    private TextView heroTitle, heroTimeWindow, heroStatus, heroDate, heroInfoButton;
    private RecyclerView scheduleRecyclerView;
    private WasteScheduleCardAdapter scheduleAdapter;
    private List<WasteSchedule> scheduleList;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waste_schedule);

        initializeViews();
        setupHeroCard();
        setupFilterChips();
        setupRecyclerView();
        loadScheduleData();

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateTitleWithLocality();
    }
    
    private void updateTitleWithLocality() {
        android.content.SharedPreferences prefs = getSharedPreferences("WasteManagement", MODE_PRIVATE);
        String localityName = prefs.getString("selected_locality_name", "All Areas");
        
        TextView pageTitle = findViewById(R.id.pageTitle);
        if (pageTitle != null) {
            pageTitle.setText("Schedule (" + localityName + ")");
        }
    }

    private void initializeViews() {
        notificationButton = findViewById(R.id.notificationButton);
        locationButton = findViewById(R.id.locationButton);
        notificationDot = findViewById(R.id.notificationDot);
        
        chipAll = findViewById(R.id.chipAll);
        chipGeneral = findViewById(R.id.chipGeneral);
        chipRecycling = findViewById(R.id.chipRecycling);
        chipOrganic = findViewById(R.id.chipOrganic);
        chipGlass = findViewById(R.id.chipGlass);
        
        heroImage = findViewById(R.id.heroImage);
        heroIcon = findViewById(R.id.heroIcon);
        heroTitle = findViewById(R.id.heroTitle);
        heroTimeWindow = findViewById(R.id.heroTimeWindow);
        heroStatus = findViewById(R.id.heroStatus);
        heroDate = findViewById(R.id.heroDate);
        heroInfoButton = findViewById(R.id.heroInfoButton);
        
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
    }

    private void setupHeroCard() {
        // Set next collection data (this would come from database/API)
        heroTitle.setText("Plastic & Metal");
        heroTimeWindow.setText("06:00 AM - 08:00 AM");
        heroStatus.setText("Tomorrow Morning");
        heroDate.setText("Wednesday, Oct 24");
        
        heroInfoButton.setOnClickListener(v -> {
            // Show dialog with waste category information
            showWasteInfoDialog("Plastic & Metal");
        });
        
        notificationButton.setOnClickListener(v -> {
            // Open notifications
            openNotifications();
        });
        
        locationButton.setOnClickListener(v -> openLocationSelection());
    }

    private void setupFilterChips() {
        View.OnClickListener chipClickListener = v -> {
            Chip selectedChip = (Chip) v;
            currentFilter = selectedChip.getText().toString();
            filterScheduleData(currentFilter);
        };
        
        chipAll.setOnClickListener(chipClickListener);
        chipGeneral.setOnClickListener(chipClickListener);
        chipRecycling.setOnClickListener(chipClickListener);
        chipOrganic.setOnClickListener(chipClickListener);
        chipGlass.setOnClickListener(chipClickListener);
    }

    private void setupRecyclerView() {
        scheduleList = new ArrayList<>();
        scheduleAdapter = new WasteScheduleCardAdapter(this, scheduleList);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(scheduleAdapter);
    }

    private void loadScheduleData() {
        // Sample data - replace with actual database/API calls
        scheduleList.clear();
        
        WasteSchedule organic = new WasteSchedule(
            "1",
            "Organic Waste",
            "Upcoming",
            "Friday, Oct 26",
            "07:00 AM - 09:00 AM",
            "Sector 7, Block A",
            "Twice a week",
            "Separate food waste from packaging",
            R.drawable.ic_compost,
            R.color.circle_orange,
            R.color.icon_orange
        );
        
        WasteSchedule general = new WasteSchedule(
            "2",
            "General Waste",
            "Upcoming",
            "Sunday, Oct 28",
            "08:00 PM - 10:00 PM",
            "Sector 7, Block A",
            "Daily",
            "All non-recyclable waste",
            R.drawable.ic_delete,
            R.color.circle_gray,
            R.color.icon_gray
        );
        
        WasteSchedule glass = new WasteSchedule(
            "3",
            "Glass Collection",
            "Next Week",
            "Tue, Oct 30",
            "06:00 AM - 08:00 AM",
            "Sector 7, Block A",
            "Weekly",
            "Clean glass bottles and jars",
            R.drawable.ic_wine_bar,
            R.color.circle_purple,
            R.color.icon_purple
        );
        
        scheduleList.add(organic);
        scheduleList.add(general);
        scheduleList.add(glass);
        
        scheduleAdapter.notifyDataSetChanged();
    }

    private void filterScheduleData(String filter) {
        if (filter.equals("All")) {
            loadScheduleData();
            return;
        }
        
        List<WasteSchedule> filteredList = new ArrayList<>();
        for (WasteSchedule schedule : scheduleList) {
            if (schedule.getType().toLowerCase().contains(filter.toLowerCase())) {
                filteredList.add(schedule);
            }
        }
        
        scheduleList.clear();
        scheduleList.addAll(filteredList);
        scheduleAdapter.notifyDataSetChanged();
    }

    private void showWasteInfoDialog(String wasteType) {
        // Implement dialog showing what items belong to this waste category
        // You can use AlertDialog or BottomSheetDialog
    }

    private void openNotifications() {
        android.content.Intent intent = new android.content.Intent(this, com.megaproject.connecto.ReminderSettingsActivity.class);
        startActivity(intent);
    }
    
    private void openLocationSelection() {
        android.content.Intent intent = new android.content.Intent(this, com.megaproject.connecto.LocalitySelectionActivity.class);
        startActivity(intent);
    }
}



