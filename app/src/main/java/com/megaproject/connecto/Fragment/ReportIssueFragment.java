package com.megaproject.connecto.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.megaproject.connecto.Adapter.CategoryAdapter;
import com.megaproject.connecto.Adapter.RecentActivityAdapter;
import com.megaproject.connecto.Model.IssueActivity;
import com.megaproject.connecto.Model.ReportCategory;
import com.megaproject.connecto.R;

import java.util.ArrayList;
import java.util.List;

public class ReportIssueFragment extends Fragment {

    private RecyclerView rvCategories;
    private RecyclerView rvRecentActivity;
    private Button btnContinue;
    private CategoryAdapter categoryAdapter;
    private RecentActivityAdapter activityAdapter;

    private TextView tvRecentActivityTitle;

    public ReportIssueFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_issue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
        tvRecentActivityTitle = view.findViewById(R.id.tv_recent_activity_title);
        btnContinue = view.findViewById(R.id.btn_continue);

        setupCategories();
        setupRecentActivity();
        
        view.findViewById(R.id.btn_my_reports).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getContext(), com.megaproject.connecto.MyReportsActivity.class));
        });

        btnContinue.setOnClickListener(v -> {
             ReportCategory selectedCategory = null;
             // Find selected category (assuming adapter logic updates list, or we track it)
             // simplified for now: iterate to find selected
             List<ReportCategory> currentList = ((CategoryAdapter) rvCategories.getAdapter()).getCategories(); // Need to expose this or track in Fragment
             for (ReportCategory cat : currentList) {
                 if (cat.isSelected()) {
                     selectedCategory = cat;
                     break;
                 }
             }

             if (selectedCategory != null) {
                 android.content.Intent intent = new android.content.Intent(getContext(), com.megaproject.connecto.ReportIssueDetailsActivity.class);
                 intent.putExtra("CATEGORY_NAME", selectedCategory.getName());
                 startActivity(intent);
             } else {
                 Toast.makeText(getContext(), "Please select a category first", Toast.LENGTH_SHORT).show();
             }
        });
        
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
             if (getActivity() != null) {
                 getActivity().onBackPressed();
             }
        });
    }

    private void setupCategories() {
        List<ReportCategory> categories = new ArrayList<>();
        categories.add(new ReportCategory("1", "Pothole", R.drawable.ic_warning)); 
        categories.add(new ReportCategory("2", "Broken Light", R.drawable.ic_light_mode));
        categories.add(new ReportCategory("3", "Water Leak", R.drawable.ic_warning));
        categories.add(new ReportCategory("4", "Graffiti", R.drawable.ic_edit));
        categories.add(new ReportCategory("5", "Waste", R.drawable.ic_recycling));
        categories.add(new ReportCategory("6", "Other", R.drawable.ic_more_horiz));

        // Pre-select the first one if desired
        // categories.get(0).setSelected(true);

        categoryAdapter = new CategoryAdapter(getContext(), categories, category -> {
            // Handle category click if needed beyond selection
        });
        
        // 2 columns grid
        rvCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupRecentActivity() {
        List<IssueActivity> activities = new ArrayList<>();
        // Mock data commented out to simulate empty state initially
        // activities.add(new IssueActivity("1", "Pothole on 5th Ave", "Draft", "Draft • 2 hours ago", R.drawable.ic_warning));
        // activities.add(new IssueActivity("2", "Light fix on Main St", "Submitted", "Submitted • Yesterday", R.drawable.ic_light_mode));

        if (activities.isEmpty()) {
            tvRecentActivityTitle.setVisibility(View.GONE);
            rvRecentActivity.setVisibility(View.GONE);
        } else {
            tvRecentActivityTitle.setVisibility(View.VISIBLE);
            rvRecentActivity.setVisibility(View.VISIBLE);
            
            activityAdapter = new RecentActivityAdapter(getContext(), activities);
            rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecentActivity.setAdapter(activityAdapter);
        }
    }
}
