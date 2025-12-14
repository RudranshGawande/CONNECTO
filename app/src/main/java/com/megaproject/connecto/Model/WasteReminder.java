package com.megaproject.connecto.Model;

public class WasteReminder {
    private String id;
    private String wasteType;
    private String reminderTime;
    private boolean isEnabled;
    private int iconResource;
    private int backgroundColor;
    private int iconColor;

    public WasteReminder(String id, String wasteType, String reminderTime, boolean isEnabled,
                         int iconResource, int backgroundColor, int iconColor) {
        this.id = id;
        this.wasteType = wasteType;
        this.reminderTime = reminderTime;
        this.isEnabled = isEnabled;
        this.iconResource = iconResource;
        this.backgroundColor = backgroundColor;
        this.iconColor = iconColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }
}


