package com.megaproject.connecto;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import androidx.viewpager2.widget.ViewPager2;
import com.megaproject.connecto.Adapter.ImageCarouselAdapter;
import java.util.ArrayList;
import java.util.Collections;

public class LostFoundItemDetailsActivity extends AppCompatActivity implements com.google.android.gms.maps.OnMapReadyCallback {

    private ImageView btnBack, btnShare, ivCategoryIcon, ivProfileImage;
    private ViewPager2 vpImageCarousel;
    private TextView tvImageCounter;
    private TextView tvStatusBadge, tvTitle, tvCategory, tvDate;
    private TextView tvLocationName, tvLocationDetails;
    private TextView tvDescription;
    private TextView tvReporterName;
    
    private com.google.android.gms.maps.MapView mapPreview;
    private com.google.android.gms.maps.GoogleMap googleMap;
    private View viewMapOverlay;
    // Default: Central Park, NY coordinates for the demo
    private final LatLng defaultLocation = new LatLng(40.7812, -73.9665); 
    private LatLng itemLocation = null;
    private String itemLocationName = "";
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper()); 

    // Carousel components
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Object> imageList = new ArrayList<>();
    private ImageCarouselAdapter carouselAdapter; 
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_found_item_details);

        initViews();
        
        // Initialize MapView
        mapPreview.onCreate(savedInstanceState);
        mapPreview.getMapAsync(this);
        
        setupListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        
        vpImageCarousel = findViewById(R.id.vpImageCarousel);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        
        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationDetails = findViewById(R.id.tvLocationDetails);
        
        tvDescription = findViewById(R.id.tvDescription);
        
        tvReporterName = findViewById(R.id.tvReporterName);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        
        mapPreview = findViewById(R.id.mapPreview);
        viewMapOverlay = findViewById(R.id.viewMapOverlay);
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
                    tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#22C55E"));
                } else {
                     tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_lost);
                     tvStatusBadge.setTextColor(android.graphics.Color.WHITE);
                }
            }

            String date = getIntent().getStringExtra("date");
            if (date != null) tvDate.setText(date);

            String location = getIntent().getStringExtra("location");
            if (location != null) {
                tvLocationName.setText(location);
                itemLocationName = location;
                geocodeLocation(location);
            }
            
            String description = getIntent().getStringExtra("description");
            if (description != null) tvDescription.setText(description);

            // Handle Images
            imageList.clear();
            ArrayList<String> urls = getIntent().getStringArrayListExtra("image_urls");
            String singleImageUri = getIntent().getStringExtra("imageUri");
            int imageResId = getIntent().getIntExtra("image_res_id", 0);

            if (urls != null && !urls.isEmpty()) {
                imageList.addAll(urls);
            } else if (singleImageUri != null) {
                imageList.add(singleImageUri);
            } else if (imageResId != 0) {
                imageList.add(imageResId);
            }

            // Setup Carousel
            setupCarousel();
            
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
        });
        
        viewMapOverlay.setOnClickListener(v -> openLocationOnMap());
        findViewById(R.id.tvViewMap).setOnClickListener(v -> openLocationOnMap());
        findViewById(R.id.cardMapPreview).setOnClickListener(v -> openLocationOnMap());
        
        // Also ensure the parent container of location details is clickable if user taps "View" area loosely
        findViewById(R.id.tvLocationName).setOnClickListener(v -> openLocationOnMap());
        findViewById(R.id.tvLocationDetails).setOnClickListener(v -> openLocationOnMap());

        // Pause auto-scroll on touch
        vpImageCarousel.getChildAt(0).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sliderHandler.removeCallbacks(sliderRunnable);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (imageList.size() > 1) {
                    sliderHandler.postDelayed(sliderRunnable, 3000);
                }
            }
            return false;
        });
    }

    private void geocodeLocation(String locationName) {
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                handler.post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        itemLocation = new LatLng(address.getLatitude(), address.getLongitude());
                        updateMapPreview();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateMapPreview() {
        if (googleMap != null && itemLocation != null) {
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(itemLocation, 15f));
            googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(itemLocation));
        }
    }

    private void setupCarousel() {
        if (imageList.isEmpty()) {
            // Default placeholder if needed, or hide
             imageList.add(R.drawable.ic_wallet); // Fallback
        }

        carouselAdapter = new ImageCarouselAdapter(this, imageList);
        vpImageCarousel.setAdapter(carouselAdapter);

        int totalImages = imageList.size();
        tvImageCounter.setText("1/" + totalImages);

        if (totalImages > 1) {
            // Infinite loop setup
            // Start in the middle
            int middle = Integer.MAX_VALUE / 2;
            int startPos = middle - (middle % totalImages);
            vpImageCarousel.setCurrentItem(startPos, false);

            // Page Change Callback for Counter
            vpImageCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                 @Override
                 public void onPageSelected(int position) {
                     int realPos = position % totalImages;
                     tvImageCounter.setText((realPos + 1) + "/" + totalImages);
                 }
            });

            // Auto Scroll
            sliderRunnable = new Runnable() {
                @Override
                public void run() {
                    vpImageCarousel.setCurrentItem(vpImageCarousel.getCurrentItem() + 1);
                    sliderHandler.postDelayed(this, 3000);
                }
            };
        } else {
            // No auto scroll for single image
            tvImageCounter.setText("1/1");
        }
    }
    
    private void openLocationOnMap() {
        if (itemLocationName == null || itemLocationName.isEmpty()) return;
        
        Intent intent = new Intent(this, LocationPickerActivity.class);
        intent.putExtra("is_view_only", true);
        intent.putExtra("address", itemLocationName);
        if (itemLocation != null) {
            intent.putExtra("latitude", itemLocation.latitude);
            intent.putExtra("longitude", itemLocation.longitude);
        }
        startActivity(intent);
    }

    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Move camera to default location
        // Move camera to default location or item location if loaded
        LatLng target = itemLocation != null ? itemLocation : defaultLocation;
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(target, 15f));
        googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(target));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapPreview != null) mapPreview.onResume();
        if (imageList.size() > 1 && sliderRunnable != null) {
             sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapPreview != null) mapPreview.onPause();
        if (sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapPreview != null) mapPreview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapPreview != null) mapPreview.onLowMemory();
    }
}
