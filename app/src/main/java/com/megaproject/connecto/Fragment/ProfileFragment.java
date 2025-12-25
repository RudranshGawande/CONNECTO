package com.megaproject.connecto.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.megaproject.connecto.R;

public class ProfileFragment extends Fragment {

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

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Setup Logout Button
        View logoutButton = view.findViewById(R.id.btnLogout);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes, Logout", (dialog, which) -> {
                        // Firebase Sign Out
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                        
                        // Google Sign Out
                        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                        com.google.android.gms.auth.api.signin.GoogleSignInClient googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso);
                        googleSignInClient.signOut();
                        
                        // Redirect to LoginActivity
                        if (getActivity() != null) {
                            android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.LoginActivity.class);
                            // Clear the back stack and start fresh
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish(); // Ensure the current activity is finished
                        }
                        
                        Toast.makeText(getContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
        
        // Setup interactive elements placeholder logic
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        // Edit Profile
        LinearLayout btnEditProfile = view.findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.EditProfileActivity.class);
                startActivity(intent);
            });
        }
        
        // Add other listeners as needed
        LinearLayout btnChangePassword = view.findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.VerifyPasswordActivity.class);
                startActivity(intent);
            });
        }

        // App Theme
        LinearLayout btnAppTheme = view.findViewById(R.id.btnAppTheme);
        if (btnAppTheme != null) {
            btnAppTheme.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.megaproject.connecto.AppThemeActivity.class);
                startActivity(intent);
            });
            
            // Update Theme Status Text
            android.widget.TextView tvThemeStatus = view.findViewById(R.id.tvThemeStatus);
            if (tvThemeStatus != null && getActivity() != null) {
                android.content.SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
                int savedMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                
                String statusText = "System";
                if (savedMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
                    statusText = "Light";
                } else if (savedMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
                    statusText = "Dark";
                }
                tvThemeStatus.setText(statusText);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh theme status when returning from AppThemeActivity
        if (getView() != null) {
            android.widget.TextView tvThemeStatus = getView().findViewById(R.id.tvThemeStatus);
            if (tvThemeStatus != null && getActivity() != null) {
                 android.content.SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
                int savedMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                
                String statusText = "System";
                if (savedMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
                    statusText = "Light";
                } else if (savedMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
                    statusText = "Dark";
                }
                tvThemeStatus.setText(statusText);
            }
        }
    }
}
