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
                "sample_id_1",
                "Silver ring",
                "It is a silver ring with a diamond in it.",
                "Railway station",
                "Accessories",
                "open",
                "12/9/2025, 2:35:33 PM",
                true,
                "Rudransh Gawande",
                "rudranshgawande007@gmail.com",
                "+91345677890",
                new ArrayList<>(),
                "lost",
                "sample_user_id"
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






