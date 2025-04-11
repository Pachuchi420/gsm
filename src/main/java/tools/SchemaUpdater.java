package tools;

import java.sql.*;
import java.util.*;

public class SchemaUpdater {

    private final String dbUrl;

    public SchemaUpdater(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void ensureSchemaCompatibility(Map<String, Map<String, String>> expectedSchema) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            DatabaseMetaData meta = conn.getMetaData();

            for (String table : expectedSchema.keySet()) {
                if (!tableExists(conn, table)) {
                    System.out.println("⚠️ Table not found: " + table + ". Skipping column check.");
                    continue;
                }

                Set<String> existingColumns = getTableColumns(meta, table);
                Map<String, String> requiredColumns = expectedSchema.get(table);

                for (Map.Entry<String, String> entry : requiredColumns.entrySet()) {
                    String column = entry.getKey();
                    String type = entry.getValue();

                    if (!existingColumns.contains(column)) {
                        try (Statement stmt = conn.createStatement()) {
                            String alterSql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + ";";
                            stmt.execute(alterSql);
                            System.out.println("✅ Added missing column: " + column + " to table: " + table);
                        } catch (SQLException e) {
                            System.out.println("❌ Error adding column " + column + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private Set<String> getTableColumns(DatabaseMetaData meta, String table) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, table, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }
}
