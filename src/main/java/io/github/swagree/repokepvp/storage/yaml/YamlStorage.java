package io.github.swagree.repokepvp.storage.yaml;

import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class YamlStorage implements Storage {
    private final YamlConfiguration config;
    private final File file;
    private static final int DEFAULT_SCORE = 100;

    public YamlStorage(File dataFolder) {
        this.file = new File(dataFolder, "player_data.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("无法创建存储文件", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public boolean checkFirstWin(UUID uuid, LocalDate today) {
        ConfigurationSection playerData = getPlayerSection(uuid);
        String lastDate = playerData.getString("last_win_date");
        return lastDate == null || !lastDate.equals(today.toString());
    }

    @Override
    public void updateWinRecord(UUID uuid, LocalDate today, Integer totalMatch, Integer wins) {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        playerData.set("last_win_date", today.toString());
        playerData.set("player_name", Bukkit.getOfflinePlayer(uuid).getName());
        playerData.set("total_match", totalMatch);
        playerData.set("wins", wins);
        saveConfig();
    }

    @Override
    public void addScore(UUID uuid, int points) {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        playerData.set("score", playerData.getInt("score") + points);
        saveConfig();
    }

    @Override
    public void reduceScore(UUID uuid, int points) {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        int currentScore = playerData.getInt("score");
        playerData.set("score", Math.max(currentScore - points, 0));
        saveConfig();
    }

    @Override
    public void addTotalMatch(UUID uuid) {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        playerData.set("total_match", playerData.getInt("total_match") + 1);
        saveConfig();
    }

    @Override
    public void addWins(UUID uuid) {
        ConfigurationSection playerData = getOrCreatePlayerSection(uuid);
        playerData.set("wins", playerData.getInt("wins") + 1);
        saveConfig();
    }

    @Override
    public int getScore(UUID uuid) {
        return getPlayerSection(uuid).getInt("score");
    }

    @Override
    public int getWins(UUID uuid) {
        return getPlayerSection(uuid).getInt("wins");
    }

    @Override
    public int getTotalMatch(UUID uuid) {
        return getPlayerSection(uuid).getInt("total_match");
    }

    @Override
    public List<PlayerScore> getTopPlayers(int limit) {
        List<PlayerScore> scores = new ArrayList<>();
        for (String uuidStr : config.getKeys(false)) {
            ConfigurationSection playerData = config.getConfigurationSection(uuidStr);
            if (playerData != null) {
                scores.add(new PlayerScore(
                        UUID.fromString(uuidStr),
                        playerData.getString("player_name"),
                        playerData.getInt("score")
                ));
            }
        }
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return scores.subList(0, Math.min(limit, scores.size()));
    }

    @Override
    public void close() {
        saveConfig();
    }

    private ConfigurationSection getOrCreatePlayerSection(UUID uuid) {
        String uuidStr = uuid.toString();
        ConfigurationSection section = config.getConfigurationSection(uuidStr);
        if (section == null) {
            section = config.createSection(uuidStr);
            section.set("player_name", Bukkit.getOfflinePlayer(uuid).getName());
            section.set("score", DEFAULT_SCORE);
            section.set("last_win_date", "1970-01-01");
            section.set("wins", 0);
            section.set("total_match", 0);
        }
        return section;
    }

    private ConfigurationSection getPlayerSection(UUID uuid) {
        ConfigurationSection section = config.getConfigurationSection(uuid.toString());
        return section != null ? section : getOrCreatePlayerSection(uuid);
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException("保存数据文件失败", e);
        }
    }
}