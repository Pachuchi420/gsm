package com.pach.gsm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Item implements Serializable {
    @Expose private String id;

    @Expose
    @SerializedName("userid")
    private String userID;

    @Expose private String name;
    @Expose private String description;
    @Expose private byte[] imageData;
    @Expose private byte[] thumbnailData;
    @Expose private int price;
    @Expose private String currency;
    @Expose private LocalDateTime date;

    @Expose
    private Reservation reservation = new Reservation(null, null, null, false);

    @Expose private LocalDate uploadDate;

    @Expose private int priority;
    @Expose private Boolean sold = false;
    @Expose private Boolean toDelete = false;
    @Expose private Boolean toUpdate = false;
    @Expose private Boolean supabaseSync = false;


    public Item(String name, String description, byte[] imageData, int price, String currency, int priority) {
        this.id = makeUniqueID();
        this.userID = storageManager.getInstance().getUserID();
        this.name = name;
        this.description = description;
        this.imageData = imageData;
        this.price= price;
        this.currency = currency;
        this.date = LocalDateTime.now();
        this.sold = false;
        this.uploadDate = null;
        this.priority = priority;
        this.supabaseSync = false;
        this.toDelete = false;
    }

    public Item() {
        this.date = LocalDateTime.now(); // fallback
    }


    private String makeUniqueID(){
        return UUID.randomUUID().toString();
    }

    // Setters & Getters

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public Image getImage() {
        if (imageData != null) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null; // Return null or a default image
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getSold() {
        return this.sold;
    }

    public void setSold(Boolean sold) {
        this.sold = sold;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDate getReservationDate() {
        return reservation.getDate();
    }

    public Reservation getReservation(){return reservation;}
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }


    public Boolean isReserved(){
        return reservation.getReserved();
    }

    public Boolean uploadedToday() {
        LocalDate today = LocalDate.now();

        if (this.uploadDate == null) {
            System.out.println("Item has never been uploaded, uploading...");
            return false; // Upload since it was never uploaded before
        }

        if (this.uploadDate.isEqual(today)) {
            System.out.println("Item already uploaded today, skipping...");
            return true; // Prevents uploading twice on the same day
        }

        long daysSinceLastUpload = java.time.temporal.ChronoUnit.DAYS.between(this.uploadDate, today);

        switch (this.priority) {
            case 1: // Priority 1: Upload every day
                return false; // Always allow uploading (except if already uploaded today)

            case 2: // Priority 2: Upload every other day
                return daysSinceLastUpload < 2;

            case 3: // Priority 3: Upload only once a week
                return daysSinceLastUpload < 7;

            default:
                return true; // Default case: Prevent upload if priority is undefined
        }
    }


    public void resetUploadDate(){
        this.uploadDate = null;
    }

    public void setUploadedDate(){
        this.uploadDate = LocalDate.now();
    }

    public String getImagePath() {
        if (imageData == null) return null;

        try {
            // Define the directory and ensure it exists
            Path tempDir = Files.createTempDirectory("item_images");
            File tempFile = new File(tempDir.toFile(), id + ".png"); // Unique filename using the item's ID

            // Write image data to the file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(imageData);
            }

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public LocalDate getUploadDate(){
        return this.uploadDate;
    }

    public void setSupabaseSync(Boolean state){
        this.supabaseSync = state;
    }
    public Boolean getSupabaseSync(){
        return this.supabaseSync;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean setToDelete) {
        this.toDelete = setToDelete;
    }


    public Boolean getToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(Boolean toUpdate) {
        this.toUpdate = toUpdate;
    }

    public byte[] getThumbnailData() {
        return thumbnailData;
    }

    public void setThumbnailData(byte[] thumbnailData) {
        this.thumbnailData = thumbnailData;
    }


}
