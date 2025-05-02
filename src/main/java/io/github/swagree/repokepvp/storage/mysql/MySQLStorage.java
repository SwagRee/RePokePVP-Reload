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

    public MySQLStorage(String host, int port, String database, 
                       String user, String password, String table)  {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false", host, port, database);
            this.connection = DriverManager.getConnection(url, user, password);
            this.tableName = table;
            initTable();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 更新表结构添加积分字段
    private void initTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建包含所有必要字段的表
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "last_win_date DATE, " +
                            "score INT DEFAULT 0, " +     // 新增积分字段
                            "player_name VARCHAR(16)" +    // 新增玩家名称字段
                            ")";
            stmt.executeUpdate(createTableSQL);

            // 兼容旧表升级
            try {
                stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN score INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN player_name VARCHAR(16)");
            } catch (SQLException e) {
                // 忽略字段已存在的错误
            }
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

    }

    public void updateWinRecord(UUID uuid, LocalDate today) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (uuid, last_win_date, score, player_name) " +
                "VALUES (?, ?, COALESCE((SELECT score FROM " + tableName + " WHERE uuid = ?), 0), ?) " +
                "ON DUPLICATE KEY UPDATE last_win_date = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            ps.setString(1, uuid.toString());
            ps.setDate(2, Date.valueOf(today));
            ps.setString(3, uuid.toString()); // 用于 COALESCE
            ps.setString(4, playerName);
            ps.setDate(5, Date.valueOf(today)); // 更新日期
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public int getScore(UUID uuid) throws SQLException {
        String sql = "SELECT score FROM " + tableName + " WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("score");
                }
                // 没有记录时返回0
                return 0;
            }
        }
    }

    // 其他方法保持不变...

    @Override
    public void addScore(UUID uuid, int points) throws SQLException {
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        String sql = "INSERT INTO " + tableName + " (uuid, score, player_name) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "score = score + VALUES(score), " +  // 累加积分
                "player_name = VALUES(player_name)";  // 更新最新名称

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, points);
            ps.setString(3, playerName);
            ps.executeUpdate();
        }
    }

    @Override
    public List<PlayerScore> getTopPlayers(int limit) throws SQLException {
        String sql = "SELECT uuid, player_name, score FROM " + tableName + " " +
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
    public int getWins(UUID uuid) throws SQLException {
        return 0;
    }

    @Override
    public int getTotalMatch(UUID uuid) throws SQLException {
        return 0;
    }

    @Override
    public void addTotalMatch(UUID uuid) throws SQLException {

    }

    @Override
    public void addWins(UUID uuid) throws SQLException {

    }

    @Override
    public void reduceScore(UUID uuid, int points) {

    }
}