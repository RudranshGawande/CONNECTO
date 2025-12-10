package com.megaproject.urbanspace.Model;

public class LostFoundItem {
    private final String title;
    private final String category;
    private final String status;
    private final String description;
    private final String contactName;
    private final String contactEmail;
    private final String contactPhone;
    private final String meta;

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

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getContactName() { return contactName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public String getMeta() { return meta; }
}

