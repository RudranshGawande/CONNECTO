package com.megaproject.connecto;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageButton btnToggleCurrent, btnToggleNew, btnToggleConfirm, btnBack;
    private TextView tvForgotPassword;
    private AppCompatButton btnUpdatePassword;

    // Password requirements UI
    private ImageView ivCheckLength, ivCheckNumber, ivCheckSpecial;
    private TextView tvCheckLength, tvCheckNumber, tvCheckSpecial;
    private View strengthBar1, strengthBar2, strengthBar3, strengthBar4;
    private TextView tvStrengthLabel;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnToggleCurrent = findViewById(R.id.btnToggleCurrent);
        btnToggleNew = findViewById(R.id.btnToggleNew);
        btnToggleConfirm = findViewById(R.id.btnToggleConfirm);
        btnBack = findViewById(R.id.btnBack);
        
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Validation UI
        ivCheckLength = findViewById(R.id.ivCheckLength);
        ivCheckNumber = findViewById(R.id.ivCheckNumber);
        ivCheckSpecial = findViewById(R.id.ivCheckSpecial);
        
        tvCheckLength = findViewById(R.id.tvCheckLength);
        tvCheckNumber = findViewById(R.id.tvCheckNumber);
        tvCheckSpecial = findViewById(R.id.tvCheckSpecial);

        strengthBar1 = findViewById(R.id.strengthBar1);
        strengthBar2 = findViewById(R.id.strengthBar2);
        strengthBar3 = findViewById(R.id.strengthBar3);
        strengthBar4 = findViewById(R.id.strengthBar4);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class));
        });

        // Visibility Toggles
        btnToggleCurrent.setOnClickListener(v -> {
            isCurrentVisible = !isCurrentVisible;
            togglePasswordVisibility(etCurrentPassword, btnToggleCurrent, isCurrentVisible);
        });

        btnToggleNew.setOnClickListener(v -> {
            isNewVisible = !isNewVisible;
            togglePasswordVisibility(etNewPassword, btnToggleNew, isNewVisible);
        });

        btnToggleConfirm.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirm, isConfirmVisible);
        });

        // Real-time validation for new password
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_close_eye); // Assuming user meant open_eye for visible, but matching XML
            // Actually, based on logic: close_eye usually means "hidden".
            // If visible, show "open_eye" maybe?
            // Checking common icons: XML used ic_close_eye by default.
            // Let's assume ic_open_eye exists or swap.
            // I'll stick to a standard swap.
            // If visible -> show "hide" icon.
             toggleButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.home_primary)));
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_close_eye); 
            toggleButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_500)));
        }
        editText.setSelection(editText.getText().length());
    }

    private void validatePasswordStrength(String password) {
        boolean lengthMet = password.length() >= 8;
        boolean numberMet = password.matches(".*\\d.*");
        boolean specialMet = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        updateValidationUI(ivCheckLength, tvCheckLength, lengthMet);
        updateValidationUI(ivCheckNumber, tvCheckNumber, numberMet);
        updateValidationUI(ivCheckSpecial, tvCheckSpecial, specialMet);

        // Strength Bars
        int score = 0;
        if (lengthMet) score++;
        if (numberMet) score++;
        if (specialMet) score++;
        if (password.length() >= 12) score++; // Bonus for length

        updateStrengthBars(score);
    }

    private void updateValidationUI(ImageView icon, TextView text, boolean isMet) {
        if (isMet) {
            icon.setImageResource(R.drawable.ic_check_circle_green); 
            icon.setImageTintList(null); // Use original colors of the check icon
            text.setTextColor(ContextCompat.getColor(this, R.color.home_text_main));
        } else {
            icon.setImageResource(R.drawable.ic_circle_outline_gray);
            icon.setImageTintList(null);
            text.setTextColor(ContextCompat.getColor(this, R.color.home_text_secondary));
        }
    }

    private void updateStrengthBars(int score) {
        int activeColor = ContextCompat.getColor(this, R.color.gray_200);
        String label = "Weak";

        if (score == 1) {
            activeColor = Color.RED;
            label = "Weak";
        } else if (score == 2) {
            activeColor = Color.YELLOW; // Or orange
            label = "Fair";
        } else if (score == 3) {
            activeColor = ContextCompat.getColor(this, R.color.home_primary); // Blue/Green
            label = "Good";
        } else if (score >= 4) {
            activeColor = Color.GREEN;
            label = "Strong";
        }

        // Reset
        strengthBar1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_200)));
        strengthBar2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_200)));
        strengthBar3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_200)));
        strengthBar4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_200)));

        if (score >= 1) strengthBar1.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        if (score >= 2) strengthBar2.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        if (score >= 3) strengthBar3.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        if (score >= 4) strengthBar4.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        
        tvStrengthLabel.setText("Password Strength: " + label);
    }

    private void updatePassword() {
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPass)) {
            etCurrentPassword.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(newPass)) {
            etNewPassword.setError("Required");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }
        
        // Basic re-check of requirements
        if (newPass.length() < 8) {
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            btnUpdatePassword.setEnabled(false);
            btnUpdatePassword.setText("Updating...");

            // Re-authenticate
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update Password
                            user.updatePassword(newPass)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            btnUpdatePassword.setEnabled(true);
                                            btnUpdatePassword.setText("Update Password");
                                            Toast.makeText(ChangePasswordActivity.this, "Failed to update: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            btnUpdatePassword.setEnabled(true);
                            btnUpdatePassword.setText("Update Password");
                            Toast.makeText(ChangePasswordActivity.this, "Authentication failed. check current password.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
