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

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_light_mode);
        splashVideo.setVideoURI(video);

        splashVideo.setOnCompletionListener(mp -> {
            // Directly go to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });

        splashVideo.start();
    }
}

