package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AppCompatButton btnLogin;
    private TextView tvForgotPassword, tvSignUp;
    private LinearLayout btnGoogle;
    private ImageView ivShowPassword;

    // Track visibility state
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.et_usernameOrEmail);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);
        btnGoogle = findViewById(R.id.btn_google);
        ivShowPassword = findViewById(R.id.iv_showPassword);

        // Toggle password visibility on eye icon click
        ivShowPassword.setOnClickListener(v -> {
            togglePasswordVisibility(etPassword, ivShowPassword, true);
        });

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // For now, just show a success toast (no Firebase)
            Toast.makeText(this, "Login successful (local validation only)", Toast.LENGTH_SHORT).show();

            // Navigate to HomeActivity
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        });

        // Forgot password click
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        );

        // Sign up click
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class))
        );

        // Google Sign-In click (placeholder)
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Google Sign-In clicked!", Toast.LENGTH_SHORT).show()
        );
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
        }
        editText.setSelection(editText.getText().length());
    }
}


