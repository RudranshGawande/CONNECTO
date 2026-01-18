package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView ivShowPassword, ivShowConfirmPassword;
    private MaterialButton btnSignUp;
    private LinearLayout btnGoogle;
    private TextView tvLoginHere;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivShowPassword = findViewById(R.id.iv_showPassword);
        ivShowConfirmPassword = findViewById(R.id.iv_showConfirmPassword);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnGoogle = findViewById(R.id.btn_google);
        tvLoginHere = findViewById(R.id.tv_login_here);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Toggle Password Visibility
        ivShowPassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_close_eye);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_open_eye);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Toggle Confirm Password Visibility
        ivShowConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivShowConfirmPassword.setImageResource(R.drawable.ic_close_eye);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivShowConfirmPassword.setImageResource(R.drawable.ic_open_eye);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnSignUp.setOnClickListener(v -> validateAndSignUp());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvLoginHere.setOnClickListener(v -> {
             // Just finish to go back to Login which called this, or ensure explicit intent
             Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             finish();
        });
    }

    private void validateAndSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password should be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        signUpWithFirebase(email, password);
    }

    private void signUpWithFirebase(String email, String password) {
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user);
                    } else {
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Sign Up");
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user);
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("email", email);
            userData.put("createdAt", System.currentTimeMillis());
            
            // Use Email as document ID as per requirement in EditProfileActivity (consistent logic)
            String safeEmail = email != null ? email.trim().toLowerCase(java.util.Locale.ROOT) : uid;

            db.collection("users").document(safeEmail)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SignUpActivity.this, "Account Created Successfully.", Toast.LENGTH_SHORT).show();
                        // Navigate to Complete Profile Onboarding
                        Intent intent = new Intent(SignUpActivity.this, CompleteProfileOnboardingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Even if Firestore fails, Auth succeeded, so we might still let them in or retry.
                        // For now, let's treat it as a semi-success but warn.
                        Toast.makeText(SignUpActivity.this, "Account created but profile setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, CompleteProfileOnboardingActivity.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
        }
    }
}
