package com.megaproject.urbanspace.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import soup.neumorphism.NeumorphCardView;
import com.megaproject.urbanspace.R;

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

        // Initialize all cards
        NeumorphCardView transportCard = view.findViewById(R.id.transportCard);
        NeumorphCardView wasteCard = view.findViewById(R.id.wasteCard);
        NeumorphCardView eventsCard = view.findViewById(R.id.eventsCard);
        NeumorphCardView lostFoundCard = view.findViewById(R.id.lostFoundCard);
        NeumorphCardView reportIssueCard = view.findViewById(R.id.reportIssueCard);
        NeumorphCardView emergencyCard = view.findViewById(R.id.emergencyCard);
        NeumorphCardView chatCard = view.findViewById(R.id.chatCard);

        // Click Listeners
        transportCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransportFragment())
                        .addToBackStack(null)
                        .commit());

        wasteCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WasteFragment())
                        .addToBackStack(null)
                        .commit());

        eventsCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EventsFragment())
                        .addToBackStack(null)
                        .commit());

        lostFoundCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LostFoundFragment())
                        .addToBackStack(null)
                        .commit());

        reportIssueCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ReportIssueFragment())
                        .addToBackStack(null)
                        .commit());

        emergencyCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EmergencyFragment())
                        .addToBackStack(null)
                        .commit());

        chatCard.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CommunityChatFragment())
                        .addToBackStack(null)
                        .commit());

        return view;
    }
}
