package com.megaproject.connecto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import java.util.Calendar;

public class ReportLostItemActivity extends AppCompatActivity {

    private EditText etDateLost, etItemName, etDescription, etLocation;
    private Spinner spinnerCategory;
    private ImageView ivSelectedPhoto;
    private View layoutUploadPlaceholder;
    private Uri selectedImageUri;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Photo Picker Launcher
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivSelectedPhoto.setImageURI(uri);
                    ivSelectedPhoto.setVisibility(View.VISIBLE);
                    layoutUploadPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost_item);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
    }

    private void initViews() {
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Category Spinner
        spinnerCategory = findViewById(R.id.spinnerCategory);
        String[] categories = new String[]{"Select category", "Electronics", "Wallet & Money", "Keys", "Clothing & Accessories", "Documents", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown_unified, categories);
        // Fallback if item_dropdown_unified doesn't exist suitable for text, use simple_spinner_item
        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(simpleAdapter);

        // Inputs
        etItemName = findViewById(R.id.etItemName);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);

        // Date Picker
        etDateLost = findViewById(R.id.etDateLost);
        etDateLost.setOnClickListener(v -> showDatePicker());

        // Description Counter
        final TextView tvDescriptionCount = findViewById(R.id.tvDescriptionCount);
        etDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescriptionCount.setText(s.length() + "/300");
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Photo Upload
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        findViewById(R.id.btnUploadPhoto).setOnClickListener(v -> {
            pickImage.launch("image/*");
        });

        // My Location
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> getCurrentLocation());

        // Submit Button
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitReport());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Update: use simple format or something readable "Oct 24" style if preferred, keeping simple for now
                    // The dummy data uses "Oct 24" or "Yesterday". Let's use simple date string.
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etDateLost.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Geocode
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        // Try to get a relevant name: FeatureName or thoroughfare
                        String locName = address.getThoroughfare(); // Street
                        if (locName == null) locName = address.getFeatureName(); // Feature
                        if (locName == null) locName = address.getLocality(); // City
                        
                        // Combine for readability
                        String fullAddr = "";
                        if (address.getThoroughfare() != null) fullAddr += address.getThoroughfare();
                        if (address.getLocality() != null) fullAddr += (fullAddr.isEmpty() ? "" : ", ") + address.getLocality();
                        
                        if (fullAddr.isEmpty()) fullAddr = "Unknown Location (" + location.getLatitude() + "," + location.getLongitude() + ")";
                        
                        etLocation.setText(fullAddr);
                    } else {
                        etLocation.setText(location.getLatitude() + ", " + location.getLongitude());
                    }
                } catch (IOException e) {
                    etLocation.setText(location.getLatitude() + ", " + location.getLongitude());
                    Toast.makeText(this, "Geocoding failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitReport() {
        String title = etItemName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = etDateLost.getText().toString().trim();
        int categoryPos = spinnerCategory.getSelectedItemPosition(); // 0 is Select Category
        String category = categoryPos > 0 ? spinnerCategory.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(title)) {
            etItemName.setError("Title required");
            return;
        }
        if (categoryPos == 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(date)) {
            etDateLost.setError("Date required");
            return;
        }
        
        // Return data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("category", category);
        resultIntent.putExtra("location", location);
        resultIntent.putExtra("date", date);
        resultIntent.putExtra("description", desc);
        // Pass URI string if photo selected
        if (selectedImageUri != null) {
            resultIntent.putExtra("imageUri", selectedImageUri.toString());
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
