package com.megaproject.connecto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.Calendar;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private EditText etFullName, etUsername, etEmail, etDob, etPassword, etConfirmPassword;
    private ImageView ivShowPassword, ivShowConfirmPassword;
    private AppCompatButton btnSignUp;
    private TextView tvLoginHere;
    private LinearLayout btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Init views
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etDob = findViewById(R.id.et_dob);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivShowPassword = findViewById(R.id.iv_showPassword);
        ivShowConfirmPassword = findViewById(R.id.iv_showConfirmPassword);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvLoginHere = findViewById(R.id.tv_login_here);
        btnGoogle = findViewById(R.id.btn_google);

        // Date of Birth picker
        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> etDob.setText(day + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Password toggle
        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, ivShowPassword, true));
        ivShowConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivShowConfirmPassword, false));

        // Sign-up button click with validations
        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 1. Full Name Validation
            if (fullName.isEmpty() || fullName.length() < 3) {
                Toast.makeText(this, "Full name must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            // 2. Email Validation
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            // 3. Username Validation
            if (username.isEmpty() || username.length() < 4 || !username.matches("[a-zA-Z0-9_]+")) {
                Toast.makeText(this, "Username must be at least 4 characters and contain only letters, numbers, or underscores", Toast.LENGTH_SHORT).show();
                return;
            }
            // 4. Password Strength Validation
            if (!isStrongPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters, include upper & lowercase letters, a number, and a special character", Toast.LENGTH_LONG).show();
                return;
            }
            // 5. Confirm Password Match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Store user data locally
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("fullName", fullName);
            editor.putString("email", email);
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();

            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

            // Go to login screen
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        // Login click
        tvLoginHere.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        // Google sign-up click
        btnGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Sign-Up clicked", Toast.LENGTH_SHORT).show());
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView, boolean isMainPassword) {
        if (isMainPassword) {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imageView.setImageResource(R.drawable.ic_close_eye);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imageView.setImageResource(R.drawable.ic_open_eye);
            }
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imageView.setImageResource(R.drawable.ic_close_eye);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imageView.setImageResource(R.drawable.ic_open_eye);
            }
        }
        editText.setSelection(editText.getText().length());
    }

    private boolean isStrongPassword(String password) {
        Pattern PASSWORD_PATTERN = Pattern.compile(
                "^(?=.*[0-9])" +         // at least 1 digit
                        "(?=.*[a-z])" + // at least 1 lower case
                        "(?=.*[A-Z])" + // at least 1 upper case
                        "(?=.*[@#$%^&+=!])" + // at least 1 special char
                        "(?=\\S+$).{8,}$" // no spaces, min 8 chars
        );
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}


