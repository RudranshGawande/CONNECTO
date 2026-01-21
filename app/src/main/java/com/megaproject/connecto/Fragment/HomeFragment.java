package com.megaproject.connecto.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.cardview.widget.CardView;
import com.megaproject.connecto.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView ivHeaderProfile;
    private TextView tvGreeting;

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ivHeaderProfile = view.findViewById(R.id.ivHeaderProfile);
        // Assuming there is a greeting textview, if not I'll just skip it or try to find it.
        // Looking at xml line 120, it doesn't have an ID, it has text "@string/home_greeting".
        // I won't try to change greeting text right now unless user asked, but user just said "dp".
        // Code below focuses on DP.

        CardView cardLiveTransport = view.findViewById(R.id.cardLiveTransport);
        CardView cardWaste = view.findViewById(R.id.cardWaste);
        CardView cardReportIssue = view.findViewById(R.id.cardReportIssue);
        CardView cardLostFound = view.findViewById(R.id.cardLostFound);
        CardView cardEmergency = view.findViewById(R.id.cardEmergency);
        // New service transport card
        CardView cardServiceTransport = view.findViewById(R.id.cardServiceTransport);
        CardView cardSosRow = view.findViewById(R.id.cardSosRow);

        if (cardLiveTransport != null) {
            cardLiveTransport.setOnClickListener(v -> navigateTo(new TransportFragment()));
        }
        if (cardWaste != null) {
            cardWaste.setOnClickListener(v -> navigateTo(new WasteFragment()));
        }
        if (cardReportIssue != null) {
            cardReportIssue.setOnClickListener(v -> navigateTo(new ReportIssueFragment()));
        }
        if (cardLostFound != null) {
            cardLostFound.setOnClickListener(v -> navigateTo(new LostFoundFragment()));
        }

        
        // Additional listeners for new cards
        if (cardServiceTransport != null) {
            cardServiceTransport.setOnClickListener(v -> navigateTo(new TransportFragment()));
        }
        if (cardSosRow != null) {
            cardSosRow.setOnClickListener(v -> navigateTo(new EmergencyFragment()));
        }

        // Profile Navigation
        View profileContainer = view.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> navigateTo(new ProfileFragment()));
        }

        // SOS Slider Logic
        View sliderTrack = view.findViewById(R.id.sosSliderTrack);
        View sliderHandle = view.findViewById(R.id.sosSliderHandle);
        View sliderProgress = view.findViewById(R.id.sosSliderProgress);
        View sliderText = view.findViewById(R.id.sosSliderText);

        if (sliderTrack != null && sliderHandle != null && sliderProgress != null) {
            sliderHandle.setOnTouchListener(new View.OnTouchListener() {
                float dX;

                @Override
                public boolean onTouch(View v, android.view.MotionEvent event) {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            // Prevent parent ScrollView from hijacking the touch event
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            dX = v.getX() - event.getRawX();
                            return true;

                        case android.view.MotionEvent.ACTION_MOVE:
                            float newX = event.getRawX() + dX;
                            float maxX = sliderTrack.getWidth() - v.getWidth();
                            // Clamp
                            if (newX < 0) newX = 0;
                            if (newX > maxX) newX = maxX;
                            
                            v.setX(newX);
                            
                            // Update Progress Width
                            ViewGroup.LayoutParams params = sliderProgress.getLayoutParams();
                            params.width = (int) (newX + v.getWidth());
                            sliderProgress.setLayoutParams(params);

                            // Update Text Position (Parallax: Move Left as Handle moves Right)
                            if (sliderText != null) {
                                // Move text left by 40% of the handle movement
                                sliderText.setTranslationX(-newX * 0.4f);
                            }
                            return true;

                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            // Allow parent scrolling again
                            v.getParent().requestDisallowInterceptTouchEvent(false);

                            float finalX = v.getX();
                            float maxLimit = sliderTrack.getWidth() - v.getWidth();
                            // If dragged more than 80%
                            if (finalX > maxLimit * 0.8f) {
                                // Trigger Action
                                navigateTo(new EmergencyFragment());
                                
                                // Snap back visual mainly for effect if staying on page
                                android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(finalX, 0);
                                animator.setDuration(200);
                                animator.addUpdateListener(animation -> {
                                    float val = (float) animation.getAnimatedValue();
                                    v.setX(val);
                                    ViewGroup.LayoutParams p = sliderProgress.getLayoutParams();
                                    p.width = (int) (val + v.getWidth());
                                    sliderProgress.setLayoutParams(p);
                                    
                                    if (sliderText != null) {
                                        sliderText.setTranslationX(-val * 0.4f);
                                    }
                                });
                                animator.start();
                            } else {
                                // Snap back
                                android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(finalX, 0);
                                animator.setDuration(200);
                                animator.addUpdateListener(animation -> {
                                    float val = (float) animation.getAnimatedValue();
                                    v.setX(val);
                                    ViewGroup.LayoutParams p = sliderProgress.getLayoutParams();
                                    p.width = (int) (val + v.getWidth());
                                    sliderProgress.setLayoutParams(p);

                                    if (sliderText != null) {
                                        sliderText.setTranslationX(-val * 0.4f);
                                    }
                                });
                                animator.start();
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
        
        loadUserData();

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (mAuth == null) return;
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && ivHeaderProfile != null) {
            String email = user.getEmail();
            
            // Default: Check Google Photo or Set Default
            if (user.getPhotoUrl() != null) {
                 Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .centerCrop()
                    .into(ivHeaderProfile);
            } else {
                ivHeaderProfile.setImageResource(R.drawable.ic_default_profile);
            }

            // Sync with Firestore
            if (email != null) {
                String safeEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
                db.collection("users").document(safeEmail).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                             String photoUrl = null;
                             if (documentSnapshot.contains("photoUrl")) {
                                 photoUrl = documentSnapshot.getString("photoUrl");
                             }
                             
                             if (photoUrl != null && !photoUrl.isEmpty()) {
                                 if (getActivity() != null) {
                                     Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_default_profile)
                                        .error(R.drawable.ic_default_profile)
                                        .centerCrop()
                                        .into(ivHeaderProfile);
                                 }
                             }
                        }
                    });
            }
        }
    }

    private void navigateTo(@NonNull Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}


