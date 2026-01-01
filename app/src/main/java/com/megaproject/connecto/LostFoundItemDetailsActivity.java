package com.megaproject.connecto;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LostFoundItemDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, ivItemImage, ivCategoryIcon, ivProfileImage;
    private TextView tvStatusBadge, tvTitle, tvCategory, tvDate;
    private TextView tvLocationName, tvLocationDetails;
    private TextView tvDescription;
    private TextView tvReporterName;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_found_item_details);

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        
        ivItemImage = findViewById(R.id.ivItemImage);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        
        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationDetails = findViewById(R.id.tvLocationDetails);
        
        tvDescription = findViewById(R.id.tvDescription);
        
        tvReporterName = findViewById(R.id.tvReporterName);
        ivProfileImage = findViewById(R.id.ivProfileImage);
    }

    private void loadData() {
        if (getIntent() != null) {
            String title = getIntent().getStringExtra("title");
            if (title != null) tvTitle.setText(title);

            String category = getIntent().getStringExtra("category");
            if (category != null) tvCategory.setText(category);

            String status = getIntent().getStringExtra("status");
            if (status != null) {
                tvStatusBadge.setText(status.toUpperCase());
                if ("FOUND".equalsIgnoreCase(status)) {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_tag_found);
                    // Also maybe change text color if needed, but tag_found usually has green bg
                } else {
                     tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_lost);
                }
            }

            String date = getIntent().getStringExtra("date");
            if (date != null) tvDate.setText(date);

            String location = getIntent().getStringExtra("location");
            if (location != null) {
                tvLocationName.setText(location);
                // If we have full address vs name, we might need split logic, but for now just set what we have
            }
            
            String description = getIntent().getStringExtra("description");
            if (description != null) tvDescription.setText(description);

            int imageResId = getIntent().getIntExtra("image_res_id", 0);
            if (imageResId != 0) {
                ivItemImage.setImageResource(imageResId);
            }
            
            // Reporter info - if passed, else default to Sarah J for the demo feel
            String reporter = getIntent().getStringExtra("reporter_name");
            if (reporter != null) tvReporterName.setText(reporter);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> {
            Toast.makeText(this, "Sharing item...", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnContactReporter).setOnClickListener(v -> {
            Toast.makeText(this, "Opening chat with Sarah J...", Toast.LENGTH_SHORT).show();
            // Implement chat opening logic here
        });
    }
}
