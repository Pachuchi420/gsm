package com.pach.gsm;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

    // Singleton Instance
    private static supabaseAuthentication singletonInstance;
    private String refreshToken;
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
            String responseBody = requestResponse.body().string();  // Get API response as string
            int registrationCode = requestResponse.code(); // Get HTTP status code

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
                String accessToken = jsonResponse.getString("access_token");
                String refreshToken = jsonResponse.getString("refresh_token");

                if (rememberMe){
                    supabaseAuthentication.getInstance().saveAndEncryptRefreshToken(refreshToken);
                }

                return "✅ Login successful!";
            }

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

    // **Load and Decrypt Refresh Token**
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

    // **Get or Create Secret Key**
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

    // **Encrypt Token**
    private byte[] encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    // **Decrypt Token**
    private String decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }

    // **Get the Current Refresh Token (Decrypted)**
    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean logoutUser() {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                System.out.println("⚠️ No active session found. User is already logged out.");
                return false;
            }



            File tokenFile = new File(TOKEN_FILE);
            if (tokenFile.exists()) {
                if (tokenFile.delete()) {
                    System.out.println("✅ Refresh token deleted successfully.");
                } else {
                    System.out.println("⚠️ Failed to delete refresh token file.");
                }
            }

            this.refreshToken = null;

            singletonInstance = null;

            System.out.println("✅ User logged out successfully.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("🚨 Error during logout.");
            return false;
        }
    }


}