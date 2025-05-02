package io.github.swagree.repokepvp.command.sub;

import io.github.swagree.repokepvp.manager.ServiceManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// BasePlayerCommand.java - 基础命令处理器
public abstract class BasePlayerCommand implements ISubCommand {
    protected final ServiceManager serviceManager;

    protected BasePlayerCommand(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    protected final Player checkPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令只能由玩家执行");
            return null;
        }
        return (Player) sender;
    }
}
