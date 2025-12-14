package com.megaproject.connecto;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class ReminderSettingsActivity extends AppCompatActivity {

    private ImageButton backButton;
    private SwitchMaterial masterSwitch;
    
    private LinearLayout generalWasteItem, recyclingItem, organicItem, glassItem;
    private SwitchMaterial generalWasteSwitch, recyclingSwitch, organicSwitch, glassSwitch;
    private TextView generalWasteTime, recyclingTime, organicTime, glassTime;
    
    private LinearLayout defaultTimeButton;
    private TextView defaultTimeText;
    
    private boolean isMasterEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        initializeViews();
        loadSavedSettings();
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        masterSwitch = findViewById(R.id.masterSwitch);
        
        generalWasteItem = findViewById(R.id.generalWasteItem);
        recyclingItem = findViewById(R.id.recyclingItem);
        organicItem = findViewById(R.id.organicItem);
        glassItem = findViewById(R.id.glassItem);
        
        generalWasteSwitch = findViewById(R.id.generalWasteSwitch);
        recyclingSwitch = findViewById(R.id.recyclingSwitch);
        organicSwitch = findViewById(R.id.organicSwitch);
        glassSwitch = findViewById(R.id.glassSwitch);
        
        generalWasteTime = findViewById(R.id.generalWasteTime);
        recyclingTime = findViewById(R.id.recyclingTime);
        organicTime = findViewById(R.id.organicTime);
        glassTime = findViewById(R.id.glassTime);
        
        defaultTimeButton = findViewById(R.id.defaultTimeButton);
        defaultTimeText = findViewById(R.id.defaultTimeText);
    }

    private void loadSavedSettings() {
        // Load settings from SharedPreferences
        isMasterEnabled = getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getBoolean("master_reminder_enabled", true);
        masterSwitch.setChecked(isMasterEnabled);
        
        // Load individual category settings
        generalWasteSwitch.setChecked(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getBoolean("general_reminder_enabled", true));
        recyclingSwitch.setChecked(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getBoolean("recycling_reminder_enabled", true));
        organicSwitch.setChecked(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getBoolean("organic_reminder_enabled", true));
        glassSwitch.setChecked(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getBoolean("glass_reminder_enabled", false));
        
        // Load reminder times
        generalWasteTime.setText(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getString("general_reminder_time", "1 hour before pickup"));
        recyclingTime.setText(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getString("recycling_reminder_time", "The night before"));
        organicTime.setText(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .getString("organic_reminder_time", "30 mins before"));
        
        updateGlassTimeDisplay();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        masterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMasterEnabled = isChecked;
            saveMasterSetting(isChecked);
            updateCategorySwitchesState(isChecked);
            
            String message = isChecked ? "All reminders enabled" : "All reminders disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
        
        // General Waste
        generalWasteItem.setOnClickListener(v -> {
            if (generalWasteSwitch.isChecked()) {
                showTimePickerDialog("General Waste", generalWasteTime);
            }
        });
        generalWasteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveReminderSetting("general_reminder_enabled", isChecked);
        });
        
        // Recycling
        recyclingItem.setOnClickListener(v -> {
            if (recyclingSwitch.isChecked()) {
                showTimePickerDialog("Recycling", recyclingTime);
            }
        });
        recyclingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveReminderSetting("recycling_reminder_enabled", isChecked);
        });
        
        // Organic
        organicItem.setOnClickListener(v -> {
            if (organicSwitch.isChecked()) {
                showTimePickerDialog("Organic", organicTime);
            }
        });
        organicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveReminderSetting("organic_reminder_enabled", isChecked);
        });
        
        // Glass
        glassItem.setOnClickListener(v -> {
            if (glassSwitch.isChecked()) {
                showTimePickerDialog("Glass & Bottles", glassTime);
            }
        });
        glassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveReminderSetting("glass_reminder_enabled", isChecked);
            updateGlassTimeDisplay();
        });
        
        // Default Time Button
        defaultTimeButton.setOnClickListener(v -> {
            showDefaultTimePickerDialog();
        });
    }

    private void updateCategorySwitchesState(boolean enabled) {
        generalWasteSwitch.setEnabled(enabled);
        recyclingSwitch.setEnabled(enabled);
        organicSwitch.setEnabled(enabled);
        glassSwitch.setEnabled(enabled);
    }

    private void updateGlassTimeDisplay() {
        if (glassSwitch.isChecked()) {
            glassTime.setText(getSharedPreferences("WasteManagement", MODE_PRIVATE)
                    .getString("glass_reminder_time", "1 hour before"));
            glassTime.setTextColor(getResources().getColor(R.color.primary, null));
        } else {
            glassTime.setText("Off");
            glassTime.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void showTimePickerDialog(String category, TextView timeTextView) {
        String[] options = {
            "30 mins before",
            "1 hour before",
            "2 hours before",
            "The night before",
            "Custom time"
        };
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Set reminder for " + category);
        builder.setItems(options, (dialog, which) -> {
            String selectedTime = options[which];
            timeTextView.setText(selectedTime);
            saveReminderTime(category, selectedTime);
            Toast.makeText(this, "Reminder set: " + selectedTime, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showDefaultTimePickerDialog() {
        String[] options = {
            "30 mins before",
            "1 hour before",
            "2 hours before",
            "The night before"
        };
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Set default reminder time");
        builder.setItems(options, (dialog, which) -> {
            String selectedTime = options[which];
            defaultTimeText.setText(selectedTime);
            
            // Apply to all enabled categories
            if (generalWasteSwitch.isChecked()) {
                generalWasteTime.setText(selectedTime);
                saveReminderTime("General Waste", selectedTime);
            }
            if (recyclingSwitch.isChecked()) {
                recyclingTime.setText(selectedTime);
                saveReminderTime("Recycling", selectedTime);
            }
            if (organicSwitch.isChecked()) {
                organicTime.setText(selectedTime);
                saveReminderTime("Organic", selectedTime);
            }
            if (glassSwitch.isChecked()) {
                glassTime.setText(selectedTime);
                saveReminderTime("Glass & Bottles", selectedTime);
            }
            
            Toast.makeText(this, "Default time applied to all categories", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void saveMasterSetting(boolean enabled) {
        getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .edit()
                .putBoolean("master_reminder_enabled", enabled)
                .apply();
    }

    private void saveReminderSetting(String key, boolean enabled) {
        getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .edit()
                .putBoolean(key, enabled)
                .apply();
    }

    private void saveReminderTime(String category, String time) {
        String key = category.toLowerCase().replace(" & ", "_").replace(" ", "_") + "_reminder_time";
        getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .edit()
                .putString(key, time)
                .apply();
    }
}


