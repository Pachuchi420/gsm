package com.pach.gsm;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable {
    @Expose private String buyer;
    @Expose private String place;
    @Expose private LocalDate date;
    @Expose private Boolean reserved;
    @Expose private int hour;   // New field to store the hour
    @Expose private int minute; // New field to store the minute

    public Reservation(String buyer, String place, LocalDate date, Boolean reserved){
        this.buyer = buyer;
        this.place = place;
        this.date = date;
        this.reserved = reserved;
        this.hour = 0;   // Default hour to 0
        this.minute = 0; // Default minute to 0
    }

    public Reservation() {
        this.buyer = null;
        this.place = null;
        this.date = null;
        this.reserved = false;
        this.hour = 0;
        this.minute = 0;
    }


    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDate(LocalDate date){
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public Boolean getReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void undoReservation() {
        this.buyer = null;
        this.place = null;
        this.date = null;
        this.reserved = false;
        this.hour = 0;
        this.minute = 0;
    }
}