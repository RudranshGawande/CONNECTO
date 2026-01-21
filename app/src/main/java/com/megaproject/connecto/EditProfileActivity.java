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

    private ImageButton btnBack;
    private android.widget.ImageView ivProfileImage;
    private android.widget.TextView tvChangePhoto;
    private androidx.cardview.widget.CardView cvProfileImage;
    
    // Form views
    private EditText etFullName, etEmail, etProfilePhoneNumber, etAddress, etBio;
    private Button btnSaveChanges;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.storage.FirebaseStorage storage;
    private com.google.firebase.storage.StorageReference storageRef;

    private android.net.Uri selectedImageUri; // The uri of image to upload
    
    // Pick Image
    private final androidx.activity.result.ActivityResultLauncher<String> mGetContent = registerForActivityResult(
        new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                startCrop(uri);
            }
        });

    // Crop Image
    private final androidx.activity.result.ActivityResultLauncher<Intent> mCropContent = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    final android.net.Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        selectedImageUri = resultUri;
                        ivProfileImage.setImageURI(resultUri);
                    }
                } else if (result.getResultCode() == com.yalantis.ucrop.UCrop.RESULT_ERROR) {
                    final Throwable cropError = com.yalantis.ucrop.UCrop.getError(result.getData());
                    if (cropError != null) {
                        Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Location Picker
    private final androidx.activity.result.ActivityResultLauncher<Intent> mPickLocation = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedAddress = result.getData().getStringExtra("selected_address");
                    if (selectedAddress != null) {
                        etAddress.setText(selectedAddress);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Init Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etProfilePhoneNumber = findViewById(R.id.etProfilePhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        etBio = findViewById(R.id.etBio);
        
        ivProfileImage = findViewById(R.id.ivProfileImage);
        
        // Setup image click listeners
        android.view.View.OnClickListener imageClickListener = v -> mGetContent.launch("image/*");
        ivProfileImage.setOnClickListener(imageClickListener);
        findViewById(R.id.ivProfileImage).setOnClickListener(imageClickListener); // Ensure ID match
        
        btnSaveChanges = findViewById(R.id.btnSaveChanges); 

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
        
        // Pick Location Click
        android.view.View btnPickLocation = findViewById(R.id.btnPickLocation);
        if (btnPickLocation != null) {
            btnPickLocation.setOnClickListener(v -> {
                Intent intent = new Intent(EditProfileActivity.this, LocationPickerActivity.class);
                mPickLocation.launch(intent);
            });
        }

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
            
            if (email != null) {
                String safeEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
                db.collection("users").document(safeEmail).get()
                    .addOnSuccessListener(documentSnapshot -> {
                         if (documentSnapshot.exists()) {
                             if (documentSnapshot.contains("fullName")) {
                                 etFullName.setText(documentSnapshot.getString("fullName"));
                             }
                             if (documentSnapshot.contains("address")) {
                                 etAddress.setText(documentSnapshot.getString("address"));
                             } else if (documentSnapshot.contains("homeCity")) {
                                 // Fallback migration: If address is missing but homeCity exists, show homeCity in address field
                                 etAddress.setText(documentSnapshot.getString("homeCity"));
                             }
                             
                              if (documentSnapshot.contains("bio")) {
                                  etBio.setText(documentSnapshot.getString("bio"));
                              }
                              // Load User Profile Image
                              String photoUrl = null;
                              if (documentSnapshot.contains("photoUrl")) {
                                  photoUrl = documentSnapshot.getString("photoUrl");
                              }
                              
                              if (photoUrl != null && !photoUrl.isEmpty()) {
                                  com.bumptech.glide.Glide.with(this)
                                      .load(photoUrl)
                                      .placeholder(R.drawable.ic_default_profile)
                                      .error(R.drawable.ic_default_profile)
                                      .centerCrop()
                                      .into(ivProfileImage);
                              } else {
                                  // Fallback to Google Auth Photo
                                  if (user.getPhotoUrl() != null) {
                                      com.bumptech.glide.Glide.with(this)
                                          .load(user.getPhotoUrl())
                                          .placeholder(R.drawable.ic_default_profile)
                                          .error(R.drawable.ic_default_profile)
                                          .centerCrop()
                                          .into(ivProfileImage);
                                  } else {
                                      // Fallback to Default Anime Boy
                                      ivProfileImage.setImageResource(R.drawable.ic_default_profile);
                                  }
                              }
                         } else {
                             // Document doesn't exist, try Google Photo or Default
                             if (user.getPhotoUrl() != null) {
                                  com.bumptech.glide.Glide.with(this)
                                      .load(user.getPhotoUrl())
                                      .placeholder(R.drawable.ic_default_profile)
                                      .error(R.drawable.ic_default_profile)
                                      .centerCrop()
                                      .into(ivProfileImage);
                              } else {
                                  ivProfileImage.setImageResource(R.drawable.ic_default_profile);
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
        String address = etAddress.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        if (!address.isEmpty()) updates.put("address", address);
        if (!bio.isEmpty()) updates.put("bio", bio);
        
        String safeEmail = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        updates.put("uid", user.getUid());

        // Check if we need to upload image first
        if (selectedImageUri != null) {
            btnSaveChanges.setEnabled(false);
            btnSaveChanges.setText("Uploading...");
            
            // Upload Image
            com.google.firebase.storage.StorageReference fileRef = storageRef.child("profile_images/" + user.getUid() + ".jpg");
            
            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            updates.put("photoUrl", downloadUrl);
                            saveFirestoreData(safeEmail, updates, user, newName);
                        }).addOnFailureListener(e -> {
                            btnSaveChanges.setEnabled(true);
                            btnSaveChanges.setText("Save Changes");
                            Toast.makeText(this, "Upload successful but failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        btnSaveChanges.setEnabled(true);
                        btnSaveChanges.setText("Save Changes");
                        Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
             saveFirestoreData(safeEmail, updates, user, newName);
        }
    }
    
    private void saveFirestoreData(String safeEmail, Map<String, Object> updates, FirebaseUser user, String newName) {
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
                if (selectedImageUri != null) {
                    btnSaveChanges.setEnabled(true);
                    btnSaveChanges.setText("Save Changes");
                }
            });
    }

    private void startCrop(android.net.Uri uri) {
        String destinationFileName = "croppedImage.jpg";
        com.yalantis.ucrop.UCrop uCrop = com.yalantis.ucrop.UCrop.of(uri, android.net.Uri.fromFile(new java.io.File(getCacheDir(), destinationFileName)));
        
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(1000, 1000);
        
        com.yalantis.ucrop.UCrop.Options options = new com.yalantis.ucrop.UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true); 
        
        // UI Customization: Clean Dark Theme to fix overlap/visuals
        options.setToolbarColor(android.graphics.Color.BLACK);
        options.setStatusBarColor(android.graphics.Color.BLACK);
        options.setToolbarWidgetColor(android.graphics.Color.WHITE);
        options.setRootViewBackgroundColor(android.graphics.Color.BLACK);
        options.setActiveControlsWidgetColor(androidx.core.content.ContextCompat.getColor(this, R.color.home_primary));
        options.setDimmedLayerColor(android.graphics.Color.parseColor("#AA000000"));
        options.setLogoColor(android.graphics.Color.BLACK); 
        
        uCrop.withOptions(options);
        
        mCropContent.launch(uCrop.getIntent(this));
    }

}

