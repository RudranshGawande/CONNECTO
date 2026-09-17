package com.megaproject.connecto.Model;

public class ReportCategory {
    private String id;
    private String name;
    private int iconResId;
    private boolean isSelected;

    public ReportCategory(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
