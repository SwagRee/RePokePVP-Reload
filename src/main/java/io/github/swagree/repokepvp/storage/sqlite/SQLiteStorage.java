package io.github.swagree.repokepvp.storage.sqlite;

import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.storage.Storage;
import org.bukkit.Bukkit;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteStorage implements Storage {
    private Connection connection;

    public SQLiteStorage(File dataFolder) {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(dataFolder, "daily_wins.db");

            // 新增目录创建逻辑
            File parentDir = dbFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs(); // 自动创建所有不存在的父目录
            }

            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            initTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int DEFAULT_SCORE = 100;

    private void initTable() throws SQLException {
        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS daily_wins (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "last_win_date TEXT, " +
                        "score INTEGER DEFAULT %d, " +  // 使用格式化字符串
                        "wins INTEGER DEFAULT 0, " +
                        "total_match INTEGER DEFAULT 0, " +
                        "player_name TEXT)",
                DEFAULT_SCORE);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    @Override
    public void updateWinRecord(UUID uuid, LocalDate today, Integer total_match, Integer wins) throws SQLException {
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();

        String sql = "INSERT OR REPLACE INTO daily_wins " +
                "(uuid, last_win_date, score, player_name, total_match, wins) " +
                "VALUES (?, ?, " +
                "COALESCE((SELECT score FROM daily_wins WHERE uuid = ?), 100), " + // 修改此处，将0改为100
                "?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, today.toString());
            ps.setString(3, uuid.toString());
            ps.setString(4, playerName);
            ps.setInt(5, total_match);
            ps.setInt(6, wins);

            ps.executeUpdate();
        }
    }

    // 确保其他方法字段匹配
    @Override
    public void addScore(UUID uuid, int points) throws SQLException {
        String sql = "UPDATE daily_wins SET score = score + ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }
    // 确保其他方法字段匹配
    @Override
    public void reduceScore(UUID uuid, int points) throws SQLException {
        try {
            int currentScore = getScore(uuid);
            if (currentScore == 0) {
                return;
            }
            // 移除检查DEFAULT_SCORE的条件
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 确保记录存在，若不存在则插入默认记录
        String insertSql = "INSERT OR IGNORE INTO daily_wins (uuid, score, player_name) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, DEFAULT_SCORE);
            ps.setString(3, Bukkit.getOfflinePlayer(uuid).getName());
            ps.executeUpdate();
        }

        String sql = "UPDATE daily_wins SET score = score - ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

@Override
    public void addTotalMatch(UUID uuid) throws SQLException {
        String sql = "UPDATE daily_wins SET total_match = total_match + 1 WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }
@Override
    public void addWins(UUID uuid) throws SQLException {
        String sql = "UPDATE daily_wins SET wins = wins + 1 WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }


        @Override
    public boolean checkFirstWin(UUID uuid, LocalDate today) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT last_win_date FROM daily_wins WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return !rs.next() || !rs.getString(1).equals(today.toString());
        }
    }


    @Override
    public int getScore(UUID uuid) throws SQLException {
        String sql = "SELECT score FROM daily_wins WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("score");
                }
                // 如果找不到记录返回0
                return 0;
            }
        }
    }
    @Override
    public int getWins(UUID uuid) throws SQLException {
        String sql = "SELECT wins FROM daily_wins WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("wins");
                }
                // 如果找不到记录返回0
                return 0;
            }
        }
    }
    @Override
    public int getTotalMatch(UUID uuid) throws SQLException {
        String sql = "SELECT total_match FROM daily_wins WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_match");
                }
                // 如果找不到记录返回0
                return 0;
            }
        }
    }

    @Override
    public List<PlayerScore> getTopPlayers(int limit) throws SQLException {
        String sql = "SELECT uuid, player_name, score FROM daily_wins " +
                "ORDER BY score DESC LIMIT ?";

        List<PlayerScore> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PlayerScore(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name"),
                            rs.getInt("score")
                    ));
                }
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
