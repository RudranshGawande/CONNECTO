package com.megaproject.connecto.Model;

public class IssueActivity {
    private String id;
    private String title;
    private String status;
    private String date;
    private int imageResId;

    public IssueActivity(String id, String title, String status, String date, int imageResId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.date = date;
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public int getImageResId() {
        return imageResId;
    }
}
