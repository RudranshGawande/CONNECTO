package com.megaproject.connecto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CompleteProfileOnboardingActivity extends AppCompatActivity {

    private ImageView ivOnboardingProfile;
    private EditText etFullName, etPhoneNumber, etHomeCity, etAddress, etBio;
    private TextView btnVerify;
    private MaterialButton btnGetStarted;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri;
    private boolean isImageChanged = false;

    // Permissions Request
    // Note: Scoped Storage doesn't usually need READ_EXTERNAL_STORAGE for photo picker logic
    // but uCrop might. For brevity we are using the standard picker contract.
    
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    private final ActivityResultLauncher<Intent> mCropContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    final Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        selectedImageUri = resultUri;
                        isImageChanged = true;
                        ivOnboardingProfile.setImageURI(resultUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(result.getData());
                    if (cropError != null) {
                        Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile_onboarding);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        initViews();
        setupListeners();
        
        // Pre-fill data if available (e.g. from Google Auth)
        loadInitialData();
    }

    private void initViews() {
        ivOnboardingProfile = findViewById(R.id.ivOnboardingProfile);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etHomeCity = findViewById(R.id.etHomeCity);
        etAddress = findViewById(R.id.etAddress);
        etBio = findViewById(R.id.etBio);
        btnVerify = findViewById(R.id.btnVerify);
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }

    private void setupListeners() {
        // Image Picker
        View profileContainer = (View) ivOnboardingProfile.getParent().getParent(); // RelativeLayout
        profileContainer.setOnClickListener(v -> mGetContent.launch("image/*"));
        ivOnboardingProfile.setOnClickListener(v -> mGetContent.launch("image/*"));

        // City Selector
        etHomeCity.setOnClickListener(v -> {
            LocationSelectorBottomSheet bottomSheet = new LocationSelectorBottomSheet();
            bottomSheet.setOnLocationSelectedListener(fullPath -> {
                etHomeCity.setText(fullPath);
            });
            bottomSheet.show(getSupportFragmentManager(), "LocationSelector");
        });
        
        // Verify Button
        btnVerify.setOnClickListener(v -> {
             // Logic to verify phone number - likely opens PhoneVerificationActivity
             // For now we can navigate there passing the number
             String number = etPhoneNumber.getText().toString().trim();
             if(!number.isEmpty()){
                 Intent intent = new Intent(CompleteProfileOnboardingActivity.this, PhoneVerificationActivity.class);
                 intent.putExtra("phoneNumber", number);
                 startActivity(intent);
             } else {
                 Toast.makeText(this, "Enter a phone number first", Toast.LENGTH_SHORT).show();
             }
        });

        // Submit Button
        btnGetStarted.setOnClickListener(v -> saveProfile());
        
        // Location Picker
        View btnPickLocation = findViewById(R.id.btnPickLocation);
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(CompleteProfileOnboardingActivity.this, LocationPickerActivity.class);
            mPickLocation.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> mPickLocation = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedAddress = result.getData().getStringExtra("selected_address");
                    if (selectedAddress != null) {
                        etAddress.setText(selectedAddress);
                    }
                }
            });

    private void loadInitialData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            
            // 1. Name
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                etFullName.setText(user.getDisplayName());
            }

            // 2. Phone
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                etPhoneNumber.setText(user.getPhoneNumber());
                btnVerify.setText("Verified");
                btnVerify.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                btnVerify.setBackgroundResource(R.drawable.bg_chip_pill); // Or similar green tint
                btnVerify.setEnabled(false);
            }

            // 3. Profile Picture
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .centerCrop()
                        .into(ivOnboardingProfile);
            } else {
                // If no Google Photo, show default
                ivOnboardingProfile.setImageResource(R.drawable.ic_default_profile);
            }
        }
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String homeCity = etHomeCity.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Name is required");
            return;
        }
        if (homeCity.isEmpty()) {
            Toast.makeText(this, "Please select your home city", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGetStarted.setEnabled(false);
        btnGetStarted.setText("Setting up...");

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("phoneNumber", phoneNumber);
        userData.put("homeCity", homeCity);
        userData.put("address", address);
        userData.put("bio", bio);
        userData.put("onboardingComplete", true);
        
        String safeEmail = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

        // Upload Image if changed
        if (isImageChanged && selectedImageUri != null) {
             StorageReference fileRef = storageRef.child("profile_images/" + user.getUid() + ".jpg");
             fileRef.putFile(selectedImageUri)
                     .addOnSuccessListener(taskSnapshot -> {
                         taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                             userData.put("photoUrl", uri.toString());
                             saveToFirestore(safeEmail, userData);
                         });
                     })
                     .addOnFailureListener(e -> {
                         // Failed to upload image, but still save text data? 
                         // Or fail. Let's fail for now or skip image.
                         Toast.makeText(this, "Image upload failed. Saving profile without it.", Toast.LENGTH_SHORT).show();
                         saveToFirestore(safeEmail, userData);
                     });
        } else {
            // If user has a Google Photo URL and didn't change it, we might want to save that URL to Firestore
            // so it persists as the "Source of Truth" in future sessions if they sign in via other means?
            // But logic says: Priority 1 Firestore, Priority 2 Google. So if Firestore is empty, it falls back to Google.
            // So we don't strictly NEED to save it, but it's good practice to explicit set it if we want "completing profile" to mean "snapshotting current state".
            // However, previous logic in EditProfile says if null, use Google. So we can leave it null in Firestore.
            
            saveToFirestore(safeEmail, userData);
        }
    }

    private void saveToFirestore(String docId, Map<String, Object> data) {
        db.collection("users").document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Welcome to Connecto!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CompleteProfileOnboardingActivity.this, HomeActivity.class));
                    finishAffinity(); // Clear back stack
                })
                .addOnFailureListener(e -> {
                     btnGetStarted.setEnabled(true);
                     btnGetStarted.setText("Get Started");
                     Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startCrop(Uri uri) {
        String destinationFileName = "croppedImage.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(1000, 1000);
        
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true); 
        
        // Match Theme
        options.setToolbarColor(getResources().getColor(R.color.home_background));
        options.setStatusBarColor(getResources().getColor(R.color.home_background));
        options.setToolbarWidgetColor(getResources().getColor(R.color.home_text_main));
        options.setRootViewBackgroundColor(getResources().getColor(R.color.home_background));
        options.setActiveControlsWidgetColor(getResources().getColor(R.color.home_primary));
        
        uCrop.withOptions(options);
        
        mCropContent.launch(uCrop.getIntent(this));
    }
}
