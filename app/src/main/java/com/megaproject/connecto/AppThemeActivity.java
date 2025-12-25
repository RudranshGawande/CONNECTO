package com.megaproject.connecto;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

public class AppThemeActivity extends AppCompatActivity {

    private LinearLayout optionSystem, optionLight, optionDark;
    private ImageView checkSystem, checkLight, checkDark;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_theme);

        // Set Status Bar Color to match topBar
        android.view.Window window = getWindow();
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.app_theme_surface));
        
        // Adjust Status Bar Icon Color based on theme
        // If theme is NOT night (light mode), use light-bg logic (dark icons)
        // If theme IS night (dark mode), use dark-bg logic (light icons)
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            // Light Mode: Dark Icons
             window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            // Dark Mode: Light Icons (Clear the flag)
             window.getDecorView().setSystemUiVisibility(0);
        }

        // Bind Views
        optionSystem = findViewById(R.id.optionSystem);
        optionLight = findViewById(R.id.optionLight);
        optionDark = findViewById(R.id.optionDark);
        checkSystem = findViewById(R.id.checkSystem);
        checkLight = findViewById(R.id.checkLight);
        checkDark = findViewById(R.id.checkDark);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        // Listeners for Theme Selection
        optionSystem.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });

        optionLight.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
        });

        optionDark.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
        });

        updateUI();
    }

    private void applyTheme(int mode) {
        // Save preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("night_mode", mode);
        editor.apply();

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(mode);
        
        // Update UI logic (checkmarks)
        updateUI();
        
        // Recreate activity for immediate visual update? 
        // For night mode switch, Android usually handles it if delegate is used, 
        // but explicit recreate ensures all resources reload.
        // However, standard behavior is automatic recreation if configuration changes.
    }

    private void updateUI() {
        // Get current mode
        // We use the one from prefs because AppCompatDelegate.getDefaultNightMode() might return UNSPECIFIED
        int savedMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Reset All
        setUnchecked(checkSystem);
        setUnchecked(checkLight);
        setUnchecked(checkDark);

        // Set Selected
        if (savedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            setChecked(checkSystem);
        } else if (savedMode == AppCompatDelegate.MODE_NIGHT_NO) {
            setChecked(checkLight);
        } else if (savedMode == AppCompatDelegate.MODE_NIGHT_YES) {
            setChecked(checkDark);
        }
    }

    private void setChecked(ImageView view) {
        view.setImageResource(R.drawable.ic_check_circle_filled);
        view.setColorFilter(ContextCompat.getColor(this, R.color.theme_selected_blue));
    }

    private void setUnchecked(ImageView view) {
        view.setImageResource(R.drawable.ic_radio_unchecked);
        // Using gray_300 for unselected ring
        view.setColorFilter(ContextCompat.getColor(this, R.color.gray_300));
    }
}
