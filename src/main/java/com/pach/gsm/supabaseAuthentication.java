package com.pach.gsm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import okhttp3.*;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class supabaseAuthentication {

    // Supabase credentials
    private static final String SUPABASE_URL = "https://zurmzywsshlnqisrlupq.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1cm16eXdzc2hsbnFpc3JsdXBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk4NzI0OTAsImV4cCI6MjA1NTQ0ODQ5MH0.onMgwEmnbE8AjAUEQvXtKXVbTjL5q115jjkui2Dh3mw";
    private static final OkHttpClient client = new OkHttpClient();

    // Singleton Instance
    private static supabaseAuthentication singletonInstance;
    private  boolean online;
    private boolean wasOnline;
    private static Runnable refreshTableCallback;

    public static void setRefreshTableCallback(Runnable callback) {
        refreshTableCallback = callback;
    }



    // Call the singleton instance
    public static supabaseAuthentication getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new supabaseAuthentication();
        }
        return singletonInstance;
    }

    public static void sessionManager() {
        supabaseAuthentication auth = supabaseAuthentication.getInstance();
        auth.online = false;
        auth.wasOnline = false;

        Thread checker = new Thread(() -> {
            while (true) {
                boolean isCurrentlyOnline = checkIfOnline();

                if (!isCurrentlyOnline) {
                    auth.wasOnline = false;
                } else if (!auth.wasOnline) {
                    storageManager localStorage = storageManager.getInstance();
                    auth.wasOnline = true;
                    String refreshToken = localStorage.getRefreshToken();
                    String accessToken = generateAccessToken(refreshToken);
                    if (accessToken != null) {
                        System.out.println("✅ Connection Restored: Access token refreshed!");

                        // 🔄 Wait for any ongoing syncs to finish before reinitializing DB
                        synchronized (storageManager.class) {
                            System.out.println("🔄 Reinitializing local database...");
                            localStorage.initializeDatabase(localStorage.getUserID());
                        }

                        // ✅ Only sync failed items AFTER database reinitialization
                        localStorage.syncFailedItems();

                        // ✅ Refresh TableView in UI
                        if (refreshTableCallback != null) {
                            javafx.application.Platform.runLater(() -> refreshTableCallback.run());
                        }
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("❌ Session thread interrupted: " + e.getMessage());
                    break;
                }
            }
        });

        checker.setDaemon(true);
        checker.start();
    }

    public static boolean checkIfOnline() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            // System.out.println("✅ Online!");
            supabaseAuthentication.getInstance().online = (200 <= responseCode && responseCode < 400);
            return (200 <= responseCode && responseCode < 400);
        } catch (IOException e) {
            // System.out.println("❌ Offline");
            supabaseAuthentication.getInstance().online = false;
            return false;
        }
    }


    public static String registerUser(String email, String password){
        String jsonRequest = "{"
                              + "\"email\":" + "\"" + email +"\""
                              + ",\"password\":" + "\"" + password +"\"" +
                              "}";


        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));

        Request request = new Request.Builder()
                            .url(SUPABASE_URL + "/auth/v1/signup")
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .build();

        try (Response response = client.newCall(request).execute()){
            String serverResponse = response.body().string();
            System.out.println(serverResponse);
            JsonObject jsonResponse = JsonParser.parseString(serverResponse).getAsJsonObject();

            if (response.isSuccessful() && jsonResponse.has("id")){
                System.out.println("✅Registration Successful: \nE-Mail: " + jsonResponse.get("email").getAsString() + "\nConfirmation sent at " + jsonResponse.get("confirmation_sent_at").getAsString());
                return "success";
            } else {
                int errorCode = response.code();
                System.out.println("❌ Registration failed, code: " + errorCode);
                return manageErrorCodeRegistration(errorCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String manageErrorCodeRegistration(int errorCode){
        switch (errorCode) {
            case 400:
                return "Bad Request! Are you sure that E-Mail exists?" ;
            case 401:
                return "Unauthorized access: Please check your API key";
            case 403:
                return "Forbidden: You do not have permission to perform this action";
            case 409:
                return "Email is already registered";
            case 422:
                return "Validation error";
            case 500:
                return "Server error: Please try again later";
            default:
                return "Unknown error occurred";
        }
    }

    public static String loginUser(String email, String password, boolean rememberMe) {
        String jsonRequest = "{"
                + "\"email\":" + "\"" + email + "\""
                + ",\"password\":" + "\"" + password + "\","
                + "\"grant_type\":\"password\"" +
                "}";

        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String serverResponse = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(serverResponse).getAsJsonObject();
            storageManager localStorage = storageManager.getInstance();

            if (response.isSuccessful() && jsonResponse.has("access_token")) {
                System.out.println("✅ Login Successful!");
                String accessToken = jsonResponse.get("access_token").getAsString();
                localStorage.addCredential("accessToken", accessToken);

                if (rememberMe) {
                    String refreshToken = jsonResponse.has("refresh_token") ? jsonResponse.get("refresh_token").getAsString() : null;
                    if (refreshToken != null) {
                        localStorage.addCredential("refreshToken", refreshToken);
                    }


                }

                if (jsonResponse.has("user") && jsonResponse.get("user").getAsJsonObject().has("id")) {
                    String userID = jsonResponse.get("user").getAsJsonObject().get("id").getAsString();
                    localStorage.saveUserID("userID", userID);

                    List<Item> supabaseItems = supabaseDB.fetchItems(userID);
                    if (supabaseItems != null) {
                        for (Item item : supabaseItems) {
                            localStorage.addItemLocal(item); // Store in local SQLite
                        }
                        System.out.println("✅ Synced items from Supabase to local database.");
                    } else {
                        System.out.println("⚠️ No items found in Supabase.");
                    }
                }
                return "success";
            } else {
                int errorCode = response.code();
                System.out.println("❌ Login failed, code: " + errorCode);
                return manageErrorCodeLogin(errorCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String manageErrorCodeLogin(int errorCode){
        switch (errorCode) {
            case 400:
                return "Oops! Something went wrong with your request. Please try again." ;
            case 401:
                return "Incorrect username or password. Please try again.";
            case 403:
                return "You don’t have permission to access this account or resource.";
            case 404:
                return "The login service is unavailable. Please check the URL and try again.";
            case 405:
                return "Invalid request method. Please contact support if the issue persists.";
            case 429:
                return "Too many login attempts! Please wait a few minutes and try again.";
            case 500:
                return "Something went wrong on our end. Please try again later.";
            case 502:
                return "We’re experiencing temporary issues. Please try again in a few minutes.";
            case 503:
                return "Our servers are currently down for maintenance. Please check back later.";
            case 504:
                return "The login request took too long. Please refresh and try again.";
            default:
                return "Unknown error occurred";
        }
    }




    public static boolean autoLogin(){
        storageManager localStorage = storageManager.getInstance();
        String refreshToken = localStorage.getRefreshToken();
        if (refreshToken == null){
            System.out.println("⚠️ No refresh token stored.");
            return false;
        }

        if (!supabaseAuthentication.getInstance().online) {
            System.out.println("☑️ Auto-login pending, entering session...");
            return true;
        }

        String accessToken = generateAccessToken(refreshToken);

        if (accessToken != null) {
            String newUserID = getUserIDFromSupabase(accessToken);
            localStorage.addCredential("accessToken", accessToken);
            if(newUserID == null){
                System.out.println("❌ Failed to fetch user ID. Logging out...");
                logoutUser();
                return false;
            }


            if (localStorage.getUserID() != null && !localStorage.getUserID().equals(newUserID)) {
                System.out.println("⚠️ User ID mismatch detected! Logging out...");
                logoutUser();
                return false;
            }

            localStorage.addCredential("accessToken", accessToken); // 🔥 Store accessToken
            System.out.println("✅ Auto-login successful.");
            return true;
        } else{
            System.out.println("❌ Auto-login failed. Redirecting to login.");
            logoutUser();
            return false;
        }
    }

    public static String getUserIDFromSupabase(String accessToken) {
        String USER_INFO_URL = SUPABASE_URL + "/auth/v1/user";

        Request request = new Request.Builder()
                .url(USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("apikey", SUPABASE_API_KEY)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.optString("id", null);
            } else {
                System.out.println("❌ Failed to fetch user ID from Supabase. Code: " + response.code());
                return null;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error while fetching user ID: " + e.getMessage());
            return null;
        }
    }

    public static String generateAccessToken(String refreshToken){

        String REFRESH_URL = SUPABASE_URL + "/auth/v1/token?grant_type=refresh_token";
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("refresh_token", refreshToken);


        RequestBody body = RequestBody.create(
                jsonRequest.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        // Build request
        Request request = new Request.Builder()
                .url(REFRESH_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                if (jsonResponse.has("access_token")) {
                    String accessToken = jsonResponse.getString("access_token");
                    System.out.println("✅ Access token refreshed successfully.");
                    return accessToken;
                } else {
                    System.out.println("❌ Refresh token failed: No access token in response.");
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("❌ Network error: " + e.getMessage());
            return null;
        }

    }

    public static Boolean logoutUser() {
        storageManager localStorage = storageManager.getInstance();
        localStorage.removeCredential("refreshToken");
        localStorage.removeCredential("userID");

        String checkToken = localStorage.getRefreshToken();
        if (checkToken == null) {
            System.out.println("✅ Successfully logged out: Refresh token removed.");
            return true;
        } else {
            System.out.println("❌ Logout failed: Refresh token still exists.");
            return false;
        }
    }







}