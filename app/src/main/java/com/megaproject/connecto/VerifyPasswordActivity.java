package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyPasswordActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore; // Defined as 'firestore' to resolve potential user error

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_password);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText etPassword = findViewById(R.id.etPassword);
        ImageButton btnToggleVisibility = findViewById(R.id.btnToggleVisibility);

        // Initial state
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        btnToggleVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleVisibility.setImageResource(R.drawable.ic_close_eye);
            } else {
                // Show password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleVisibility.setImageResource(R.drawable.ic_open_eye);
            }
            isPasswordVisible = !isPasswordVisible;
            // Move cursor to end
            etPassword.setSelection(etPassword.getText().length());
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                
                // Show loading state (optional, just toast for now)
                Toast.makeText(this, "Verifying...", Toast.LENGTH_SHORT).show();

                user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Navigate to ChangePasswordActivity
                        android.content.Intent intent = new android.content.Intent(VerifyPasswordActivity.this, ChangePasswordActivity.class);
                        // Optionally pass the current password to avoid re-entering, but user requirement and UI asks for it again.
                        startActivity(intent);
                        finish(); 
                    })
                    .addOnFailureListener(e -> {
                        // Show Error Dialog
                        IncorrectPasswordDialog dialog = new IncorrectPasswordDialog();
                        dialog.setActionListener(new IncorrectPasswordDialog.ActionListener() {
                            @Override
                            public void onTryAgain() {
                                // Clear password field or select it
                                etPassword.setText("");
                                etPassword.requestFocus();
                            }

                            @Override
                            public void onForgotPassword() {
                                 // Trigger the reset flow
                                 Intent intent = new Intent(VerifyPasswordActivity.this, ForgotPasswordActivity.class);
                                 startActivity(intent);
                            }
                        });
                        dialog.show(getSupportFragmentManager(), "IncorrectPasswordDialog");
                    });
            } else {
                Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(VerifyPasswordActivity.this, ForgotPasswordActivity.class);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                intent.putExtra("EMAIL", user.getEmail());
            }
            startActivity(intent);
        });
    }
}
