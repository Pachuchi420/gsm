package com.pach.gsm;

import com.google.gson.Gson;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.prefs.Preferences;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;


public class storageManager {

    private static final String SECRET_KEY = "0123456789abcdef0123456789abcdef"; // 32-char AES key
    private static final String ALGORITHM = "AES";
    private static final String FILE_PATH = "local_items.json";
    private static String DATABASE_URL = "jdbc:sqlite:gsmLocal.db";
    private static storageManager instance;


    private final Gson gson = new Gson();
    private final Preferences prefs;

    private List<Item> itemList;


    private storageManager() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        initializeDatabase(getUserID());
    }




    public static synchronized storageManager getInstance() {
        if (instance == null) {
            instance = new storageManager();
        }
        return instance;
    }



    // CREDENTIALS & REFRESH TOKEN STORAGE
    public void addCredential(String key, String value) {
        try {
            String encryptedValue = encrypt(value);
            prefs.put(key, encryptedValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRefreshToken() {
        try {
            String encryptedValue = prefs.get("refreshToken", null);
            return (encryptedValue != null) ? decrypt(encryptedValue) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAccessToken() {
        try {
            String encryptedValue = prefs.get("accessToken", null);
            return (encryptedValue != null) ? decrypt(encryptedValue) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserID() {
        try {
            String userID = prefs.get("userID", null);
            return userID;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeCredential(String key) {
        prefs.remove(key);
    }

    public void clearAllCredentials() {
        try {
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public void saveUserID(String key, String userID) {
        try {
            prefs.put(key, userID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // FILE STORAGE CRUD OPERATIONS WITH SQLITE-3

    public void initializeDatabase(String userID) {
        if (userID == null) {
            throw new IllegalStateException("❌ Cannot initialize database, userID is null.");
        }

        DATABASE_URL = "jdbc:sqlite:" + getDatabasePath(userID);

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                    "id TEXT PRIMARY KEY," +
                    "userID TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "imagedata BLOB," +
                    "price INTEGER NOT NULL," +
                    "currency TEXT," +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "sold BOOLEAN DEFAULT FALSE," +
                    "uploaddate DATE," +
                    "priority INTEGER," +
                    "reservation_buyer TEXT," +
                    "reservation_place TEXT," +
                    "reservation_date DATE," +
                    "reservation_reserved BOOLEAN DEFAULT FALSE," +
                    "reservation_hour INTEGER," +
                    "reservation_minute INTEGER);";

            stmt.execute(sql);
            System.out.println("✅ User-specific database initialized for: " + userID);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Database initialization failed.");
        }
    }

    public String getDatabasePath(String userID) {
        if (userID == null) {
            throw new IllegalStateException("❌ Cannot determine database path, userID is null.");
        }

        String baseDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "GSM_DB";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Ensure directory exists
        }

        return baseDir + File.separator + "user_" + userID + ".db";
    }

    public void addItemLocal(Item item) {
        String sql = "INSERT INTO items (id, userID, name, description, imagedata, price, currency, date, sold, uploaddate, priority, " +
                "reservation_buyer, reservation_place, reservation_date, reservation_reserved, reservation_hour, reservation_minute) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getUserID());
            pstmt.setString(3, item.getName());
            pstmt.setString(4, item.getDescription());
            pstmt.setBytes(5, item.getImageData());
            pstmt.setInt(6, item.getPrice());
            pstmt.setString(7, item.getCurrency());
            pstmt.setTimestamp(8, java.sql.Timestamp.valueOf(item.getDate()));
            pstmt.setBoolean(9, item.getSold());
            pstmt.setDate(10, item.getUploadDate() != null ? java.sql.Date.valueOf(item.getUploadDate()) : null);
            pstmt.setInt(11, item.getPriority());
            pstmt.setString(12, item.getReservation().getBuyer());
            pstmt.setString(13, item.getReservation().getPlace());
            pstmt.setDate(14, item.getReservationDate() != null ? java.sql.Date.valueOf(item.getReservationDate()) : null);
            pstmt.setBoolean(15, item.getReservation().getReserved());
            pstmt.setInt(16, item.getReservation().getHour());
            pstmt.setInt(17, item.getReservation().getMinute());

            pstmt.executeUpdate();
            System.out.println("✅ Item added to local database!");

            // 🔥 Sync to Supabase if online
            if (supabaseAuthentication.checkIfOnline()) {
                boolean success = supabaseDB.addItem(item.getUserID(), item);
                if (success) {
                    System.out.println("✅ Item synced with Supabase.");
                } else {
                    System.out.println("⚠️ Failed to sync item to Supabase.");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Error adding item to local database: " + e.getMessage());
        }
    }
    public List<Item> getAllLocalItems(String userID) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE userID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Item item = new Item(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBytes("imagedata"),
                        rs.getInt("price"),
                        rs.getString("currency"),
                        rs.getInt("priority")
                );
                item.setId(rs.getString("id"));
                item.setSold(rs.getBoolean("sold"));
                item.setUploadedDate();
                items.add(item);
            }
            System.out.println("✅ Loaded " + items.size() + " items from local database for user: " + userID);

        } catch (SQLException e) {
            System.out.println("❌ Error fetching items for user: " + userID + " - " + e.getMessage());
        }
        return items;
    }
    public void syncWithSupabase() {
        if (!supabaseAuthentication.checkIfOnline()) {
            System.out.println("⚠️ Not online. Sync postponed.");
            return;
        }

        List<Item> localItems = getAllLocalItems(getUserID());

        for (Item item : localItems) {
            boolean success = supabaseDB.addItem(item.getUserID(), item);
            if (success) {
                System.out.println("✅ Synced item: " + item.getName() + " with Supabase.");
            } else {
                System.out.println("❌ Failed to sync item: " + item.getName());
            }
        }
    }


    public void deleteItem(String itemId) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
            System.out.println("✅ Item deleted from local database.");

            // 🔥 Sync deletion to Supabase
            if (supabaseAuthentication.checkIfOnline()) {
                boolean success = supabaseDB.deleteItem(itemId);
                if (success) {
                    System.out.println("✅ Item deleted from Supabase.");
                } else {
                    System.out.println("⚠️ Failed to delete item from Supabase.");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Error deleting item from local database: " + e.getMessage());
        }
    }



}