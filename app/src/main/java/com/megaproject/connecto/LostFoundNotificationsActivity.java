package com.megaproject.connecto;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.connecto.Adapter.LostFoundNotificationsAdapter;
import com.megaproject.connecto.Model.LostFoundNotificationItem;

import java.util.ArrayList;
import java.util.List;

public class LostFoundNotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LostFoundNotificationsAdapter adapter;
    private List<LostFoundNotificationItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_found_notifications);

        initViews();
        setupRecyclerView();
        loadDummyData();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        ImageView btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        items = new ArrayList<>();
        adapter = new LostFoundNotificationsAdapter(this, items);
        rvNotifications.setAdapter(adapter);
    }

    private void loadDummyData() {
        items.clear();
        
        // TODAY
        items.add(new LostFoundNotificationItem("TODAY"));
        
        items.add(new LostFoundNotificationItem(
            "Potential Match Found",
                    "2m ago",
            "A recently reported \"<b>Black Leather Wallet</b>\" matches the description of your lost item.",
            true,
            R.drawable.ic_manage_search,
            Color.parseColor("#EFF6FF"), // Blue 50
            Color.parseColor("#2b8cee") // Primary Blue
        ));

        items.add(new LostFoundNotificationItem(
            "New Message",
            "1h ago",
            "<b>Alex M.</b> sent you a message regarding \"Golden Retriever\".",
            true,
            R.drawable.ic_chat,
            Color.parseColor("#F3E8FF"), // Purple 50
            Color.parseColor("#A855F7") // Purple
        ));

        items.add(new LostFoundNotificationItem(
            "Post Published",
            "4h ago",
            "Your lost report for \"iPhone 14 Pro Max\" is now live and visible to the community.",
            false,
            R.drawable.ic_check_circle_filled, // Or circle green if I have it
            Color.parseColor("#D1FAE5"), // Green 50
            Color.parseColor("#059669") // Green
        ));

        // YESTERDAY
        items.add(new LostFoundNotificationItem("YESTERDAY"));

        items.add(new LostFoundNotificationItem(
            "Similar Item Reported",
            "Yesterday",
            "Someone reported finding \"Keys\" near Central Park. Check if it matches yours.",
            false,
            R.drawable.ic_manage_search, // recycling manage search
            Color.parseColor("#F3F4F6"), // Gray 100
            Color.parseColor("#6B7280") // Gray 500
        ));

        items.add(new LostFoundNotificationItem(
            "Safety Reminder",
            "Yesterday",
            "When meeting to retrieve items, always choose a public location like a police station or cafe.",
            false,
            R.drawable.ic_shield_person,
            Color.parseColor("#FFF7ED"), // Orange 50
            Color.parseColor("#ea580c") // Orange
        ));

        // OCT 24
        items.add(new LostFoundNotificationItem("OCT 24"));

        items.add(new LostFoundNotificationItem(
            "Case Closed",
            "Oct 24",
            "You marked \"Blue Backpack\" as successfully returned. Thanks for using Connecto!",
            false,
            R.drawable.ic_check,
            Color.parseColor("#F3F4F6"), // Gray 100
            Color.parseColor("#9CA3AF") // Gray 400
        ));

        adapter.notifyDataSetChanged();
    }
}
