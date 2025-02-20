package io.github.swagree.repokepvp;


import io.github.swagree.repokepvp.command.PlayerCommand;
import io.github.swagree.repokepvp.listener.PokemonEventListener;
import io.github.swagree.repokepvp.manager.dataManager.BattleManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    public static Main INSTANCE;
    public static BattleManager dailyWinManager;

    @Override
    public void onEnable() {

        INSTANCE = this;

        this.getCommand("rpp").setExecutor(new PlayerCommand());

        Bukkit.getPluginManager().registerEvents(new PokemonEventListener(), this);

        dailyWinManager = new BattleManager(INSTANCE);

        Bukkit.getConsoleSender().sendMessage("§7[RePokePVP] §b作者§fSwagRee §cQQ:§f352208610");

        saveDefaultConfigs();

        reloadConfig();


    }
    @Override
    public void onDisable() {
        if (dailyWinManager != null) {
            dailyWinManager.close();
        }
    }
    // 新增方法：保存默认配置文件
    private void saveDefaultConfigs () {
        // 主配置文件
        saveDefaultConfig();

        File defaultFile = new File(getDataFolder(), "pvplist/default.yml");

        if (!defaultFile.exists()) {
            saveResource("pvplist/default.yml", false);
        }

        if(!getConfig().getString("Storage.Type").equalsIgnoreCase("yaml")){
            return;
        }

        File daily_wins = new File(getDataFolder(), "daily_wins.yml");

        if (!daily_wins.exists()) {
            saveResource("daily_wins.yml", false);
        }
    }

}


