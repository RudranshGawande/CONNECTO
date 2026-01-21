package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private AppCompatButton btnSendReset;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();

        // Check for email passed from LoginActivity
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return;
            }
            sendPasswordResetEmail(email);
        });
    }

    private void sendPasswordResetEmail(String email) {
        setLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                        finish(); // Close activity so user can go back to login
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendReset.setText(""); // Hide text to show spinner
            btnSendReset.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendReset.setText("Send Reset Link");
            btnSendReset.setEnabled(true);
        }
    }
}
