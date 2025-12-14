package com.megaproject.connecto.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.megaproject.connecto.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EmergencyFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long SOS_HOLD_DURATION = 3000; // 3 seconds

    private TextView currentLocationText;
    private TextView signalStrengthText;
    private ProgressBar signalStrengthBar;
    private ImageView gpsStatusIcon;
    private View sosButton;
    private MaterialCardView medicalCard, fireCard, policeCard;
    private ImageButton backButton, contactsButton;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler sosHandler;
    private Runnable sosRunnable;
    private boolean isSosPressed = false;
    private String selectedEmergencyType = "General";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        initializeViews(view);
        setupLocationServices();
        setupSosButton();
        setupEmergencyTypeCards();
        detectCurrentLocation();

        return view;
    }

    private void initializeViews(View view) {
        currentLocationText = view.findViewById(R.id.currentLocationText);
        signalStrengthText = view.findViewById(R.id.signalStrengthText);
        signalStrengthBar = view.findViewById(R.id.signalStrengthBar);
        gpsStatusIcon = view.findViewById(R.id.gpsStatusIcon);
        sosButton = view.findViewById(R.id.sosButton);
        medicalCard = view.findViewById(R.id.medicalCard);
        fireCard = view.findViewById(R.id.fireCard);
        policeCard = view.findViewById(R.id.policeCard);
        backButton = view.findViewById(R.id.backButton);
        contactsButton = view.findViewById(R.id.contactsButton);

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        contactsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.EmergencyProfileActivity.class);
            startActivity(intent);
        });

        sosHandler = new Handler(Looper.getMainLooper());
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupSosButton() {
        sosButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isSosPressed = true;
                    sosButton.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    
                    sosRunnable = () -> {
                        if (isSosPressed) {
                            triggerSosAlert();
                        }
                    };
                    sosHandler.postDelayed(sosRunnable, SOS_HOLD_DURATION);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isSosPressed = false;
                    sosButton.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    sosHandler.removeCallbacks(sosRunnable);
                    return true;
            }
            return false;
        });
    }

    private void setupEmergencyTypeCards() {
        medicalCard.setOnClickListener(v -> {
            selectedEmergencyType = "Medical";
            highlightSelectedCard(medicalCard);
            Toast.makeText(getContext(), "Medical emergency selected", Toast.LENGTH_SHORT).show();
        });

        fireCard.setOnClickListener(v -> {
            selectedEmergencyType = "Fire";
            highlightSelectedCard(fireCard);
            Toast.makeText(getContext(), "Fire emergency selected", Toast.LENGTH_SHORT).show();
        });

        policeCard.setOnClickListener(v -> {
            selectedEmergencyType = "Police";
            highlightSelectedCard(policeCard);
            Toast.makeText(getContext(), "Police emergency selected", Toast.LENGTH_SHORT).show();
        });
    }

    private void highlightSelectedCard(MaterialCardView selectedCard) {
        // Reset all cards
        medicalCard.setStrokeWidth(1);
        fireCard.setStrokeWidth(1);
        policeCard.setStrokeWidth(1);

        // Highlight selected
        selectedCard.setStrokeWidth(4);
        selectedCard.setStrokeColor(getResources().getColor(R.color.emergency_red, null));
    }

    private void detectCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                        updateGpsSignalStrength(85); // Simulated strong signal
                    } else {
                        currentLocationText.setText("Unable to detect location");
                        updateGpsSignalStrength(0);
                    }
                })
                .addOnFailureListener(e -> {
                    currentLocationText.setText("Location error");
                    updateGpsSignalStrength(0);
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                currentLocationText.setText(addressLine != null ? addressLine : "Location detected");
            }
        } catch (IOException e) {
            currentLocationText.setText("Address unavailable");
        }
    }

    private void updateGpsSignalStrength(int strength) {
        signalStrengthBar.setProgress(strength);
        
        if (strength >= 70) {
            signalStrengthText.setText("Strong");
            signalStrengthText.setTextColor(getResources().getColor(R.color.status_active, null));
            signalStrengthBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_active, null))
            );
        } else if (strength >= 40) {
            signalStrengthText.setText("Moderate");
            signalStrengthText.setTextColor(getResources().getColor(R.color.status_today, null));
            signalStrengthBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_today, null))
            );
        } else {
            signalStrengthText.setText("Weak");
            signalStrengthText.setTextColor(getResources().getColor(R.color.status_missed, null));
            signalStrengthBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_missed, null))
            );
        }
    }

    private static final int PERMISSION_REQUEST_CODE_SOS = 1002;

    private void triggerSosAlert() {
        if (getContext() == null) return;
        
        // Check permissions
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE_SOS);
            return;
        }

        String emergencyNumber;
        if (selectedEmergencyType.equalsIgnoreCase("Fire")) {
            emergencyNumber = "101";
        } else if (selectedEmergencyType.equalsIgnoreCase("Medical")) {
            emergencyNumber = "108";
        } else {
            emergencyNumber = "100"; // Police or General
        }
        
        // 1. Call
        try {
            android.content.Intent callIntent = new android.content.Intent(android.content.Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + emergencyNumber));
            startActivity(callIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to make call", Toast.LENGTH_SHORT).show();
        }
        
        // 2. SMS
        sendEmergencySms();
        
        // Confirmation
        Toast.makeText(getContext(), "Calling " + emergencyNumber + ". Alert sent to contacts.", Toast.LENGTH_LONG).show();
        
        // Vibrate via helper
        vibrateDevice();
    }
    
    private void sendEmergencySms() {
        // Load contacts
        java.util.List<String> phoneNumbers = new java.util.ArrayList<>();
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("EmergencyPrefs", android.content.Context.MODE_PRIVATE);
        String json = prefs.getString("saved_contacts", null);
        if (json != null) {
            try {
                org.json.JSONArray array = new org.json.JSONArray(json);
                for(int i=0; i<array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);
                    if (obj.getBoolean("active")) {
                        phoneNumbers.add(obj.getString("phone"));
                    }
                }
            } catch (Exception e) {}
        }
        
        if (phoneNumbers.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("No Emergency Contacts")
                .setMessage("Please add at least one emergency contact to send SMS alerts.")
                .setPositiveButton("Add Now", (d, w) -> {
                    startActivity(new android.content.Intent(getContext(), com.megaproject.connecto.EmergencyContactsActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
            return; 
        }

        // Get User Info
        android.content.SharedPreferences userPrefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        String userName = userPrefs.getString("fullName", "User");
        String location = currentLocationText != null ? currentLocationText.getText().toString() : "Unknown Location";
        String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        
        String msg = "SOS Alert! Type: " + selectedEmergencyType + ".\n" +
                     "This is an emergency. I need help immediately.\n" +
                     "Name: " + userName + "\n" +
                     "Loc: " + location + "\n" +
                     "Time: " + timestamp;

        try {
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            java.util.ArrayList<String> parts = smsManager.divideMessage(msg);
            
            for (String phone : phoneNumbers) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void vibrateDevice() {
        if (getContext() != null) {
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) 
                        getContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                         vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                         vibrator.vibrate(500);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectCurrentLocation();
            } else {
                currentLocationText.setText("Location unavailable");
                Toast.makeText(getContext(), "Location permission required for emergency services",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE_SOS) {
            boolean callGranted = false;
            boolean smsGranted = false;
            
            // Check results
             for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CALL_PHONE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    callGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.SEND_SMS) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    smsGranted = true;
                }
            }
            
            if (callGranted && smsGranted) {
                triggerSosAlert();
            } else {
                Toast.makeText(getContext(), "Call & SMS permissions blocked. Cannot execute SOS.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sosHandler != null && sosRunnable != null) {
            sosHandler.removeCallbacks(sosRunnable);
        }
    }
}


