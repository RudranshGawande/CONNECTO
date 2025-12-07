package com.megaproject.urbanspace.Model;

public class WasteSchedule {
    private String type;
    private String status;
    private String date;
    private String time;
    private String area;
    private String frequency;
    private String instructions;

    public WasteSchedule(String type, String status, String date, String time,
                         String area, String frequency, String instructions) {
        this.type = type;
        this.status = status;
        this.date = date;
        this.time = time;
        this.area = area;
        this.frequency = frequency;
        this.instructions = instructions;
    }

    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getArea() { return area; }
    public String getFrequency() { return frequency; }
    public String getInstructions() { return instructions; }
}
