package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Adapter.MyLostFoundAdapter;
import com.megaproject.connecto.Model.LostFoundItem;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MyLostFoundActivity extends AppCompatActivity implements MyLostFoundAdapter.OnItemActionListener {

    private RecyclerView rvMyPosts;
    private MyLostFoundAdapter adapter;
    private List<LostFoundItem> myItems;
    private List<LostFoundItem> allMyItems; // Store original data

    private String currentType = "ALL";
    private String currentStatus = "Active";

    private TextView tabAll, tabLost, tabFound;
    private TextView chipActive, chipRecovered, chipClosed, chipExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lost_found);

        initViews();
        setupRecyclerView();
        loadDummyData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Tabs
        tabAll = findViewById(R.id.tabAll);
        tabLost = findViewById(R.id.tabLost);
        tabFound = findViewById(R.id.tabFound);
        
        View.OnClickListener tabListener = v -> {
             resetTabs();
             TextView active = (TextView) v;
             active.setBackgroundResource(R.drawable.bg_tab_item_selected);
             active.setTextColor(getResources().getColor(R.color.home_text_main));
             active.setElevation(2f); // Using 2f for better shadow

             if (v == tabAll) currentType = "ALL";
             else if (v == tabLost) currentType = "LOST";
             else if (v == tabFound) currentType = "FOUND";
             
             filterData();
        };
        
        tabAll.setOnClickListener(tabListener);
        tabLost.setOnClickListener(tabListener);
        tabFound.setOnClickListener(tabListener);

        // Chips
        chipActive = findViewById(R.id.chipActive);
        chipRecovered = findViewById(R.id.chipRecovered);
        chipClosed = findViewById(R.id.chipClosed);
        chipExpired = findViewById(R.id.chipExpired);

        View.OnClickListener chipListener = v -> {
            resetChips();
            TextView active = (TextView) v;
            // Always keep the same pill shape background
            active.setBackgroundResource(R.drawable.bg_chip_pill);
            active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            active.setTextColor(getResources().getColor(R.color.white));

            if (v == chipActive) currentStatus = "Active";
            else if (v == chipRecovered) currentStatus = "Recovered";
            else if (v == chipClosed) currentStatus = "Closed";
            else if (v == chipExpired) currentStatus = "Expired";

            filterData();
        };

        chipActive.setOnClickListener(chipListener);
        chipRecovered.setOnClickListener(chipListener);
        chipClosed.setOnClickListener(chipListener);
        chipExpired.setOnClickListener(chipListener);
    }
    
    private void resetTabs() {
        resetTabStyle(tabAll);
        resetTabStyle(tabLost);
        resetTabStyle(tabFound);
    }

    private void resetTabStyle(TextView tab) {
        tab.setBackgroundResource(0);
        tab.setTextColor(getResources().getColor(R.color.home_text_secondary));
        tab.setElevation(0f);
    }

    private void resetChips() {
        resetChipStyle(chipActive);
        resetChipStyle(chipRecovered);
        resetChipStyle(chipClosed);
        resetChipStyle(chipExpired);
    }

    private void resetChipStyle(TextView chip) {
        // Use same pill background for inactive, just different tint
        chip.setBackgroundResource(R.drawable.bg_chip_pill);
        chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)));
        chip.setTextColor(getResources().getColor(R.color.gray_700));
    }

    private void setupRecyclerView() {
        rvMyPosts = findViewById(R.id.rvMyPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        myItems = new ArrayList<>();
        allMyItems = new ArrayList<>();
        adapter = new MyLostFoundAdapter(this, myItems, this);
        rvMyPosts.setAdapter(adapter);
    }

    private void loadDummyData() {
        List<LostFoundItem> items = com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().getMyItems();
        allMyItems.clear();
        allMyItems.addAll(items);
        filterData();
    }

    private void filterData() {
        myItems.clear();
        for (LostFoundItem item : allMyItems) {
            boolean matchesType = "ALL".equals(currentType) || 
                                ("LOST".equals(currentType) && "lost".equalsIgnoreCase(item.getType())) ||
                                ("FOUND".equals(currentType) && "found".equalsIgnoreCase(item.getType()));

            boolean matchesStatus = false;
            // Map UI status to data status
            // Assuming item.getStatus() returns lowercase: "open", "found", "closed", "expired"
            String itemStatus = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
            
            if ("Active".equals(currentStatus)) {
                // Active usually means 'open' or 'lost' depending on context, assuming 'open' is the default active status
                matchesStatus = "open".equals(itemStatus) || "active".equals(itemStatus);
            } else if ("Recovered".equals(currentStatus)) {
                matchesStatus = "found".equals(itemStatus) || "recovered".equals(itemStatus);
            } else if ("Closed".equals(currentStatus)) {
                matchesStatus = "closed".equals(itemStatus);
            } else if ("Expired".equals(currentStatus)) {
                matchesStatus = "expired".equals(itemStatus);
            }

            if (matchesType && matchesStatus) {
                myItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        
        // Update count if needed (assuming there's a count TextView, but based on XML it's static "4 items" or need a findView.
        // I'll check if I should update the '4 items' text.
        // XML has '4 items' in a TextView but it doesn't have an ID in the original XML I saw?
        // Wait, step 184 shows <TextView ... text="4 items" ... /> inside sectionHeader linear layout.
        // It does NOT have an ID. I should probably add one if I want to update it, but the user asked to FIX FILTERS.
        // I'll stick to fixing filters logic first.
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDummyData();
    }

    @Override
    public void onMenuClick(LostFoundItem item) {
        UpdateStatusBottomSheet bottomSheet = UpdateStatusBottomSheet.newInstance(item.getTitle(), item.getStatus());
        bottomSheet.setListener(newStatus -> {
            com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().updateItemStatus(item, newStatus);
            loadDummyData(); 
        });
        bottomSheet.show(getSupportFragmentManager(), "UpdateStatusBottomSheet");
    }
}
