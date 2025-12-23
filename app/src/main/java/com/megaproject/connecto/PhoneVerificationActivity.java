package com.megaproject.connecto;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

    private static final String TAG = "PhoneVerification";

    private LinearLayout layoutOtpInput;
    private Button btnMainAction;
    private ImageView ivCheck;
    private EditText etPhoneNumber;
    private TextView tvCountryCode; // To get the selected code
    private boolean isOtpSent = false;

    // Firebase
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();

        // UI References
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        layoutOtpInput = findViewById(R.id.layoutOtpInput);
        btnMainAction = findViewById(R.id.btnMainAction);
        ivCheck = findViewById(R.id.ivCheck);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tvCountryCode = findViewById(R.id.tvCountryCode);

        btnMainAction.setOnClickListener(v -> {
            if (!isOtpSent) {
                initiateVerification();
            } else {
                verifyOtpInput();
            }
        });

        // Country Selection Logic
        LinearLayout layoutCountrySelect = findViewById(R.id.layoutCountrySelect);
        TextView tvFlag = findViewById(R.id.tvFlag);

        layoutCountrySelect.setOnClickListener(v -> {
            CountrySelectionBottomSheet bottomSheet = new CountrySelectionBottomSheet();
            bottomSheet.setOnCountrySelectedListener(country -> {
                tvFlag.setText(country.getFlag());
                tvCountryCode.setText(country.getDialCode());
            });
            bottomSheet.show(getSupportFragmentManager(), "CountrySelectionBottomSheet");
        });

        // Initialize Firebase Callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter an OTP.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                
                String code = credential.getSmsCode();
                if (code != null) {
                    fillOtpFields(code);
                }
                
                linkWithPhoneCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                btnMainAction.setEnabled(true);
                btnMainAction.setText("Send OTP");
                
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneVerificationActivity.this, "Invalid phone number.", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(PhoneVerificationActivity.this, "Phone number already linked to another account.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PhoneVerificationActivity.this, "Verification failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // Update UI
                isOtpSent = true;
                layoutOtpInput.setVisibility(View.VISIBLE);
                ivCheck.setVisibility(View.VISIBLE);
                btnMainAction.setText("Verify");
                btnMainAction.setEnabled(true);
                etPhoneNumber.setEnabled(false); // Lock phone number
                
                setupOtpInputs(); // Initialize OTP logic
                startResendTimer(); // Start countdown
                Toast.makeText(PhoneVerificationActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void initiateVerification() {
        String phoneRaw = etPhoneNumber.getText().toString().trim();
        if (phoneRaw.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String dialCode = tvCountryCode.getText().toString().trim();
        String fullNumber = dialCode + phoneRaw;

        btnMainAction.setEnabled(false);
        btnMainAction.setText("Sending...");

        startPhoneNumberVerification(fullNumber);
    }
    
    private void startPhoneNumberVerification(String phoneNumber) {
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
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtpInput() {
        String otp = getOtp();
        if (otp.length() < 6) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        btnMainAction.setEnabled(false);
        btnMainAction.setText("Verifying...");

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        linkWithPhoneCredential(credential);
    }

    private void linkWithPhoneCredential(PhoneAuthCredential credential) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Link success
                            FirebaseUser user = task.getResult().getUser();
                            updateFirestoreProfile(user);
                        } else {
                            // Link failed
                            btnMainAction.setEnabled(true);
                            btnMainAction.setText("Verify");
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(PhoneVerificationActivity.this, "This phone number is already linked to another account.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PhoneVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        }
    }
    
    private void updateFirestoreProfile(FirebaseUser user) {
        // Update the phone number in Firestore if using a separate user collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user.getPhoneNumber() != null) {
             Map<String, Object> data = new HashMap<>();
             data.put("phoneNumber", user.getPhoneNumber());
             
             db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    // Navigate back regardless of firestore success (Auth consistency is key)
                    Toast.makeText(PhoneVerificationActivity.this, "Phone Number Verified!", Toast.LENGTH_SHORT).show();
                    btnMainAction.setText("Verified");
                    new Handler(Looper.getMainLooper()).postDelayed(PhoneVerificationActivity.this::finish, 1000);
                });
        } else {
             Toast.makeText(PhoneVerificationActivity.this, "Phone Number Verified!", Toast.LENGTH_SHORT).show();
             new Handler(Looper.getMainLooper()).postDelayed(PhoneVerificationActivity.this::finish, 1000);
        }
    }

    private void setupOtpInputs() {
        EditText[] otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };

        // Clear initial text (removing "4" placeholder)
        for (EditText et : otpFields) {
            et.setText("");
        }
        otpFields[0].requestFocus();

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpFields[index].getText().toString().isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].setText(""); // Clear previous on backspace
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private String getOtp() {
        StringBuilder otp = new StringBuilder();
        otp.append(((EditText) findViewById(R.id.etOtp1)).getText().toString());
        otp.append(((EditText) findViewById(R.id.etOtp2)).getText().toString());
        otp.append(((EditText) findViewById(R.id.etOtp3)).getText().toString());
        otp.append(((EditText) findViewById(R.id.etOtp4)).getText().toString());
        otp.append(((EditText) findViewById(R.id.etOtp5)).getText().toString());
        otp.append(((EditText) findViewById(R.id.etOtp6)).getText().toString());
        return otp.toString();
    }
    
    private void fillOtpFields(String code) {
        if (code == null || code.length() < 6) return;
        EditText[] otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };
        for (int i = 0; i < 6; i++) {
            otpFields[i].setText(String.valueOf(code.charAt(i)));
        }
    }
    
    private void startResendTimer() {
        TextView tvTimer = findViewById(R.id.tvTimer);
        TextView tvResend = findViewById(R.id.tvResendOtp);
        tvResend.setTextColor(getResources().getColor(R.color.gray_500));
        tvResend.setClickable(false);
        
        new CountDownTimer(60000, 1000) { // 60 seconds standard for Firebase

            public void onTick(long millisUntilFinished) {
                tvTimer.setText("00:" + String.format("%02d", millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                tvResend.setTextColor(getResources().getColor(R.color.blue_600)); // Active blue
                tvResend.setClickable(true);
                tvResend.setOnClickListener(v -> {
                    if (mResendToken != null) {
                       Toast.makeText(PhoneVerificationActivity.this, "Resending OTP...", Toast.LENGTH_SHORT).show();
                       String dialCode = tvCountryCode.getText().toString().trim();
                       String phoneRaw = etPhoneNumber.getText().toString().trim();
                       resendVerificationCode(dialCode + phoneRaw, mResendToken);
                       startResendTimer();
                    }
                });
            }
        }.start();
    }
}
