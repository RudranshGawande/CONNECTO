package com.megaproject.connecto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.megaproject.connecto.R;

public class ReportSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_success);

        // Setup Buttons
        findViewById(R.id.btn_view_reports).setOnClickListener(v -> {
            // Navigate to My Reports (Activity or Fragment)
            // For now, back to Dashboard is safe, or specific My Reports activity if exists
            Toast.makeText(this, "Navigating to Reports...", Toast.LENGTH_SHORT).show();
            navigateToDashboard();
        });

        findViewById(R.id.btn_dashboard).setOnClickListener(v -> {
            navigateToDashboard();
        });

        findViewById(R.id.btn_close).setOnClickListener(v -> {
            navigateToDashboard();
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
