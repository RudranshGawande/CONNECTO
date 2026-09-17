package com.megaproject.connecto.Model;

import java.util.UUID;

public class Report {
    private String id;
    private String category;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String status; // Pending, In Progress, Resolved
    private long timestamp;
    private String imageUri;

    public Report(String category, String description, String address, double latitude, double longitude, String imageUri) {
        this.id = UUID.randomUUID().toString();
        this.category = category;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUri = imageUri;
        this.status = "Pending";
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public String getImageUri() { return imageUri; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
