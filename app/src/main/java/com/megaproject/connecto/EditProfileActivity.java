package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etProfilePhoneNumber, etHomeCity, etBio;
    private Button btnSaveChanges;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Init Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etProfilePhoneNumber = findViewById(R.id.etProfilePhoneNumber);
        etHomeCity = findViewById(R.id.etHomeCity);
        etBio = findViewById(R.id.etBio);
        
        // Find Save Button (It's the Button in the sticky footer)
        // Since it doesn't have an ID in XML provided in context, I'll need to assume one or look for Button type.
        // Looking at XML from Step 198: 
        // <Button android:layout_width="match_parent" ... android:text="Save Changes" ... />
        // It has NO ID. I must fix this or use findViewWithTag if possible, but finding by ID is standard.
        // PROACTIVE FIX: I will assume I can find it by structure or I should have added an ID. 
        // Actually, I can't modify XML in this tool call reliably AND Java. 
        // I'll assume the user might have missed the ID or I need to add it.
        // Wait, I can use `findViewsWithText` or similar hack, but better to just add the ID to XML first?
        // No, I'll just write Java code that *would* work if ID existed, or I'll try to find it.
        // ACTUALLY, I'll check if I can just assume an ID. The XML output showed NO ID.
        // I will add an ID "btnSaveChanges" to the XML first.
        
        // ... (But I am replacing Java content).
        // Let's defer binding the button if I can't find it? No, the user wants "Save Logic".
        // I will blindly assume R.id.btnSaveChanges and fix XML in next step.
        btnSaveChanges = findViewById(R.id.btnSaveChanges); 

        // Phone Number Click - Opens Verification
        // Phone Number Click - Opens Verification
        etProfilePhoneNumber.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                // Already Verified - Ask for confirmation
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Change Phone Number")
                    .setMessage("You already have a verified phone number: " + user.getPhoneNumber() + ".\n\nDo you want to change it?")
                    .setPositiveButton("Yes, Change", (dialog, which) -> {
                        Intent intent = new Intent(EditProfileActivity.this, PhoneVerificationActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else {
                // Not verified - Start directly
                Intent intent = new Intent(EditProfileActivity.this, PhoneVerificationActivity.class);
                startActivity(intent);
            }
        });
        
        // Home City Click
        etHomeCity.setOnClickListener(v -> {
            LocationSelectorBottomSheet bottomSheet = new LocationSelectorBottomSheet();
            bottomSheet.setOnLocationSelectedListener(fullPath -> {
                etHomeCity.setText(fullPath);
                // We'll save this in saveProfileChanges
            });
            bottomSheet.show(getSupportFragmentManager(), "LocationSelector");
        });

        // Save Changes Click
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        }

        // Back Button Logic
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set Email (Always from Auth, Read-Only)
            String email = user.getEmail();
            if (email != null) {
                etEmail.setText(email);
            }
            etEmail.setEnabled(false);
            etEmail.setFocusable(false);
            etEmail.setClickable(false);
            
            // Set Phone Number (From Auth first, effectively "Verified")
            String phone = user.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                etProfilePhoneNumber.setText(phone);
                // Show Verified Badge (Green Check)
                etProfilePhoneNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_verified_badge, 0);
            } else {
                etProfilePhoneNumber.setText("");
                etProfilePhoneNumber.setHint("Tap to verify phone number");
                // Remove any badge
                etProfilePhoneNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            }

            // Fetch Name from Firestore
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                etFullName.setText(user.getDisplayName());
            } 
            
            // CRITICAL FIX: Use Email as Document ID (Single Source of Truth)
            if (email != null) {
                String safeEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
                db.collection("users").document(safeEmail).get()
                    .addOnSuccessListener(documentSnapshot -> {
                         if (documentSnapshot.exists()) {
                             if (documentSnapshot.contains("fullName")) {
                                 etFullName.setText(documentSnapshot.getString("fullName"));
                             }
                             if (documentSnapshot.contains("homeCity")) {
                                 etHomeCity.setText(documentSnapshot.getString("homeCity"));
                             }
                             if (documentSnapshot.contains("bio")) {
                                 etBio.setText(documentSnapshot.getString("bio"));
                             }
                         }
                    });
            }
        }
    }
    
    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String newName = etFullName.getText().toString().trim();
        String homeCity = etHomeCity.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        if (!homeCity.isEmpty()) updates.put("homeCity", homeCity);
        if (!bio.isEmpty()) updates.put("bio", bio);
        
        // CRITICAL FIX: Use Email as Document ID (Single Source of Truth)
        // We write to users/{email} so all data (profile + lostFound) lives together.
        String safeEmail = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        
        // Also save the Auth UID for reference/security rules if needed later
        updates.put("uid", user.getUid());

        db.collection("users").document(safeEmail)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                // Optional: Update Auth Display Name
                com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
                user.updateProfile(profileUpdates);
                finish(); // Close activity on success
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                android.util.Log.e("EditProfile", "Error saving profile", e);
            });
    }
}

