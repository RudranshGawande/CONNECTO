package com.megaproject.urbanspace.Fragment;

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
import com.megaproject.urbanspace.Adapter.WasteScheduleAdapter;
import com.megaproject.urbanspace.Model.WasteSchedule;
import com.megaproject.urbanspace.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WasteFragment extends Fragment {

    private RecyclerView recyclerView;
    private WasteScheduleAdapter adapter;
    private List<WasteSchedule> scheduleList;
    private List<WasteSchedule> filteredList;

    // Stats text
    private TextView totalCollectedText, recyclingRateText, wasteReducedText, nextPickupText, upcomingPickupsText;

    // Filter buttons
    private MaterialButton filterAllBtn, filterGeneralBtn, filterRecyclingBtn, filterOrganicBtn, filterHazardousBtn;

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

        return view;
    }

    private void loadSchedules() {
        scheduleList = new ArrayList<>();

        scheduleList.add(new WasteSchedule("General Waste", "Scheduled", "Tomorrow", "7:00 AM - 9:00 AM",
                "Sector 7, Block A", "Twice a week", "Place bins outside by 6:30 AM"));

        scheduleList.add(new WasteSchedule("Recycling Waste", "Scheduled", "In 2 days", "8:00 AM - 10:00 AM",
                "Sector 7, Block A", "Once a week", "Separate plastics and glass"));

        scheduleList.add(new WasteSchedule("Organic Waste", "Completed", "Today", "6:00 AM - 8:00 AM",
                "Sector 7, Block A", "Daily", "Ensure only biodegradable waste"));

        scheduleList.add(new WasteSchedule("Hazardous Waste", "Scheduled", "Next Week", "9:00 AM - 11:00 AM",
                "Sector 7, Block A", "Monthly", "Do not mix with general waste"));

        scheduleList.add(new WasteSchedule("General Waste", "Delayed", "Next Monday", "7:30 AM - 9:30 AM",
                "Sector 7, Block A", "Twice a week", "Collection delayed due to maintenance"));
    }

    // Apply filter and highlight selected button
    private void applyFilter(String type, MaterialButton selectedButton) {
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

        // Sort by next pickup time ascending
        Collections.sort(filteredList, Comparator.comparingLong(this::getPickupTimestamp));

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No schedules found for " + type, Toast.LENGTH_SHORT).show();
        }

        adapter.updateList(filteredList);
    }

    // Highlight selected button
    private void setSelectedFilter(MaterialButton selectedButton) {
        // Reset all buttons
        int inactiveText = getResources().getColor(R.color.filter_inactive_text);
        filterAllBtn.setBackgroundResource(R.drawable.filter_button_selector);
        filterAllBtn.setTextColor(inactiveText);
        filterGeneralBtn.setBackgroundResource(R.drawable.filter_button_selector);
        filterGeneralBtn.setTextColor(inactiveText);
        filterRecyclingBtn.setBackgroundResource(R.drawable.filter_button_selector);
        filterRecyclingBtn.setTextColor(inactiveText);
        filterOrganicBtn.setBackgroundResource(R.drawable.filter_button_selector);
        filterOrganicBtn.setTextColor(inactiveText);
        filterHazardousBtn.setBackgroundResource(R.drawable.filter_button_selector);
        filterHazardousBtn.setTextColor(inactiveText);

        // Set selected button style
        selectedButton.setBackgroundResource(R.drawable.filter_button_active);
        selectedButton.setTextColor(getResources().getColor(R.color.filter_active_text));
    }

    private long getPickupTimestamp(WasteSchedule item) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Date handling
        String dateText = item.getDate().toLowerCase(Locale.US);
        if (dateText.contains("today")) {
            // no change
        } else if (dateText.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } else if (dateText.contains("next week")) {
            cal.add(Calendar.DAY_OF_YEAR, 7);
        } else if (dateText.contains("next monday")) {
            int today = cal.get(Calendar.DAY_OF_WEEK);
            int target = Calendar.MONDAY;
            int diff = (target - today + 7) % 7;
            if (diff == 0) diff = 7;
            cal.add(Calendar.DAY_OF_YEAR, diff);
        } else {
            Matcher m = Pattern.compile("in (\\d+) days").matcher(dateText);
            if (m.find()) {
                int days = Integer.parseInt(m.group(1));
                cal.add(Calendar.DAY_OF_YEAR, days);
            }
        }

        // Time handling: take start time before the dash and set hour/minute while keeping date
        String timeRange = item.getTime();
        String startPart = timeRange.split("-")[0].trim();
        SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.US);
        try {
            java.util.Date parsed = fmt.parse(startPart);
            if (parsed != null) {
                Calendar parsedCal = Calendar.getInstance();
                parsedCal.setTime(parsed);
                cal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE));
            }
        } catch (ParseException e) {
            // fallback to existing time if parse fails
        }

        return cal.getTimeInMillis();
    }
}
