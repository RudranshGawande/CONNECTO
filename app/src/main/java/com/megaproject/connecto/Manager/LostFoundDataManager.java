package com.megaproject.connecto.Manager;

import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;
import java.util.ArrayList;
import java.util.List;

public class LostFoundDataManager {
    private static LostFoundDataManager instance;
    private List<LostFoundItem> allItems;

    private LostFoundDataManager() {
        allItems = new ArrayList<>();
        loadDummyData();
    }

    public static synchronized LostFoundDataManager getInstance() {
        if (instance == null) {
            instance = new LostFoundDataManager();
        }
        return instance;
    }

    public List<LostFoundItem> getAllItems() {
        return allItems;
    }

    public void addItem(LostFoundItem item) {
        allItems.add(0, item);
    }

    // items created by "me"
    public List<LostFoundItem> getMyItems() {
        List<LostFoundItem> myItems = new ArrayList<>();
        for (LostFoundItem item : allItems) {
            if (item.isMine()) {
                myItems.add(item);
            }
        }
        return myItems;
    }

    public void updateItemStatus(LostFoundItem item, String newStatus) {
        // Find item in allItems and update
        for (LostFoundItem i : allItems) {
            if (i == item || (i.getTitle().equals(item.getTitle()) && i.getDateTime().equals(item.getDateTime()))) {
                i.setStatus(newStatus);
                break;
            }
        }
    }

    private void loadDummyData() {
        // --- MY POSTS (Pre-populated) ---
        // 1. Black Leather Wallet (Lost)
        LostFoundItem myItem1 = new LostFoundItem();
        myItem1.setTitle("Black Leather Wallet");
        myItem1.setLocation("Downtown");
        myItem1.setDateTime("Oct 24");
        myItem1.setType("lost");
        myItem1.setCategory("Personal");
        myItem1.setImageResourceId(R.drawable.ic_wallet); 
        myItem1.setMine(true);
        myItem1.setStatus("open"); // Default status
        allItems.add(myItem1);

        // EXTRA: Flash Drive Phone (Requested)
        LostFoundItem flashDrive = new LostFoundItem();
        flashDrive.setTitle("Flash Drive Phone");
        flashDrive.setLocation("University Campus");
        flashDrive.setDateTime("Yesterday");
        flashDrive.setType("lost");
        flashDrive.setCategory("Electronics");
        flashDrive.setImageResourceId(R.drawable.ic_usb); // Assuming usb icon exists, or fallback to something generic if not, usually ic_smartphone or similar if usb not there. I'll check drawables. 
        // Based on user request, it's main homepage data? 
        // I'll stick to generic icon for now or ic_smartphone as fallback.
        flashDrive.setImageResourceId(R.drawable.ic_smartphone); 
        flashDrive.setMine(true);
        flashDrive.setStatus("open");
        allItems.add(flashDrive);

        // 2. Golden Retriever (Found)
        LostFoundItem myItem2 = new LostFoundItem();
        myItem2.setTitle("Golden Retriever");
        myItem2.setLocation("Central Park");
        myItem2.setDateTime("Oct 20");
        myItem2.setType("found");
        myItem2.setCategory("Pets");
        myItem2.setImageResourceId(R.drawable.ic_pets);
        myItem2.setMine(true);
        myItem2.setStatus("open");
        allItems.add(myItem2);

        // 3. iPhone 13 Pro (Lost)
        LostFoundItem myItem3 = new LostFoundItem();
        myItem3.setTitle("iPhone 13 Pro");
        myItem3.setLocation("Metro Station");
        myItem3.setDateTime("Oct 15");
        myItem3.setType("lost");
         myItem3.setCategory("Electronics");
        myItem3.setImageResourceId(R.drawable.ic_smartphone);
        myItem3.setMine(true);
        myItem3.setStatus("open");
        allItems.add(myItem3);

        // 4. Blue Hiking Backpack (Found)
        LostFoundItem myItem4 = new LostFoundItem();
        myItem4.setTitle("Blue Hiking Backpack");
        myItem4.setLocation("City Library");
        myItem4.setDateTime("2 days ago");
        myItem4.setType("found");
         myItem4.setCategory("Accessories");
        myItem4.setImageResourceId(R.drawable.ic_backpack);
        myItem4.setMine(true);
        myItem4.setStatus("open");
        allItems.add(myItem4);


        // --- OTHERS' POSTS ---
        // 5. Toyota Car Keys (Found)
        LostFoundItem others1 = new LostFoundItem();
        others1.setTitle("Toyota Car Keys");
        others1.setCategory("Personal");
        others1.setDateTime("30 mins ago");
        others1.setLocation("Main St. Parking Lot");
        others1.setType("found");
        others1.setMine(false);
        others1.setStatus("open");
        allItems.add(others1);

        // 6. Ray-Ban Sunglasses (Found)
        LostFoundItem others2 = new LostFoundItem();
        others2.setTitle("Ray-Ban Sunglasses");
        others2.setCategory("Accessories");
        others2.setDateTime("1 hr ago");
        others2.setLocation("Central Park Bench");
        others2.setType("found");
        others2.setMine(false);
        others2.setStatus("open");
        allItems.add(others2);
        
        // 7. Kids School Bag (Lost)
        LostFoundItem others3 = new LostFoundItem();
        others3.setTitle("Kids School Bag");
        others3.setCategory("Accessories");
        others3.setDateTime("Oct 22");
        others3.setLocation("City Library, Main Hall");
        others3.setType("lost");
        others3.setMine(false);
        others3.setStatus("open");
        allItems.add(others3);
    }
}
