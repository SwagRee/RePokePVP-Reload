package io.github.swagree.repokepvp.storage.yaml;

import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class YamlStorage implements Storage {
    private final YamlConfiguration config;
    private final File file;

    public YamlStorage(File dataFolder) {
        this.file = new File(dataFolder, "player_data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("无法创建存储文件", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public boolean checkFirstWin(UUID uuid, LocalDate today) {
        ConfigurationSection playerData = config.getConfigurationSection(uuid.toString());
        if (playerData == null) return true;
        String lastDate = playerData.getString("last_win_date");
        return lastDate == null || !lastDate.equals(today.toString());
    }

    @Override
    public void updateWinRecord(UUID uuid, LocalDate today, Integer totalMatch, Integer wins) throws SQLException {

    }

    public void updateWinRecord(UUID uuid, LocalDate today) {
        ConfigurationSection playerData = config.createSection(uuid.toString());
        playerData.set("last_win_date", today.toString());
        playerData.set("player_name", Bukkit.getOfflinePlayer(uuid).getName());

        // 初始化积分字段（如果不存在）
        if (!playerData.contains("score")) {
            playerData.set("score", 0);
        }

        saveConfig();
    }

    @Override
    public void addScore(UUID uuid, int points)  {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        int currentScore = playerData.getInt("score", 0);
        playerData.set("score", currentScore + points);
        saveConfig();
    }

    @Override
    public int getScore(UUID uuid) {
        ConfigurationSection playerData = config.getConfigurationSection(uuid.toString());
        return playerData != null ? playerData.getInt("score", 0) : 0;
    }

    @Override
    public List<PlayerScore> getTopPlayers(int limit) {
        List<PlayerScore> scores = new ArrayList<>();

        // 遍历所有玩家数据
        for (String uuidStr : config.getKeys(false)) {
            ConfigurationSection playerData = config.getConfigurationSection(uuidStr);
            if (playerData != null) {
                UUID uuid = UUID.fromString(uuidStr);
                scores.add(new PlayerScore(
                        uuid,
                        playerData.getString("player_name", "未知玩家"),
                        playerData.getInt("score", 0)
                ));
            }
        }

        // 排序并限制数量
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return scores.subList(0, Math.min(limit, scores.size()));
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

    private ConfigurationSection getOrCreatePlayerSection(UUID uuid) {
        String uuidStr = uuid.toString();
        ConfigurationSection section = config.getConfigurationSection(uuidStr);
        if (section == null) {
            section = config.createSection(uuidStr);
            section.set("player_name", Bukkit.getOfflinePlayer(uuid).getName());
            section.set("score", 0);
            section.set("last_win_date", "1970-01-01");
        }
        return section;
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException("保存数据文件失败", e);
        }
    }

    @Override
    public void close() {
        saveConfig();
    }
}