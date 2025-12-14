package com.megaproject.connecto.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.megaproject.connecto.Model.LostFoundItem;

import java.util.ArrayList;
import java.util.List;

public class LostFoundViewModel extends ViewModel {

    public enum Tab { LOST, FOUND }

    private final MutableLiveData<Tab> activeTab = new MutableLiveData<>(Tab.LOST);
    private final MutableLiveData<List<LostFoundItem>> foundItems = new MutableLiveData<>(new ArrayList<>());

    public LostFoundViewModel() {
        // placeholder data to mirror UI; replace with repository data when available
        List<LostFoundItem> sample = new ArrayList<>();
        sample.add(new LostFoundItem(
                "Silver ring",
                "Electronics",
                "Open",
                "It is a silver ring with a diamond in it.",
                "RudranshGawande",
                "rudranshgawande007@gmail.com",
                "12345677890",
                "12/9/2025, 2:35:33 PM • Railway station"
        ));
        foundItems.setValue(sample);
    }

    public LiveData<Tab> getActiveTab() {
        return activeTab;
    }

    public LiveData<List<LostFoundItem>> getFoundItems() {
        return foundItems;
    }

    public void setActiveTab(Tab tab) {
        activeTab.setValue(tab);
    }
}






