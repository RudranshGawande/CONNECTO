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
        
        // Tabs (Visual only)
        TextView tabAll = findViewById(R.id.tabAll);
        TextView tabLost = findViewById(R.id.tabLost);
        TextView tabFound = findViewById(R.id.tabFound);
        
        View.OnClickListener tabListener = v -> {
             // Reset all
             resetTab(tabAll);
             resetTab(tabLost);
             resetTab(tabFound);
             
             // Set active
             TextView active = (TextView) v;
             active.setBackgroundResource(R.drawable.bg_tab_item_selected);
             active.setTextColor(getResources().getColor(R.color.home_text_main));
             active.setElevation(1f);
        };
        
        tabAll.setOnClickListener(tabListener);
        tabLost.setOnClickListener(tabListener);
        tabFound.setOnClickListener(tabListener);
    }
    
    private void resetTab(TextView tab) {
        tab.setBackgroundResource(0);
        tab.setTextColor(getResources().getColor(R.color.home_text_secondary));
        tab.setElevation(0f);
    }

    private void setupRecyclerView() {
        rvMyPosts = findViewById(R.id.rvMyPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        myItems = new ArrayList<>();
        adapter = new MyLostFoundAdapter(this, myItems, this);
        rvMyPosts.setAdapter(adapter);
    }

    private void loadDummyData() {
        // Load MY items from Manager
        // Note: In real app, we would observe LiveData/Flow. Here we just reload onResume or init.
        // For simplicity, just load now.
        List<LostFoundItem> items = com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().getMyItems();
        
        myItems.clear();
        myItems.addAll(items);
        adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload data in case new items were added via other flows (though here FAB is removed)
        // or just to be safe.
        loadDummyData();
    }

    @Override
    public void onMenuClick(LostFoundItem item) {
        UpdateStatusBottomSheet bottomSheet = UpdateStatusBottomSheet.newInstance(item.getTitle(), item.getStatus());
        bottomSheet.setListener(newStatus -> {
            // Update Data
            com.megaproject.connecto.Manager.LostFoundDataManager.getInstance().updateItemStatus(item, newStatus);
            // Refresh UI
            loadDummyData(); // Reloads data into adapter
        });
        bottomSheet.show(getSupportFragmentManager(), "UpdateStatusBottomSheet");
    }
}
