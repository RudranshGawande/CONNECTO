package com.megaproject.connecto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Theme from Preferences
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO); // Default Light
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != savedMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(savedMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView splashVideo = findViewById(R.id.splashVideo);

        try {
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_light_mode);
            splashVideo.setVideoURI(video);

            splashVideo.setOnCompletionListener(mp -> navigateNext());
            
            // Handle video errors (e.g., emulator codec issues) by navigating anyway
            splashVideo.setOnErrorListener((mp, what, extra) -> {
                navigateNext();
                return true;
            });

            splashVideo.start();
        } catch (Exception e) {
            navigateNext();
        }

        // Fallback timer in case video hangs or doesn't complete
        new android.os.Handler().postDelayed(this::navigateNext, 3000); // 3 seconds timeout
    }

    private boolean isNavigated = false;

    private synchronized void navigateNext() {
        if (isNavigated) return;
        isNavigated = true;

        // Check if user is logged in
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            // User is signed in, go to Home
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            // No user is signed in, go to Onboarding
            intent = new Intent(SplashActivity.this, WelcomeOnboardingActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

