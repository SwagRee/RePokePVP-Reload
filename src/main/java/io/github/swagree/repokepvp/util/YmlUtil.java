package io.github.swagree.repokepvp.util;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class YmlUtil {

    private static final Map<String, FileConfiguration> CONFIG_CACHE = new ConcurrentHashMap<>();


    private YmlUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 根据路径获取配置（自动缓存）
     * @param path 配置文件名
     */
    public static FileConfiguration getConfig(String path) {
        return CONFIG_CACHE.computeIfAbsent(path, YmlUtil::loadConfiguration);
    }

    /**
     * 清理指定缓存
     * @param path 配置文件名
     */
    public static void clearCache(String path) {
        CONFIG_CACHE.remove(path);
    }

    /**
     * 清理所有缓存
     */
    public static void clearAllCache() {
        CONFIG_CACHE.clear();
    }

    private static FileConfiguration loadConfiguration(String path) {
        final Main plugin = Main.getInstance();
        final File configFile = new File(plugin.getDataFolder(), path + ".yml");

        // 第一次加载时自动保存默认配置
        if (!configFile.exists()) {
            try {
                plugin.saveResource(path + ".yml", false);
                plugin.getLogger().info("已生成默认配置文件: " + path);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("配置文件不存在且无法生成: " + path);
                throw new RuntimeException("Missing config file: " + path, e);
            }
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);


        return config;
    }


}