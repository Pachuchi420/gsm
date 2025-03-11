package com.pach.gsm;

import com.google.gson.Gson;
import tools.DBWorker;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.sql.*;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.ArrayList;


public class storageManager {

    private static final String SECRET_KEY = "0123456789abcdef0123456789abcdef"; // 32-char AES key
    private static final String ALGORITHM = "AES";
    private static final String FILE_PATH = "local_items.json";
    private static String DATABASE_URL = "jdbc:sqlite:gsmLocal.db";
    private static storageManager instance;


    private final Gson gson = new Gson();
    private final Preferences prefs;

    private Boolean dbReady = false;
    private static DBWorker dbWorker = new DBWorker();
    private List<Item> itemList;


    private storageManager() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        String userID = getUserID();
        if (userID == null) {
            System.out.println("⚠️ Warning: UserID is null. Database initialization postponed.");
        } else {
            initializeDatabase(userID);
        }
    }

    public void submitToDBWorker(Runnable task) {
        dbWorker.submitTask(task);
    }




    public static synchronized storageManager getInstance() {
        if (instance == null) {
            instance = new storageManager();
            dbWorker = new DBWorker();
        }
        return instance;
    }



    // CREDENTIALS & REFRESH TOKEN STORAGE MANAGEMENT
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


            // 🔄 Set timeout to allow SQLite to wait if locked
            try (PreparedStatement timeoutStmt = conn.prepareStatement("PRAGMA busy_timeout = 5000;")) {
                timeoutStmt.execute();
            }

            String sqlItem = "CREATE TABLE IF NOT EXISTS items (" +
                    "id TEXT PRIMARY KEY," +
                    "userID TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "imagedata BLOB," +
                    "price INTEGER NOT NULL," +
                    "currency TEXT," +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "sold INTEGER DEFAULT 0," +  // 🔄 BOOLEAN → INTEGER (0 = false, 1 = true)
                    "uploaddate DATE," +
                    "priority INTEGER," +
                    "reservation_buyer TEXT," +
                    "reservation_place TEXT," +
                    "reservation_date DATE," +
                    "reservation_reserved INTEGER DEFAULT 0," +  // 🔄 BOOLEAN → INTEGER
                    "reservation_hour INTEGER," +
                    "reservation_minute INTEGER," +
                    "supabaseSync INTEGER DEFAULT 0,"+
                    "toDelete INTEGER DEFAULT 0);";

            String sqlGroup = "CREATE TABLE IF NOT EXISTS groups (" +
                    "id TEXT PRIMARY KEY," +
                    "userID TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "interval INTEGER," +
                    "startHour INTEGER," +
                    "startMinute INTEGER," +
                    "endHour INTEGER," +
                    "endMinute INTEGER," +
                    "last_uploaded TIMESTAMP);";

            String sqlItemGroup = "CREATE TABLE IF NOT EXISTS item_groups (" +
                    "itemID TEXT NOT NULL, " +
                    "groupID TEXT NOT NULL, " +
                    "last_uploaded TIMESTAMP, " +
                    "FOREIGN KEY(itemID) REFERENCES items(id), " +
                    "FOREIGN KEY(groupID) REFERENCES groups(id), " +
                    "PRIMARY KEY (itemID, groupID));";



            stmt.execute(sqlItem);
            stmt.execute(sqlGroup);
            stmt.execute(sqlItemGroup);
            System.out.println("✅ User-specific database initialized for: " + userID);
            setDbReady(true);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Database initialization failed.");
            setDbReady(false);
        }
    }

    public String getDatabasePath(String userID) {
        if (userID == null) {
            throw new IllegalStateException("❌ Cannot determine database path, userID is null.");
        }

        String baseDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "GSM" + File.separator +  "DB";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Ensure directory exists
        }

        return baseDir + File.separator + "user_" + userID + ".db";
    }

    public void addItemLocal(Item item) {
        dbWorker.submitTask(() -> {
        String sql = "INSERT INTO items (id, userID, name, description, imagedata, price, currency, date, sold, uploaddate, priority, " +
                "reservation_buyer, reservation_place, reservation_date, reservation_reserved, reservation_hour, reservation_minute, supabaseSync, toDelete) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            pstmt.setInt(9, item.getSold() ? 1:0);
            pstmt.setDate(10, item.getUploadDate() != null ? java.sql.Date.valueOf(item.getUploadDate()) : null);
            pstmt.setInt(11, item.getPriority());
            pstmt.setString(12, item.getReservation().getBuyer());
            pstmt.setString(13, item.getReservation().getPlace());
            pstmt.setDate(14, item.getReservationDate() != null ? java.sql.Date.valueOf(item.getReservationDate()) : null);
            pstmt.setInt(15, item.getReservation().getReserved()  ? 1:0);
            pstmt.setInt(16, item.getReservation().getHour());
            pstmt.setInt(17, item.getReservation().getMinute());
            pstmt.setInt(18, item.getSupabaseSync() ? 1:0);
            pstmt.setInt(19, item.getToDelete() ? 1:0);
            pstmt.executeUpdate();
            System.out.println("✅ Item added to local database!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding item to local database: " + e.getMessage());
        }
        });
    }


    public void updateItemLocal(Item item) {
        dbWorker.submitTask(() -> {
            String sql = "UPDATE items SET name = ?, description = ?, imagedata = ?, price = ?, currency = ?, date = ?, sold = ?, uploaddate = ?, priority = ?, " +
                    "reservation_buyer = ?, reservation_place = ?, reservation_date = ?, reservation_reserved = ?, reservation_hour = ?, reservation_minute = ?, supabaseSync = ?, toDelete = ? " +
                    "WHERE id = ? AND userID = ?";  // Ensure we update the correct item

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, item.getName());
                pstmt.setString(2, item.getDescription());
                pstmt.setBytes(3, item.getImageData());
                pstmt.setInt(4, item.getPrice());
                pstmt.setString(5, item.getCurrency());
                pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(item.getDate()));
                pstmt.setInt(7, item.getSold() ? 1 : 0);
                pstmt.setDate(8, item.getUploadDate() != null ? java.sql.Date.valueOf(item.getUploadDate()) : null);
                pstmt.setInt(9, item.getPriority());

                // Reservation details
                pstmt.setString(10, item.getReservation().getBuyer());
                pstmt.setString(11, item.getReservation().getPlace());
                pstmt.setDate(12, item.getReservationDate() != null ? java.sql.Date.valueOf(item.getReservationDate()) : null);
                pstmt.setInt(13, item.getReservation().getReserved() ? 1 : 0);
                pstmt.setInt(14, item.getReservation().getHour());
                pstmt.setInt(15, item.getReservation().getMinute());

                // Sync status
                pstmt.setInt(16, item.getSupabaseSync() ? 1 : 0);

                // Delete status
                pstmt.setInt(17, item.getToDelete() ? 1 : 0);

                // Identify which item to update
                pstmt.setString(18, item.getId());
                pstmt.setString(19, item.getUserID());

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("✅ Item updated in local database: " + item.getId() + " | supabaseSync: " + item.getSupabaseSync());
                } else {
                    System.out.println("⚠️ No item found with ID: " + item.getId() + " | Update failed.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Error updating item in local database: " + e.getMessage());
            }
        });
    }


    public void getAllLocalItems(String userID, Consumer<List<Item>> callback) {
        dbWorker.submitTask(() -> {
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
                    item.setSupabaseSync(rs.getInt("supabaseSync") == 1);
                    item.setToDelete(rs.getInt("toDelete") == 1);
                    items.add(item);
                }

                System.out.println("✅ Retrieved " + items.size() + " items from local database.");

            } catch (SQLException e) {
                System.out.println("❌ Error fetching items for user: " + userID + " - " + e.getMessage());
            }

            // Execute callback on JavaFX thread
            javafx.application.Platform.runLater(() -> callback.accept(items));
        });
    }



    public void deleteItem(String itemId) {
        dbWorker.submitTask(() -> {
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
        }});
    }

    public void getItemByName(String name, Consumer<Item> callback) {
        dbWorker.submitTask(() -> {
            String sql = "SELECT * FROM items WHERE name = ? AND userID = ?";
            String userID = getUserID();

            if (userID == null) {
                System.out.println("⚠️ No user ID available. Cannot fetch item.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, userID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
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
                    item.setSupabaseSync(rs.getInt("supabaseSync") == 1);
                    item.setToDelete(rs.getInt("toDelete") == 1);

                    javafx.application.Platform.runLater(() -> callback.accept(item));
                } else {
                    System.out.println("❌ No item found with name: " + name);
                    javafx.application.Platform.runLater(() -> callback.accept(null));
                }

            } catch (SQLException e) {
                System.out.println("❌ Error fetching item by name: " + e.getMessage());
                javafx.application.Platform.runLater(() -> callback.accept(null));
            }
        });
    }


    public void syncFailedItems() {
        String sql = "SELECT * FROM items WHERE supabaseSync = 0";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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

                // Convert SQLite INTEGER to Java boolean
                item.setSupabaseSync(false);

                // 🔄 Attempt to sync with Supabase
                boolean success = supabaseDB.addItem(item.getUserID(), item);

                if (success) {
                    System.out.println("✅ Successfully re-synced item: " + item.getId());

                    // ✅ If successful, update local database
                    item.setSupabaseSync(true);
                    updateItemLocal(item);
                } else {
                    System.out.println("⚠️ Failed to sync item: " + item.getId());
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Error syncing failed items: " + e.getMessage());
        }
    }

    public Boolean getDbReady() {
        return dbReady;
    }

    public void setDbReady(Boolean dbReady) {
        this.dbReady = dbReady;
    }

    public void deleteToDeleteItems() {
        dbWorker.submitTask(() -> {
            String sql = "DELETE FROM items WHERE toDelete = 1";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                int rowsDeleted = pstmt.executeUpdate();
                System.out.println("✅ Deleted " + rowsDeleted + " items marked for deletion.");

            } catch (SQLException e) {
                System.out.println("❌ Error deleting items marked for deletion: " + e.getMessage());
            }
        });
    }


    public void addGroup(Group group) {
        dbWorker.submitTask(() -> {
            String sql = "INSERT INTO groups (id, userID, name, interval, startHour, startMinute, endHour, endMinute, last_uploaded) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, group.getId());
                pstmt.setString(2, group.getUserID());
                pstmt.setString(3, group.getName());
                pstmt.setInt(4, group.getInterval());
                pstmt.setInt(5, group.getStartHour());
                pstmt.setInt(6, group.getStartMinute());
                pstmt.setInt(7, group.getEndHour());
                pstmt.setInt(8, group.getEndMinute());
                pstmt.setTimestamp(9, group.getLastUpload() != null ? Timestamp.valueOf(group.getLastUpload()) : null);

                pstmt.executeUpdate();
                System.out.println("✅ Group added to local database!");
            } catch (SQLException e) {
                System.out.println("❌ Error adding group to local database: " + e.getMessage());
            }
        });
    }

    public void updateGroup(Group group) {
        dbWorker.submitTask(() -> {
            String sql = "UPDATE groups SET name = ?, interval = ?, startHour = ?, startMinute = ?, endHour = ?, endMinute = ? , last_uploaded = ? " +
                    "WHERE id = ? AND userID = ?";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, group.getName());
                pstmt.setInt(2, group.getInterval());
                pstmt.setInt(3, group.getStartHour());
                pstmt.setInt(4, group.getStartMinute());
                pstmt.setInt(5, group.getEndHour());
                pstmt.setInt(6, group.getEndMinute());
                pstmt.setTimestamp(7, group.getLastUpload() != null ? Timestamp.valueOf(group.getLastUpload()) : null);
                pstmt.setString(8, group.getId());
                pstmt.setString(9, group.getUserID());

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Group updated in local database!");
                } else {
                    System.out.println("⚠️ No group found with ID: " + group.getId());
                }
            } catch (SQLException e) {
                System.out.println("❌ Error updating group in local database: " + e.getMessage());
            }
        });
    }

    public void getAllGroups(String userID, Consumer<List<Group>> callback) {
        dbWorker.submitTask(() -> {
            List<Group> groups = new ArrayList<>();
            String sql = "SELECT * FROM groups WHERE userID = ?";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, userID);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Group group = new Group(
                            rs.getString("name"),
                            rs.getInt("interval"),
                            rs.getInt("startHour"),
                            rs.getInt("startMinute"),
                            rs.getInt("endHour"),
                            rs.getInt("endMinute")
                    );
                    group.setId(rs.getString("id"));
                    Timestamp ts = rs.getTimestamp("last_uploaded");
                    if (ts != null) {
                        group.setLastUpload(ts.toLocalDateTime());
                    }
                    groups.add(group);

                }
                System.out.println("✅ Retrieved " + groups.size() + " groups from local database.");
            } catch (SQLException e) {
                System.out.println("❌ Error fetching groups for user: " + userID + " - " + e.getMessage());
            }
            javafx.application.Platform.runLater(() -> callback.accept(groups));
        });
    }


    public Group getGroupByName(String name) {
        String sql = "SELECT * FROM groups WHERE name = ? AND userID = ?";
        String userID = getUserID();  // Retrieve current user's ID

        if (userID == null) {
            System.out.println("⚠️ No userID available. Cannot fetch group.");
            return null;
        }

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Group group = new Group(
                        rs.getString("name"),
                        rs.getInt("interval"),
                        rs.getInt("startHour"),
                        rs.getInt("startMinute"),
                        rs.getInt("endHour"),
                        rs.getInt("endMinute")
                );
                group.setId(rs.getString("id"));
                Timestamp lastUploadedts = rs.getTimestamp("last_uploaded");
                if (lastUploadedts != null) {
                    group.setLastUpload(lastUploadedts.toLocalDateTime());
                }
                return group;
            } else {
                System.out.println("⚠️ No group found with name: " + name);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching group: " + e.getMessage());
        }

        return null;
    }



    public void deleteGroup(String groupId) {
        dbWorker.submitTask(() -> {
            String sql = "DELETE FROM groups WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, groupId);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Group deleted from local database.");
                } else {
                    System.out.println("⚠️ No group found with ID: " + groupId + " (nothing deleted).");
                }

            } catch (SQLException e) {
                System.out.println("❌ Error deleting group from local database: " + e.getMessage());
            }
        });
    }


    public void linkItemToGroups(String itemID, List<String> groupIDs) {
        dbWorker.submitTask(() -> {
            String sql = "INSERT OR IGNORE INTO item_groups (itemID, groupID, last_uploaded) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (String groupID : groupIDs) {
                    pstmt.setString(1, itemID);
                    pstmt.setString(2, groupID);
                    pstmt.setTimestamp(3, null);
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                System.out.println("✅ Linked item to selected groups.");
            } catch (SQLException e) {
                System.out.println("❌ Error linking item to groups: " + e.getMessage());
            }
        });
    }

    public List<String> getGroupIDsForItem(String itemID) {
        List<String> groupIDs = new ArrayList<>();
        String sql = "SELECT groupID FROM item_groups WHERE itemID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                groupIDs.add(rs.getString("groupID"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching group IDs for item: " + e.getMessage());
        }

        return groupIDs;
    }

    public void updateItemGroupLinks(String itemID, List<String> newGroupIDs) {
        dbWorker.submitTask(() -> {
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                // 1. Delete existing links
                String deleteSQL = "DELETE FROM item_groups WHERE itemID = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setString(1, itemID);
                    deleteStmt.executeUpdate();
                }




                // 2. Insert new ones
                String insertSQL = "INSERT INTO item_groups (itemID, groupID, last_uploaded) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (String groupID : newGroupIDs) {
                        insertStmt.setString(1, itemID);
                        insertStmt.setString(2, groupID);
                        insertStmt.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                System.out.println("✅ Item-group links updated for item: " + itemID);
            } catch (SQLException e) {
                System.out.println("❌ Failed to update item-group links: " + e.getMessage());
            }
        });
    }



    public List<Group> getGroupsForItem(String itemId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g " +
                "JOIN item_groups ig ON g.id = ig.groupID " +
                "WHERE ig.itemID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Group group = new Group(
                        rs.getString("name"),
                        rs.getInt("interval"),
                        rs.getInt("startHour"),
                        rs.getInt("startMinute"),
                        rs.getInt("endHour"),
                        rs.getInt("endMinute")
                );
                group.setId(rs.getString("id"));
                groups.add(group);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching groups for item: " + e.getMessage());
        }

        return groups;
    }

    public java.time.LocalDateTime getLastUploadTime(String itemId, String groupId) {
        String sql = "SELECT last_uploaded FROM item_groups WHERE itemID = ? AND groupID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            pstmt.setString(2, groupId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("last_uploaded");
                return (ts != null) ? ts.toLocalDateTime() : null;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting last_uploaded time: " + e.getMessage());
        }

        return null;
    }

    public void updateItemGroupLastUploaded(String itemId, String groupId, java.time.LocalDateTime time) {
        dbWorker.submitTask(() -> {
            String sql = "UPDATE item_groups SET last_uploaded = ? WHERE itemID = ? AND groupID = ?";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setTimestamp(1, java.sql.Timestamp.valueOf(time));
                pstmt.setString(2, itemId);
                pstmt.setString(3, groupId);
                pstmt.executeUpdate();

                System.out.println("✅ Updated last_uploaded for itemID " + itemId + " and groupID " + groupId);
            } catch (SQLException e) {
                System.out.println("❌ Failed to update last_uploaded: " + e.getMessage());
            }
        });
    }

    public List<Item> getEligibleItems() {
        List<Item> eligibleItems = new ArrayList<>();
        String userID = getUserID();

        if (userID == null) {
            System.out.println("⚠️ No user ID available. Cannot fetch eligible items.");
            return eligibleItems;
        }

        String sql = "SELECT * FROM items WHERE userID = ? AND sold = 0 AND reservation_reserved = 0 AND toDelete = 0";

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
                item.setSold(false);  // already filtered
                item.setSupabaseSync(rs.getInt("supabaseSync") == 1);
                item.setToDelete(false);
                eligibleItems.add(item);
            }

            System.out.println("✅ Retrieved " + eligibleItems.size() + " eligible items.");
        } catch (SQLException e) {
            System.out.println("❌ Error fetching eligible items: " + e.getMessage());
        }

        return eligibleItems;
    }
}