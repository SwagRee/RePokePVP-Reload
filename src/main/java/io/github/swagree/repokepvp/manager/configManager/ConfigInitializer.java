package io.github.swagree.repokepvp.manager.configManager;

import io.github.swagree.repokepvp.Main;

import java.io.File;

public class ConfigInitializer {
    private final Main plugin;

    public ConfigInitializer(Main plugin) {
        this.plugin = plugin;
    }

    public void setupDefaultConfigs() {
        // 主配置
        plugin.saveDefaultConfig();

        // 战斗配置
        saveIfMissing("pvplist/default.yml");

        // 数据存储配置
        if (isYamlStorage()) {
            saveIfMissing("daily_wins.yml");
        }
    }

    private void saveIfMissing(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    private boolean isYamlStorage() {
        return plugin.getConfig().getString("Storage.Type", "yaml")
                .equalsIgnoreCase("yaml");
    }
}