package io.github.swagree.repokepvp.command.sub.admin;

import io.github.swagree.Main;
import io.github.swagree.repokepvp.command.sub.BasePlayerCommand;
import io.github.swagree.repokepvp.manager.ServiceManager;
import io.github.swagree.repokepvp.util.YmlUtil;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Objects;

// ReloadCommand.java - 重载配置
public class ReloadCommand extends BasePlayerCommand {
    public ReloadCommand(ServiceManager serviceManager) {
        super(serviceManager);
    }

    @Override public String getName() { return "reload"; }
    @Override public String getPermission() { return "pokepvp.admin"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            YmlUtil.clearAllCache();

            File configDir = new File(Main.getInstance().getDataFolder(), "pvplist");
            if (!configDir.exists() || !configDir.isDirectory()) {
                sender.sendMessage("§c配置目录不存在: pvplist");
                return false;
            }

            int loadedCount = 0;
            for (File file : Objects.requireNonNull(configDir.listFiles((dir, name) -> name.endsWith(".yml")))) {
                String configName = file.getName().replace(".yml", "");
                YmlUtil.getConfig("pvplist/" + configName); // 重新加载配置
                loadedCount++;
            }

            sender.sendMessage("§a配置重载完成，共加载 " + loadedCount + " 个文件");
            return true;
        } catch (Exception e) {
            String errorMsg = "配置重载失败: " + e.getClass().getSimpleName();
            sender.sendMessage("§c" + errorMsg);
            Main.getInstance().getLogger().severe(errorMsg + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
