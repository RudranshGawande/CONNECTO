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

    private String userId; // UID of the poster
    private String claimedByUserId; // UID of the person who claimed/found it
    private long createdAt; // Server timestamp
    private List<String> searchKeywords; // For search functionality

    public LostFoundItem() {
    }

    public LostFoundItem(String id, String title, String description, String location, 
                       String category, String status, String dateTime, boolean allowContact,
                       String contactName, String contactEmail, String contactPhone,
                       List<String> imageUris, String type, String userId) {
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
        this.userId = userId;
        this.searchKeywords = generateKeywords(title + " " + description + " " + category + " " + location);
        this.createdAt = System.currentTimeMillis();
    }

    // Keyword generation helper
    public static List<String> generateKeywords(String input) {
        List<String> keywords = new java.util.ArrayList<>();
        if (input == null) return keywords;
        
        String[] words = input.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        for (String word : words) {
            if (word.length() > 2) { // Filter out small words
                keywords.add(word);
            }
        }
        return keywords;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public boolean isAllowContact() { return allowContact; }
    public void setAllowContact(boolean allowContact) { this.allowContact = allowContact; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public List<String> getImageUris() { return imageUris; }
    public void setImageUris(List<String> imageUris) { this.imageUris = imageUris; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getClaimedByUserId() { return claimedByUserId; }
    public void setClaimedByUserId(String claimedByUserId) { this.claimedByUserId = claimedByUserId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }

    // Legacy field for backward compatibility
    private String meta;
    public String getMeta() { return meta; }

    private int imageResourceId;
    public int getImageResourceId() { return imageResourceId; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }

    private boolean isMine;
    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }
}






