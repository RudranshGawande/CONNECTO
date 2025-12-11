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
import androidx.core.view.GravityCompat;
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
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    menuOpen = true;
                    updateMenuButtonState();
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    menuOpen = false;
                    updateMenuButtonState();
                }
            });
        }

        View brandGroup = findViewById(R.id.brandGroup);
        if (brandGroup != null) {
            brandGroup.setOnClickListener(v -> navigateHome());
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
            buttonMenu.setOnClickListener(this::onToggleMenu);
        }

        // Load HomeFragment initially
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    public void onToggleMenu(View v) {
        toggleMenu();
    }

    private void toggleMenu() {
        if (drawerLayout == null) return;

        // Avoid crashes if no START drawer view is configured yet
        if (!hasStartDrawer()) {
            Log.w("TopBar", "No start drawer configured in DrawerLayout; ignoring hamburger toggle.");
            return;
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            menuOpen = false;
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
            menuOpen = true;
        }

        Toolbar toolbar = findViewById(R.id.topBar);
        if (toolbar != null) {
            toolbar.setElevation(menuOpen ? 8f : 2f);
        }

        updateMenuButtonState();
        Log.d("TopBar", "menu toggle: " + (menuOpen ? "open" : "closed"));
    }

    private boolean hasStartDrawer() {
        if (drawerLayout == null) return false;

        for (int i = 0; i < drawerLayout.getChildCount(); i++) {
            View child = drawerLayout.getChildAt(i);
            DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) child.getLayoutParams();
            if ((lp.gravity & GravityCompat.START) == GravityCompat.START) {
                return true;
            }
        }
        return false;
    }

    private void updateMenuButtonState() {
        ImageButton buttonMenu = findViewById(R.id.buttonMenu);
        if (buttonMenu != null) {
            int descRes = menuOpen ? R.string.aria_close_menu : R.string.aria_open_menu;
            buttonMenu.setContentDescription(getString(descRes));
        }
    }

    private void navigateHome() {
        getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If there are fragments in the back stack, pop one instead of exiting
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

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
