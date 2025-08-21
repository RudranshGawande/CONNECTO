package com.megaproject.urbanspace.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.megaproject.urbanspace.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<Fragment> fragmentList; // ✅ Store fragments here

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ✅ Prepare fragment list in same order
        fragmentList = new ArrayList<>();
        fragmentList.add(new TransportFragment());
        fragmentList.add(new WasteFragment());
        fragmentList.add(new EventsFragment());
        fragmentList.add(new EmergencyFragment());
        fragmentList.add(new LostFoundFragment());
        fragmentList.add(new CommunityChatFragment());
        fragmentList.add(new ReportIssueFragment());
        fragmentList.add(new ProfileFragment());

        return view;
    }
}

