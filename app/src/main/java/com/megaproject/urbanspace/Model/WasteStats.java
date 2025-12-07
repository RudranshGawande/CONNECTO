package com.megaproject.urbanspace.Model;

public class WasteStats {
    private int totalCollected;
    private int recyclingRate;
    private int wasteReduced;
    private String nextPickup;

    public WasteStats(int totalCollected, int recyclingRate, int wasteReduced, String nextPickup) {
        this.totalCollected = totalCollected;
        this.recyclingRate = recyclingRate;
        this.wasteReduced = wasteReduced;
        this.nextPickup = nextPickup;
    }

    // Getters and setters
    public int getTotalCollected() { return totalCollected; }
    public void setTotalCollected(int totalCollected) { this.totalCollected = totalCollected; }

    public int getRecyclingRate() { return recyclingRate; }
    public void setRecyclingRate(int recyclingRate) { this.recyclingRate = recyclingRate; }

    public int getWasteReduced() { return wasteReduced; }
    public void setWasteReduced(int wasteReduced) { this.wasteReduced = wasteReduced; }

    public String getNextPickup() { return nextPickup; }
    public void setNextPickup(String nextPickup) { this.nextPickup = nextPickup; }
}
