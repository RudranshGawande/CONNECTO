package com.megaproject.connecto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.regex.Pattern;



public class SignUpActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private static final int RC_SIGN_IN = 9001;

    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView ivShowPassword, ivShowConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLoginHere;
    private LinearLayout btnGoogle;
    private ImageButton btnBack;

    private com.google.firebase.auth.FirebaseAuth mAuth;
    private com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Init Firebase
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        // Configure Google Sign In
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        // Init views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivShowPassword = findViewById(R.id.iv_showPassword);
        ivShowConfirmPassword = findViewById(R.id.iv_showConfirmPassword);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvLoginHere = findViewById(R.id.tv_login_here);
        btnGoogle = findViewById(R.id.btn_google);
        btnBack = findViewById(R.id.btn_back);

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Password toggle
        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, ivShowPassword, true));
        ivShowConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivShowConfirmPassword, false));

        // Sign-up button click with validations
        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isStrongPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters, include upper & lowercase letters, a number, and a special character", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            createAccount(email, password);
        });

        // Login click
        tvLoginHere.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        // Google sign-up click
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void createAccount(String email, String password) {
        btnSignUp.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnSignUp.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                        // Navigate to Home or Login
                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
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
            com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                com.google.android.gms.auth.api.signin.GoogleSignInAccount account = task.getResult(com.google.android.gms.common.api.ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (com.google.android.gms.common.api.ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
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


