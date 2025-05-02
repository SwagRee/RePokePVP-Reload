package io.github.swagree.repokepvp.manager.configManager;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.Arrays;

public class StorageConfig {
    private final String storageType;
    private final MySQLConfig mysqlConfig;

    public StorageConfig(FileConfiguration config) {
        String rawType = config.getString("Storage.Type", "sqlite").toLowerCase();
        this.storageType = validateType(rawType);
        this.mysqlConfig = new MySQLConfig(config);
    }

    private String validateType(String type) {
        if (Arrays.asList("mysql", "sqlite", "yaml").contains(type)) {
            return type;
        }
        return "sqlite";
    }

    public String getStorageType() { return storageType; }
    public MySQLConfig getMySQLConfig() { return mysqlConfig; }

    public static class MySQLConfig {
        public final String host;
        public final int port;
        public final String database;
        public final String user;
        public final String password;
        public final String table;

        public MySQLConfig(FileConfiguration config) {
            this.host = config.getString("Storage.MySQL.Host", "localhost");
            this.port = config.getInt("Storage.MySQL.Port", 3306);
            this.database = config.getString("Storage.MySQL.Database", "minecraft");
            this.user = config.getString("Storage.MySQL.Username", "root");
            this.password = config.getString("Storage.MySQL.Password", "");
            this.table = config.getString("Storage.MySQL.Table", "daily_wins");
        }
    }
}