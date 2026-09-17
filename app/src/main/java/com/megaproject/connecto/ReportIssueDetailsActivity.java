package com.megaproject.connecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaproject.connecto.R;

public class ReportIssueDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvIssueType;
    private String categoryName;

    private static final int REQUEST_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    private androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher;
    private androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher;
    private androidx.recyclerview.widget.RecyclerView rvPhotos;
    private com.megaproject.connecto.Adapter.PhotoAdapter photoAdapter;
    private java.util.List<android.net.Uri> photoUris = new java.util.ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;


    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> mapSelectionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue_details);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get data from intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null) categoryName = "Issue";

        tvIssueType = findViewById(R.id.tv_issue_type);
        tvIssueType.setText(categoryName);
        
        rvPhotos = findViewById(R.id.rv_photos);
        photoAdapter = new com.megaproject.connecto.Adapter.PhotoAdapter(this, photoUris, position -> {
            photoUris.remove(position);
            photoAdapter.notifyItemRemoved(position);
        });
        rvPhotos.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        setupPermissionLaunchers();
        setupMapLauncher();
        setupButtons();
        setupIssueTypeDropdown();
    }

    private void setupIssueTypeDropdown() {
        String[] issues = {"Pothole", "Street Light", "Waste Management", "Water Leakage", "Stray Animals", "Encroachment", "Other"};
        
        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown, issues));
        listPopupWindow.setAnchorView(tvIssueType);
        listPopupWindow.setModal(true);
        
        // Custom background for the popup window
        listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_dropdown_popup));
        
        // Set width to match the TextView
        tvIssueType.post(() -> listPopupWindow.setWidth(tvIssueType.getWidth()));

        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            categoryName = issues[position];
            tvIssueType.setText(categoryName);
            listPopupWindow.dismiss();
        });

        tvIssueType.setOnClickListener(v -> listPopupWindow.show());
    }

    private void setupMapLauncher() {
        mapSelectionLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("ADDRESS");
                        double lat = result.getData().getDoubleExtra("LAT", 0);
                        double lng = result.getData().getDoubleExtra("LNG", 0);
                        
                        android.widget.EditText etLocation = findViewById(R.id.et_location);
                        etLocation.setText(address);
                        
                        if (mMap != null) {
                            LatLng newLocation = new LatLng(lat, lng);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(newLocation));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
                        }
                    }
                }
        );
    }

    private void setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                photoUris.add(uri);
                photoAdapter.notifyItemInserted(photoUris.size() - 1);
            }
        });
    }

    private void setupButtons() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            // Save report
            String location = ((android.widget.EditText) findViewById(R.id.et_location)).getText().toString();
            String desc = ((android.widget.EditText) findViewById(R.id.et_description)).getText().toString(); // Check if et_description exists, assuming standard naming or I need to verify layout
            if (location.isEmpty()) location = "Unknown Location";
            
            com.megaproject.connecto.Model.Report report = new com.megaproject.connecto.Model.Report(
                    categoryName, 
                    desc, 
                    location, 
                    0, 0, null); // LatLng and image pending full impl
                    
            new com.megaproject.connecto.Manager.ReportRepository(this).saveReport(report);

            android.content.Intent intent = new android.content.Intent(this, com.megaproject.connecto.ReportSuccessActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_add_photo).setOnClickListener(v -> {
           checkPermissionAndOpenGallery();
        });

        findViewById(R.id.btn_edit_on_map).setOnClickListener(v -> {
            openMapSelection();
        });
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false); // Disable interaction on mini-map
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        checkLocationPermission();
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                updateLocationUI(currentLatLng);
                            } else {
                                // Default location if location is null
                                LatLng defaultLocation = new LatLng(37.7749, -122.4194);
                                updateLocationUI(defaultLocation);
                            }
                        });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateLocationUI(LatLng latLng) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        getAddress(latLng);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                // Permission denied, set default location
                LatLng defaultLocation = new LatLng(37.7749, -122.4194);
                updateLocationUI(defaultLocation);
            }
        }
    }

    public void openMapSelection() {
        android.content.Intent intent = new android.content.Intent(this, MapSelectionActivity.class);
        mapSelectionLauncher.launch(intent);
    }

    private void getAddress(LatLng latLng) {
        android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
        try {
            java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                android.widget.EditText etLocation = findViewById(R.id.et_location);
                etLocation.setText(addressText);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Fallback to coordinates
             android.widget.EditText etLocation = findViewById(R.id.et_location);
             etLocation.setText(latLng.latitude + ", " + latLng.longitude);
        }
    }
}
