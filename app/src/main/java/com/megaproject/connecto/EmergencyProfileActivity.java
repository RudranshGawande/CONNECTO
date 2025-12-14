package com.megaproject.connecto;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class EmergencyProfileActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView saveButton, editButton, cancelButton;
    private MaterialButton signOutButton;
    
    private EditText inputName, inputPhone, inputWeight, inputAddress, inputConditions, inputAllergies;
    private AutoCompleteTextView inputBloodType;
    
    private MaterialCardView genderCard, birthdateCard;
    private TextView genderValue, birthdateValue;
    
    private MaterialButton addMedicationBtn, addContactBtn;
    private ImageView profileImage;
    private FrameLayout editImageButton;
    
    private static final int PICK_IMAGE_GALLERY = 100;
    private static final int PICK_IMAGE_CAMERA = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;
    
    private Uri cameraUri;
    
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EmergencyProfile";

    // Helper for Unified Dropdown
    private void showUnifiedDropdown(View anchor, java.util.List<String> items, TextView targetView) {
        // Reorder list: Selection at top
        java.util.List<String> orderedItems = new java.util.ArrayList<>(items);
        String currentSelection = targetView.getText().toString();
        
        if (!currentSelection.isEmpty() && orderedItems.contains(currentSelection)) {
            orderedItems.remove(currentSelection);
            orderedItems.add(0, currentSelection);
        }

        com.megaproject.connecto.ui.UnifiedDropdownAdapter adapter = new com.megaproject.connecto.ui.UnifiedDropdownAdapter(this, orderedItems);
        adapter.setSelectedItem(currentSelection);

        android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(this);
        popup.setAnchorView(anchor);
        popup.setAdapter(adapter);
        popup.setWidth(anchor.getWidth() > 0 ? anchor.getWidth() : 600); 
        popup.setHeight(android.widget.ListPopupWindow.WRAP_CONTENT);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_white_rounded));
        popup.setVerticalOffset(16); // Small offset for visual separation

        popup.setOnItemClickListener((parent, view, position, id) -> {
            String selected = orderedItems.get(position);
            targetView.setText(selected);
            targetView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            popup.dismiss();
            
            // Special handling for Gender 'Other' input
            if (targetView == genderValue && selected.equals("Other")) {
                showOtherGenderDialog();
            }
        });

        popup.show();
    }
    
    private void showOtherGenderDialog() {
         EditText input = new EditText(this);
         new AlertDialog.Builder(this)
             .setTitle("Enter Gender")
             .setView(input)
             .setPositiveButton("OK", (d, w) -> {
                 String val = input.getText().toString();
                 if(!val.isEmpty()) genderValue.setText(val);
             })
             .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_profile);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        loadData();
        loadProfileImage();
        setEditMode(false); // Initial state: View Mode
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        signOutButton = findViewById(R.id.signOutButton);

        inputName = findViewById(R.id.inputName);
        inputPhone = findViewById(R.id.inputPhone);
        inputWeight = findViewById(R.id.inputWeight);
        inputAddress = findViewById(R.id.inputAddress);
        inputBloodType = findViewById(R.id.inputBloodType);
        inputConditions = findViewById(R.id.inputConditions);
        inputAllergies = findViewById(R.id.inputAllergies);

        genderCard = findViewById(R.id.genderCard);
        birthdateCard = findViewById(R.id.birthdateCard);
        genderValue = findViewById(R.id.genderValue);
        birthdateValue = findViewById(R.id.birthdateValue);

        profileImage = findViewById(R.id.profileImage);
        editImageButton = findViewById(R.id.editImageButton);
        
        addMedicationBtn = findViewById(R.id.addMedicationBtn);
        addContactBtn = findViewById(R.id.addContactBtn);



        // Listeners
        View.OnClickListener imgListener = v -> showImagePickerDialog();
        editImageButton.setOnClickListener(imgListener);
        profileImage.setOnClickListener(imgListener);
        
        genderCard.setOnClickListener(v -> {
            java.util.List<String> genders = java.util.Arrays.asList("Male", "Female", "Prefer not to say", "Other");
            showUnifiedDropdown(genderCard, genders, genderValue);
        });
        
        birthdateCard.setOnClickListener(v -> showDatePicker());
        
        // For Blood Type, we need to handle the click specially since it's an AutoCompleteTextView
        // Disabling standard dropdown and showing our custom one
        inputBloodType.setOnClickListener(v -> {
            java.util.List<String> bloodTypes = java.util.Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
            // Find a way to pass this view as TextView
            showUnifiedDropdown(inputBloodType, bloodTypes, inputBloodType);
        });
        // Disable keyboard for blood type
        inputBloodType.setCursorVisible(false);
        inputBloodType.setFocusable(false);
        inputBloodType.setClickable(true);
        
        backButton.setOnClickListener(v -> onBackPressed());
        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> {
            loadData(); // Revert changes
            setEditMode(false);
        });
        saveButton.setOnClickListener(v -> saveData());
        
        signOutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
            // Implement actual sign out logic here
        });

        if (addMedicationBtn != null) addMedicationBtn.setOnClickListener(v -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
        if (addContactBtn != null) addContactBtn.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));
    }
    
    private void setEditMode(boolean enable) {
        // Toggle Buttons
        editButton.setVisibility(enable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        cancelButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        
        editImageButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        
        // Toggle Inputs
        setEditTextEnabled(inputName, enable);
        setEditTextEnabled(inputPhone, enable);
        setEditTextEnabled(inputWeight, enable);
        setEditTextEnabled(inputAddress, enable);
        setEditTextEnabled(inputConditions, enable);
        setEditTextEnabled(inputAllergies, enable);
        
        // Autocomplete
        inputBloodType.setEnabled(enable);
        inputBloodType.setFocusable(enable);
        inputBloodType.setFocusableInTouchMode(enable);
        if(!enable) inputBloodType.dismissDropDown();
        
        // Cards
        genderCard.setClickable(enable);
        birthdateCard.setClickable(enable);
        
        // Profile Image Click
        profileImage.setClickable(enable);
        
        // Action Buttons
        if (addMedicationBtn != null) addMedicationBtn.setEnabled(enable);
        if (addContactBtn != null) addContactBtn.setEnabled(enable); // Maybe keep contact list accessible? Assuming locked for now.
    }
    
    private void setEditTextEnabled(EditText editText, boolean enabled) {
        editText.setFocusable(enabled);
        editText.setFocusableInTouchMode(enabled);
        editText.setClickable(enabled);
        editText.setCursorVisible(enabled);
    }

    private void loadData() {
        String name = sharedPreferences.getString("name", "");
        if (name.isEmpty()) {
            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            name = userPrefs.getString("fullName", "");
        }
        
        inputName.setText(name);
        inputPhone.setText(sharedPreferences.getString("phone", ""));
        genderValue.setText(sharedPreferences.getString("gender", getString(R.string.select_gender)));
        birthdateValue.setText(sharedPreferences.getString("dob", getString(R.string.select_birthdate)));
        
        inputBloodType.setText(sharedPreferences.getString("bloodType", ""));
        inputWeight.setText(sharedPreferences.getString("weight", ""));
        inputAddress.setText(sharedPreferences.getString("address", ""));
        inputConditions.setText(sharedPreferences.getString("conditions", ""));
        inputAllergies.setText(sharedPreferences.getString("allergies", ""));
    }

    private void saveData() {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            inputName.setError("Name is required");
            inputName.requestFocus();
            return;
        }

        String blood = inputBloodType.getText().toString().trim().toUpperCase();
        if (!blood.isEmpty() && !isValidBloodType(blood)) {
            inputBloodType.setError("Invalid Blood Type");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("phone", inputPhone.getText().toString());
        editor.putString("gender", genderValue.getText().toString());
        editor.putString("dob", birthdateValue.getText().toString());
        editor.putString("bloodType", blood);
        editor.putString("weight", inputWeight.getText().toString());
        editor.putString("address", inputAddress.getText().toString());
        editor.putString("conditions", inputConditions.getText().toString());
        editor.putString("allergies", inputAllergies.getText().toString());
        editor.apply();

        Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
        setEditMode(false);
    }
    
    private boolean isValidBloodType(String type) {
        String[] validIn = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for(String s : validIn) {
            if(s.equals(type)) return true;
        }
        return false;
    }

    // --- Profile Image Logic ---

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        new AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options, (dialog, which) -> {
                if (which == 0) checkCameraPermission();
                else if (which == 1) openGallery();
                else removePhoto();
            })
            .show();
    }
    
    private void removePhoto() {
        File file = new File(getFilesDir(), "profile_photo.jpg");
        if(file.exists()) file.delete();
        profileImage.setImageResource(R.drawable.ic_person_outline);
        profileImage.setPadding(16,16,16,16); // Reset padding
        profileImage.setImageTintList(ContextCompat.getColorStateList(this, R.color.icon_gray)); // Reset tint
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            launchCamera();
        }
    }
    
    private void launchCamera() {
        try {
            File photoFile = new File(getExternalCacheDir(), "camera_image.jpg");
            cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            startActivityForResult(intent, PICK_IMAGE_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error launching camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_GALLERY);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_GALLERY && data != null) {
                startCrop(data.getData());
            } else if (requestCode == PICK_IMAGE_CAMERA) {
                if (cameraUri != null) startCrop(cameraUri);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) saveProfileImage(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) cropError.printStackTrace();
            Toast.makeText(this, "Cropping failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startCrop(Uri uri) {
        String destFileName = "profile_final.jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setCompressionQuality(90);
        options.withMaxResultSize(1000, 1000); // Max size

        // Fix for red tint/overlay: explicitly set neutral colors
        options.setToolbarColor(ContextCompat.getColor(this, android.R.color.black));
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, android.R.color.white));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.primary));
        options.setDimmedLayerColor(ContextCompat.getColor(this, R.color.black));

        UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destFileName)))
            .withAspectRatio(1, 1)
            .withOptions(options)
            .start(this);
    }
    
    private void saveProfileImage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File file = new File(getFilesDir(), "profile_photo.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            loadProfileImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImage() {
        File file = new File(getFilesDir(), "profile_photo.jpg");
        if (file.exists()) {
            profileImage.setPadding(0, 0, 0, 0);
            profileImage.setImageTintList(null);
            profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            profileImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }

    // --- Bottom Sheet & Date Picker ---

    private void showGenderBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_gender_sheet, null);
        bottomSheetDialog.setContentView(view);
        
        RadioGroup rg = view.findViewById(R.id.genderRadioGroup);
        EditText inputOther = view.findViewById(R.id.inputOtherGender);
        Button btnApply = view.findViewById(R.id.btnApplyGender);
        
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOther) inputOther.setVisibility(View.VISIBLE);
            else inputOther.setVisibility(View.GONE);
        });
        
        btnApply.setOnClickListener(v -> {
            int selectedId = rg.getCheckedRadioButtonId();
            if (selectedId == -1) {
                bottomSheetDialog.dismiss();
                return;
            }
            
            String gender = "";
            if (selectedId == R.id.radioMale) gender = getString(R.string.gender_male);
            else if (selectedId == R.id.radioFemale) gender = getString(R.string.gender_female);
            else if (selectedId == R.id.radioPreferNotSay) gender = getString(R.string.gender_prefer_not_say);
            else if (selectedId == R.id.radioOther) {
                gender = inputOther.getText().toString().trim();
                if (gender.isEmpty()) gender = getString(R.string.gender_other);
            }
            
            genderValue.setText(gender);
            genderValue.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            bottomSheetDialog.dismiss();
        });
        
        bottomSheetDialog.show();
    }
    
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        long maxDate = System.currentTimeMillis();
        
        // Parse current if valid
        // ... omitted simpler parse logic for brevity, just defaulting to now
        
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dob = dayOfMonth + "/" + (month + 1) + "/" + year;
            birthdateValue.setText(dob);
            birthdateValue.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        
        dialog.getDatePicker().setMaxDate(maxDate);
        dialog.show();
    }
}



