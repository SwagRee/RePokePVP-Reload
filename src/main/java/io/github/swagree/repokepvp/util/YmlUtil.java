package io.github.swagree.repokepvp.util;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class YmlUtil {
    // 私有化缓存 + final 确保不可变引用
    private static final Map<String, FileConfiguration> CONFIG_CACHE = new ConcurrentHashMap<>();

    // 私有构造方法防止实例化
    private YmlUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 根据路径获取配置（自动缓存）
     * @param path 配置文件名（无需.yml后缀）
     * @return 永远不会返回 null，若加载失败会抛出明确异常
     */
    public static FileConfiguration getConfig(String path) {
        return CONFIG_CACHE.computeIfAbsent(path, YmlUtil::loadConfiguration);
    }

    /**
     * 清理指定缓存（插件重载时使用）
     * @param path 配置文件名
     */
    public static void clearCache(String path) {
        CONFIG_CACHE.remove(path);
    }

    /**
     * 清理所有缓存（插件禁用时调用）
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

        // 可选的自动重载监听（根据需求打开注释）
        /*
        watchForChanges(configFile, path);
        */

        return config;
    }

    /**
     * （可选）监听文件变化自动重载配置（根据需求决定是否启用）
     */
    /*
    private static void watchForChanges(File configFile, String path) {
        final Main plugin = Main.INSTANCE;
        final long interval = 20 * 5; // 每5秒检查一次

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            final long lastModified = configFile.lastModified();
            final long cachedModified = CONFIG_CACHE.get(path) instanceof YamlConfiguration
                ? ((YamlConfiguration) CONFIG_CACHE.get(path)).getDefaults().hashCode()
                : 0;

            if (lastModified > cachedModified) {
                plugin.getLogger().info("检测到配置文件变化，重载: " + path);
                CONFIG_CACHE.put(path, YamlConfiguration.loadConfiguration(configFile));
            }
        }, interval, interval);
    }
    */
}