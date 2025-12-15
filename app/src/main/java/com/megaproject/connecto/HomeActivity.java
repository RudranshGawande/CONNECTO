package com.megaproject.connecto;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

import com.megaproject.connecto.Fragment.CommunityChatFragment;
import com.megaproject.connecto.Fragment.EmergencyFragment;
import com.megaproject.connecto.Fragment.EventsFragment;
import com.megaproject.connecto.Fragment.HomeFragment;
import com.megaproject.connecto.Fragment.LostFoundFragment;
import com.megaproject.connecto.Fragment.ReportIssueFragment;
import com.megaproject.connecto.Fragment.TransportFragment;
import com.megaproject.connecto.Fragment.WasteFragment;

public class HomeActivity extends AppCompatActivity {

    public boolean doubletap = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
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
        // Initialize Theme from Preferences
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO); // Default Light
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != savedMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(savedMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.topBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup Theme Toggle
        setupThemeToggle();

        drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    menuOpen = true;
                    updateMenuButtonState();
                    syncMenuSelection(); // Also sync when opened via swipe
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

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void setupThemeToggle() {
        ImageButton themeToggle = findViewById(R.id.themeToggle);
        if (themeToggle != null) {
            updateThemeIcon(themeToggle);
            themeToggle.setOnClickListener(v -> toggleAppTheme(themeToggle));
        }
    }

    private void updateThemeIcon(ImageButton btn) {
        int nightMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
        if (nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
             btn.setImageResource(R.drawable.ic_light_mode); 
             btn.setContentDescription("Switch to Light Mode");
        } else {
             btn.setImageResource(R.drawable.ic_dark_mode);
             btn.setContentDescription("Switch to Dark Mode");
        }
    }

    private void toggleAppTheme(ImageButton btn) {
        int currentMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
        int newMode;
        if (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        }
        
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode);
        
        // Save preference
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putInt("night_mode", newMode).apply();
        
        // Icon update happens automatically due to activity recreation, but we can set it
        updateThemeIcon(btn); 
    }

    public void onToggleMenu(View v) {
        // Just for safety if button click comes here
        toggleMenu();
    }

    private void toggleMenu() {
        if (drawerLayout == null) return;

        // Avoid crashes if no END drawer view is configured yet
        if (!hasEndDrawer()) {
            Log.w("TopBar", "No end drawer configured in DrawerLayout; ignoring menu toggle.");
            return;
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            menuOpen = false;
        } else {
            // SYNC STATE BEFORE OPENING
            syncMenuSelection();
            drawerLayout.openDrawer(GravityCompat.END);
            menuOpen = true;
        }

        Toolbar toolbar = findViewById(R.id.topBar);
        if (toolbar != null) {
            toolbar.setElevation(menuOpen ? 8f : 2f);
        }

        updateMenuButtonState();
        Log.d("TopBar", "menu toggle: " + (menuOpen ? "open" : "closed"));
    }

    private boolean hasEndDrawer() {
        if (drawerLayout == null) return false;

        for (int i = 0; i < drawerLayout.getChildCount(); i++) {
            View child = drawerLayout.getChildAt(i);
            DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) child.getLayoutParams();
            if ((lp.gravity & GravityCompat.END) == GravityCompat.END) {
                return true;
            }
        }
        return false;
    }

    // Determine which fragment is active and update navigationView checked item
    private void syncMenuSelection() {
        if (navigationView == null) return;
        
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        int idToCheck = R.id.nav_home; // Default

        if (current instanceof com.megaproject.connecto.Fragment.TransportFragment) {
            idToCheck = R.id.nav_transport;
        } else if (current instanceof com.megaproject.connecto.Fragment.WasteFragment) {
            idToCheck = R.id.nav_waste;
        } else if (current instanceof com.megaproject.connecto.Fragment.ReportIssueFragment) {
            idToCheck = R.id.nav_report_issue;
        } else if (current instanceof com.megaproject.connecto.Fragment.LostFoundFragment) {
            idToCheck = R.id.nav_lost_found;
        } else if (current instanceof com.megaproject.connecto.Fragment.EventsFragment) {
            idToCheck = R.id.nav_events;
        } else if (current instanceof com.megaproject.connecto.Fragment.EmergencyFragment) {
            idToCheck = R.id.nav_emergency;
        } else if (current instanceof com.megaproject.connecto.Fragment.CommunityChatFragment) {
            idToCheck = R.id.nav_community;
        } else if (current instanceof com.megaproject.connecto.Fragment.HomeFragment) {
             idToCheck = R.id.nav_home;
        }

        navigationView.setCheckedItem(idToCheck);
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
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }
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
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_home) {
            navigateHome();
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        } else if (id == R.id.nav_transport) {
            fragment = transportFragment;
        } else if (id == R.id.nav_waste) {
            fragment = wasteFragment;
        } else if (id == R.id.nav_report_issue) {
            fragment = reportIssueFragment;
        } else if (id == R.id.nav_lost_found) {
            fragment = lostFoundFragment;
        } else if (id == R.id.nav_events) {
            fragment = eventsFragment;
        } else if (id == R.id.nav_emergency) {
            fragment = emergencyFragment;
        } else if (id == R.id.nav_community) {
            fragment = communityChatFragment;
        } else if (id == R.id.nav_sign_out) {
            // Firebase Sign Out
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

            // Google Sign Out
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            com.google.android.gms.auth.api.signin.GoogleSignInClient googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut();

            // Navigate to Login
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finishAffinity();
            return true;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }
}


