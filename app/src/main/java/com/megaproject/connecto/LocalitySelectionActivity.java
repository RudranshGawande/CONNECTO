package com.megaproject.connecto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.megaproject.connecto.Adapter.LocalityAdapter;
import com.megaproject.connecto.Model.Locality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalitySelectionActivity extends AppCompatActivity implements LocalityAdapter.OnLocalityClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private ImageButton backButton;
    private MaterialButton detectLocationButton;
    private TextInputEditText searchEditText;
    private RecyclerView recentLocationsRecyclerView;
    private RecyclerView allLocalitiesRecyclerView;
    
    private LocalityAdapter recentAdapter;
    private LocalityAdapter allLocalitiesAdapter;
    private List<Locality> recentLocalities;
    private List<Locality> allLocalities;
    private List<Locality> filteredLocalities;
    
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locality_selection);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        initializeViews();
        setupRecyclerViews();
        loadLocalitiesData();
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        detectLocationButton = findViewById(R.id.detectLocationButton);
        searchEditText = findViewById(R.id.searchEditText);
        recentLocationsRecyclerView = findViewById(R.id.recentLocationsRecyclerView);
        allLocalitiesRecyclerView = findViewById(R.id.allLocalitiesRecyclerView);
    }

    private void setupRecyclerViews() {
        recentLocalities = new ArrayList<>();
        allLocalities = new ArrayList<>();
        filteredLocalities = new ArrayList<>();
        
        recentAdapter = new LocalityAdapter(this, recentLocalities, this);
        recentLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentLocationsRecyclerView.setAdapter(recentAdapter);
        
        allLocalitiesAdapter = new LocalityAdapter(this, filteredLocalities, this);
        allLocalitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allLocalitiesRecyclerView.setAdapter(allLocalitiesAdapter);
    }

    private void loadLocalitiesData() {
        // Load recent localities
        recentLocalities.clear();
        recentLocalities.add(new Locality("1", "Downtown District", "Zone A", "Mon/Thu Pickup", true));
        recentLocalities.add(new Locality("2", "North Hills", "Zone B", "Wed Pickup", true));
        recentAdapter.notifyDataSetChanged();
        
        // Load all localities
        allLocalities.clear();
        allLocalities.add(new Locality("3", "Central Business Zone", "Zone C", "Tue/Fri Pickup", false));
        allLocalities.add(new Locality("4", "East Riverside", "Zone D", "Mon/Wed Pickup", false));
        allLocalities.add(new Locality("5", "Garden Grove", "Zone E", "Thu Pickup", false));
        allLocalities.add(new Locality("6", "Highland Park", "Zone F", "Tue/Thu Pickup", false));
        allLocalities.add(new Locality("7", "Westside Suburbs", "Zone G", "Wed/Sat Pickup", false));
        
        filteredLocalities.clear();
        filteredLocalities.addAll(allLocalities);
        allLocalitiesAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        detectLocationButton.setOnClickListener(v -> detectCurrentLocation());
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocalities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void detectCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), 
                    location.getLongitude(), 
                    1
            );
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                String subLocality = address.getSubLocality();
                
                String detectedLocation = subLocality != null ? subLocality : locality;
                searchEditText.setText(detectedLocation);
                Toast.makeText(this, "Location detected: " + detectedLocation, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterLocalities(String query) {
        filteredLocalities.clear();
        
        if (query.isEmpty()) {
            filteredLocalities.addAll(allLocalities);
        } else {
            for (Locality locality : allLocalities) {
                if (locality.getName().toLowerCase().contains(query.toLowerCase()) ||
                    locality.getZone().toLowerCase().contains(query.toLowerCase())) {
                    filteredLocalities.add(locality);
                }
            }
        }
        
        allLocalitiesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLocalityClick(Locality locality) {
        // Save selected locality to SharedPreferences or Database
        saveSelectedLocality(locality);
        
        Toast.makeText(this, "Selected: " + locality.getName(), Toast.LENGTH_SHORT).show();
        
        // Return to previous screen or navigate to main screen
        finish();
    }

    private void saveSelectedLocality(Locality locality) {
        // Save to SharedPreferences
        getSharedPreferences("WasteManagement", MODE_PRIVATE)
                .edit()
                .putString("selected_locality_id", locality.getId())
                .putString("selected_locality_name", locality.getName())
                .putString("selected_locality_zone", locality.getZone())
                .apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


