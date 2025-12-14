package com.megaproject.connecto.Model;

public class EmergencyContact {
    private String id;
    private String name;
    private String phoneNumber;
    private String imageUrl; // For now simplified, could be URI
    private boolean isActive;
    private boolean isSystemContact; // For 911 etc

    public EmergencyContact(String id, String name, String phoneNumber, String imageUrl, boolean isActive, boolean isSystemContact) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isSystemContact = isSystemContact;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isSystemContact() { return isSystemContact; }

    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}


