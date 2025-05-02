package io.github.swagree.repokepvp.manager.dataManager;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.entity.PlayerScore;
import io.github.swagree.repokepvp.manager.rewardManager.RewardExecutor;
import io.github.swagree.repokepvp.manager.configManager.StorageConfig;
import io.github.swagree.repokepvp.storage.mysql.MySQLStorage;
import io.github.swagree.repokepvp.storage.sqlite.SQLiteStorage;
import io.github.swagree.repokepvp.storage.Storage;
import io.github.swagree.repokepvp.storage.yaml.YamlStorage;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BattleManager {
    private final Main plugin;
    private final Storage storage;
    private final List<String> rewardCommands;

    public BattleManager(Main plugin){
        this.plugin = plugin;
        StorageConfig config = new StorageConfig(plugin.getConfig());
        this.storage = initStorage(config);
        this.rewardCommands = plugin.getConfig().getStringList("DailyFirstWinCommand");
    }

    private Storage initStorage(StorageConfig config){
        switch (config.getStorageType()) {
            case "mysql":
                StorageConfig.MySQLConfig mysql = config.getMySQLConfig();
                return new MySQLStorage(
                        mysql.host, mysql.port, mysql.database,
                        mysql.user, mysql.password, mysql.table
                );
            case "yaml":
                return new YamlStorage(plugin.getDataFolder());
            default:
                return new SQLiteStorage(plugin.getDataFolder());
        }
    }
    public void handleBattleEnd(Player player) {
        try {
            UUID uuid = player.getUniqueId();
            storage.addTotalMatch(uuid);
            LocalDate today = LocalDate.now();;
            storage.updateWinRecord(uuid, today,storage.getTotalMatch(uuid),storage.getWins(uuid));
        } catch (Exception e) {
            plugin.getLogger().severe("保存数据失败: " + e.getMessage());
        }
    }

    public void handleVictory(Player player) {
        try {
            if (isFirstWin(player)) {
                RewardExecutor.executeCommands(player, rewardCommands);
                plugin.getLogger().info(player.getName() + " 获得今日首胜");
            }
            storage.addScore(player.getUniqueId(),10);
            storage.addWins(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().severe("首胜处理失败: " + e.getMessage());
        }
    }

    public void handleDefeat(Player player) {
        reduceScore(player.getUniqueId(),10);
    }

    public void reduceScore(UUID uuid,Integer i) {
        try {
            storage.reduceScore(uuid,i);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<PlayerScore> getTop() throws SQLException {
        return storage.getTopPlayers(10);
    }

    public Integer getWins(UUID uuid) throws SQLException {
        return storage.getWins(uuid);
    }

    public Integer getScore(UUID uuid) throws SQLException {
        return storage.getScore(uuid);
    }


    public Integer getTotalMatch(UUID uuid) throws SQLException {
        return storage.getTotalMatch(uuid);
    }

    private boolean isFirstWin(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        LocalDate today = LocalDate.now();
        boolean isFirst = storage.checkFirstWin(uuid, today);
        if (isFirst) {
            storage.updateWinRecord(uuid, today,storage.getTotalMatch(uuid),storage.getWins(uuid));
        }
        return isFirst;
    }

    public void close() {
        try {
            storage.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("关闭存储失败: " + e.getMessage());
        }
    }
}