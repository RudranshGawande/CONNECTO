package com.megaproject.connecto.Manager;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megaproject.connecto.Model.Report;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportRepository {
    private static final String PREF_NAME = "connecto_reports";
    private static final String KEY_REPORTS = "reports_list";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public ReportRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveReport(Report report) {
        List<Report> reports = getReports();
        reports.add(0, report); // Add to top
        saveList(reports);
    }

    public List<Report> getReports() {
        String json = sharedPreferences.getString(KEY_REPORTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Report>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveList(List<Report> reports) {
        String json = gson.toJson(reports);
        sharedPreferences.edit().putString(KEY_REPORTS, json).apply();
    }
}
