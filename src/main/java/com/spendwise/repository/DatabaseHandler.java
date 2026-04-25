package com.spendwise.repository;

import com.spendwise.model.Subscription;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class DatabaseHandler {

    private static final String DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home").replace("\\", "/") + "/spendwise.db";

    // ── Connection ────────────────────────────────────────────────────────────

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ── Initialise tables ────────────────────────────────────────────────────

    public void initializeDatabase() {
        String subscriptions = """
                CREATE TABLE IF NOT EXISTS subscriptions (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    name          TEXT    NOT NULL,
                    amount        REAL    NOT NULL,
                    currency      TEXT    DEFAULT 'INR',
                    category      TEXT,
                    renewal_date  TEXT,
                    billing_cycle TEXT
                );
                """;

        String settings = """
                CREATE TABLE IF NOT EXISTS settings (
                    key   TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
                """;

        // Add currency column if upgrading from v1 (column may not exist yet)
        String addCurrency = "ALTER TABLE subscriptions ADD COLUMN currency TEXT DEFAULT 'INR'";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(subscriptions);
            stmt.execute(settings);
            try { stmt.execute(addCurrency); } catch (SQLException ignored) {}

            // Seed default settings if absent
            insertDefaultSetting(conn, "budget_limit", "5000");
            insertDefaultSetting(conn, "home_currency", "INR");

            System.out.println("[DB] Ready – " + DB_URL);
        } catch (SQLException e) {
            System.err.println("[DB Error] init: " + e.getMessage());
        }
    }

    private void insertDefaultSetting(Connection conn, String key, String value) throws SQLException {
        String sql = "INSERT OR IGNORE INTO settings(key, value) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    // ── Subscriptions CRUD ───────────────────────────────────────────────────

    public void addSubscription(Subscription sub) {
        String sql = "INSERT INTO subscriptions(name,amount,currency,category,renewal_date,billing_cycle) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sub.getName());
            ps.setDouble(2, sub.getAmount());
            ps.setString(3, sub.getCurrency());
            ps.setString(4, sub.getCategory());
            ps.setString(5, sub.getRenewalDate());
            ps.setString(6, sub.getBillingCycle());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB Error] add: " + e.getMessage());
        }
    }

    public void deleteSubscription(int id) {
        String sql = "DELETE FROM subscriptions WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB Error] delete: " + e.getMessage());
        }
    }

    public List<Subscription> getAllSubscriptions() {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions ORDER BY renewal_date ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Subscription(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("currency"),
                        rs.getString("category"),
                        rs.getString("renewal_date"),
                        rs.getString("billing_cycle")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] getAll: " + e.getMessage());
        }
        return list;
    }

    // ── Settings CRUD ────────────────────────────────────────────────────────

    public String getSetting(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] getSetting: " + e.getMessage());
        }
        return null;
    }

    public void saveSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO settings(key, value) VALUES(?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB Error] saveSetting: " + e.getMessage());
        }
    }

    public Map<String, String> getAllSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT key, value FROM settings";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("key"), rs.getString("value"));
        } catch (SQLException e) {
            System.err.println("[DB Error] getAllSettings: " + e.getMessage());
        }
        return map;
    }
}
