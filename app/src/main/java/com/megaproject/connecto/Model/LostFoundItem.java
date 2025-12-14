package com.megaproject.connecto.Model;

import java.util.List;

public class LostFoundItem {
    private String id;
    private String title;
    private String description;
    private String location;
    private String category;
    private String status; // "open", "found", "closed"
    private String dateTime;
    private boolean allowContact;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private List<String> imageUris;
    private String type; // "lost" or "found"

    public LostFoundItem() {
    }

    public LostFoundItem(String title, String category, String status, String description,
                         String contactName, String contactEmail, String contactPhone, String meta) {
        this.title = title;
        this.category = category;
        this.status = status;
        this.description = description;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.meta = meta;
    }

    public LostFoundItem(String id, String title, String description, String location, 
                       String category, String status, String dateTime, boolean allowContact,
                       String contactName, String contactEmail, String contactPhone,
                       List<String> imageUris, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.category = category;
        this.status = status;
        this.dateTime = dateTime;
        this.allowContact = allowContact;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.imageUris = imageUris;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isAllowContact() {
        return allowContact;
    }

    public void setAllowContact(boolean allowContact) {
        this.allowContact = allowContact;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Legacy field for backward compatibility
    private String meta;

    public String getMeta() { return meta; }
}






