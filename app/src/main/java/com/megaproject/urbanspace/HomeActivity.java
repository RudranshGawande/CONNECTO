package com.megaproject.urbanspace;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.megaproject.urbanspace.Fragment.CommunityChatFragment;
import com.megaproject.urbanspace.Fragment.EmergencyFragment;
import com.megaproject.urbanspace.Fragment.EventsFragment;
import com.megaproject.urbanspace.Fragment.HomeFragment;
import com.megaproject.urbanspace.Fragment.LostFoundFragment;
import com.megaproject.urbanspace.Fragment.ReportIssueFragment;
import com.megaproject.urbanspace.Fragment.TransportFragment;
import com.megaproject.urbanspace.Fragment.WasteFragment;

public class HomeActivity extends AppCompatActivity {

    public boolean doubletap = false;
    private DrawerLayout drawerLayout;

    // Fragments
    TransportFragment transportFragment = new TransportFragment();
    EventsFragment eventsFragment = new EventsFragment();
    WasteFragment wasteFragment = new WasteFragment();
    LostFoundFragment lostFoundFragment = new LostFoundFragment();
    ReportIssueFragment reportIssueFragment = new ReportIssueFragment();
    EmergencyFragment emergencyFragment = new EmergencyFragment();
    CommunityChatFragment communityChatFragment = new CommunityChatFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);

        // Load HomeFragment initially
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (doubletap) {
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(HomeActivity.this, "Press again to Exit!",
                    Toast.LENGTH_SHORT).show();

            doubletap = true;

            Handler handler = new Handler();
            handler.postDelayed(() -> doubletap = false, 2000);
        }
    }
}
