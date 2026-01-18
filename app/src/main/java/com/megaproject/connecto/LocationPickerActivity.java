package com.megaproject.connecto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.megaproject.connecto.Adapter.PlaceSuggestionAdapter;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private TextView tvSelectedAddressTitle, tvSelectedAddressSubtitle, tvTooltipLocation;
    private EditText etSearch;
    private View btnConfirmLocation, fabMyLocation, cardSearch, cardBottom;
    private String currentAddress = "";
    private boolean isViewOnly = false;
    private LatLng initialLocation = null;
    
    // Suggestions
    private RecyclerView rvSuggestions;
    private View cardSuggestions;
    private PlaceSuggestionAdapter suggestionAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private boolean isProgrammaticChange = false;

    // Views
    private View btnZoomIn, btnZoomOut;
    private TextView tvRadiusBadge;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupMap();
    }
    
    private static final int SPEECH_REQUEST_CODE = 1003;

    private boolean handleSearch(TextView v, int actionId, android.view.KeyEvent event) {
        String query = etSearch.getText().toString();
        if (!query.isEmpty()) {
            searchLocation(query);
            hideKeyboard();
            if (cardSuggestions != null) cardSuggestions.setVisibility(View.GONE);
        }
        return true;
    }

    private void initViews() {
        // Extras and existing core views setup...
        isViewOnly = getIntent().getBooleanExtra("is_view_only", false);
        isRadiusMode = getIntent().getBooleanExtra("is_radius_mode", false);
        currentRadiusKm = getIntent().getIntExtra("initial_radius", 15);
        
        // ... (Keep existing Intent extras handling)
        double lat = getIntent().getDoubleExtra("latitude", 0);
        double lng = getIntent().getDoubleExtra("longitude", 0);
        if (lat != 0 && lng != 0) {
            initialLocation = new LatLng(lat, lng);
        }
        String passedAddress = getIntent().getStringExtra("address");
        if (passedAddress != null) currentAddress = passedAddress;

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Views
        tvSelectedAddressTitle = findViewById(R.id.tvSelectedAddressTitle);
        tvSelectedAddressSubtitle = findViewById(R.id.tvSelectedAddressSubtitle);
        // Tooltip removed in new layout or optional
        // tvTooltipLocation = findViewById(R.id.tvTooltipLocation); 
        etSearch = findViewById(R.id.etSearch);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        cardSearch = findViewById(R.id.cardSearch);
        cardBottom = findViewById(R.id.cardBottom);
        
        // New Views
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        tvRadiusBadge = findViewById(R.id.tvRadiusBadge);
        
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }
        
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
            });
        }
        
        // Radius Logic
        if (isRadiusMode) {
             // In new design, radius is just displayed as a badge on map
             if (tvRadiusBadge != null) {
                 tvRadiusBadge.setVisibility(View.VISIBLE);
                 tvRadiusBadge.setText(currentRadiusKm + "km Radius");
             }
        }

        if (isViewOnly) {
            btnConfirmLocation.setVisibility(View.GONE);
            fabMyLocation.setVisibility(View.GONE);
            cardSearch.setVisibility(View.GONE); 
             if (btnZoomIn != null) btnZoomIn.setVisibility(View.GONE);
             if (btnZoomOut != null) btnZoomOut.setVisibility(View.GONE);
             
            if (!TextUtils.isEmpty(currentAddress)) {
                tvSelectedAddressTitle.setText(currentAddress); 
            }
        } else if (isRadiusMode) {
            // Radius Mode - Hide Search?
            // User requested "Exact image", the image HAS a search bar.
            // "I want you to create the exact image given into the screenshot... a radius should appear".
            // So we KEEP the search bar.
            // Only confirm button and Map controls.
            
            // Ensure Confirm Button and FAB are visible
            findViewById(R.id.fabMyLocation).setOnClickListener(v -> checkPermissionAndGoToLocation());
            findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> confirmLocation());
        } else {
            // Normal Picker Mode
            findViewById(R.id.fabMyLocation).setOnClickListener(v -> checkPermissionAndGoToLocation());
            findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> confirmLocation());
        }
        
        // ... (Keep existing Search listeners)
        etSearch.setOnEditorActionListener(this::handleSearch);
        
        // Suggestions setup...
        rvSuggestions = findViewById(R.id.rvSuggestions);
        cardSuggestions = findViewById(R.id.cardSuggestions);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        
        suggestionAdapter = new PlaceSuggestionAdapter(address -> {
            // On Item Click
            isProgrammaticChange = true;
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            
             // Set Text
            String name = address.getFeatureName();
            if (name == null) name = address.getThoroughfare();
            if (name == null) name = address.getLocality();
            etSearch.setText(name);
            etSearch.clearFocus();
            hideKeyboard();
            
            cardSuggestions.setVisibility(View.GONE);
            isProgrammaticChange = false;
        });
        rvSuggestions.setAdapter(suggestionAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
             // ... (Keep existing implementation)
             @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
             @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammaticChange) return;
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                if (s.length() > 2) {
                    searchRunnable = () -> fetchSuggestions(s.toString());
                    handler.postDelayed(searchRunnable, 500); 
                } else {
                    cardSuggestions.setVisibility(View.GONE);
                }
             }
             @Override public void afterTextChanged(Editable s) {}
        });
        

    }

    private void searchLocation(String locationName) {
        // Use FetchSuggestions logic but pick first result?
        // Or keep existing synchronous logic but careful on main thread exceptions (Geocoder can be blocking)
        // Existing logic is technically risky on UI thread.
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
                handler.post(() -> {
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Search error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchSuggestions(String query) {
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                // Get up to 5 results
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                handler.post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        suggestionAdapter.setSuggestions(addresses);
                        cardSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        cardSuggestions.setVisibility(View.GONE);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                // Fail silently for suggestions
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Customize Map settings
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We use our own FAB
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Default startup location (Pune, India) to ensure map isn't blank while asking permission
        LatLng defaultLoc = new LatLng(18.5204, 73.8567);
        
        if (initialLocation != null) {
            defaultLoc = initialLocation;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 15));
            // Add marker for view only
            if (isViewOnly) {
                mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(initialLocation));
                getAddressFromLocation(initialLocation); // populate text
                mMap.getUiSettings().setAllGesturesEnabled(true); // Allow zoom/pan
            }
        } else {
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 15));
        }

        if (!isViewOnly) {
            // Try to enable my location layer if permission granted (for the blue dot)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                if (initialLocation == null) goToCurrentLocation();
            } else {
                // Request permission immediately on load to show blue dot
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            // Listener for camera movement (center pin is fixed, map moves)
            mMap.setOnCameraIdleListener(this::onCameraIdle);
            
            if (isRadiusMode) {
                 updateMapCircle(mMap.getCameraPosition().target, currentRadiusKm);
            }
        } else {
             // In view only...
        }
    }

    private void onCameraIdle() {
        if (mMap == null) return;
        
        LatLng center = mMap.getCameraPosition().target;
        getAddressFromLocation(center);
        
        if (isRadiusMode) {
            updateMapCircle(center, currentRadiusKm);
        }
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                // Parse address
                String street = address.getThoroughfare(); // "2464 Royal Ln"
                String subThoroughfare = address.getSubThoroughfare(); // "2464"
                if (street == null) street = address.getFeatureName();
                
                String locality = address.getLocality(); // "Mesa"
                String adminArea = address.getAdminArea(); // "New Jersey" (using State for admin area usually)
                String postalCode = address.getPostalCode(); // "45463"
                String countryCode = address.getCountryCode(); // "US"
                
                // Construct Title: "2464 Royal Ln. Mesa"
                StringBuilder title = new StringBuilder();
                if (street != null) title.append(street);
                if (locality != null) {
                    if (title.length() > 0) title.append(". ");
                    title.append(locality);
                }
                if (title.length() == 0) title.append("Unknown Location");
                
                // Construct Subtitle: "New Jersey 45463, US"
                StringBuilder subtitle = new StringBuilder();
                if (adminArea != null) subtitle.append(adminArea);
                if (postalCode != null) subtitle.append(" ").append(postalCode);
                if (countryCode != null) {
                    if (subtitle.length() > 0) subtitle.append(", ");
                    subtitle.append(countryCode);
                }

                // Update UI
                String titleStr = title.toString();
                String subtitleStr = subtitle.toString();
                
                tvSelectedAddressTitle.setText(titleStr);
                tvSelectedAddressSubtitle.setText(subtitleStr);
                
                // Tooltip often just shows the street or title
                tvTooltipLocation.setText(street != null ? street : titleStr);
                
                // Store for return
                currentAddress = titleStr + ", " + subtitleStr;
                
            } else {
                tvSelectedAddressTitle.setText("Unknown Location");
                tvSelectedAddressSubtitle.setText(latLng.latitude + ", " + latLng.longitude);
                currentAddress = latLng.latitude + ", " + latLng.longitude;
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvSelectedAddressTitle.setText("Location Error");
            tvSelectedAddressSubtitle.setText("Check internet connection");
        }
    }

    private void checkPermissionAndGoToLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (mMap != null) mMap.setMyLocationEnabled(true);
            goToCurrentLocation();
        }
    }

    private void goToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 17));
                }
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
                goToCurrentLocation();
            } else {
                Toast.makeText(this, "Permission required to find location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            java.util.ArrayList<String> result = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                etSearch.setText(spokenText);
                searchLocation(spokenText);
            }
        }
    }

    private void confirmLocation() {
        if (TextUtils.isEmpty(currentAddress)) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", currentAddress);
        if (isRadiusMode) {
            resultIntent.putExtra("selected_radius", currentRadiusKm);
            
            // Return center as well if needed
            LatLng target = mMap.getCameraPosition().target;
            resultIntent.putExtra("latitude", target.latitude);
            resultIntent.putExtra("longitude", target.longitude);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    // Radius Mode Vars
    private boolean isRadiusMode = false;
    private int currentRadiusKm = 15;
    private com.google.android.gms.maps.model.Circle currentCircle;
    private TextView tvRadiusValue;
    private android.widget.SeekBar seekBarRadius;
    private View layoutRadiusSelector;

    // Helper to update circle
    private void updateMapCircle(LatLng center, int radiusKm) {
        if (mMap == null) return;
        
        if (currentCircle == null) {
            currentCircle = mMap.addCircle(new com.google.android.gms.maps.model.CircleOptions()
                .center(center)
                .radius(radiusKm * 1000) // meters
                .strokeWidth(2f)
                .strokeColor(getResources().getColor(R.color.home_primary))
                .fillColor(android.graphics.Color.parseColor("#402b8cee"))); // 25% primary
        } else {
            currentCircle.setCenter(center);
            currentCircle.setRadius(radiusKm * 1000);
        }
    }
}
