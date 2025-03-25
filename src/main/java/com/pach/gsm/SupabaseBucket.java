package com.pach.gsm;

import okhttp3.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SupabaseBucket {

    private static final String SUPABASE_URL = "https://zurmzywsshlnqisrlupq.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1cm16eXdzc2hsbnFpc3JsdXBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk4NzI0OTAsImV4cCI6MjA1NTQ0ODQ5MH0.onMgwEmnbE8AjAUEQvXtKXVbTjL5q115jjkui2Dh3mw"; // Secure API key
    private static final String BUCKET_NAME = "item-images";
    private static final OkHttpClient client = new OkHttpClient();

    // Upload image
    public static boolean uploadImage(String itemId, File imageFile) {
        String accessToken = storageManager.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) return false;

        String path = "items/" + itemId + ".png";

        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            RequestBody body = RequestBody.create(fileBytes, MediaType.parse("image/png"));

            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("apikey", SUPABASE_API_KEY)
                    .put(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("✅ Image uploaded: " + path);
                    return true;
                } else {
                    System.out.println("❌ Upload failed: " + response.code() + " - " + response.body().string());
                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error uploading image: " + e.getMessage());
            return false;
        }
    }

    // Download image
    public static boolean downloadImage(String itemId, File destinationFile) {
        String accessToken = storageManager.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) return false;

        String path = "items/" + itemId + ".png";

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + path)
                .header("Authorization", "Bearer " + accessToken)
                .header("apikey", SUPABASE_API_KEY)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                byte[] imageBytes = response.body().bytes();
                try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                    fos.write(imageBytes);
                }
                System.out.println("✅ Image downloaded: " + path);
                return true;
            } else {
                System.out.println("❌ Failed to download image: " + response.code());
                return false;
            }
        } catch (IOException e) {
            System.out.println("❌ Error downloading image: " + e.getMessage());
            return false;
        }
    }

    // Delete image (optional)
    public static boolean deleteImage(String itemId) {
        String accessToken = storageManager.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) return false;

        String path = "items/" + itemId + ".png";

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path)
                .header("Authorization", "Bearer " + accessToken)
                .header("apikey", SUPABASE_API_KEY)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("🗑️ Image deleted: " + path);
                return true;
            } else {
                System.out.println("❌ Failed to delete image: " + response.code());
                return false;
            }
        } catch (IOException e) {
            System.out.println("❌ Error deleting image: " + e.getMessage());
            return false;
        }
    }

}