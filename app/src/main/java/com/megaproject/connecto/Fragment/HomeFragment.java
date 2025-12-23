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

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

        return view;
    }

    private void navigateTo(@NonNull Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}


