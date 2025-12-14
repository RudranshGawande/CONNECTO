package com.megaproject.connecto.Model;

public class Locality {
    private String id;
    private String name;
    private String zone;
    private String pickupDays;
    private boolean isRecent;

    public Locality(String id, String name, String zone, String pickupDays, boolean isRecent) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.pickupDays = pickupDays;
        this.isRecent = isRecent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPickupDays() {
        return pickupDays;
    }

    public void setPickupDays(String pickupDays) {
        this.pickupDays = pickupDays;
    }

    public boolean isRecent() {
        return isRecent;
    }

    public void setRecent(boolean recent) {
        isRecent = recent;
    }

    public String getDetails() {
        return zone + " • " + pickupDays;
    }
}


