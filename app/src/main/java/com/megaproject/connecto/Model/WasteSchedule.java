package com.megaproject.connecto.Model;

public class WasteSchedule {
    private String id;
    private String type;
    private String status;
    private String date;
    private String time;
    private String area;
    private String frequency;
    private String instructions;
    private int iconResource;
    private int backgroundColor;
    private int iconColor;
    private boolean isNextCollection;
    private String imageUrl;

    public WasteSchedule(String id, String type, String status, String date, String time,
                         String area, String frequency, String instructions) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.date = date;
        this.time = time;
        this.area = area;
        this.frequency = frequency;
        this.instructions = instructions;
        this.isNextCollection = false;
    }

    public WasteSchedule(String id, String type, String status, String date, String time,
                         String area, String frequency, String instructions,
                         int iconResource, int backgroundColor, int iconColor) {
        this(id, type, status, date, time, area, frequency, instructions);
        this.iconResource = iconResource;
        this.backgroundColor = backgroundColor;
        this.iconColor = iconColor;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getArea() { return area; }
    public String getFrequency() { return frequency; }
    public String getInstructions() { return instructions; }
    public int getIconResource() { return iconResource; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getIconColor() { return iconColor; }
    public boolean isNextCollection() { return isNextCollection; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setArea(String area) { this.area = area; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setIconResource(int iconResource) { this.iconResource = iconResource; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
    public void setIconColor(int iconColor) { this.iconColor = iconColor; }
    public void setNextCollection(boolean nextCollection) { isNextCollection = nextCollection; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Timestamp for robust sorting
    private long sortTimestamp;
    public long getSortTimestamp() { return sortTimestamp; }
    public void setSortTimestamp(long sortTimestamp) { this.sortTimestamp = sortTimestamp; }
}


