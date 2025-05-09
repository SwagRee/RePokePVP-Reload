package io.github.swagree.repokepvp.storage.mysql;

import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.storage.Storage;
import org.bukkit.Bukkit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLStorage implements Storage {
    private Connection connection;
    private String tableName;
    private static final int DEFAULT_SCORE = 100;

    public MySQLStorage(String host, int port, String database,
                        String user, String password, String table) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false", host, port, database);
            this.connection = DriverManager.getConnection(url, user, password);
            this.tableName = table;
            initTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "last_win_date DATE, " +
                            "score INT DEFAULT " + DEFAULT_SCORE + ", " +
                            "wins INT DEFAULT 0, " +
                            "total_match INT DEFAULT 0, " +
                            "player_name VARCHAR(16)" +
                            ")";
            stmt.executeUpdate(createTableSQL);

            // 添加可能缺失的列
            addColumnIfNotExists("score", "INT DEFAULT " + DEFAULT_SCORE);
            addColumnIfNotExists("wins", "INT DEFAULT 0");
            addColumnIfNotExists("total_match", "INT DEFAULT 0");
            addColumnIfNotExists("player_name", "VARCHAR(16)");
        }
    }

    private void addColumnIfNotExists(String columnName, String columnDef) {
        try {
            connection.createStatement().executeUpdate(
                    "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef
            );
        } catch (SQLException e) {
            // 忽略重复列错误
        }
    }

    @Override
    public boolean checkFirstWin(UUID uuid, LocalDate today) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT last_win_date FROM " + tableName + " WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return !rs.next() || !rs.getDate(1).toLocalDate().equals(today);
        }
    }

    @Override
    public void updateWinRecord(UUID uuid, LocalDate today, Integer totalMatch, Integer wins) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (uuid, last_win_date, score, player_name, total_match, wins) " +
                "VALUES (?, ?, COALESCE((SELECT score FROM " + tableName + " WHERE uuid = ?), ?), ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "last_win_date = VALUES(last_win_date), " +
                "score = VALUES(score), " +
                "player_name = VALUES(player_name), " +
                "total_match = VALUES(total_match), " +
                "wins = VALUES(wins)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            ps.setString(1, uuid.toString());
            ps.setDate(2, Date.valueOf(today));
            ps.setString(3, uuid.toString());
            ps.setInt(4, DEFAULT_SCORE);
            ps.setString(5, playerName);
            ps.setInt(6, totalMatch);
            ps.setInt(7, wins);
            ps.executeUpdate();
        }
    }

    @Override
    public void addScore(UUID uuid, int points) throws SQLException {
        String updateSql = "UPDATE " + tableName + " SET score = score + ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setInt(1, points);
            ps.setString(2, uuid.toString());
            if (ps.executeUpdate() == 0) {
                insertDefaultRecord(uuid);
            }
        }
    }

    @Override
    public void reduceScore(UUID uuid, int points) throws SQLException {
        try {
            int currentScore = getScore(uuid);
            if (currentScore == 0) return;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String updateSql = "UPDATE " + tableName + " SET score = GREATEST(score - ?, 0) WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setInt(1, points);
            ps.setString(2, uuid.toString());
            if (ps.executeUpdate() == 0) {
                insertDefaultRecord(uuid);
            }
        }
    }

    private void insertDefaultRecord(UUID uuid) throws SQLException {
        String sql = "INSERT IGNORE INTO " + tableName + " (uuid, score, player_name) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, DEFAULT_SCORE);
            ps.setString(3, Bukkit.getOfflinePlayer(uuid).getName());
            ps.executeUpdate();
        }
    }

    @Override
    public void addTotalMatch(UUID uuid) throws SQLException {
        String sql = "UPDATE " + tableName + " SET total_match = total_match + 1 WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public void addWins(UUID uuid) throws SQLException {
        String sql = "UPDATE " + tableName + " SET wins = wins + 1 WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public int getScore(UUID uuid) throws SQLException {
        String sql = "SELECT score FROM " + tableName + " WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("score") : 0;
        }
    }

    @Override
    public int getWins(UUID uuid) throws SQLException {
        String sql = "SELECT wins FROM " + tableName + " WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("wins") : 0;
        }
    }

    @Override
    public int getTotalMatch(UUID uuid) throws SQLException {
        String sql = "SELECT total_match FROM " + tableName + " WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("total_match") : 0;
        }
    }

    @Override
    public List<PlayerScore> getTopPlayers(int limit) throws SQLException {
        List<PlayerScore> result = new ArrayList<>();
        String sql = "SELECT uuid, player_name, score FROM " + tableName + " ORDER BY score DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new PlayerScore(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getInt("score")
                ));
            }
        }
        return result;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}