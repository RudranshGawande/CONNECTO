package com.megaproject.connecto;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private View bar1, bar2, bar3, bar4;
    private TextView tvStrengthLabel;
    private ImageView ivCheckLength, ivCheckNumber, ivCheckSpecial;
    private TextView tvCheckLength, tvCheckNumber, tvCheckSpecial;
    
    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    // Regex
    private static final Pattern PATTERN_NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern PATTERN_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*");

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
        
        bar1 = findViewById(R.id.strengthBar1);
        bar2 = findViewById(R.id.strengthBar2);
        bar3 = findViewById(R.id.strengthBar3);
        bar4 = findViewById(R.id.strengthBar4);
        
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
        
        ivCheckLength = findViewById(R.id.ivCheckLength);
        ivCheckNumber = findViewById(R.id.ivCheckNumber);
        ivCheckSpecial = findViewById(R.id.ivCheckSpecial);
        
        tvCheckLength = findViewById(R.id.tvCheckLength);
        tvCheckNumber = findViewById(R.id.tvCheckNumber);
        tvCheckSpecial = findViewById(R.id.tvCheckSpecial);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Setup initial password type
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setupListeners() {
        // Toggles
        setupToggle(findViewById(R.id.btnToggleCurrent), etCurrentPassword, 1);
        setupToggle(findViewById(R.id.btnToggleNew), etNewPassword, 2);
        setupToggle(findViewById(R.id.btnToggleConfirm), etConfirmPassword, 3);
        
        // Text Watcher for Strength
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateStrengthAndRequirements(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Update Button
        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> attemptUpdatePassword());
        
        // Forgot Password Link
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                intent.putExtra("EMAIL", user.getEmail());
            }
            startActivity(intent);
        });
    }

    private void setupToggle(ImageButton btn, EditText et, int type) {
        btn.setOnClickListener(v -> {
            boolean isVisible;
            if (type == 1) {
                isCurrentVisible = !isCurrentVisible;
                isVisible = isCurrentVisible;
            } else if (type == 2) {
                isNewVisible = !isNewVisible;
                isVisible = isNewVisible;
            } else {
                isConfirmVisible = !isConfirmVisible;
                isVisible = isConfirmVisible;
            }
            
            if (isVisible) {
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btn.setImageResource(R.drawable.ic_open_eye);
            } else {
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btn.setImageResource(R.drawable.ic_close_eye);
            }
            et.setSelection(et.getText().length());
        });
    }

    private void updateStrengthAndRequirements(String password) {
        boolean hasLength = password.length() >= 8;
        boolean hasNumber = PATTERN_NUMBER.matcher(password).matches();
        boolean hasSpecial = PATTERN_SPECIAL.matcher(password).matches();

        // Update Icons
        updateRequirementItem(ivCheckLength, tvCheckLength, hasLength);
        updateRequirementItem(ivCheckNumber, tvCheckNumber, hasNumber);
        updateRequirementItem(ivCheckSpecial, tvCheckSpecial, hasSpecial);

        // Calculate Strength (0-4)
        int score = 0;
        if (password.length() > 0) score++;
        if (hasLength) score++;
        if (hasNumber) score++;
        if (hasSpecial) score++;
        
        updateStrengthBars(score);
    }

    private void updateRequirementItem(ImageView iv, TextView tv, boolean isValid) {
        if (isValid) {
            iv.setImageResource(R.drawable.ic_check_circle_green);
            // Green text
            // tv.setTextColor(ContextCompat.getColor(this, R.color.green_600)); 
            // The HTML keeps text gray mostly, but let's keep it subtle or green if desired. 
            // HTML: text-slate-600 (gray) even when checked, icon is green.
            iv.setColorFilter(null); // Clear any tint if used
        } else {
            iv.setImageResource(R.drawable.ic_circle_outline_gray);
            // tv.setTextColor(ContextCompat.getColor(this, R.color.gray_500));
        }
    }

    private void updateStrengthBars(int score) {
        int colorRes = R.color.gray_200;
        String label = "Weak";
        int activeColor = R.color.red_500;

        if (score <= 1) {
            activeColor = R.color.red_500; // Weak
            label = "Weak";
        } else if (score == 2) {
            activeColor = R.color.orange_500; // Fair
            label = "Fair";
        } else if (score == 3) {
            activeColor = R.color.blue_500; // Good
            label = "Good";
        } else if (score == 4) {
            activeColor = R.color.green_500; // Strong
            label = "Strong";
        }
        
        int color = ContextCompat.getColor(this, activeColor);
        int inactive = ContextCompat.getColor(this, R.color.gray_200);

        tvStrengthLabel.setText(label + " password strength");
        tvStrengthLabel.setTextColor(color);

        // Set bars
        bar1.setBackgroundTintList(ColorStateList.valueOf(score >= 1 ? color : inactive));
        bar2.setBackgroundTintList(ColorStateList.valueOf(score >= 2 ? color : inactive));
        bar3.setBackgroundTintList(ColorStateList.valueOf(score >= 3 ? color : inactive));
        bar4.setBackgroundTintList(ColorStateList.valueOf(score >= 4 ? color : inactive));
    }

    private void attemptUpdatePassword() {
        String current = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (current.isEmpty()) {
            etCurrentPassword.setError("Required");
            etCurrentPassword.requestFocus();
            return;
        }

        if (newPass.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 8 || !PATTERN_NUMBER.matcher(newPass).matches() || !PATTERN_SPECIAL.matcher(newPass).matches()) {
            Toast.makeText(this, "Password does not meet requirements", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);
            
            Toast.makeText(this, "Updating password...", Toast.LENGTH_SHORT).show();
            
            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                // Update Password
                user.updatePassword(newPass).addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(this, "Password updated. Please log in again.", Toast.LENGTH_SHORT).show();
                    
                    // Sign out to force re-login with new credentials
                    mAuth.signOut();
                    
                    // Google Sign-out as well to be clean
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso).signOut();

                    // Redirect to Login
                    android.content.Intent intent = new android.content.Intent(ChangePasswordActivity.this, LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
