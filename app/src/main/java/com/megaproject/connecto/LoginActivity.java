package com.megaproject.connecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivShowPassword;
    private TextView tvForgotPassword, tvSignUp;
    private MaterialButton btnLogin;
    private LinearLayout btnGoogle;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupListeners();
    }
    
    // Check if user is already signed in (non-null) and update UI accordingly.
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            navigateToHome();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_usernameOrEmail);
        etPassword = findViewById(R.id.et_password);
        ivShowPassword = findViewById(R.id.iv_showPassword);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google);
    }

    private void setupListeners() {
        // Toggle Password Visibility
        ivShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide Password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_open_eye);
            } else {
                // Show Password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_close_eye); 
                // Assuming ic_close_eye exists, if not fallback or specific logic needed.
                // Based on previous file list, ic_close_eye.png exists.
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        // Login Button
        btnLogin.setOnClickListener(v -> loginUser());

        // Google Sign In
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                intent.putExtra("email", email);
            }
            startActivity(intent);
        });

        // Sign Up
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        // Show progress or disable button here if needed
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Log In");
                    if (task.isSuccessful()) {
                        // Sign in success
                        navigateToHome();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            navigateToHome();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToHome() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String safeEmail = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
            FirebaseFirestore.getInstance().collection("users").document(safeEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean onboardingComplete = documentSnapshot.contains("onboardingComplete") && Boolean.TRUE.equals(documentSnapshot.getBoolean("onboardingComplete"));
                    
                    Intent intent;
                    if (onboardingComplete) {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, CompleteProfileOnboardingActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                     // Fallback to Home if check fails? Or Login? Let's go Home.
                     Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);
                     finish();
                });
        }
    }
}
