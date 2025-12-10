package com.megaproject.urbanspace;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
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
    private boolean menuOpen = false;

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

        Toolbar toolbar = findViewById(R.id.topBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawerLayout);

        View brandGroup = findViewById(R.id.brandGroup);
        if (brandGroup != null) {
            brandGroup.setOnClickListener(v -> finishAffinity());
            brandGroup.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP &&
                        (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE)) {
                    brandGroup.performClick();
                    return true;
                }
                return false;
            });
        }

        ImageButton buttonMenu = findViewById(R.id.buttonMenu);
        if (buttonMenu != null) {
            buttonMenu.setOnClickListener(v -> toggleMenu());
        }

        // Load HomeFragment initially
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void toggleMenu() {
        menuOpen = !menuOpen;
        Toolbar toolbar = findViewById(R.id.topBar);
        if (toolbar != null) {
            toolbar.setElevation(menuOpen ? 8f : 2f);
        }
        Log.d("TopBar", "menu toggle");
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
