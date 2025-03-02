package com.pach.gsm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class supabaseDB {

    private static final String SUPABASE_URL = "https://zurmzywsshlnqisrlupq.supabase.co";

    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1cm16eXdzc2hsbnFpc3JsdXBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk4NzI0OTAsImV4cCI6MjA1NTQ0ODQ5MH0.onMgwEmnbE8AjAUEQvXtKXVbTjL5q115jjkui2Dh3mw"; // Secure API key

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static supabaseDB supabaseDBInstance;



    public static supabaseDB getInstance() {
        if (supabaseDBInstance == null) {
            supabaseDBInstance = new supabaseDB();
        }
        return supabaseDBInstance;
    }

    public static List<Item> fetchItems(String userID) {
        String url = SUPABASE_URL + "/rest/v1/items?userid=eq." + userID;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> items = gson.fromJson(jsonResponse, listType);
                System.out.println("✅ Retrieved " + items.size() + " items from Supabase.");
                return items;
            } else {
                System.out.println("❌ Error fetching items: " + response.code());
                return null;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error while fetching items: " + e.getMessage());
            return null;
        }
    }

    public static boolean addItem(String userID, Item item) {
        String accessToken = storageManager.getInstance().getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            System.out.println("❌ Error: No access token. Cannot sync item to Supabase.");
            return false;
        }

        // ✅ Properly Construct JSON String Without Extra Commas
        StringBuilder jsonBuilder = new StringBuilder("{");

        jsonBuilder.append("\"id\":\"").append(item.getId()).append("\",");
        jsonBuilder.append("\"userid\":\"").append(userID).append("\",");
        jsonBuilder.append("\"name\":\"").append(item.getName()).append("\",");

        if (item.getDescription() != null) {
            jsonBuilder.append("\"description\":\"").append(item.getDescription().replace("\t", " ")).append("\","); // ✅ Fix: Remove tabs
        }

        jsonBuilder.append("\"price\":").append(item.getPrice()).append(",");
        jsonBuilder.append("\"currency\":\"").append(item.getCurrency()).append("\",");
        jsonBuilder.append("\"priority\":").append(item.getPriority()).append(",");
        jsonBuilder.append("\"sold\":").append(item.getSold()).append(",");
        jsonBuilder.append("\"date\":\"").append(item.getDate()).append("\",");

        if (item.getUploadDate() != null) {
            jsonBuilder.append("\"uploaddate\":\"").append(item.getUploadDate()).append("\",");
        }
        if (item.getReservation().getBuyer() != null) {
            jsonBuilder.append("\"reservation_buyer\":\"").append(item.getReservation().getBuyer()).append("\",");
        }
        if (item.getReservation().getPlace() != null) {
            jsonBuilder.append("\"reservation_place\":\"").append(item.getReservation().getPlace()).append("\",");
        }
        if (item.getReservationDate() != null) {
            jsonBuilder.append("\"reservation_date\":\"").append(item.getReservationDate()).append("\",");
        }

        jsonBuilder.append("\"reservation_reserved\":").append(item.getReservation().getReserved()).append(",");
        jsonBuilder.append("\"reservation_hour\":").append(item.getReservation().getHour()).append(",");
        jsonBuilder.append("\"reservation_minute\":").append(item.getReservation().getMinute());

        jsonBuilder.append("}"); // ✅ Ensuring last field doesn't have a comma

        String jsonRequest = jsonBuilder.toString();

        // ✅ Log JSON before sending
        System.out.println("📤 JSON Sent to Supabase: " + jsonRequest);

        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Item successfully synced to Supabase.");
                return true;
            } else {
                // ✅ Log full Supabase error response
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                System.out.println("❌ Error adding item: " + response.code());
                System.out.println("🔍 Supabase Error Response: " + errorBody);
                return false;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error while syncing item: " + e.getMessage());
            return false;
        }
    }
    public static boolean updateItem(String itemID, Item updatedItem) {
        String url = SUPABASE_URL + "/rest/v1/items?id=eq." + itemID;

        String jsonUpdate = gson.toJson(updatedItem);
        RequestBody body = RequestBody.create(jsonUpdate, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .patch(body) // PATCH request to update
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Item updated in Supabase: " + updatedItem.getName());
                return true;
            } else {
                System.out.println("❌ Error updating item: " + response.code());
                return false;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error while updating item: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteItem(String itemID) {
        String url = SUPABASE_URL + "/rest/v1/items?id=eq." + itemID;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Item deleted from Supabase: " + itemID);
                return true;
            } else {
                System.out.println("❌ Error deleting item: " + response.code());
                return false;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error while deleting item: " + e.getMessage());
            return false;
        }
    }

}
