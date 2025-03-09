package com.pach.gsm;

import java.util.UUID;

public class Group {
    private String userID;
    private String name;
    private String id;


    private int interval, startHour, startMinute, endHour, endMinute;


    public Group(String name, int interval, int startHour, int startMinute, int endHour, int endMinute){
        this.id = makeUniqueID();
        this.userID = storageManager.getInstance().getUserID();
        this.name = name;
        this.interval = interval;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
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


}
