package io.github.swagree.repokepvp;

import io.github.swagree.repokepvp.manager.configManager.ConfigInitializer;
import io.github.swagree.repokepvp.command.PlayerCommand;
import io.github.swagree.repokepvp.listener.PokemonEventListener;
import io.github.swagree.repokepvp.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

    private static Main instance;
    private ServiceManager serviceManager;

    @Override
    public void onEnable() {
        instance = this;
        // 1. 初始化服务管理器
        this.serviceManager = new ServiceManager(this);

        // 2. 注册命令和监听器
        getCommand("rpp").setExecutor(new PlayerCommand(serviceManager));
        Bukkit.getPluginManager().registerEvents(new PokemonEventListener(serviceManager), this);

        // 3. 配置初始化
        new ConfigInitializer(this).setupDefaultConfigs();
        reloadConfig();

        Bukkit.getConsoleSender().sendMessage("§7[RePokePVP] §b作者§fSwagRee §cQQ:§f352208610");
    }

    @Override
    public void onDisable() {
        if (serviceManager != null) {
            serviceManager.shutdown();
        }
    }


    public static Main getInstance() {
        return instance;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}

