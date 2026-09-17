package com.megaproject.connecto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.megaproject.connecto.Adapter.ReportsAdapter;
import com.megaproject.connecto.Manager.ReportRepository;
import com.megaproject.connecto.Model.Report;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private ReportsAdapter adapter;
    private ReportRepository repository;
    private List<Report> allReports = new ArrayList<>();
    private EditText etSearch;
    private String currentFilter = "All"; // All, Pending, In Progress, Resolved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        repository = new ReportRepository(this);
        rvReports = findViewById(R.id.rv_reports);
        etSearch = findViewById(R.id.et_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.fab_add).setOnClickListener(v -> finish()); // Just go back to report screen for now 

        setupFilters();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    private void loadReports() {
        allReports = repository.getReports();
        filterReports();
    }

    private void setupFilters() {
        TextView btnAll = findViewById(R.id.btn_filter_all);
        TextView btnPending = findViewById(R.id.btn_filter_pending);
        TextView btnProgress = findViewById(R.id.btn_filter_inprogress);
        TextView btnResolved = findViewById(R.id.btn_filter_resolved);

        View.OnClickListener listener = v -> {
            // Reset styles
            resetFilterStyle(btnAll);
            resetFilterStyle(btnPending);
            resetFilterStyle(btnProgress);
            resetFilterStyle(btnResolved);

            // Set active style
            TextView clicked = (TextView) v;
            clicked.setBackgroundTintList(getColorStateList(R.color.blue_600));
            clicked.setTextColor(Color.WHITE);

            currentFilter = clicked.getText().toString();
            filterReports();
        };

        btnAll.setOnClickListener(listener);
        btnPending.setOnClickListener(listener);
        btnProgress.setOnClickListener(listener);
        btnResolved.setOnClickListener(listener);
    }

    private void resetFilterStyle(TextView tv) {
        tv.setBackgroundTintList(getColorStateList(R.color.white));
        tv.setTextColor(Color.BLACK);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterReports() {
        String query = etSearch.getText().toString().toLowerCase();
        
        List<Report> filtered = new ArrayList<>();
        for (Report r : allReports) {
            boolean matchesSearch = r.getCategory().toLowerCase().contains(query) || 
                                    r.getAddress().toLowerCase().contains(query);
            boolean matchesFilter = currentFilter.equals("All") || r.getStatus().equalsIgnoreCase(currentFilter);
            
            if (matchesSearch && matchesFilter) {
                filtered.add(r);
            }
        }

        if (adapter == null) {
            adapter = new ReportsAdapter(this, filtered);
            rvReports.setLayoutManager(new LinearLayoutManager(this));
            rvReports.setAdapter(adapter);
        } else {
            adapter.setReports(filtered);
        }
    }
}
