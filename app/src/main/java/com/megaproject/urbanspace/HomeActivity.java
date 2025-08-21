package com.megaproject.urbanspace;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.megaproject.urbanspace.Fragment.CommunityChatFragment;
import com.megaproject.urbanspace.Fragment.EmergencyFragment;
import com.megaproject.urbanspace.Fragment.EventsFragment;
import com.megaproject.urbanspace.Fragment.HomeFragment;
import com.megaproject.urbanspace.Fragment.LostFoundFragment;
import com.megaproject.urbanspace.Fragment.ProfileFragment;
import com.megaproject.urbanspace.Fragment.ReportIssueFragment;
import com.megaproject.urbanspace.Fragment.TransportFragment;
import com.megaproject.urbanspace.Fragment.WasteFragment;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Setup hamburger icon
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        HomeFragment homeFragment = new HomeFragment();
        TransportFragment transportFragment = new TransportFragment();
        EventsFragment eventsFragment = new EventsFragment();
        WasteFragment wasteFragment = new WasteFragment();
        LostFoundFragment lostFoundFragment = new LostFoundFragment();
        ProfileFragment profileFragment = new ProfileFragment();
        ReportIssueFragment reportIssueFragment = new ReportIssueFragment();
        EmergencyFragment emergencyFragment = new EmergencyFragment();
        CommunityChatFragment communityChatFragment = new CommunityChatFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();


    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
