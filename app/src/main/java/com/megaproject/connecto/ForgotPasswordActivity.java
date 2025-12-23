package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendReset;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBar);

        // Pre-fill email
        String passedEmail = getIntent().getStringExtra("EMAIL");
        if (passedEmail != null) {
             etEmail.setText(passedEmail);
        } else {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                etEmail.setText(user.getEmail());
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSendReset.setOnClickListener(v -> sendResetLink());
    }

    private void sendResetLink() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Show loading state
        setLoading(true);

        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                finish(); // Return to previous screen
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                String error = "Failed to send reset email.";
                if (e.getMessage() != null) {
                    error += " " + e.getMessage();
                }
                Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_LONG).show();
            });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendReset.setText(""); // Hide text
            btnSendReset.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendReset.setText("Send Reset Link");
            btnSendReset.setEnabled(true);
        }
    }
}
