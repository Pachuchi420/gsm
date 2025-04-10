package com.pach.gsm;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class Group {
    private String userID;
    private String name;
    private String id;

    private int itemsPerCycle;

    private LocalDateTime lastUpload;


    private int interval, startHour, startMinute, endHour, endMinute;


    public Group(String name, int interval, int startHour, int startMinute, int endHour, int endMinute, int itemsPerCycle){
        this.id = makeUniqueID();
        this.userID = storageManager.getInstance().getUserID();
        this.name = name;
        this.interval = interval;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.lastUpload = null;
        this.itemsPerCycle = itemsPerCycle;
    }


    public Group(String id, String name, int interval, int startHour, int startMinute, int endHour, int endMinute, int itemsPerCycle){
        this.id = id;
        this.userID = storageManager.getInstance().getUserID();
        this.name = name;
        this.interval = interval;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.lastUpload = null;
        this.itemsPerCycle = itemsPerCycle;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public  String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getStartTime(){
        return String.format("%02d:%02d", startHour, startMinute);
    }

    public String getEndTime(){
        return String.format("%02d:%02d", endHour, endMinute);
    }

    private String makeUniqueID(){
        return UUID.randomUUID().toString();
    }


    public LocalDateTime getLastUpload() {
        return this.lastUpload;
    }

    public void setLastUpload(LocalDateTime givenDateTime){
        this.lastUpload = givenDateTime;
    }

    public boolean isNowWithinTimeWindow() {
        LocalTime start = LocalTime.of(startHour, startMinute);
        LocalTime end = LocalTime.of(endHour, endMinute);
        LocalTime now = LocalTime.now();

        if (end.isBefore(start)) {
            return now.isAfter(start) || now.isBefore(end); // Overnight case
        } else {
            return !now.isBefore(start) && !now.isAfter(end);
        }
    }

    public boolean hasIntervalPassed() {
        if (lastUpload == null) {
            System.out.println("⏱️ No last upload recorded — passing interval check by default.");
            return true; // Never uploaded → allow sending
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSince = java.time.Duration.between(lastUpload, now).toMinutes();

        if (minutesSince >= interval) {
            return true;
        } else {
            System.out.println("⏳ Only " + minutesSince + " minutes since last upload. Required: " + interval + " minutes.");
            return false;
        }
    }

    public int getItemsPerCycle() {
        return itemsPerCycle;
    }

    public void setItemsPerCycle(int itemsPerCycle) {
        this.itemsPerCycle = itemsPerCycle;
    }
}
