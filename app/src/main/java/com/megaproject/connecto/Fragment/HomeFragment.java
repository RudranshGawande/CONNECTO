package com.megaproject.connecto.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
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

        MaterialCardView cardLiveTransport = view.findViewById(R.id.cardLiveTransport);
        MaterialCardView cardWaste = view.findViewById(R.id.cardWaste);
        MaterialCardView cardReportIssue = view.findViewById(R.id.cardReportIssue);
        MaterialCardView cardLostFound = view.findViewById(R.id.cardLostFound);
        MaterialCardView cardEvents = view.findViewById(R.id.cardEvents);
        MaterialCardView cardEmergency = view.findViewById(R.id.cardEmergency);
        MaterialCardView cardCommunity = view.findViewById(R.id.cardCommunity);

        LinearLayout navHome = view.findViewById(R.id.navHome);
        LinearLayout navTransport = view.findViewById(R.id.navTransport);
        LinearLayout navWaste = view.findViewById(R.id.navWaste);
        LinearLayout navReport = view.findViewById(R.id.navReport);

        cardLiveTransport.setOnClickListener(v -> navigateTo(new TransportFragment()));
        cardWaste.setOnClickListener(v -> navigateTo(new WasteFragment()));
        cardReportIssue.setOnClickListener(v -> navigateTo(new ReportIssueFragment()));
        cardLostFound.setOnClickListener(v -> navigateTo(new LostFoundFragment()));
        cardEvents.setOnClickListener(v -> navigateTo(new EventsFragment()));
        cardEmergency.setOnClickListener(v -> navigateTo(new EmergencyFragment()));
        cardCommunity.setOnClickListener(v -> navigateTo(new CommunityChatFragment()));

        navHome.setOnClickListener(v -> {
            // stay on home
        });
        navTransport.setOnClickListener(v -> navigateTo(new TransportFragment()));
        navWaste.setOnClickListener(v -> navigateTo(new WasteFragment()));
        navReport.setOnClickListener(v -> navigateTo(new ReportIssueFragment()));

        return view;
    }

    private void navigateTo(@NonNull Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}


