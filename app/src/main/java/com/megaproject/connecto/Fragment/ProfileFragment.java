package com.megaproject.connecto.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.megaproject.connecto.AppThemeActivity;
import com.megaproject.connecto.ChangePasswordActivity;
import com.megaproject.connecto.EditProfileActivity;
import com.megaproject.connecto.LoginActivity;
import com.megaproject.connecto.R;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private ImageView ivProfileAvatar;
    private LinearLayout btnEditProfile, btnChangePassword, btnAppTheme;
    private TextView btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners();
        loadUserData();
    }

    private void initViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnAppTheme = view.findViewById(R.id.btnAppTheme);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        // Back button if in fragment might be handled by parent or Activity, 
        // but layout has a back button R.id.btnBack. 
        // If this is a top-level fragment in Home navigation, back might logically just go to Home or do nothing.
        // If checking layout:
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                 if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                     getParentFragmentManager().popBackStack();
                 }
            });
        }
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });

        btnAppTheme.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AppThemeActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // Refresh data when returning from Edit Profile
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            
            // Default load from Auth
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            }
            
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .centerCrop()
                    .into(ivProfileAvatar);
            } else {
                ivProfileAvatar.setImageResource(R.drawable.ic_default_profile);
            }

            // Load from Firestore for additional details or sync
            if (email != null) {
                String safeEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
                db.collection("users").document(safeEmail).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("fullName")) {
                                String dbName = documentSnapshot.getString("fullName");
                                if (dbName != null && !dbName.isEmpty()) {
                                    tvProfileName.setText(dbName);
                                }
                            }
                            if (documentSnapshot.contains("photoUrl")) {
                                String photoUrl = documentSnapshot.getString("photoUrl");
                                if (photoUrl != null && !photoUrl.isEmpty()) {
                                    Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_default_profile)
                                        .error(R.drawable.ic_default_profile)
                                        .centerCrop()
                                        .into(ivProfileAvatar);
                                }
                            }
                        }
                    });
            }
        }
    }
}
