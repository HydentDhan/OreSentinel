package me.dhanraj.oresentinel;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final OreSentinel plugin;
    private Connection connection;

    public DatabaseManager(OreSentinel plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            File dbFile = new File(dataFolder, "mining_history.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database: " + e.getMessage());
        }
    }

    private void createTables() {
        if (connection == null) return;
        String sql = "CREATE TABLE IF NOT EXISTS mining_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "ore_name TEXT NOT NULL," +
                "y_level INTEGER DEFAULT 0," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables: " + e.getMessage());
        }
    }

    public void logMining(UUID uuid, String playerName, String oreName, int yLevel) {
        if (connection == null) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO mining_logs(uuid, player_name, ore_name, y_level) VALUES(?,?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, playerName);
                pstmt.setString(3, oreName);
                pstmt.setInt(4, yLevel);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log mining activity: " + e.getMessage());
            }
        });
    }

    public List<String> getRecentHistory(String playerName, int limit) {
        List<String> history = new ArrayList<>();
        if (connection == null) return history;

        String sql = "SELECT ore_name, y_level, timestamp FROM mining_logs WHERE player_name = ? ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(rs.getString("timestamp") + " - " + rs.getString("ore_name") + " (Y: " + rs.getInt("y_level") + ")");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to retrieve mining history: " + e.getMessage());
        }
        return history;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection: " + e.getMessage());
        }
    }
}