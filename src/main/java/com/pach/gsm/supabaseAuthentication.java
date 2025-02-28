package com.pach.gsm;

import java.io.*;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.*;
import org.json.JSONObject;

import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class supabaseAuthentication {

    // Supabase credentials
    private static final String SUPABASE_URL = "https://zurmzywsshlnqisrlupq.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1cm16eXdzc2hsbnFpc3JsdXBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk4NzI0OTAsImV4cCI6MjA1NTQ0ODQ5MH0.onMgwEmnbE8AjAUEQvXtKXVbTjL5q115jjkui2Dh3mw"; // Secure API key

    private static final String TOKEN_FILE = "refresh_token.dat"; // File to store encrypted token
    private static final String SECRET_KEY_FILE = "secret.key"; // File to store encryption key
    private static final String USER_ID_FILE = "userid.id";
    private static boolean connectionManagerRunning = false; // Prevent multiple threads



    // Singleton Instance
    private static supabaseAuthentication singletonInstance;
    private String refreshToken;
    private String userID;
    private  boolean online;



    private static final OkHttpClient mainClient = new OkHttpClient();

    // Call the singleton instance
    public static supabaseAuthentication getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new supabaseAuthentication();
        }
        return singletonInstance;
    }

    private supabaseAuthentication(){
        this.refreshToken = loadAndDecryptRefreshToken();
    }

    public void setUserID(String userID) throws FileNotFoundException {
        this.userID = userID;
        try (FileOutputStream fos = new FileOutputStream(USER_ID_FILE)) {
            fos.write(userID.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getUserID(){
        if (this.userID != null){
            return this.userID;
        }
        File file = new File(USER_ID_FILE);
        if (!file.exists()){
            return "";
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data); // Read file contents into byte array
            this.userID = new String(data).trim(); // Convert bytes to String and trim spaces
            return this.userID;
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + e.getMessage(), e);
        }
    }



    public static String registerUser(String givenEmail, String givenPassword) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", givenEmail);
        requestBody.put("password", givenPassword);

        Request registrationRequest = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .header("apikey", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response requestResponse = mainClient.newCall(registrationRequest).execute()) {
            String responseBody = requestResponse.body().string();
            int registrationCode = requestResponse.code();

            if (!requestResponse.isSuccessful()) {
                // Handle specific error codes
                switch (registrationCode) {
                    case 400:
                        String msg1 = "Invalid email format or missing password.";
                        System.out.println("❌"+msg1);
                        return msg1;
                    case 401:
                        String msg2 = "Unauthorized - API key is missing or invalid.";;
                        System.out.println("❌"+msg2);
                        return msg2;
                    case 409:
                        String msg3 = "Email already registered. Try logging in.";
                        System.out.println("⚠️"+msg3);
                        return msg3;
                    case 500:
                        String msg4 = "Server error. Please try again later.";
                        System.out.println("🚨"+msg4);
                        return msg4;
                    default:
                        String msg5 = "Unknown error: " + responseBody;
                        System.out.println("❌"+msg5);
                        return msg5;
                }
            } else {
                return "✅ Registration successful! Please log in.";
            }
        }
    }

    public static String loginUser(String givenEmail, String givenPassword, boolean rememberMe) throws IOException{
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", givenEmail);
        requestBody.put("password", givenPassword);

        Request loginRequest = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .header("apikey", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try(Response requestResponse = mainClient.newCall(loginRequest).execute()){
            String responseBody = requestResponse.body().string();
            int responseCode = requestResponse.code();

            if(!requestResponse.isSuccessful()){
                switch (responseCode) {
                    case 400:
                        String msg1 = "Invalid credentials. Check email and password.";
                        System.out.println("❌ " + msg1);
                        return msg1;
                    case 401:
                        String msg2 = "Unauthorized - API key is missing or invalid.";
                        System.out.println("❌ " + msg2);
                        return msg2;
                    case 500:
                        String msg3 = "Server error. Please try again later.";
                        System.out.println("🚨 " + msg3);
                        return msg3;
                    default:
                        String msg4 = "Login failed: " + responseBody;
                        System.out.println("❌ " + msg4);
                        return msg4;
                }
            } else {
                JSONObject jsonResponse = new JSONObject(responseBody);
                String refreshToken = jsonResponse.getString("refresh_token");
                String userID = jsonResponse.getJSONObject("user").getString("id");

                supabaseAuthentication auth = supabaseAuthentication.getInstance();
                auth.setUserID(userID);


                if (rememberMe){
                    auth.saveAndEncryptRefreshToken(refreshToken);
                }

                System.out.println("✅ Login successful with userID: " + userID);
                return "✅ Login successful";
            }

        }
    }

    public static int autoLogin() {
        supabaseAuthentication auth = supabaseAuthentication.getInstance();
        String refreshToken = auth.getRefreshToken();

        if (refreshToken == null) {
            System.out.println("❌ Auto Login unsuccessful! No refresh token stored.");
            return 1;
        }

        if (!checkIfOnline()) {
            System.out.println("⚠️ No internet connection! Using refresh token for offline access.");
            connectionManager(); // ✅ Start connection checker only if not running
            return 2;
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("refresh_token", refreshToken);

        Request refreshRequest = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=refresh_token")
                .header("apikey", SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response requestResponse = mainClient.newCall(refreshRequest).execute()) {
            if (!requestResponse.isSuccessful()) {
                System.out.println("❌ Request not successful. Auto-login failed.");
                return 3;
            }

            JSONObject jsonResponse = new JSONObject(requestResponse.body().string());
            String newRefreshToken = jsonResponse.getString("refresh_token");
            String userID = jsonResponse.getJSONObject("user").getString("id");

            if (!userID.equals(auth.getUserID())) {
                System.out.println("⚠️ User IDs do not match! Logging out...");
                auth.logoutUser();
                return 5;
            }

            // Store new credentials
            auth.setUserID(userID);
            auth.saveAndEncryptRefreshToken(newRefreshToken);
            System.out.println("✅ Auto-Login successful!");

            connectionManager(); // ✅ Ensures connection manager runs only once
            return 4;

        } catch (IOException e) {
            System.out.println("🚨 Error during auto-login: " + e.getMessage());
            return 3;
        }
    }



    private static void connectionManager() {
        if (connectionManagerRunning) {
            System.out.println("⚠️ Connection Manager is already running. Skipping duplicate start.");
            return; // Prevent multiple instances
        }

        connectionManagerRunning = true; // Mark as running
        System.out.println("✅ Connection Manager has been started!");

        Thread connectionThread = new Thread(() -> {
            while (true) {
                boolean wasOnline = supabaseAuthentication.getInstance().online;
                boolean isOnline = checkIfOnline();

                if (isOnline && !wasOnline) {
                    System.out.println("✅ Internet restored! Attempting to validate session...");
                    supabaseAuthentication.getInstance().online = true;
                    autoLogin();
                } else if (!isOnline && wasOnline) {
                    System.out.println("❌ Lost internet connection!");
                    supabaseAuthentication.getInstance().online = false;
                }

                try {
                    Thread.sleep(2000); // Run every 2 seconds
                } catch (InterruptedException e) {
                    System.out.println("❌ Connection Manager Thread interrupted.");
                    break;
                }
            }
        });

        connectionThread.setDaemon(true);
        connectionThread.start();
    }



    private static boolean checkIfOnline() {
        try {
            Request request = new Request.Builder()
                    .url("https://www.google.com")
                    .head()
                    .build();

            try (Response response = mainClient.newCall(request).execute()) {
                return response.isSuccessful();}
            catch (IOException e) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void saveAndEncryptRefreshToken(String token) {
        try {
            SecretKey key = getOrCreateSecretKey();
            byte[] encryptedToken = encrypt(token, key);

            try (FileOutputStream fos = new FileOutputStream(TOKEN_FILE)) {
                fos.write(encryptedToken);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadAndDecryptRefreshToken() {
        try {
            SecretKey key = getOrCreateSecretKey();
            File tokenFile = new File(TOKEN_FILE);

            if (!tokenFile.exists()) return null;

            byte[] encryptedToken = new byte[(int) tokenFile.length()];
            try (FileInputStream fis = new FileInputStream(tokenFile)) {
                fis.read(encryptedToken);
            }

            return decrypt(encryptedToken, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SecretKey getOrCreateSecretKey() throws Exception {
        File keyFile = new File(SECRET_KEY_FILE);

        if (!keyFile.exists()) {
            // Create a new key and store it
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            try (FileOutputStream fos = new FileOutputStream(keyFile)) {
                fos.write(secretKey.getEncoded());
            }
            return secretKey;
        } else {
            // Load the existing key
            byte[] keyBytes = new byte[(int) keyFile.length()];
            try (FileInputStream fis = new FileInputStream(keyFile)) {
                fis.read(keyBytes);
            }
            return new SecretKeySpec(keyBytes, "AES");
        }
    }

    private byte[] encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean logoutUser() {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                System.out.println("⚠️ No active session found.");
                return false;
            }

            File tokenFile = new File(TOKEN_FILE);
            if (tokenFile.exists()) {
                if (tokenFile.delete()) {
                    System.out.println("✅ Refresh token deleted.");
                } else {
                    System.out.println("⚠️ Failed to delete refresh token file.");
                }
            }

            this.refreshToken = null;
            this.userID = null;
            singletonInstance = null;

            System.out.println("✅ User logged out successfully.");
            return true;

        } catch (Exception e) {
            System.out.println("🚨 Error during logout.");
            return false;
        }
    }








}