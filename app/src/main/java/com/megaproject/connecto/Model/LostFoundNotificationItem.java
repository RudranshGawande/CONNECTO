package com.megaproject.connecto.Model;

public class LostFoundNotificationItem {
    private String headerTitle; // "TODAY", "YESTERDAY" etc. - null if not a header
    private boolean isHeader;

    private String title;
    private String time;
    private String description; // Supports simple bolding if we parse it, but standard text for now
    private boolean isUnread;
    private int iconResId;
    private int bgTintColor; // Color Int
    private int iconTintColor; // Color Int

    // Constructor for Header
    public LostFoundNotificationItem(String headerTitle) {
        this.isHeader = true;
        this.headerTitle = headerTitle;
    }

    // Constructor for Item
    public LostFoundNotificationItem(String title, String time, String description, boolean isUnread, int iconResId, int bgTintColor, int iconTintColor) {
        this.isHeader = false;
        this.title = title;
        this.time = time;
        this.description = description;
        this.isUnread = isUnread;
        this.iconResId = iconResId;
        this.bgTintColor = bgTintColor;
        this.iconTintColor = iconTintColor;
    }

    public boolean isHeader() { return isHeader; }
    public String getHeaderTitle() { return headerTitle; }
    
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public boolean isUnread() { return isUnread; }
    public int getIconResId() { return iconResId; }
    public int getBgTintColor() { return bgTintColor; }
    public int getIconTintColor() { return iconTintColor; }
}
