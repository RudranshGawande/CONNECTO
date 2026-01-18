package com.megaproject.connecto;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {

    private android.widget.Spinner spinnerCountryCode;
    private EditText etPhoneNumber;
    private EditText[] etOtps; // Array for 6 boxes
    private Button btnSendCode, btnVerify;
    private TextView tvResend;
    private LinearLayout layoutPhoneInput, layoutOtpInput;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupOtpInputs(); // Set up text watchers for 6 boxes
        setupCallbacks();
        
        // Setup Spinner Adapter with Custom Layout for Visibility
        android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(
                this,
                R.array.country_codes,
                R.layout.item_spinner
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCountryCode.setAdapter(adapter);
        
        // Handle Intent Extra
        if (getIntent().hasExtra("phoneNumber")) {
            String incomingNumber = getIntent().getStringExtra("phoneNumber");
            if (incomingNumber != null && !incomingNumber.isEmpty()) {
                String[] codes = getResources().getStringArray(R.array.country_codes);
                String matchedCode = "";
                int matchedIndex = 0;
                
                for (int i = 0; i < codes.length; i++) {
                     String item = codes[i];
                     String code = item.substring(item.indexOf("+"));
                     if (incomingNumber.startsWith(code)) {
                         matchedCode = code;
                         matchedIndex = i;
                         break;
                     }
                }
                
                if (!matchedCode.isEmpty()) {
                    spinnerCountryCode.setSelection(matchedIndex);
                    etPhoneNumber.setText(incomingNumber.substring(matchedCode.length()));
                } else {
                    etPhoneNumber.setText(incomingNumber);
                }
            }
        }

        btnBack.setOnClickListener(v -> finish());
        
        btnSendCode.setOnClickListener(v -> {
            String rawNumber = etPhoneNumber.getText().toString().trim();
            if (rawNumber.isEmpty()) {
                etPhoneNumber.setError("Phone number required");
                return;
            }
            
            // Get Country Code
            String spinnerText = spinnerCountryCode.getSelectedItem().toString(); 
            String code = spinnerText.substring(spinnerText.indexOf("+"));
            
            String fullNumber = code + rawNumber;
            
            if (isValidPhoneNumber(fullNumber)) {
                sendVerificationCode(fullNumber);
            } else {
                etPhoneNumber.setError("Enter a valid phone number");
            }
        });

        btnVerify.setOnClickListener(v -> {
            String code = getOtpCode();
            if (code.length() < 6) {
                Toast.makeText(this, "Please enter full 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyCode(code);
        });

        tvResend.setOnClickListener(v -> {
             String rawNumber = etPhoneNumber.getText().toString().trim();
             String spinnerText = spinnerCountryCode.getSelectedItem().toString(); 
             String code = spinnerText.substring(spinnerText.indexOf("+"));
             String fullNumber = code + rawNumber;
             
            if (isValidPhoneNumber(fullNumber)) {
                resendVerificationCode(fullNumber, mResendToken);
            }
        });
    }

    private void initViews() {
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        
        // Init 6 OTP boxes
        etOtps = new EditText[6];
        etOtps[0] = findViewById(R.id.etOtp1);
        etOtps[1] = findViewById(R.id.etOtp2);
        etOtps[2] = findViewById(R.id.etOtp3);
        etOtps[3] = findViewById(R.id.etOtp4);
        etOtps[4] = findViewById(R.id.etOtp5);
        etOtps[5] = findViewById(R.id.etOtp6);

        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);
        layoutPhoneInput = findViewById(R.id.layoutPhoneInput);
        layoutOtpInput = findViewById(R.id.layoutOtpInput);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupOtpInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            etOtps[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        etOtps[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        etOtps[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
            
            // Handle backspace logic better if needed (e.g. key listeners), but basic empty check above works for simple cases
            etOtps[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (etOtps[index].getText().toString().isEmpty() && index > 0) {
                         etOtps[index - 1].requestFocus();
                         return true;
                    }
                }
                return false;
            });
        }
    }
    
    private String getOtpCode() {
        StringBuilder code = new StringBuilder();
        for (EditText et : etOtps) {
            code.append(et.getText().toString().trim());
        }
        return code.toString();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && phoneNumber.length() >= 10;
    }

    private void sendVerificationCode(String phoneNumber) {
        setLoading(true);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        if (token == null) {
            sendVerificationCode(phoneNumber);
            return;
        }
        
        setLoading(true);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setupCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                setLoading(false);
                String code = credential.getSmsCode();
                if (code != null) {
                    // Populate Boxes
                    for (int i = 0; i < code.length() && i < 6; i++) {
                        etOtps[i].setText(String.valueOf(code.charAt(i)));
                    }
                    verifyCode(code);
                } else {
                    signInWithPhoneAuthCredential(credential);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification.
                // For instance, if the the phone number format is not valid.
                setLoading(false);
                Toast.makeText(PhoneVerificationActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we now need to ask the user to enter the code
                // and then construct a credential by combining the code with a verification ID.
                setLoading(false);
                mVerificationId = verificationId;
                mResendToken = token;

                // Update UI to show OTP input
                layoutPhoneInput.setVisibility(View.GONE);
                layoutOtpInput.setVisibility(View.VISIBLE);
                Toast.makeText(PhoneVerificationActivity.this, "Code sent!", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void verifyCode(String code) {
        if (mVerificationId == null) {
            Toast.makeText(this, "Error: Verification ID lost. Try resending.", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        setLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Update the existing user's phone number
            user.updatePhoneNumber(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateFirestore(user);
                            } else {
                                setLoading(false);
                                Toast.makeText(PhoneVerificationActivity.this, "Update Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String phoneNumber = user.getPhoneNumber();
        String email = user.getEmail();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", phoneNumber);
        updates.put("isPhoneVerified", true);
        
        // Update user document
        String docId = (email != null) ? email : user.getUid(); // Fallback if no email, but EditProfile uses email.
        
        db.collection("users").document(docId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(PhoneVerificationActivity.this, "Phone number updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(PhoneVerificationActivity.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish(); // Still verification succeeded, just db failed. User can retry later implicitly.
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendCode.setEnabled(false);
            btnVerify.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendCode.setEnabled(true);
            btnVerify.setEnabled(true);
        }
    }
}
