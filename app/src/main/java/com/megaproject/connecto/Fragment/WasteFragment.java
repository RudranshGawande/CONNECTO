package com.megaproject.connecto.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.megaproject.connecto.Adapter.WasteScheduleAdapter;
import com.megaproject.connecto.Model.WasteSchedule;
import com.megaproject.connecto.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WasteFragment extends Fragment {

    private RecyclerView recyclerView;
    private WasteScheduleAdapter adapter;
    private List<WasteSchedule> scheduleList;
    private List<WasteSchedule> filteredList;

    // Stats text
    private TextView totalCollectedText, recyclingRateText, wasteReducedText, nextPickupText, upcomingPickupsText;

    // Filter buttons
    private MaterialButton filterAllBtn, filterGeneralBtn, filterRecyclingBtn, filterOrganicBtn, filterHazardousBtn;
    
    // Current Filter State
    private String currentFilterType = "All";

    public WasteFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_waste, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Disable nested scrolling for RecyclerView as it's inside a ScrollView
        recyclerView.setNestedScrollingEnabled(false);

        totalCollectedText = view.findViewById(R.id.totalCollectedText);
        recyclingRateText = view.findViewById(R.id.recyclingRateText);
        wasteReducedText = view.findViewById(R.id.wasteReducedText);
        nextPickupText = view.findViewById(R.id.nextPickupText);
        upcomingPickupsText = view.findViewById(R.id.upcomingPickupsText);

        filterAllBtn = view.findViewById(R.id.filterAllBtn);
        filterGeneralBtn = view.findViewById(R.id.filterGeneralBtn);
        filterRecyclingBtn = view.findViewById(R.id.filterRecyclingBtn);
        filterOrganicBtn = view.findViewById(R.id.filterOrganicBtn);
        filterHazardousBtn = view.findViewById(R.id.filterHazardousBtn);

        // Load dummy data
        loadSchedules();

        adapter = new WasteScheduleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Set default filter -> All
        applyFilter("All", filterAllBtn);

        // Set filters
        filterAllBtn.setOnClickListener(v -> applyFilter("All", filterAllBtn));
        filterGeneralBtn.setOnClickListener(v -> applyFilter("General", filterGeneralBtn));
        filterRecyclingBtn.setOnClickListener(v -> applyFilter("Recycling", filterRecyclingBtn));
        filterOrganicBtn.setOnClickListener(v -> applyFilter("Organic", filterOrganicBtn));
        filterHazardousBtn.setOnClickListener(v -> applyFilter("Hazardous", filterHazardousBtn));

        // Navigation to full schedule
        MaterialButton viewFullScheduleBtn = view.findViewById(R.id.viewFullScheduleBtn);
        viewFullScheduleBtn.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.WasteScheduleActivity.class);
            startActivity(intent);
        });
        
        // Notification Icon Click
        View notificationIcon = view.findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.ReminderSettingsActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void loadSchedules() {
        scheduleList = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);
        
        // Helper to create and add schedule with calculated timestamp
        // 1. Organic - TODAY Morning
        Calendar cal1 = (Calendar) calendar.clone();
        cal1.set(Calendar.HOUR_OF_DAY, 7); // 7 AM
        cal1.set(Calendar.MINUTE, 0);
        WasteSchedule s1 = new WasteSchedule("1", "Organic Waste", "Scheduled", "Today", "07:00 AM - 09:00 AM",
                "Sector 7, Block A", "Daily", "Ensure only biodegradable waste");
        s1.setSortTimestamp(cal1.getTimeInMillis());
        scheduleList.add(s1);

        // 2. General - TOMORROW Morning
        Calendar cal2 = (Calendar) calendar.clone();
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 8); // 8 AM
        cal2.set(Calendar.MINUTE, 0);
        String date2 = dateFormat.format(cal2.getTime());
        WasteSchedule s2 = new WasteSchedule("2", "General Waste", "Scheduled", "Tomorrow, " + date2.split(",")[1].trim(), "08:00 AM - 10:00 AM",
                "Sector 7, Block A", "Twice a week", "All non-recyclable waste");
        s2.setSortTimestamp(cal2.getTimeInMillis());
        scheduleList.add(s2);

        // 3. Organic - TOMORROW Afternoon
        Calendar cal3 = (Calendar) calendar.clone();
        cal3.add(Calendar.DAY_OF_YEAR, 1);
        cal3.set(Calendar.HOUR_OF_DAY, 16); // 4 PM
        cal3.set(Calendar.MINUTE, 0);
        WasteSchedule s3 = new WasteSchedule("3", "Organic Waste", "Scheduled", "Tomorrow", "04:00 PM - 06:00 PM",
                "Sector 7, Block A", "Daily", "Afternoon pickup");
        s3.setSortTimestamp(cal3.getTimeInMillis());
        scheduleList.add(s3);
        
        // 4. Recycling - Day after Tomorrow
        Calendar cal4 = (Calendar) calendar.clone();
        cal4.add(Calendar.DAY_OF_YEAR, 2);
        cal4.set(Calendar.HOUR_OF_DAY, 6); // 6 AM
        String date4 = dateFormat.format(cal4.getTime());
        WasteSchedule s4 = new WasteSchedule("4", "Recycling Waste", "Scheduled", date4, "06:00 AM - 08:00 AM",
                "Sector 7, Block A", "Weekly", "Sort paper, plastic, and glass");
        s4.setSortTimestamp(cal4.getTimeInMillis());
        scheduleList.add(s4);

        // 5. Hazardous - Next Week (5 days from now)
        Calendar cal5 = (Calendar) calendar.clone();
        cal5.add(Calendar.DAY_OF_YEAR, 5);
        cal5.set(Calendar.HOUR_OF_DAY, 9);
        String date5 = dateFormat.format(cal5.getTime());
        WasteSchedule s5 = new WasteSchedule("5", "Hazardous Waste", "Scheduled", date5, "09:00 AM - 11:00 AM",
                "Sector 7, Block A", "Monthly", "Batteries and electronics");
        s5.setSortTimestamp(cal5.getTimeInMillis());
        scheduleList.add(s5);
    }

    // Apply filter and highlight selected button
    private void applyFilter(String type, MaterialButton selectedButton) {
        currentFilterType = type;
        filteredList = new ArrayList<>();

        // Highlight selected filter
        setSelectedFilter(selectedButton);

        if (type.equals("All")) {
            filteredList.addAll(scheduleList);
        } else {
            for (WasteSchedule item : scheduleList) {
                if (item.getType().toLowerCase().contains(type.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        // Sort using the explicit timestamp
        Collections.sort(filteredList, (o1, o2) -> Long.compare(o1.getSortTimestamp(), o2.getSortTimestamp()));

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No schedules found for " + type, Toast.LENGTH_SHORT).show();
        }

        adapter.updateList(filteredList);
        
        // Update top text
        if (!filteredList.isEmpty()) {
            WasteSchedule next = filteredList.get(0);
            if (nextPickupText != null) {
                // Determine display string for 'Next Pickup' text
                String dayStr = next.getDate();
                if (dayStr.contains(",")) dayStr = dayStr.split(",")[0];
                nextPickupText.setText(dayStr + " at " + next.getTime().split("-")[0].trim());
            }
        }
    }

    // Highlight selected button
    private void setSelectedFilter(MaterialButton selectedButton) {
        // Reset all buttons
        int inactiveText = getResources().getColor(R.color.filter_inactive_text);
        
        // Helper to reset style
        resetButtonStyle(filterAllBtn, inactiveText);
        resetButtonStyle(filterGeneralBtn, inactiveText);
        resetButtonStyle(filterRecyclingBtn, inactiveText);
        resetButtonStyle(filterOrganicBtn, inactiveText);
        resetButtonStyle(filterHazardousBtn, inactiveText);

        // Set selected button style
        selectedButton.setBackgroundResource(R.drawable.filter_button_active);
        selectedButton.setTextColor(getResources().getColor(R.color.filter_active_text));
    }
    
    private void resetButtonStyle(MaterialButton btn, int color) {
        btn.setBackgroundResource(R.drawable.filter_button_selector);
        btn.setTextColor(color);
    }
}



