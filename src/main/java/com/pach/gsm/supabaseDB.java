package com.pach.gsm;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class supabaseDB {
    private static final String SUPABASE_URL = "https://zurmzywsshlnqisrlupq.supabase.co";

    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1cm16eXdzc2hsbnFpc3JsdXBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk4NzI0OTAsImV4cCI6MjA1NTQ0ODQ5MH0.onMgwEmnbE8AjAUEQvXtKXVbTjL5q115jjkui2Dh3mw"; // Secure API key

    private static final OkHttpClient client = new OkHttpClient();

    // ✅ INSERT ITEM
    public String insertItem(Item item) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", UUID.randomUUID().toString());  // Generate UUID
        requestBody.put("name", item.getName());
        requestBody.put("description", item.getDescription());
        requestBody.put("price", item.getPrice());
        requestBody.put("currency", item.getCurrency());
        requestBody.put("priority", item.getPriority());
        requestBody.put("sold", item.getSold());

        if (item.getReservation() != null && item.getReservation().getReserved()) {
            requestBody.put("buyer", item.getReservation().getBuyer());
            requestBody.put("place", item.getReservation().getPlace());
            requestBody.put("reservation_date", item.getReservation().getDate().toString());
            requestBody.put("reserved", item.getReservation().getReserved());
            requestBody.put("reservation_hour", item.getReservation().getHour());
            requestBody.put("reservation_minute", item.getReservation().getMinute());
        }

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error inserting item: " + response.body().string());
            return "✅ Item inserted successfully!";
        }
    }

    // ✅ GET ALL ITEMS
    public List<Item> getAllItems() throws IOException {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items?select=*")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error fetching items: " + response.body().string());

            JSONArray jsonArray = new JSONArray(response.body().string());
            List<Item> items = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Item item = new Item(
                        obj.getString("name"),
                        obj.getString("description"),
                        null, // Handle image separately if needed
                        obj.getInt("price"),
                        obj.getString("currency"),
                        obj.getInt("priority")
                );
                item.setId(obj.getString("id"));
                item.setSold(obj.getBoolean("sold"));

                if (obj.optBoolean("reserved", false)) {
                    item.getReservation().setBuyer(obj.getString("buyer"));
                    item.getReservation().setPlace(obj.getString("place"));
                    item.getReservation().setDate(LocalDate.parse(obj.getString("reservation_date")));
                    item.getReservation().setHour(obj.getInt("reservation_hour"));
                    item.getReservation().setMinute(obj.getInt("reservation_minute"));
                }

                items.add(item);
            }
            return items;
        }
    }

    // ✅ UPDATE ITEM (Mark as Sold)
    public void markItemAsSold(String itemId, boolean isSold) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("sold", isSold);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items?id=eq." + itemId)
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .patch(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error updating item: " + response.body().string());
            System.out.println("✅ Item marked as sold.");
        }
    }

    // ✅ DELETE ITEM
    public void deleteItem(String itemId) throws IOException {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items?id=eq." + itemId)
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error deleting item: " + response.body().string());
            System.out.println("✅ Item deleted.");
        }
    }

    // ✅ UPDATE RESERVATION
    public void updateReservation(String itemId, Reservation reservation) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("buyer", reservation.getBuyer());
        requestBody.put("place", reservation.getPlace());
        requestBody.put("reservation_date", reservation.getDate().toString());
        requestBody.put("reserved", reservation.getReserved());
        requestBody.put("reservation_hour", reservation.getHour());
        requestBody.put("reservation_minute", reservation.getMinute());

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items?id=eq." + itemId)
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .patch(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error updating reservation: " + response.body().string());
            System.out.println("✅ Reservation updated.");
        }
    }

    // ✅ DELETE RESERVATION
    public void cancelReservation(String itemId) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("buyer", JSONObject.NULL);
        requestBody.put("place", JSONObject.NULL);
        requestBody.put("reservation_date", JSONObject.NULL);
        requestBody.put("reserved", false);
        requestBody.put("reservation_hour", 0);
        requestBody.put("reservation_minute", 0);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/items?id=eq." + itemId)
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .patch(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error canceling reservation: " + response.body().string());
            System.out.println("✅ Reservation canceled.");
        }
    }
}